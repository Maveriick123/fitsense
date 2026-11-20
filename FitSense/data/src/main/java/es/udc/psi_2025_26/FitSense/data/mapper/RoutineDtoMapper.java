package es.udc.psi_2025_26.FitSense.data.mapper;

import java.util.HashMap;
import java.util.Map;

import es.udc.psi_2025_26.FitSense.data.dto.RoutineDto;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.model.Routine;

// Clase para pasar de entidad del dominio a DTO de la base de datos y al revés
public final class RoutineDtoMapper {

    private RoutineDtoMapper() {}

    public static RoutineDto toDto(Routine routine) {
        return new RoutineDto(routine.getUserId(),
                ExerciseDbMapper.toDbName(routine.getExercise()),
                (long) routine.getRepetitions());
    }

    // Retorna la entidad, o null si el registro está incompleto o no se conoce el ejercicio
    public static Routine toEntity(RoutineDto dto) {
        if (dto == null || dto.reps == null) return null;
        ExerciseType exercise = ExerciseDbMapper.fromDbName(dto.exercise);
        if (exercise == null) return null;
        return new Routine(dto.userId, exercise, dto.reps.intValue());
    }

    // Valores que se escriben en el nodo de la rutina
    public static Map<String, Object> toMap(RoutineDto dto) {
        Map<String, Object> values = new HashMap<>();
        values.put(RoutineDto.KEY_USER_ID, dto.userId);
        values.put(RoutineDto.KEY_EXERCISE, dto.exercise);
        values.put(RoutineDto.KEY_REPS, dto.reps);
        return values;
    }
}
