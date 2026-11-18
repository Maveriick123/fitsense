package es.udc.psi_2025_26.FitSense;

import android.content.Context;
import android.content.SharedPreferences;

import es.udc.psi_2025_26.FitSense.detection.DetectorParams;

/**
 * Persistencia local de los parámetros calibrados de cada ejercicio.
 *
 * Por qué SharedPreferences y no Firebase: los umbrales dependen del hardware concreto (cada
 * fabricante usa un acelerómetro y un filtrado distinto) y de dónde lleva el usuario el móvil.
 * Una calibración hecha en un teléfono no es válida en otro, así que su sitio natural es el
 * almacenamiento del dispositivo. Además funciona sin conexión y es instantáneo.
 */
public class CalibrationStore {

    private static final String PREFS_NAME = "fitsense_calibration";
    private static final String SUFFIX_THRESHOLD = "_threshold";
    private static final String SUFFIX_INTERVAL = "_min_interval_ms";

    private final SharedPreferences prefs;

    public CalibrationStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Parámetros calibrados del ejercicio o, si nunca se calibró, los valores por defecto. */
    public DetectorParams get(ExerciseType type) {
        if (!isCalibrated(type)) return type.defaultParams;
        float threshold = prefs.getFloat(type.name() + SUFFIX_THRESHOLD, type.defaultParams.threshold);
        long interval = prefs.getLong(type.name() + SUFFIX_INTERVAL, type.defaultParams.minIntervalMs);
        try {
            return new DetectorParams(threshold, interval);
        } catch (IllegalArgumentException corrupted) {
            return type.defaultParams;
        }
    }

    public boolean isCalibrated(ExerciseType type) {
        return prefs.contains(type.name() + SUFFIX_THRESHOLD);
    }

    public void save(ExerciseType type, DetectorParams params) {
        prefs.edit()
                .putFloat(type.name() + SUFFIX_THRESHOLD, params.threshold)
                .putLong(type.name() + SUFFIX_INTERVAL, params.minIntervalMs)
                .apply();
    }

    public void reset(ExerciseType type) {
        prefs.edit()
                .remove(type.name() + SUFFIX_THRESHOLD)
                .remove(type.name() + SUFFIX_INTERVAL)
                .apply();
    }
}
