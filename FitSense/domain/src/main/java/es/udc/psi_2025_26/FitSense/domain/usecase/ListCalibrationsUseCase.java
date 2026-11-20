package es.udc.psi_2025_26.FitSense.domain.usecase;

import java.util.ArrayList;
import java.util.List;

import es.udc.psi_2025_26.FitSense.domain.exception.WorkoutInProgressException;
import es.udc.psi_2025_26.FitSense.domain.model.Calibration;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.repository.WorkoutTracker;

// Calibración de cada ejercicio, para elegir cuál calibrar
// No se puede calibrar con una rutina en curso porque se mezclaría todo en el mismo sensor
public class ListCalibrationsUseCase {

    private final GetCalibrationUseCase mGetCalibration;
    private final WorkoutTracker mWorkoutTracker;

    public ListCalibrationsUseCase(GetCalibrationUseCase getCalibration, WorkoutTracker workoutTracker) {
        mGetCalibration = getCalibration;
        mWorkoutTracker = workoutTracker;
    }

    public List<Calibration> execute() throws WorkoutInProgressException {
        if (mWorkoutTracker.isRunning()) {
            throw new WorkoutInProgressException();
        }
        List<Calibration> calibrations = new ArrayList<>();
        for (ExerciseType exercise : ExerciseType.values()) {
            calibrations.add(mGetCalibration.execute(exercise));
        }
        return calibrations;
    }
}
