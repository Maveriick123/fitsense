package es.udc.psi_2025_26.FitSense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import es.udc.psi_2025_26.FitSense.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Instanciamos AuthManager pasándole el contexto actual (this)
        authManager = new AuthManager(this);

        // 2. Logica del bottone ACCEDI
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                authManager.iniciarSesion(email, password, new AuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String userId) {
                        Toast.makeText(LoginActivity.this, R.string.acceso_exitoso, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, RoutinesActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(LoginActivity.this, getString(R.string.error_inicio_sesion) + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, R.string.usuario_requerido, Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Logica del bottone REGISTRATI
        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                authManager.registrar(email, password, new AuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String userId) {
                        Toast.makeText(LoginActivity.this, R.string.acceso_exitoso, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, RoutinesActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // Corrección: Uso de getString() para evitar imprimir el ID numérico
                        Toast.makeText(LoginActivity.this, getString(R.string.error_general) + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, R.string.usuario_requerido, Toast.LENGTH_SHORT).show();
            }
        });
    }
}