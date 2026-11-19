package es.udc.psi_2025_26.FitSense.presentation.presenter;

// Interfaz con los eventos de la pantalla de login que la Activity le pasa al Presenter
public interface LoginPresenter {

    void onLogin(String email, String password);

    void onRegister(String email, String password);

    // Cuando se destruye la vista ya no se le mandan más resultados
    void onDestroy();
}
