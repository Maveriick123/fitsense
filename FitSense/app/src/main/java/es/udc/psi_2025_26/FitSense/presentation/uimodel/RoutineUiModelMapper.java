package es.udc.psi_2025_26.FitSense.presentation.uimodel;

import es.udc.psi_2025_26.FitSense.domain.model.Routine;

// Pasa una Routine a RoutineUiModel
public final class RoutineUiModelMapper {

    private RoutineUiModelMapper() {}

    public static RoutineUiModel map(Routine routine) {
        int nameRes = routine.getExercise() != null
                ? ExerciseUiModelMapper.map(routine.getExercise()).nameRes : 0;
        return new RoutineUiModel(nameRes, routine.getExerciseName(), routine.getRepetitions());
    }
}
