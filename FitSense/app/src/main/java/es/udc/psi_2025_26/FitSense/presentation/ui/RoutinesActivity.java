package es.udc.psi_2025_26.FitSense.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import es.udc.psi_2025_26.FitSense.AppContainer;
import es.udc.psi_2025_26.FitSense.GlobalApplication;
import es.udc.psi_2025_26.FitSense.R;
import es.udc.psi_2025_26.FitSense.databinding.ActivityRoutinesBinding;
import es.udc.psi_2025_26.FitSense.presentation.presenter.RoutinesPresenter;
import es.udc.psi_2025_26.FitSense.presentation.presenter.RoutinesPresenterImp;
import es.udc.psi_2025_26.FitSense.presentation.presenter.RoutinesView;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.RoutineUiModel;

// Historial de rutinas. Solo pinta y le pasa los eventos al Presenter
public class RoutinesActivity extends AppCompatActivity implements RoutinesView {

    private ActivityRoutinesBinding binding;
    private RoutineAdapter adapter;
    private RoutinesPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoutinesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new RoutineAdapter();
        binding.recyclerViewRoutines.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewRoutines.setAdapter(adapter);

        AppContainer container = ((GlobalApplication) getApplication()).getContainer();
        presenter = new RoutinesPresenterImp(this, container.watchRoutineHistoryUseCase(),
                container.signOutUseCase());
        // El historial se escucha en tiempo real mientras la pantalla exista
        presenter.onCreate();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            presenter.onClickLogout();
            return true;
        } else if (id == R.id.action_new_routine) {
            presenter.onClickNewRoutine();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Métodos de RoutinesView

    @Override
    public void addRoutine(RoutineUiModel routine) {
        adapter.addRoutine(routine);
    }

    @Override
    public void showError(String detail) {
        Toast.makeText(this, detail == null ? getString(R.string.routines_error)
                : getString(R.string.routines_error_detalle, detail), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoggedOut() {
        Toast.makeText(this, R.string.log_out, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void navigateToWorkout() {
        startActivity(new Intent(this, WorkoutActivity.class));
    }
}
