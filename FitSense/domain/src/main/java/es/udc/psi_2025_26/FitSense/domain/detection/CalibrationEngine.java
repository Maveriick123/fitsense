package es.udc.psi_2025_26.FitSense.domain.detection;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Motor de autocalibración.
 *
 * Problema: el usuario hace N repeticiones reales; con los parámetros actuales la app detecta M.
 * Queremos los parámetros que hagan M == N.
 *
 * Enfoque elegido: ajuste offline por reproducción de la señal grabada ("replay").
 * En lugar de corregir el umbral con una regla heurística (p. ej. "si cuenta de más, súbelo un
 * 10 %"), que necesita muchas iteraciones y puede oscilar, se reproduce la grabación con una
 * rejilla de combinaciones (umbral x intervalo mínimo) y se elige directamente la que reproduce
 * el número real de repeticiones. Como el detector es determinista, lo que encaja en la grabación
 * encajará en vivo con el mismo movimiento.
 *
 * Criterios de selección (en orden):
 *  1. Mínimo error total sum|detectadas_i - reales_i| sobre TODOS los intentos de la sesión.
 *     Usar todos los intentos (y no solo el último) evita sobreajustar a una única serie.
 *  2. Máxima robustez: entre las combinaciones con error mínimo, se toma el tramo contiguo de
 *     umbrales más ancho y se elige su centro geométrico. Un umbral en mitad de la zona válida
 *     tolera mejor la variabilidad natural entre series que uno pegado al borde.
 *  3. A igualdad, el intervalo mínimo más parecido al actual (cambios mínimos).
 *
 * Coste: ~150 umbrales x 9 intervalos x muestras. La señal se filtra una sola vez por intento,
 * así que cada combinación solo recorre arrays. Para un minuto de grabación son unos pocos
 * millones de comparaciones (decenas de ms en un móvil). Aun así se ejecuta fuera del hilo UI.
 */
public final class CalibrationEngine {

    /** Paso geométrico de la rejilla de umbrales (2 %): resolución relativa constante. */
    static final double THRESHOLD_GRID_RATIO = 1.02;

    /** Factores aplicados al intervalo mínimo actual para explorar alternativas. */
    static final double[] INTERVAL_FACTORS = {0.5, 0.6, 0.7, 0.85, 1.0, 1.2, 1.4, 1.7, 2.0};

    private CalibrationEngine() {}

    /** Un intento de calibración: la señal grabada y cuántas repeticiones hizo realmente el usuario. */
    public static final class LabeledRecording {
        final SignalRecording recording;
        final int realCount;

        public LabeledRecording(SignalRecording recording, int realCount) {
            if (realCount < 0) throw new IllegalArgumentException("realCount < 0");
            this.recording = recording;
            this.realCount = realCount;
        }
    }

    /** Límites físicamente razonables para cada ejercicio; la calibración nunca sale de ellos. */
    public static final class Bounds {
        final float minThreshold;
        final float maxThreshold;
        final long minIntervalMs;
        final long maxIntervalMs;

        public Bounds(float minThreshold, float maxThreshold, long minIntervalMs, long maxIntervalMs) {
            if (!(minThreshold > 0 && maxThreshold > minThreshold && maxIntervalMs >= minIntervalMs)) {
                throw new IllegalArgumentException("Límites de calibración inválidos");
            }
            this.minThreshold = minThreshold;
            this.maxThreshold = maxThreshold;
            this.minIntervalMs = minIntervalMs;
            this.maxIntervalMs = maxIntervalMs;
        }
    }

    public static final class Result {
        /** Parámetros recomendados. */
        public final DetectorParams params;
        /** sum|detectadas - reales| sobre todos los intentos con los nuevos parámetros. */
        public final int totalError;
        /** Repeticiones que se habrían detectado en el último intento con los nuevos parámetros. */
        public final int lastAttemptPredicted;
        /** Tramo de umbrales que da el mismo error mínimo (indicador de robustez). */
        public final float stableMinThreshold;
        public final float stableMaxThreshold;
        public final int attempts;

        Result(DetectorParams params, int totalError, int lastAttemptPredicted,
               float stableMinThreshold, float stableMaxThreshold, int attempts) {
            this.params = params;
            this.totalError = totalError;
            this.lastAttemptPredicted = lastAttemptPredicted;
            this.stableMinThreshold = stableMinThreshold;
            this.stableMaxThreshold = stableMaxThreshold;
            this.attempts = attempts;
        }

        /** Margen relativo del tramo estable: cuánto puede variar la intensidad sin fallar. */
        public float robustnessRatio() {
            return stableMaxThreshold / stableMinThreshold;
        }
    }

    public static Result calibrate(List<LabeledRecording> attempts, DetectorParams current,
                                   float smoothingTauSec, Bounds bounds) {
        if (attempts == null || attempts.isEmpty()) {
            throw new IllegalArgumentException("Se necesita al menos un intento etiquetado");
        }

        float[] thresholds = thresholdGrid(bounds);
        long[] intervals = intervalCandidates(current.minIntervalMs, bounds);

        // Cada grabación se filtra una sola vez (el filtrado no depende de los parámetros).
        int n = attempts.size();
        float[][] filtered = new float[n][];
        for (int k = 0; k < n; k++) {
            filtered[k] = attempts.get(k).recording.filtered(smoothingTauSec);
        }

        int bestError = Integer.MAX_VALUE;
        int bestRunLength = -1;
        double bestIntervalDistance = Double.MAX_VALUE;
        int bestStart = 0;
        int bestIntervalIdx = 0;

        for (int j = 0; j < intervals.length; j++) {
            int[] errors = new int[thresholds.length];
            for (int i = 0; i < thresholds.length; i++) {
                DetectorParams candidate = new DetectorParams(thresholds[i], intervals[j]);
                int error = 0;
                for (int k = 0; k < n; k++) {
                    LabeledRecording a = attempts.get(k);
                    int detected = RepetitionDetector.countFiltered(
                            a.recording.timestamps(), filtered[k], a.recording.size(), candidate);
                    error += Math.abs(detected - a.realCount);
                }
                errors[i] = error;
            }

            int minErrorHere = Integer.MAX_VALUE;
            for (int e : errors) minErrorHere = Math.min(minErrorHere, e);

            // Tramo contiguo más largo de umbrales con el error mínimo de este intervalo.
            int runStart = 0, runLength = 0, curStart = -1;
            for (int i = 0; i <= thresholds.length; i++) {
                boolean match = i < thresholds.length && errors[i] == minErrorHere;
                if (match && curStart < 0) curStart = i;
                if (!match && curStart >= 0) {
                    int len = i - curStart;
                    if (len > runLength) { runLength = len; runStart = curStart; }
                    curStart = -1;
                }
            }

            double intervalDistance = Math.abs(Math.log(
                    (double) intervals[j] / Math.max(1L, current.minIntervalMs)));
            boolean better = minErrorHere < bestError
                    || (minErrorHere == bestError && runLength > bestRunLength)
                    || (minErrorHere == bestError && runLength == bestRunLength
                        && intervalDistance < bestIntervalDistance);
            if (better) {
                bestError = minErrorHere;
                bestRunLength = runLength;
                bestIntervalDistance = intervalDistance;
                bestStart = runStart;
                bestIntervalIdx = j;
            }
        }

        float low = thresholds[bestStart];
        float high = thresholds[bestStart + bestRunLength - 1];
        float threshold = (float) Math.sqrt((double) low * high); // centro geométrico
        DetectorParams params = new DetectorParams(threshold, intervals[bestIntervalIdx]);

        LabeledRecording last = attempts.get(n - 1);
        int lastPredicted = last.recording.countRepetitions(params, smoothingTauSec);
        int totalError = 0;
        for (LabeledRecording a : attempts) {
            totalError += Math.abs(a.recording.countRepetitions(params, smoothingTauSec) - a.realCount);
        }
        return new Result(params, totalError, lastPredicted, low, high, n);
    }

    static float[] thresholdGrid(Bounds bounds) {
        List<Float> values = new ArrayList<>();
        for (double t = bounds.minThreshold; t <= bounds.maxThreshold * 1.0001; t *= THRESHOLD_GRID_RATIO) {
            values.add((float) t);
        }
        float[] out = new float[values.size()];
        for (int i = 0; i < out.length; i++) out[i] = values.get(i);
        return out;
    }

    static long[] intervalCandidates(long currentMs, Bounds bounds) {
        TreeSet<Long> set = new TreeSet<>();
        for (double f : INTERVAL_FACTORS) {
            long v = Math.round(currentMs * f);
            set.add(Math.max(bounds.minIntervalMs, Math.min(bounds.maxIntervalMs, v)));
        }
        long[] out = new long[set.size()];
        int i = 0;
        for (long v : set) out[i++] = v;
        return out;
    }
}
