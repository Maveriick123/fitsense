package es.udc.psi_2025_26.FitSense.data.mapper;

import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;

// Nombre con el que se guarda cada ejercicio en la base de datos
// No se traduce ni se usa ExerciseType.name() para no romper los datos que ya hay guardados
public final class ExerciseDbMapper {

    private static final String DB_SALTOS = "Saltos";
    private static final String DB_FLEXIONES = "Flexiones";
    private static final String DB_PASOS = "Pasos";
    private static final String DB_DOMINADAS = "Dominadas";

    private ExerciseDbMapper() {}

    public static String toDbName(ExerciseType exercise) {
        switch (exercise) {
            case FLEXIONES: return DB_FLEXIONES;
            case PASOS: return DB_PASOS;
            case DOMINADAS: return DB_DOMINADAS;
            case SALTOS:
            default: return DB_SALTOS;
        }
    }

    // Retorna el ejercicio, o null si el nombre no es de ninguno
    public static ExerciseType fromDbName(String dbName) {
        if (DB_SALTOS.equals(dbName)) return ExerciseType.SALTOS;
        if (DB_FLEXIONES.equals(dbName)) return ExerciseType.FLEXIONES;
        if (DB_PASOS.equals(dbName)) return ExerciseType.PASOS;
        if (DB_DOMINADAS.equals(dbName)) return ExerciseType.DOMINADAS;
        return null;
    }
}
