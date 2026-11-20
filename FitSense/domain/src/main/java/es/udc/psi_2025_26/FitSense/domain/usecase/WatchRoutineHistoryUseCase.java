package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.repository.RoutineRepository;

// Sigue en tiempo real el historial de rutinas, primero llegan las que ya hay y después
// cada rutina nueva en cuanto se guarda (de este usuario o de cualquier otro)
public class WatchRoutineHistoryUseCase {

    private final RoutineRepository mRoutineRepository;

    public WatchRoutineHistoryUseCase(RoutineRepository routineRepository) {
        mRoutineRepository = routineRepository;
    }

    // listener recibe las rutinas, con null se deja de escuchar
    public void execute(RoutineRepository.Listener listener) {
        mRoutineRepository.setListener(listener);
    }
}
