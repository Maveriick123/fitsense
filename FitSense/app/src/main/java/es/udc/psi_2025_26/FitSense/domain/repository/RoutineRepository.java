package es.udc.psi_2025_26.FitSense.domain.repository;

import java.util.List;

import es.udc.psi_2025_26.FitSense.domain.exception.DataUnavailableException;
import es.udc.psi_2025_26.FitSense.domain.model.Routine;

// Puerto del historial de rutinas
public interface RoutineRepository {

    // Guarda la rutina. Si no hay red se sincroniza más tarde, no se queda esperando
    void saveRoutine(Routine routine);

    // Rutinas que guardó el usuario, de la más antigua a la más nueva
    List<Routine> getRoutines(String userId) throws DataUnavailableException;
}
