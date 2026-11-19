package es.udc.psi_2025_26.FitSense.presentation.uimodel;

import androidx.annotation.StringRes;

import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;

// Cada opción del diálogo "¿Qué quieres calibrar?"
public class CalibrationItemUiModel {

    public final ExerciseType type;
    @StringRes public final int nameRes;
    public final boolean calibrated;

    public CalibrationItemUiModel(ExerciseType type, @StringRes int nameRes, boolean calibrated) {
        this.type = type;
        this.nameRes = nameRes;
        this.calibrated = calibrated;
    }
}
