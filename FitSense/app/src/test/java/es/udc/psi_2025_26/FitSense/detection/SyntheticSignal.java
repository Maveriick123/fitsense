package es.udc.psi_2025_26.FitSense.detection;

import java.util.Random;

/** Generador de señales sintéticas de aceleración para los tests. */
final class SyntheticSignal {

    static final long SAMPLE_PERIOD_NS = 20_000_000L; // 50 Hz, como SENSOR_DELAY_GAME

    private final SignalRecording recording = new SignalRecording();
    private final Random random = new Random(42);
    private long t = 1_000_000_000L;
    private float noise = 0f;

    SyntheticSignal noise(float amplitude) {
        this.noise = amplitude;
        return this;
    }

    /** Reposo durante {@code ms}. */
    SyntheticSignal rest(long ms) {
        for (long e = 0; e < ms; e += 20) sample(0f);
        return this;
    }

    /** Un pulso semisenoidal de amplitud {@code peak} y duración {@code widthMs}. */
    SyntheticSignal pulse(float peak, long widthMs) {
        for (long e = 0; e < widthMs; e += 20) {
            sample((float) (peak * Math.sin(Math.PI * e / widthMs)));
        }
        return this;
    }

    /** {@code n} repeticiones con un periodo {@code periodMs}. */
    SyntheticSignal reps(int n, float peak, long widthMs, long periodMs) {
        for (int i = 0; i < n; i++) {
            pulse(peak, widthMs);
            rest(periodMs - widthMs);
        }
        return this;
    }

    /** Repeticiones con DOS picos cada una (p. ej. aceleración y frenada de una flexión). */
    SyntheticSignal doublePeakReps(int n, float peak, long gapMs, long periodMs) {
        for (int i = 0; i < n; i++) {
            pulse(peak, 200);
            rest(gapMs);
            pulse(peak * 0.9f, 200);
            rest(periodMs - 400 - gapMs);
        }
        return this;
    }

    private void sample(float v) {
        float n = noise == 0f ? 0f : (float) (random.nextGaussian() * noise);
        recording.add(t, Math.abs(v + n));
        t += SAMPLE_PERIOD_NS;
    }

    long now() {
        return t;
    }

    SignalRecording build() {
        return recording;
    }
}
