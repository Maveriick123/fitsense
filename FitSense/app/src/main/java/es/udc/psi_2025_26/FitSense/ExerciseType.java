package es.udc.psi_2025_26.FitSense;

import androidx.annotation.StringRes;

import es.udc.psi_2025_26.FitSense.domain.detection.CalibrationEngine;
import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;

/**
 * Catálogo de ejercicios soportados.
 *
 * Centraliza en un único sitio todo lo que antes estaba repartido en constantes int sueltas
 * (TYPE_SALTOS, TYPE_FLEXIONES...) y ramas if/else: valores por defecto del detector, límites de
 * calibración, textos y nombre en la base de datos. Añadir un ejercicio nuevo = añadir una línea.
 *
 * Valores por defecto (aceleración lineal, móvil en el bolsillo o en la mano):
 *  - Saltos: impactos fuertes y breves -> umbral alto, casi sin suavizado.
 *  - Pasos: picos moderados al apoyar el talón, cadencia de hasta ~3 pasos/s.
 *  - Flexiones / dominadas: movimiento lento y suave -> umbral bajo, suavizado fuerte para
 *    eliminar temblor, y periodo refractario largo (una repetición dura 1,5-3 s).
 * Los valores de saltos y flexiones son los que ya teníais ajustados a mano. Son solo el punto de
 * partida: la autocalibración los adapta a cada usuario y a cada móvil.
 */
public enum ExerciseType {

    //        nombre BD     defaults (m/s², ms)          tau(s)  límites umbral   límites intervalo
    SALTOS("Saltos", new DetectorParams(13.0f, 650), 0.02f, 3.0f, 40.0f, 250, 1500,
            R.string.ex_saltos, R.string.notif_contando_saltos, R.string.calib_instr_saltos),
    FLEXIONES("Flexiones", new DetectorParams(2.5f, 1400), 0.15f, 0.3f, 10.0f, 600, 4000,
            R.string.ex_flexiones, R.string.notif_contando_flexiones, R.string.calib_instr_flexiones),
    PASOS("Pasos", new DetectorParams(2.5f, 300), 0.05f, 0.5f, 12.0f, 200, 1000,
            R.string.ex_pasos, R.string.notif_contando_pasos, R.string.calib_instr_pasos),
    DOMINADAS("Dominadas", new DetectorParams(2.0f, 1500), 0.15f, 0.3f, 10.0f, 800, 5000,
            R.string.ex_dominadas, R.string.notif_contando_dominadas, R.string.calib_instr_dominadas);

    /** Nombre que se guarda en Firebase. No se localiza para no romper los datos ya guardados. */
    public final String dbName;
    public final DetectorParams defaultParams;
    public final float smoothingTauSec;
    public final CalibrationEngine.Bounds calibrationBounds;
    @StringRes public final int nameRes;
    @StringRes public final int notificationRes;
    @StringRes public final int calibrationInstructionsRes;

    ExerciseType(String dbName, DetectorParams defaultParams, float smoothingTauSec,
                 float minThreshold, float maxThreshold, long minIntervalMs, long maxIntervalMs,
                 @StringRes int nameRes, @StringRes int notificationRes,
                 @StringRes int calibrationInstructionsRes) {
        this.dbName = dbName;
        this.defaultParams = defaultParams;
        this.smoothingTauSec = smoothingTauSec;
        this.calibrationBounds = new CalibrationEngine.Bounds(
                minThreshold, maxThreshold, minIntervalMs, maxIntervalMs);
        this.nameRes = nameRes;
        this.notificationRes = notificationRes;
        this.calibrationInstructionsRes = calibrationInstructionsRes;
    }

    /** Conversión segura desde un extra de Intent. */
    public static ExerciseType fromName(String name, ExerciseType fallback) {
        if (name == null) return fallback;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
