package es.udc.psi_2025_26.FitSense.domain.usecase;

import java.util.List;

import es.udc.psi_2025_26.FitSense.domain.exception.DataUnavailableException;
import es.udc.psi_2025_26.FitSense.domain.exception.SessionRequiredException;
import es.udc.psi_2025_26.FitSense.domain.model.Routine;
import es.udc.psi_2025_26.FitSense.domain.repository.AuthRepository;
import es.udc.psi_2025_26.FitSense.domain.repository.RoutineRepository;

// Historial de rutinas del usuario que tiene la sesión iniciada
public class GetRoutineHistoryUseCase {

    private final AuthRepository mAuthRepository;
    private final RoutineRepository mRoutineRepository;

    public GetRoutineHistoryUseCase(AuthRepository authRepository, RoutineRepository routineRepository) {
        mAuthRepository = authRepository;
        mRoutineRepository = routineRepository;
    }

    public List<Routine> execute() throws SessionRequiredException, DataUnavailableException {
        String userId = mAuthRepository.getCurrentUserId();
        if (userId == null) {
            throw new SessionRequiredException();
        }
        return mRoutineRepository.getRoutines(userId);
    }
}
