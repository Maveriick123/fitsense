package es.udc.psi_2025_26.FitSense.domain.exception;

// Se lanza si la acción necesita un usuario con la sesión iniciada
public class SessionRequiredException extends Exception {

    public SessionRequiredException() {
        super();
    }

    public SessionRequiredException(String message) {
        super(message);
    }

    public SessionRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
