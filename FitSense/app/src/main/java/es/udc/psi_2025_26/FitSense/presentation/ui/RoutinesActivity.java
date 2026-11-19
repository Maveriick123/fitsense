package es.udc.psi_2025_26.FitSense.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

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
        presenter = new RoutinesPresenterImp(this, container.getRoutineHistoryUseCase(),
                container.signOutUseCase(), container.taskRunner());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Se recarga al volver del entrenamiento para ver la rutina que se acaba de guardar
        presenter.onStart();
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
    public void showLoading() {
        binding.progressRoutines.setVisibility(View.VISIBLE);
        binding.tvRoutinesMessage.setVisibility(View.GONE);
    }

    @Override
    public void showRoutines(List<RoutineUiModel> routines) {
        binding.progressRoutines.setVisibility(View.GONE);
        binding.tvRoutinesMessage.setVisibility(View.GONE);
        binding.recyclerViewRoutines.setVisibility(View.VISIBLE);
        adapter.setRoutines(routines);
    }

    @Override
    public void showNoRoutines() {
        showMessage(R.string.routines_empty);
    }

    @Override
    public void showError() {
        showMessage(R.string.routines_error);
    }

    private void showMessage(int textRes) {
        binding.progressRoutines.setVisibility(View.GONE);
        binding.recyclerViewRoutines.setVisibility(View.GONE);
        binding.tvRoutinesMessage.setText(textRes);
        binding.tvRoutinesMessage.setVisibility(View.VISIBLE);
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
