package es.udc.psi_2025_26.FitSense.presentation.uimodel;

import androidx.annotation.StringRes;

// Una fila del historial con los datos ya preparados para pintarla
public class RoutineUiModel {

    // Nombre del ejercicio como recurso, o 0 si es un ejercicio que no se conoce
    @StringRes public final int exerciseNameRes;
    // Nombre tal cual se guardó, se muestra si exerciseNameRes es 0
    public final String exerciseName;
    public final int repetitions;

    public RoutineUiModel(@StringRes int exerciseNameRes, String exerciseName, int repetitions) {
        this.exerciseNameRes = exerciseNameRes;
        this.exerciseName = exerciseName;
        this.repetitions = repetitions;
    }
}
