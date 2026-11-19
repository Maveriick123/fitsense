package es.udc.psi_2025_26.FitSense.presentation.presenter;

// Interfaz con los eventos de la pantalla del historial
public interface RoutinesPresenter {

    // Cuando la pantalla se ve se (re)carga el historial
    void onStart();

    void onClickNewRoutine();

    void onClickLogout();

    void onDestroy();
}
