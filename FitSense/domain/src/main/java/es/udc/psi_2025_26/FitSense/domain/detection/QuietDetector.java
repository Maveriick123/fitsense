package es.udc.psi_2025_26.FitSense.domain.detection;

/**
 * Detecta el final de una serie: el usuario ha estado activo y después permanece quieto
 * durante {@code quietDurationMs}.
 *
 * Se usa en la calibración para cerrar la grabación automáticamente. Si el usuario tuviera
 * que sacar el móvil del bolsillo y pulsar "Detener", ese movimiento quedaría grabado y
 * contaminaría los datos con picos que no son repeticiones. Con este detector la grabación
 * se recorta justo en el instante en que empezó la quietud.
 */
public final class QuietDetector {

    public static final long NOT_FINISHED = Long.MIN_VALUE;

    private final float quietLevel;
    private final float activeLevel;
    private final long quietDurationNs;
    private final ExponentialSmoother smoother;

    private boolean wasActive = false;
    private long quietStartNs = NOT_FINISHED;

    /**
     * @param quietLevel      por debajo de este nivel (m/s², señal suavizada) se considera quietud
     * @param activeLevel     hay que superar este nivel al menos una vez antes de poder terminar,
     *                        para no cortar la grabación mientras el usuario se prepara
     * @param quietDurationMs tiempo de quietud continua necesario para dar la serie por acabada
     * @param tauSec          constante de tiempo del suavizado
     */
    public QuietDetector(float quietLevel, float activeLevel, long quietDurationMs, float tauSec) {
        this.quietLevel = quietLevel;
        this.activeLevel = activeLevel;
        this.quietDurationNs = quietDurationMs * 1_000_000L;
        this.smoother = new ExponentialSmoother(tauSec);
    }

    /**
     * @return el timestamp en que empezó la quietud si la serie acaba de terminar,
     *         o {@link #NOT_FINISHED} en caso contrario.
     */
    public long process(long timestampNs, float magnitude) {
        float level = smoother.filter(timestampNs, magnitude);
        if (!wasActive) {
            wasActive = level >= activeLevel;
            return NOT_FINISHED;
        }
        if (level < quietLevel) {
            if (quietStartNs == NOT_FINISHED) {
                quietStartNs = timestampNs;
            } else if (timestampNs - quietStartNs >= quietDurationNs) {
                return quietStartNs;
            }
        } else {
            quietStartNs = NOT_FINISHED;
        }
        return NOT_FINISHED;
    }
}
