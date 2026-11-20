package es.udc.psi_2025_26.FitSense.domain.model;

// Estado de la captura de calibración que está en curso, no se puede modificar
public final class CaptureState {

    public static final CaptureState IDLE = new CaptureState(CapturePhase.IDLE, 0, 0);

    public final CapturePhase phase;
    // Segundos que faltan para empezar a grabar (solo en COUNTDOWN)
    public final int secondsLeft;
    // Repeticiones detectadas en vivo con los parámetros de prueba
    public final int liveCount;

    public CaptureState(CapturePhase phase, int secondsLeft, int liveCount) {
        this.phase = phase;
        this.secondsLeft = secondsLeft;
        this.liveCount = liveCount;
    }

    public boolean isCapturing() {
        return phase == CapturePhase.COUNTDOWN || phase == CapturePhase.RECORDING;
    }
}
