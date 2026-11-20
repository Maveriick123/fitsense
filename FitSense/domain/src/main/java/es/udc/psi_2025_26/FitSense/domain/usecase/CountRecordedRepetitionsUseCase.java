package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.detection.SignalRecording;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRecorder;

// Repeticiones que detectan unos parámetros en el último intento grabado
public class CountRecordedRepetitionsUseCase {

    private final CalibrationRecorder mRecorder;

    public CountRecordedRepetitionsUseCase(CalibrationRecorder recorder) {
        mRecorder = recorder;
    }

    public int execute(ExerciseType exercise, DetectorParams params) {
        SignalRecording recording = mRecorder.getFinishedRecording();
        return (recording == null || params == null)
                ? 0 : recording.countRepetitions(params, exercise.smoothingTauSec);
    }
}
