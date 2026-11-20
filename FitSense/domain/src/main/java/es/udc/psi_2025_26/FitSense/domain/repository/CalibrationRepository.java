package es.udc.psi_2025_26.FitSense.domain.repository;

import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;

// Puerto de las calibraciones guardadas, separadas por usuario
// userId puede ser null si no hay sesión iniciada
public interface CalibrationRepository {

    // Retorna los parámetros guardados, o null si el usuario no calibró este ejercicio
    DetectorParams find(String userId, ExerciseType exercise);

    void save(String userId, ExerciseType exercise, DetectorParams params);

    void delete(String userId, ExerciseType exercise);
}
