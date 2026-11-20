package es.udc.psi_2025_26.FitSense.domain.exception;

// Excepción para cuando falla la autenticación (credenciales incorrectas, usuario ya existente...)
public class AuthException extends Exception {

    public AuthException() {
        super();
    }

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
