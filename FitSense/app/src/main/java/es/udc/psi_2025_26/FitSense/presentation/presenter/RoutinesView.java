package es.udc.psi_2025_26.FitSense.presentation.presenter;

import es.udc.psi_2025_26.FitSense.presentation.uimodel.RoutineUiModel;

// Interfaz con lo que sabe hacer la pantalla del historial, que se rellena en tiempo real
public interface RoutinesView {

    void addRoutine(RoutineUiModel routine);

    void showError(String detail);

    void showLoggedOut();

    void navigateToLogin();

    void navigateToWorkout();
}
