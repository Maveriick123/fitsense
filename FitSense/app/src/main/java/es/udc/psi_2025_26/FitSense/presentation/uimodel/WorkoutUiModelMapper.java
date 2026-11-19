package es.udc.psi_2025_26.FitSense.presentation.uimodel;

import java.util.ArrayList;
import java.util.List;

import es.udc.psi_2025_26.FitSense.domain.model.Calibration;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.model.WorkoutCounts;

// Pasa los datos de la rutina en curso a los UiModels de la pantalla de entrenamiento
public final class WorkoutUiModelMapper {

    private WorkoutUiModelMapper() {}

    public static List<CounterUiModel> mapCounters(WorkoutCounts counts) {
        List<CounterUiModel> counters = new ArrayList<>();
        for (ExerciseType type : ExerciseType.values()) {
            counters.add(new CounterUiModel(type, ExerciseUiModelMapper.map(type).counterRes,
                    counts.get(type)));
        }
        return counters;
    }

    public static List<CalibrationItemUiModel> mapCalibrations(List<Calibration> calibrations) {
        List<CalibrationItemUiModel> items = new ArrayList<>();
        for (Calibration calibration : calibrations) {
            items.add(new CalibrationItemUiModel(calibration.getExercise(),
                    ExerciseUiModelMapper.map(calibration.getExercise()).nameRes,
                    calibration.isCustom()));
        }
        return items;
    }
}
