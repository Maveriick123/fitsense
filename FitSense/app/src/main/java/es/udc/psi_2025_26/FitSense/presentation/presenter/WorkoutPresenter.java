package es.udc.psi_2025_26.FitSense.presentation.presenter;

import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;

// Interfaz con los eventos de la pantalla de entrenamiento
public interface WorkoutPresenter {

    // Cuando la pantalla se ve se muestran los contadores y se siguen sus cambios
    void onResume();

    // Cuando la pantalla deja de verse se dejan de seguir los contadores
    void onPause();

    // Los permisos ya los comprueba la Activity antes
    void onClickStart(ExerciseType type);

    void onClickSaveRoutine();

    void onClickCalibrate();

    void onCalibrationChosen(ExerciseType type);

    void onDestroy();
}
