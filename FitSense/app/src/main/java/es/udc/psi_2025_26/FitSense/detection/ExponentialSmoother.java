package es.udc.psi_2025_26.FitSense.detection;

/**
 * Filtro paso bajo exponencial (EMA) parametrizado por constante de tiempo.
 *
 * El factor alfa se calcula a partir del intervalo real entre muestras
 * (alpha = dt / (tau + dt)) en lugar de ser un valor fijo. Así el filtro se comporta igual
 * aunque Android entregue las muestras a frecuencias distintas según el dispositivo,
 * o si el sistema agrupa (batching) eventos con la pantalla apagada.
 */
public final class ExponentialSmoother {

    private static final long NO_TIMESTAMP = Long.MIN_VALUE;

    private final float tauSec;
    private float value = 0f;
    private long lastTimestampNs = NO_TIMESTAMP;

    /** @param tauSec constante de tiempo en segundos. 0 desactiva el filtrado. */
    public ExponentialSmoother(float tauSec) {
        this.tauSec = Math.max(0f, tauSec);
    }

    public float filter(long timestampNs, float sample) {
        if (lastTimestampNs == NO_TIMESTAMP) {
            // Primera muestra: partimos de reposo (aceleración lineal 0) para no
            // disparar una repetición falsa si la primera lectura es un pico.
            lastTimestampNs = timestampNs;
            if (tauSec == 0f) value = sample;
            return value;
        }
        float dt = Math.max(0f, (timestampNs - lastTimestampNs) / 1e9f);
        lastTimestampNs = timestampNs;
        float alpha = (tauSec == 0f) ? 1f : dt / (tauSec + dt);
        value += alpha * (sample - value);
        return value;
    }
}
