package es.udc.psi_2025_26.FitSense.presentation.presenter;

// Interfaz con los eventos de la pantalla del historial
public interface RoutinesPresenter {

    // Al crearse la pantalla se empieza a escuchar el historial en tiempo real
    void onCreate();

    void onClickNewRoutine();

    void onClickLogout();

    // Al destruirse la pantalla se deja de escuchar el historial
    void onDestroy();
}
