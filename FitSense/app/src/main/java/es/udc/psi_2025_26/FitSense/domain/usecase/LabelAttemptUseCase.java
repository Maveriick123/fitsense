package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.detection.CalibrationEngine;
import es.udc.psi_2025_26.FitSense.domain.detection.SignalRecording;
import es.udc.psi_2025_26.FitSense.domain.model.CalibrationSession;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRecorder;

// Añade a la sesión el último intento grabado con las repeticiones que hizo el usuario
public class LabelAttemptUseCase {

    /** Con menos repeticiones el ajuste está poco determinado (cualquier umbral "acierta"). */
    public static final int MIN_REAL_REPS = 3;
    public static final int MAX_REAL_REPS = 500;

    private final CalibrationRecorder mRecorder;

    public LabelAttemptUseCase(CalibrationRecorder recorder) {
        mRecorder = recorder;
    }

    public static boolean isValidRealCount(int realCount) {
        return realCount >= MIN_REAL_REPS && realCount <= MAX_REAL_REPS;
    }

    // Retorna false si no hay una grabación con datos que etiquetar
    public boolean execute(CalibrationSession session, int realCount) {
        if (!isValidRealCount(realCount)) {
            throw new IllegalArgumentException("Repeticiones reales fuera de rango: " + realCount);
        }
        SignalRecording recording = mRecorder.getFinishedRecording();
        if (recording == null || recording.size() == 0) {
            return false;
        }
        session.add(new CalibrationEngine.LabeledRecording(recording, realCount));
        return true;
    }
}
