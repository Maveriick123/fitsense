package es.udc.psi_2025_26.FitSense.domain.repository;

import es.udc.psi_2025_26.FitSense.domain.exception.AuthException;

// Puerto de autenticación, lo define el dominio y lo implementa la capa de datos
// Los métodos son síncronos así que se llaman fuera del hilo principal (TaskRunner)
public interface AuthRepository {

    // Retorna el ID del usuario que ha iniciado sesión
    String signIn(String email, String password) throws AuthException;

    // Crea el usuario y deja su sesión iniciada. Retorna su ID
    String register(String email, String password) throws AuthException;

    // Retorna el ID del usuario con la sesión iniciada, o null si no hay ninguno...
    String getCurrentUserId();

    void signOut();
}
