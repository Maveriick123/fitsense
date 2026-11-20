package es.udc.psi_2025_26.FitSense.presentation.uimodel;

import androidx.annotation.StringRes;

import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;

// Contador de un ejercicio en la pantalla de entrenamiento
public class CounterUiModel {

    public final ExerciseType type;
    // Formato del contador ("Saltos: %1$d")
    @StringRes public final int formatRes;
    public final int count;

    public CounterUiModel(ExerciseType type, @StringRes int formatRes, int count) {
        this.type = type;
        this.formatRes = formatRes;
        this.count = count;
    }
}
