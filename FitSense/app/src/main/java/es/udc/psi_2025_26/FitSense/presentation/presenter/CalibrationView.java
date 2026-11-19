package es.udc.psi_2025_26.FitSense.presentation.presenter;

import es.udc.psi_2025_26.FitSense.presentation.uimodel.CalibrationParamsUiModel;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.CalibrationResultUiModel;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.ExerciseUiModel;

// Interfaz con lo que sabe hacer la pantalla de calibración
// Estados: instrucciones, cuenta atrás, grabando, meter repeticiones reales y resultado
public interface CalibrationView {

    void showExercise(ExerciseUiModel exercise);

    void showParams(CalibrationParamsUiModel params);

    void showIntro();

    void showCountdown(int secondsLeft);

    void showRecording(int liveCount);

    void showReview(int detectedCount);

    void showResult(CalibrationResultUiModel result);

    void setResetEnabled(boolean enabled);

    void clearRealCount();

    void showRealCountError(int min, int max);

    void showNoDataError();

    void showWorkoutInProgress();

    void showSaved();

    void showResetDone();

    void confirmReset();

    void confirmExit();

    void close();
}
