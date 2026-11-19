package es.udc.psi_2025_26.FitSense.presentation.uimodel;

import es.udc.psi_2025_26.FitSense.R;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;

// Clase para pasar un ExerciseType del dominio a sus textos de recursos
public final class ExerciseUiModelMapper {

    private ExerciseUiModelMapper() {}

    public static ExerciseUiModel map(ExerciseType type) {
        switch (type) {
            case FLEXIONES:
                return new ExerciseUiModel(type, R.string.ex_flexiones, R.string.tv_flexiones_default,
                        R.string.notif_contando_flexiones, R.string.calib_instr_flexiones);
            case PASOS:
                return new ExerciseUiModel(type, R.string.ex_pasos, R.string.tv_pasos_default,
                        R.string.notif_contando_pasos, R.string.calib_instr_pasos);
            case DOMINADAS:
                return new ExerciseUiModel(type, R.string.ex_dominadas, R.string.tv_dominadas_default,
                        R.string.notif_contando_dominadas, R.string.calib_instr_dominadas);
            case SALTOS:
            default:
                return new ExerciseUiModel(type, R.string.ex_saltos, R.string.tv_saltos_default,
                        R.string.notif_contando_saltos, R.string.calib_instr_saltos);
        }
    }
}
