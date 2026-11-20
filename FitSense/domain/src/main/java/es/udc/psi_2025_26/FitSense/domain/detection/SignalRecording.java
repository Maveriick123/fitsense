package es.udc.psi_2025_26.FitSense.domain.detection;

import java.util.Arrays;

/**
 * Grabación de la señal cruda (timestamp + magnitud) de un intento de calibración.
 *
 * Se guarda la señal SIN filtrar ni procesar: así el motor de calibración puede volver a
 * "reproducirla" con cualquier combinación de parámetros. Ocupa poco: a ~50 Hz, un minuto son
 * ~3000 muestras (~36 KB).
 */
public final class SignalRecording {

    /** Límite de seguridad: ~20 min a 50 Hz. Una calibración nunca debería acercarse. */
    public static final int MAX_SAMPLES = 60_000;

    private long[] timestamps = new long[1024];
    private float[] magnitudes = new float[1024];
    private int size = 0;

    public void add(long timestampNs, float magnitude) {
        if (size == MAX_SAMPLES) return;
        if (size == timestamps.length) {
            int newCapacity = Math.min(MAX_SAMPLES, timestamps.length * 2);
            timestamps = Arrays.copyOf(timestamps, newCapacity);
            magnitudes = Arrays.copyOf(magnitudes, newCapacity);
        }
        timestamps[size] = timestampNs;
        magnitudes[size] = magnitude;
        size++;
    }

    /** Descarta las muestras con timestamp >= {@code endNs} (cola de la grabación). */
    public void trimFrom(long endNs) {
        while (size > 0 && timestamps[size - 1] >= endNs) {
            size--;
        }
    }

    public int size() {
        return size;
    }

    public long durationMs() {
        return size < 2 ? 0 : (timestamps[size - 1] - timestamps[0]) / 1_000_000L;
    }

    /** Cuenta repeticiones reproduciendo la grabación con los parámetros indicados. */
    public int countRepetitions(DetectorParams params, float smoothingTauSec) {
        return RepetitionDetector.countFiltered(timestamps, filtered(smoothingTauSec), size, params);
    }

    long[] timestamps() {
        return timestamps;
    }

    float[] filtered(float smoothingTauSec) {
        ExponentialSmoother smoother = new ExponentialSmoother(smoothingTauSec);
        float[] out = new float[size];
        for (int i = 0; i < size; i++) {
            out[i] = smoother.filter(timestamps[i], magnitudes[i]);
        }
        return out;
    }
}
