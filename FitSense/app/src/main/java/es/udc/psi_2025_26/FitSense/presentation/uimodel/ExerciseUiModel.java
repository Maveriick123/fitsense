package es.udc.psi_2025_26.FitSense.presentation.uimodel;

import androidx.annotation.StringRes;

import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;

// Textos con los que se enseña un ejercicio en la interfaz
public class ExerciseUiModel {

    public final ExerciseType type;
    @StringRes public final int nameRes;
    // Formato del contador en pantalla ("Saltos: %1$d")
    @StringRes public final int counterRes;
    // Formato de la notificación del servicio ("Contando saltos: %1$d")
    @StringRes public final int notificationRes;
    @StringRes public final int calibrationInstructionsRes;

    public ExerciseUiModel(ExerciseType type, @StringRes int nameRes, @StringRes int counterRes,
                           @StringRes int notificationRes, @StringRes int calibrationInstructionsRes) {
        this.type = type;
        this.nameRes = nameRes;
        this.counterRes = counterRes;
        this.notificationRes = notificationRes;
        this.calibrationInstructionsRes = calibrationInstructionsRes;
    }
}
