package es.udc.psi_2025_26.FitSense.presentation.uimodel;

import androidx.annotation.StringRes;

// Una fila del historial con los datos ya preparados para pintarla
public class RoutineUiModel {

    @StringRes public final int exerciseNameRes;
    public final int repetitions;

    public RoutineUiModel(@StringRes int exerciseNameRes, int repetitions) {
        this.exerciseNameRes = exerciseNameRes;
        this.repetitions = repetitions;
    }
}
