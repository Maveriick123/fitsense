package es.udc.psi_2025_26.FitSense.presentation.uimodel;

import java.util.ArrayList;
import java.util.List;

import es.udc.psi_2025_26.FitSense.domain.model.Routine;

// Pasa una lista de Routine a RoutineUiModel
public final class RoutineUiModelMapper {

    private RoutineUiModelMapper() {}

    public static RoutineUiModel map(Routine routine) {
        return new RoutineUiModel(ExerciseUiModelMapper.map(routine.getExercise()).nameRes,
                routine.getRepetitions());
    }

    public static List<RoutineUiModel> map(List<Routine> routines) {
        List<RoutineUiModel> uiModels = new ArrayList<>(routines.size());
        for (Routine routine : routines) {
            uiModels.add(map(routine));
        }
        return uiModels;
    }
}
