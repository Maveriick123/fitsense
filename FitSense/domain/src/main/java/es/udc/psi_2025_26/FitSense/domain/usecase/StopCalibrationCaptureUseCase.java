package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRecorder;

// Termina el intento en curso sin esperar a que el usuario se quede quieto
public class StopCalibrationCaptureUseCase {

    private final CalibrationRecorder mRecorder;

    public StopCalibrationCaptureUseCase(CalibrationRecorder recorder) {
        mRecorder = recorder;
    }

    public void execute() {
        mRecorder.stop();
    }
}
