package es.udc.psi_2025_26.FitSense.presentation.presenter;

import java.util.List;

import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.CalibrationItemUiModel;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.CounterUiModel;

// Interfaz con lo que sabe hacer la pantalla de entrenamiento
public interface WorkoutView {

    void showCounters(List<CounterUiModel> counters);

    void showRoutineSaved();

    void showSessionRequired();

    void showWorkoutInProgress();

    void showCalibrationChooser(List<CalibrationItemUiModel> items);

    void navigateToCalibration(ExerciseType type);
}
