package es.udc.psi_2025_26.FitSense.detection;

/**
 * Detector de repeticiones sobre la magnitud de la aceleración lineal.
 *
 * Pipeline: magnitud -> filtro paso bajo (EMA) -> disparador con histéresis -> periodo refractario.
 *
 * - Filtro: elimina ruido de alta frecuencia (vibraciones, temblor) que generaba falsos positivos
 *   con un umbral "a pelo".
 * - Histéresis (disparador de Schmitt): se cuenta al cruzar {@code threshold} hacia arriba y no se
 *   vuelve a armar hasta que la señal baja de {@code threshold * HYSTERESIS_RATIO}. Evita que un
 *   único pico que "tiembla" alrededor del umbral cuente varias veces.
 * - Periodo refractario ({@code minIntervalMs}): un movimiento humano real tiene una cadencia
 *   máxima; los picos secundarios dentro de esa ventana (p. ej. bajada y subida de una flexión)
 *   pertenecen a la misma repetición.
 *
 * Usa el timestamp del propio evento del sensor (no la hora del sistema), de modo que procesar
 * la señal en vivo o reproducir una grabación da exactamente el mismo resultado. Esta propiedad es
 * la que permite que la autocalibración funcione "offline" sobre lo grabado.
 *
 * Clase Java pura (sin dependencias de Android) -> testeable con JUnit.
 */
public final class RepetitionDetector {

    /** Relación entre umbral de rearme y umbral de disparo. */
    public static final float HYSTERESIS_RATIO = 0.6f;

    private final float highThreshold;
    private final float lowThreshold;
    private final long minIntervalNs;
    private final ExponentialSmoother smoother;

    private boolean armed = true;
    private boolean hasRepetition = false;
    private long lastRepetitionNs = 0L;
    private int count = 0;

    public RepetitionDetector(DetectorParams params, float smoothingTauSec) {
        this.highThreshold = params.threshold;
        this.lowThreshold = params.threshold * HYSTERESIS_RATIO;
        this.minIntervalNs = params.minIntervalMs * 1_000_000L;
        this.smoother = new ExponentialSmoother(smoothingTauSec);
    }

    /**
     * Procesa una muestra cruda (magnitud de aceleración lineal en m/s²).
     *
     * @return true si esta muestra completa una nueva repetición.
     */
    public boolean process(long timestampNs, float magnitude) {
        return processFiltered(timestampNs, smoother.filter(timestampNs, magnitude));
    }

    /**
     * Procesa una muestra ya filtrada. Lo usa el motor de calibración, que filtra cada grabación
     * una única vez y luego prueba cientos de combinaciones de parámetros sobre ella.
     */
    boolean processFiltered(long timestampNs, float filtered) {
        if (armed) {
            if (filtered >= highThreshold) {
                armed = false;
                boolean refractoryOver = !hasRepetition
                        || (timestampNs - lastRepetitionNs) >= minIntervalNs;
                if (refractoryOver) {
                    hasRepetition = true;
                    lastRepetitionNs = timestampNs;
                    count++;
                    return true;
                }
                // Cruce dentro del periodo refractario: se trata como parte del mismo movimiento.
            }
        } else if (filtered <= lowThreshold) {
            armed = true;
        }
        return false;
    }

    public int getCount() {
        return count;
    }

    /** Cuenta repeticiones sobre una señal ya filtrada, con los mismos criterios que en vivo. */
    static int countFiltered(long[] timestampsNs, float[] filtered, int size, DetectorParams params) {
        RepetitionDetector detector = new RepetitionDetector(params, 0f);
        for (int i = 0; i < size; i++) {
            detector.processFiltered(timestampsNs[i], filtered[i]);
        }
        return detector.count;
    }
}
