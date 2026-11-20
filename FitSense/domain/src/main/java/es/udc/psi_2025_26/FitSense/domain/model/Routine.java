package es.udc.psi_2025_26.FitSense.domain.model;

// Clase para la rutina que completa un usuario, el ejercicio y las repeticiones que hizo
public class Routine {

    private final String userId;
    private final ExerciseType exercise;
    private final String exerciseName;
    private final int repetitions;

    public Routine(String userId, ExerciseType exercise, int repetitions) {
        this(userId, exercise, null, repetitions);
    }

    // exercise es el ejercicio, o null si es uno que esta versión no conoce
    // exerciseName es el nombre con el que se guardó (se muestra si no se conoce el ejercicio)
    public Routine(String userId, ExerciseType exercise, String exerciseName, int repetitions) {
        this.userId = userId;
        this.exercise = exercise;
        this.exerciseName = exerciseName;
        this.repetitions = repetitions;
    }

    public String getUserId() {
        return userId;
    }

    // Retorna el ejercicio, o null si no es uno de ExerciseType
    public ExerciseType getExercise() {
        return exercise;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public int getRepetitions() {
        return repetitions;
    }
}
