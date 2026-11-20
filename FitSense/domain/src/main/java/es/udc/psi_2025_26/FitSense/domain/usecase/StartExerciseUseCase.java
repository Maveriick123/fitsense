package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.repository.WorkoutTracker;

// Empieza a contar un ejercicio con la calibración del usuario actual
// Al cambiar de ejercicio los contadores de los demás se mantienen
public class StartExerciseUseCase {

    private final GetCalibrationUseCase mGetCalibration;
    private final WorkoutTracker mWorkoutTracker;

    public StartExerciseUseCase(GetCalibrationUseCase getCalibration, WorkoutTracker workoutTracker) {
        mGetCalibration = getCalibration;
        mWorkoutTracker = workoutTracker;
    }

    public void execute(ExerciseType exercise) {
        mWorkoutTracker.start(exercise, mGetCalibration.execute(exercise).getParams());
    }
}
