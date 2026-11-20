package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.model.WorkoutCounts;
import es.udc.psi_2025_26.FitSense.domain.repository.WorkoutTracker;

// Sigue los contadores de la rutina en curso
public class WatchWorkoutUseCase {

    private final WorkoutTracker mWorkoutTracker;

    public WatchWorkoutUseCase(WorkoutTracker workoutTracker) {
        mWorkoutTracker = workoutTracker;
    }

    // listener recibe los cambios, con null se deja de escuchar. Retorna los contadores actuales
    public WorkoutCounts execute(WorkoutTracker.Listener listener) {
        mWorkoutTracker.setListener(listener);
        return mWorkoutTracker.getCounts();
    }
}
