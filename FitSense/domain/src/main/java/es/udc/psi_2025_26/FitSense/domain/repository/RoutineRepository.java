package es.udc.psi_2025_26.FitSense.domain.repository;

import es.udc.psi_2025_26.FitSense.domain.exception.DataUnavailableException;
import es.udc.psi_2025_26.FitSense.domain.model.Routine;

// Puerto del historial de rutinas
public interface RoutineRepository {

    interface Listener {
        // Se llama en el hilo principal por cada rutina guardada, las que ya había y las nuevas
        void onRoutineAdded(Routine routine);

        // No se puede seguir escuchando el historial
        void onError(DataUnavailableException e);
    }

    // Guarda la rutina. Si no hay red se sincroniza más tarde, no se queda esperando
    void saveRoutine(Routine routine);

    // Escucha en tiempo real el historial de rutinas de todos los usuarios
    // listener recibe las rutinas, con null se deja de escuchar
    void setListener(Listener listener);
}
