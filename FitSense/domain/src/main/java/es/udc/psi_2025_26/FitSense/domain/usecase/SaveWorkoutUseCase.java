package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.exception.SessionRequiredException;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.model.Routine;
import es.udc.psi_2025_26.FitSense.domain.model.WorkoutCounts;
import es.udc.psi_2025_26.FitSense.domain.repository.AuthRepository;
import es.udc.psi_2025_26.FitSense.domain.repository.RoutineRepository;
import es.udc.psi_2025_26.FitSense.domain.repository.WorkoutTracker;

// Termina la rutina y guarda en el historial una entrada por cada ejercicio hecho
// Reglas:
//  - Se deja de contar antes de guardar
//  - Los ejercicios con 0 repeticiones no se guardan
//  - Sin sesión iniciada no se guarda nada y los contadores se mantienen
//  - Después de guardar los contadores vuelven a 0 para la siguiente serie
public class SaveWorkoutUseCase {

    private final AuthRepository mAuthRepository;
    private final RoutineRepository mRoutineRepository;
    private final WorkoutTracker mWorkoutTracker;

    public SaveWorkoutUseCase(AuthRepository authRepository, RoutineRepository routineRepository,
                              WorkoutTracker workoutTracker) {
        mAuthRepository = authRepository;
        mRoutineRepository = routineRepository;
        mWorkoutTracker = workoutTracker;
    }

    // Retorna cuántos ejercicios se han guardado
    public int execute() throws SessionRequiredException {
        mWorkoutTracker.stop();
        String userId = mAuthRepository.getCurrentUserId();
        if (userId == null) {
            throw new SessionRequiredException();
        }
        WorkoutCounts counts = mWorkoutTracker.getCounts();
        int saved = 0;
        for (ExerciseType exercise : ExerciseType.values()) {
            int repetitions = counts.get(exercise);
            if (repetitions > 0) {
                mRoutineRepository.saveRoutine(new Routine(userId, exercise, repetitions));
                saved++;
            }
        }
        mWorkoutTracker.resetCounts();
        return saved;
    }
}
