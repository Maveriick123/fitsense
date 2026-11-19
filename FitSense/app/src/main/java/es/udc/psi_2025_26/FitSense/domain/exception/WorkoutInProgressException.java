package es.udc.psi_2025_26.FitSense.domain.exception;

// Se lanza si la acción no se puede hacer porque hay una rutina en curso
public class WorkoutInProgressException extends Exception {

    public WorkoutInProgressException() {
        super();
    }

    public WorkoutInProgressException(String message) {
        super(message);
    }

    public WorkoutInProgressException(String message, Throwable cause) {
        super(message, cause);
    }
}
