package es.udc.psi_2025_26.FitSense.domain.model;

import java.util.ArrayList;
import java.util.List;

import es.udc.psi_2025_26.FitSense.domain.detection.CalibrationEngine;

// Intentos etiquetados de una sesión de calibración
// El ajuste se calcula con TODOS los intentos de la sesión, no solo con el último, para no
// ajustarlo demasiado a una sola serie
public class CalibrationSession {

    private final List<CalibrationEngine.LabeledRecording> attempts = new ArrayList<>();

    public void add(CalibrationEngine.LabeledRecording attempt) {
        attempts.add(attempt);
    }

    // Copia de los intentos para poder usarla en otro hilo sin problema
    public List<CalibrationEngine.LabeledRecording> snapshot() {
        return new ArrayList<>(attempts);
    }

    public void clear() {
        attempts.clear();
    }
}
