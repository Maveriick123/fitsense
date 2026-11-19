package es.udc.psi_2025_26.FitSense.domain.exception;

// Se lanza si no se pueden obtener o guardar los datos (sin red, servidor caído...)
public class DataUnavailableException extends Exception {

    public DataUnavailableException() {
        super();
    }

    public DataUnavailableException(String message) {
        super(message);
    }

    public DataUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
