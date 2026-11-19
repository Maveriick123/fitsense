package es.udc.psi_2025_26.FitSense.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import es.udc.psi_2025_26.FitSense.AppContainer;
import es.udc.psi_2025_26.FitSense.GlobalApplication;
import es.udc.psi_2025_26.FitSense.R;
import es.udc.psi_2025_26.FitSense.databinding.ActivityLoginBinding;
import es.udc.psi_2025_26.FitSense.presentation.presenter.LoginPresenter;
import es.udc.psi_2025_26.FitSense.presentation.presenter.LoginPresenterImp;
import es.udc.psi_2025_26.FitSense.presentation.presenter.LoginView;

// Pantalla de login. Solo pinta y le pasa los eventos al Presenter
public class LoginActivity extends AppCompatActivity implements LoginView {

    private ActivityLoginBinding binding;
    private LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppContainer container = ((GlobalApplication) getApplication()).getContainer();
        presenter = new LoginPresenterImp(this, container.signInUseCase(),
                container.registerUseCase(), container.taskRunner());

        binding.btnLogin.setOnClickListener(v -> presenter.onLogin(email(), password()));
        binding.btnRegister.setOnClickListener(v -> presenter.onRegister(email(), password()));
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    private String email() {
        return binding.etEmail.getText().toString();
    }

    private String password() {
        return binding.etPassword.getText().toString();
    }

    // Métodos de LoginView

    @Override
    public void showLoading(boolean loading) {
        binding.progressLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.btnRegister.setEnabled(!loading);
    }

    @Override
    public void showMissingCredentials() {
        Toast.makeText(this, R.string.usuario_requerido, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showWelcome() {
        Toast.makeText(this, R.string.acceso_exitoso, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoginError(String detail) {
        Toast.makeText(this, detail == null ? getString(R.string.error_inicio_sesion_generico)
                : getString(R.string.error_inicio_sesion, detail), Toast.LENGTH_LONG).show();
    }

    @Override
    public void showRegisterError(String detail) {
        Toast.makeText(this, detail == null ? getString(R.string.error_registro)
                : getString(R.string.error_registro_detalle, detail), Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToRoutines() {
        startActivity(new Intent(this, RoutinesActivity.class));
        finish();
    }
}
