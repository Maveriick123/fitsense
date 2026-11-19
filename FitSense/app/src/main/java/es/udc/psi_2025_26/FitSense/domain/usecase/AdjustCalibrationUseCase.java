package es.udc.psi_2025_26.FitSense.domain.usecase;

import java.util.List;

import es.udc.psi_2025_26.FitSense.domain.detection.CalibrationEngine;
import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;

// Calcula los parámetros que mejor sacan las repeticiones reales de todos los intentos
// Tarda (prueba la señal con cientos de combinaciones) así que se llama fuera del hilo principal
// No guarda nada, los parámetros ajustados se prueban antes de guardarlos
public class AdjustCalibrationUseCase {

    public CalibrationEngine.Result execute(ExerciseType exercise,
                                            List<CalibrationEngine.LabeledRecording> attempts,
                                            DetectorParams current) {
        return CalibrationEngine.calibrate(attempts, current,
                exercise.smoothingTauSec, exercise.calibrationBounds);
    }
}
