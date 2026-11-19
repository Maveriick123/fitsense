package es.udc.psi_2025_26.FitSense.presentation.presenter;

import java.util.List;

import es.udc.psi_2025_26.FitSense.presentation.uimodel.RoutineUiModel;

// Estados de la pantalla del historial: cargando, lista, vacío y error
public interface RoutinesView {

    void showLoading();

    void showRoutines(List<RoutineUiModel> routines);

    void showNoRoutines();

    void showError();

    void showLoggedOut();

    void navigateToLogin();

    void navigateToWorkout();
}
