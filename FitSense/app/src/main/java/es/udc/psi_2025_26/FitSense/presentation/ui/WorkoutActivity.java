package es.udc.psi_2025_26.FitSense.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import es.udc.psi_2025_26.FitSense.AppContainer;
import es.udc.psi_2025_26.FitSense.CalibrationActivity;
import es.udc.psi_2025_26.FitSense.GlobalApplication;
import es.udc.psi_2025_26.FitSense.R;
import es.udc.psi_2025_26.FitSense.databinding.ActivityWorkoutBinding;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.presentation.common.PermissionHelper;
import es.udc.psi_2025_26.FitSense.presentation.presenter.WorkoutPresenter;
import es.udc.psi_2025_26.FitSense.presentation.presenter.WorkoutPresenterImp;
import es.udc.psi_2025_26.FitSense.presentation.presenter.WorkoutView;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.CalibrationItemUiModel;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.CounterUiModel;

// Pantalla de entrenamiento con los contadores de cada ejercicio. Solo pinta
public class WorkoutActivity extends AppCompatActivity implements WorkoutView {

    private ActivityWorkoutBinding binding;
    private WorkoutPresenter presenter;
    private final Map<ExerciseType, TextView> counterViews = new EnumMap<>(ExerciseType.class);

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWorkoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AppContainer container = ((GlobalApplication) getApplication()).getContainer();
        presenter = new WorkoutPresenterImp(this, container.startExerciseUseCase(),
                container.watchWorkoutUseCase(), container.saveWorkoutUseCase(),
                container.listCalibrationsUseCase());

        bindExercise(ExerciseType.SALTOS, binding.tvContadorSaltos, binding.btnStartSaltos);
        bindExercise(ExerciseType.FLEXIONES, binding.tvContadorFlexiones, binding.btnStartFlexiones);
        bindExercise(ExerciseType.PASOS, binding.tvContadorPasos, binding.btnStartPasos);
        bindExercise(ExerciseType.DOMINADAS, binding.tvContadorDominadas, binding.btnStartDominadas);
        binding.btnAutocalibrar.setOnClickListener(v -> presenter.onClickCalibrate());
        binding.btnPublishRutine.setOnClickListener(v -> presenter.onClickSaveRoutine());

        if (!PermissionHelper.hasServicePermissions(this)) {
            requestPermissionsLauncher.launch(PermissionHelper.REQUIRED_PERMISSIONS);
        }
    }

    private void bindExercise(ExerciseType type, TextView counter, Button startButton) {
        counterViews.put(type, counter);
        startButton.setOnClickListener(v -> {
            if (PermissionHelper.hasServicePermissions(this)) {
                presenter.onClickStart(type);
            } else {
                Toast.makeText(this, R.string.permiso_denegado, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    // Métodos de WorkoutView

    @Override
    public void showCounters(List<CounterUiModel> counters) {
        for (CounterUiModel counter : counters) {
            TextView view = counterViews.get(counter.type);
            if (view != null) {
                view.setText(getString(counter.formatRes, counter.count));
            }
        }
    }

    @Override
    public void showRoutineSaved() {
        Toast.makeText(this, R.string.rutinas_guardadas, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSessionRequired() {
        Toast.makeText(this, R.string.falta_sesion, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showWorkoutInProgress() {
        Toast.makeText(this, R.string.calib_error_workout_running, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showCalibrationChooser(List<CalibrationItemUiModel> items) {
        String[] labels = new String[items.size()];
        for (int i = 0; i < labels.length; i++) {
            CalibrationItemUiModel item = items.get(i);
            String name = getString(item.nameRes);
            labels[i] = item.calibrated ? getString(R.string.calib_item_calibrated, name) : name;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.calib_choose_title)
                .setItems(labels, (dialog, which) -> presenter.onCalibrationChosen(items.get(which).type))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void navigateToCalibration(ExerciseType type) {
        Intent intent = new Intent(this, CalibrationActivity.class);
        intent.putExtra(CalibrationActivity.EXTRA_EXERCISE_TYPE, type.name());
        startActivity(intent);
    }
}
