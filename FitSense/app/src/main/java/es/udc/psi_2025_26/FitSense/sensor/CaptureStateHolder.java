package es.udc.psi_2025_26.FitSense.sensor;

import androidx.annotation.MainThread;

import es.udc.psi_2025_26.FitSense.domain.detection.SignalRecording;
import es.udc.psi_2025_26.FitSense.domain.model.CapturePhase;
import es.udc.psi_2025_26.FitSense.domain.model.CaptureState;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRecorder;

// Estado de la captura de calibración que está en curso. Lo comparten JumpService (el único
// que habla con el sensor) y ServiceCalibrationRecorder
// Como los dos están en el mismo proceso es más fácil y seguro guardarlo aquí que mandar la
// grabación (miles de muestras) por un Broadcast, que además tiene límite de tamaño
// Todo se llama desde el hilo principal (los eventos del sensor y el Handler del servicio
// llegan ahí) así que no hace falta sincronizar nada
final class CaptureStateHolder {

    private static final CaptureStateHolder INSTANCE = new CaptureStateHolder();

    private CaptureState state = CaptureState.IDLE;
    private SignalRecording recording;
    private CalibrationRecorder.Listener listener;

    private CaptureStateHolder() {}

    static CaptureStateHolder getInstance() {
        return INSTANCE;
    }

    CaptureState getState() {
        return state;
    }

    CapturePhase getPhase() {
        return state.phase;
    }

    boolean isCapturing() {
        return state.isCapturing();
    }

    @MainThread
    void setListener(CalibrationRecorder.Listener listener) {
        this.listener = listener;
    }

    @MainThread
    void onCountdown(int secondsLeft) {
        recording = null;
        setState(new CaptureState(CapturePhase.COUNTDOWN, secondsLeft, 0));
    }

    @MainThread
    void onRecordingStarted() {
        recording = new SignalRecording();
        setState(new CaptureState(CapturePhase.RECORDING, 0, 0));
    }

    @MainThread
    void onSample(long timestampNs, float magnitude) {
        if (recording != null && getPhase() == CapturePhase.RECORDING) {
            recording.add(timestampNs, magnitude);
        }
    }

    @MainThread
    void onRepetition(int count) {
        if (getPhase() == CapturePhase.RECORDING) {
            setState(new CaptureState(CapturePhase.RECORDING, 0, count));
        }
    }

    /**
     * Cierra la grabación.
     *
     * @param trimFromNs descarta las muestras posteriores a este instante (inicio de la quietud
     *                   final) o {@link Long#MAX_VALUE} para no recortar.
     */
    @MainThread
    void finish(long trimFromNs) {
        CapturePhase phase = getPhase();
        if (phase == CapturePhase.COUNTDOWN) {
            // Detenido antes de empezar a grabar: no hay nada que analizar.
            reset();
            return;
        }
        if (phase != CapturePhase.RECORDING) return;
        if (recording != null) recording.trimFrom(trimFromNs);
        setState(new CaptureState(CapturePhase.FINISHED, 0, state.liveCount));
    }

    /** Grabación del último intento terminado, o null si no hay ninguno. */
    SignalRecording getFinishedRecording() {
        return getPhase() == CapturePhase.FINISHED ? recording : null;
    }

    @MainThread
    void reset() {
        recording = null;
        setState(CaptureState.IDLE);
    }

    private void setState(CaptureState newState) {
        state = newState;
        if (listener != null) {
            listener.onCaptureStateChanged(newState);
        }
    }
}
