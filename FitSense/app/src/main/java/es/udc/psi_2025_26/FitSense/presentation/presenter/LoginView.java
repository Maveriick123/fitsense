package es.udc.psi_2025_26.FitSense.presentation.presenter;

// Interfaz con lo que sabe hacer la pantalla de login, la implementa la Activity
public interface LoginView {

    void showLoading(boolean loading);

    void showMissingCredentials();

    void showWelcome();

    // detail es el mensaje de Firebase, o null si no hay
    void showLoginError(String detail);

    // detail es el mensaje de Firebase, o null si no hay
    void showRegisterError(String detail);

    void navigateToRoutines();
}
