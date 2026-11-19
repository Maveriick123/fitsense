package es.udc.psi_2025_26.FitSense.domain.model;

import es.udc.psi_2025_26.FitSense.domain.detection.CalibrationEngine;
import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;

// Ejercicios que soporta la app (entidad del dominio, Java puro)
// Aquí solo van reglas del negocio: valores por defecto del detector y límites de calibración
// Los textos que se muestran están en presentación (ExerciseUiModelMapper) y el nombre que
// se guarda en la base de datos está en la capa de datos
// Valores por defecto (aceleración lineal, móvil en el bolsillo o en la mano):
//  - Saltos: golpes fuertes y cortos -> umbral alto y casi sin suavizado
//  - Pasos: picos moderados al apoyar el talón, hasta unos 3 pasos por segundo
//  - Flexiones / dominadas: movimiento lento y suave -> umbral bajo, mucho suavizado para
//    quitar el temblor y periodo refractario largo (una repetición dura 1,5-3 s)
public enum ExerciseType {

    //         defaults (m/s², ms)          tau(s)  límites umbral   límites intervalo
    SALTOS(new DetectorParams(13.0f, 650), 0.02f, 3.0f, 40.0f, 250, 1500),
    FLEXIONES(new DetectorParams(2.5f, 1400), 0.15f, 0.3f, 10.0f, 600, 4000),
    PASOS(new DetectorParams(2.5f, 300), 0.05f, 0.5f, 12.0f, 200, 1000),
    DOMINADAS(new DetectorParams(2.0f, 1500), 0.15f, 0.3f, 10.0f, 800, 5000);

    public final DetectorParams defaultParams;
    public final float smoothingTauSec;
    public final CalibrationEngine.Bounds calibrationBounds;

    ExerciseType(DetectorParams defaultParams, float smoothingTauSec,
                 float minThreshold, float maxThreshold, long minIntervalMs, long maxIntervalMs) {
        this.defaultParams = defaultParams;
        this.smoothingTauSec = smoothingTauSec;
        this.calibrationBounds = new CalibrationEngine.Bounds(
                minThreshold, maxThreshold, minIntervalMs, maxIntervalMs);
    }

    // Método para pasar de texto a ExerciseType sin que pete (p. ej. un extra del Intent)
    public static ExerciseType fromName(String name, ExerciseType fallback) {
        if (name == null) return fallback;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
