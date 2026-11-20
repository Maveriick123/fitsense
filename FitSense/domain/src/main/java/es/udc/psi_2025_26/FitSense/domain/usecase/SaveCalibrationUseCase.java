package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.model.Calibration;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.repository.AuthRepository;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRepository;

// Guarda los parámetros calibrados de un ejercicio para el usuario actual
public class SaveCalibrationUseCase {

    private final AuthRepository mAuthRepository;
    private final CalibrationRepository mCalibrationRepository;

    public SaveCalibrationUseCase(AuthRepository authRepository, CalibrationRepository calibrationRepository) {
        mAuthRepository = authRepository;
        mCalibrationRepository = calibrationRepository;
    }

    public Calibration execute(ExerciseType exercise, DetectorParams params) {
        mCalibrationRepository.save(mAuthRepository.getCurrentUserId(), exercise, params);
        return new Calibration(exercise, params, true);
    }
}
