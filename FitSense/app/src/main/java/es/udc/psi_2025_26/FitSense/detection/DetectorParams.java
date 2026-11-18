package es.udc.psi_2025_26.FitSense.detection;

import java.util.Locale;

/**
 * Parámetros calibrables de un {@link RepetitionDetector}.
 *
 * Es un objeto de valor inmutable: el servicio, la pantalla de calibración y el motor
 * de calibración pueden compartirlo sin riesgo de que alguien lo modifique por debajo.
 */
public final class DetectorParams {

    /** Umbral superior (m/s²) que la señal suavizada debe superar para contar una repetición. */
    public final float threshold;

    /** Tiempo mínimo entre dos repeticiones consecutivas (periodo refractario), en ms. */
    public final long minIntervalMs;

    public DetectorParams(float threshold, long minIntervalMs) {
        if (threshold <= 0f) throw new IllegalArgumentException("threshold debe ser > 0");
        if (minIntervalMs < 0) throw new IllegalArgumentException("minIntervalMs debe ser >= 0");
        this.threshold = threshold;
        this.minIntervalMs = minIntervalMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetectorParams)) return false;
        DetectorParams that = (DetectorParams) o;
        return Float.compare(threshold, that.threshold) == 0 && minIntervalMs == that.minIntervalMs;
    }

    @Override
    public int hashCode() {
        return 31 * Float.floatToIntBits(threshold) + Long.hashCode(minIntervalMs);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "DetectorParams{threshold=%.2f, minIntervalMs=%d}",
                threshold, minIntervalMs);
    }
}
