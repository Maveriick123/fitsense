package es.udc.psi_2025_26.FitSense.domain.repository;

import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.model.WorkoutCounts;

// Puerto del contador de repeticiones en segundo plano (sensores)
// El dominio no sabe que detrás hay un servicio de Android y un acelerómetro
public interface WorkoutTracker {

    interface Listener {
        // Se llama en el hilo principal cada vez que cambia algún contador
        void onCountsChanged(WorkoutCounts counts);
    }

    // Empieza (o cambia) el ejercicio que se cuenta, con los parámetros que se le pasan
    void start(ExerciseType exercise, DetectorParams params);

    // Deja de contar. Los contadores se mantienen hasta resetCounts()
    void stop();

    boolean isRunning();

    WorkoutCounts getCounts();

    void resetCounts();

    // Con null se deja de escuchar
    void setListener(Listener listener);
}
