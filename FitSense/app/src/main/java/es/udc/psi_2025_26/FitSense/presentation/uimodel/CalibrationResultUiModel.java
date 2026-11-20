package es.udc.psi_2025_26.FitSense.presentation.uimodel;

import androidx.annotation.StringRes;

// Resultado de un ajuste de la calibración, ya listo para pintarlo
public class CalibrationResultUiModel {

    // true si los parámetros nuevos aciertan exactamente en todos los intentos
    public final boolean exact;
    public final int detectedBefore;
    public final int realCount;
    public final int predictedAfter;
    // false si no hay parámetros de antes con los que comparar
    public final boolean hasChanges;
    public final float thresholdBefore;
    public final float thresholdAfter;
    public final long intervalBefore;
    public final long intervalAfter;
    public final int attempts;
    @StringRes public final int robustnessRes;
    public final float stableMinThreshold;
    public final float stableMaxThreshold;

    public CalibrationResultUiModel(boolean exact, int detectedBefore, int realCount, int predictedAfter,
                                    boolean hasChanges, float thresholdBefore, float thresholdAfter,
                                    long intervalBefore, long intervalAfter, int attempts,
                                    @StringRes int robustnessRes,
                                    float stableMinThreshold, float stableMaxThreshold) {
        this.exact = exact;
        this.detectedBefore = detectedBefore;
        this.realCount = realCount;
        this.predictedAfter = predictedAfter;
        this.hasChanges = hasChanges;
        this.thresholdBefore = thresholdBefore;
        this.thresholdAfter = thresholdAfter;
        this.intervalBefore = intervalBefore;
        this.intervalAfter = intervalAfter;
        this.attempts = attempts;
        this.robustnessRes = robustnessRes;
        this.stableMinThreshold = stableMinThreshold;
        this.stableMaxThreshold = stableMaxThreshold;
    }
}
