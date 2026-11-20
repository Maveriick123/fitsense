package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.exception.WorkoutInProgressException;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRecorder;
import es.udc.psi_2025_26.FitSense.domain.repository.WorkoutTracker;

// Empieza a grabar un intento de calibración con unos parámetros de prueba
public class StartCalibrationCaptureUseCase {

    private final CalibrationRecorder mRecorder;
    private final WorkoutTracker mWorkoutTracker;

    public StartCalibrationCaptureUseCase(CalibrationRecorder recorder, WorkoutTracker workoutTracker) {
        mRecorder = recorder;
        mWorkoutTracker = workoutTracker;
    }

    public void execute(ExerciseType exercise, DetectorParams trialParams) throws WorkoutInProgressException {
        if (mWorkoutTracker.isRunning()) {
            throw new WorkoutInProgressException();
        }
        mRecorder.start(exercise, trialParams);
    }
}
