package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.model.CaptureState;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRecorder;

// Sigue el estado de la captura de calibración
public class WatchCalibrationCaptureUseCase {

    private final CalibrationRecorder mRecorder;

    public WatchCalibrationCaptureUseCase(CalibrationRecorder recorder) {
        mRecorder = recorder;
    }

    // listener recibe los cambios, con null se deja de escuchar. Retorna el estado actual
    public CaptureState execute(CalibrationRecorder.Listener listener) {
        mRecorder.setListener(listener);
        return mRecorder.getState();
    }
}
