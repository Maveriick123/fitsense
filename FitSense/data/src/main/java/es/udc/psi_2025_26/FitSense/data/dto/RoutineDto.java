package es.udc.psi_2025_26.FitSense.data.dto;

// Rutina tal y como se guarda en Firebase Realtime Database
// Los nombres de los campos no se cambian para no romper los datos que ya hay guardados
public class RoutineDto {

    // Nodo raíz de las rutinas
    public static final String NODE_ROUTINES = "ROUTINES";
    public static final String KEY_USER_ID = "USER_ID";
    public static final String KEY_EXERCISE = "EXERCISE_CAMBER";
    public static final String KEY_REPS = "REPS_CAMBER";

    public final String userId;
    public final String exercise;
    public final Long reps;

    public RoutineDto(String userId, String exercise, Long reps) {
        this.userId = userId;
        this.exercise = exercise;
        this.reps = reps;
    }
}
