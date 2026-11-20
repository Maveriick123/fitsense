package es.udc.psi_2025_26.FitSense.domain.repository;

import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.detection.SignalRecording;
import es.udc.psi_2025_26.FitSense.domain.model.CaptureState;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;

// Puerto para grabar un intento de calibración
// Empieza con una cuenta atrás, graba la señal del sensor y acaba sola cuando el usuario se
// queda quieto (o con stop())
public interface CalibrationRecorder {

    interface Listener {
        // Se llama en el hilo principal cada vez que cambia el estado de la captura
        void onCaptureStateChanged(CaptureState state);
    }

    // Empieza un intento detectando en vivo con los parámetros de prueba
    void start(ExerciseType exercise, DetectorParams trialParams);

    // Acaba la captura que está en curso sin esperar a que el usuario se quede quieto
    void stop();

    // Descarta el intento y vuelve al estado inicial
    void discard();

    CaptureState getState();

    // Señal del último intento que terminó, o null si no hay ninguno
    SignalRecording getFinishedRecording();

    // Con null se deja de escuchar
    void setListener(Listener listener);
}
