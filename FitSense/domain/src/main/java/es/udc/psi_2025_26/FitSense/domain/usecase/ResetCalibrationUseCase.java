package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.model.Calibration;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.repository.AuthRepository;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRepository;

// Borra la calibración del usuario actual, el ejercicio vuelve a los valores por defecto
public class ResetCalibrationUseCase {

    private final AuthRepository mAuthRepository;
    private final CalibrationRepository mCalibrationRepository;

    public ResetCalibrationUseCase(AuthRepository authRepository, CalibrationRepository calibrationRepository) {
        mAuthRepository = authRepository;
        mCalibrationRepository = calibrationRepository;
    }

    public Calibration execute(ExerciseType exercise) {
        mCalibrationRepository.delete(mAuthRepository.getCurrentUserId(), exercise);
        return Calibration.defaults(exercise);
    }
}
