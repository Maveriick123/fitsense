package es.udc.psi_2025_26.FitSense;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import es.udc.psi_2025_26.FitSense.domain.detection.SignalRecording;
import es.udc.psi_2025_26.FitSense.domain.model.CapturePhase;
import es.udc.psi_2025_26.FitSense.domain.model.CaptureState;

/**
 * Fuente única de verdad de la captura de calibración en curso (patrón Repository).
 *
 * La captura la realiza {@link JumpService} (el único componente que habla con el sensor) y la
 * pantalla de calibración la observa. Como ambos viven en el mismo proceso, compartir el estado
 * a través de este repositorio con LiveData es más sencillo y seguro que enviar la grabación
 * (miles de muestras) por un Broadcast, que además tiene límite de tamaño. LiveData, al ser
 * consciente del ciclo de vida, solo notifica a la Activity cuando está visible y le entrega
 * el último estado al volver (rotación, volver de segundo plano...).
 *
 * Todos los métodos se llaman desde el hilo principal (los eventos del sensor y el Handler del
 * servicio se entregan en él), así que no hace falta sincronización.
 */
public final class CalibrationRepository {

    private static final CalibrationRepository INSTANCE = new CalibrationRepository();

    private final MutableLiveData<CaptureState> state =
            new MutableLiveData<>(CaptureState.IDLE);
    private SignalRecording recording;

    private CalibrationRepository() {}

    public static CalibrationRepository getInstance() {
        return INSTANCE;
    }

    public LiveData<CaptureState> getState() {
        return state;
    }

    public CapturePhase getPhase() {
        CaptureState s = state.getValue();
        return s == null ? CapturePhase.IDLE : s.phase;
    }

    public boolean isCapturing() {
        CapturePhase p = getPhase();
        return p == CapturePhase.COUNTDOWN || p == CapturePhase.RECORDING;
    }

    @MainThread
    void onCountdown(int secondsLeft) {
        recording = null;
        state.setValue(new CaptureState(CapturePhase.COUNTDOWN, secondsLeft, 0));
    }

    @MainThread
    void onRecordingStarted() {
        recording = new SignalRecording();
        state.setValue(new CaptureState(CapturePhase.RECORDING, 0, 0));
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
            state.setValue(new CaptureState(CapturePhase.RECORDING, 0, count));
        }
    }

    /**
     * Cierra la grabación.
     *
     * @param trimFromNs descarta las muestras posteriores a este instante (inicio de la quietud
     *                   final) o {@link Long#MAX_VALUE} para no recortar.
     */
    @MainThread
    public void finish(long trimFromNs) {
        CapturePhase phase = getPhase();
        if (phase == CapturePhase.COUNTDOWN) {
            // Detenido antes de empezar a grabar: no hay nada que analizar.
            reset();
            return;
        }
        if (phase != CapturePhase.RECORDING) return;
        if (recording != null) recording.trimFrom(trimFromNs);
        CaptureState s = state.getValue();
        state.setValue(new CaptureState(CapturePhase.FINISHED, 0, s == null ? 0 : s.liveCount));
    }

    /** Grabación del último intento terminado, o null si no hay ninguno. */
    public SignalRecording getFinishedRecording() {
        return getPhase() == CapturePhase.FINISHED ? recording : null;
    }

    @MainThread
    public void reset() {
        recording = null;
        state.setValue(CaptureState.IDLE);
    }
}
