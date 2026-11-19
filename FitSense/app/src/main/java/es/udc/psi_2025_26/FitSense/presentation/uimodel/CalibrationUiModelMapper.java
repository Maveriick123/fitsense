package es.udc.psi_2025_26.FitSense.presentation.uimodel;

import es.udc.psi_2025_26.FitSense.R;
import es.udc.psi_2025_26.FitSense.domain.detection.CalibrationEngine;
import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;

// Clase para pasar los datos de la calibración a los UiModels de su pantalla
public final class CalibrationUiModelMapper {

    // A partir de este margen del tramo estable el ajuste se considera robusto
    private static final float ROBUSTNESS_HIGH = 1.5f;
    private static final float ROBUSTNESS_MEDIUM = 1.15f;

    private CalibrationUiModelMapper() {}

    // unsaved: los parámetros son de prueba y no se han guardado
    // customSaved: el usuario tiene una calibración propia guardada
    public static CalibrationParamsUiModel mapParams(DetectorParams params, boolean unsaved,
                                                     boolean customSaved) {
        int origin = unsaved ? R.string.calib_origin_trial
                : customSaved ? R.string.calib_origin_saved
                : R.string.calib_origin_default;
        return new CalibrationParamsUiModel(params.threshold, params.minIntervalMs, origin);
    }

    // before son los parámetros de antes del ajuste, o null si no se conocen
    public static CalibrationResultUiModel mapResult(CalibrationEngine.Result result,
                                                     DetectorParams before,
                                                     int detectedBefore, int realCount) {
        float ratio = result.robustnessRatio();
        int robustness = ratio >= ROBUSTNESS_HIGH ? R.string.calib_robust_high
                : ratio >= ROBUSTNESS_MEDIUM ? R.string.calib_robust_medium
                : R.string.calib_robust_low;
        return new CalibrationResultUiModel(
                result.totalError == 0, detectedBefore, realCount, result.lastAttemptPredicted,
                before != null,
                before != null ? before.threshold : 0f, result.params.threshold,
                before != null ? before.minIntervalMs : 0L, result.params.minIntervalMs,
                result.attempts, robustness, result.stableMinThreshold, result.stableMaxThreshold);
    }
}
