package es.udc.psi_2025_26.FitSense.presentation.uimodel;

import androidx.annotation.StringRes;

// Parámetros que se están usando y de dónde salen (por defecto, guardados o de prueba)
public class CalibrationParamsUiModel {

    public final float threshold;
    public final long minIntervalMs;
    @StringRes public final int originRes;

    public CalibrationParamsUiModel(float threshold, long minIntervalMs, @StringRes int originRes) {
        this.threshold = threshold;
        this.minIntervalMs = minIntervalMs;
        this.originRes = originRes;
    }
}
