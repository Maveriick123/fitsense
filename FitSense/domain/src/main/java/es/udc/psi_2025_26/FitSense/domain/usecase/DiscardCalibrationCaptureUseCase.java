package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRecorder;

// Descarta el intento de calibración (en curso o terminado)
public class DiscardCalibrationCaptureUseCase {

    private final CalibrationRecorder mRecorder;

    public DiscardCalibrationCaptureUseCase(CalibrationRecorder recorder) {
        mRecorder = recorder;
    }

    public void execute() {
        if (mRecorder.getState().isCapturing()) {
            mRecorder.stop();
        }
        mRecorder.discard();
    }
}
