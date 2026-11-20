package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.model.Calibration;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.repository.AuthRepository;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRepository;

// Calibración del usuario actual para un ejercicio
// Si el usuario nunca lo calibró se usan los valores por defecto del ejercicio
public class GetCalibrationUseCase {

    private final AuthRepository mAuthRepository;
    private final CalibrationRepository mCalibrationRepository;

    public GetCalibrationUseCase(AuthRepository authRepository, CalibrationRepository calibrationRepository) {
        mAuthRepository = authRepository;
        mCalibrationRepository = calibrationRepository;
    }

    public Calibration execute(ExerciseType exercise) {
        DetectorParams saved = mCalibrationRepository.find(mAuthRepository.getCurrentUserId(), exercise);
        return saved == null ? Calibration.defaults(exercise) : new Calibration(exercise, saved, true);
    }
}
