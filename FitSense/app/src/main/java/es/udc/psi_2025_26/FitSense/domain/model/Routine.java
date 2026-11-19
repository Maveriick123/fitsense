package es.udc.psi_2025_26.FitSense.domain.model;

// Clase para la rutina que completa un usuario, el ejercicio y las repeticiones que hizo
public class Routine {

    private final String userId;
    private final ExerciseType exercise;
    private final int repetitions;

    public Routine(String userId, ExerciseType exercise, int repetitions) {
        this.userId = userId;
        this.exercise = exercise;
        this.repetitions = repetitions;
    }

    public String getUserId() {
        return userId;
    }

    public ExerciseType getExercise() {
        return exercise;
    }

    public int getRepetitions() {
        return repetitions;
    }
}
