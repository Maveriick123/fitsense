package es.udc.psi_2025_26.FitSense;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;

/**
 * Persistencia local de los parámetros calibrados de cada ejercicio, separada por usuario.
 *
 * Ámbito: (usuario, dispositivo).
 *  - Por usuario: la forma de moverse (pisada, ritmo, intensidad) es personal; si varias
 *    personas comparten el móvil, cada una tiene su propia calibración.
 *  - Por dispositivo: los umbrales dependen del acelerómetro concreto y del filtrado que hace
 *    cada fabricante, así que una calibración no es trasladable a otro teléfono. Por eso se usa
 *    SharedPreferences (local) y no Firebase, y este archivo se excluye de las copias de
 *    seguridad de Android (ver res/xml/backup_rules.xml y data_extraction_rules.xml) para que no
 *    se restaure en un móvil nuevo con hardware distinto.
 *
 * Las claves tienen la forma {@code <userId>_<EJERCICIO>_<campo>}. El UID de Firebase solo
 * contiene caracteres alfanuméricos, así que es seguro usarlo como prefijo.
 */
public class CalibrationStore {

    /** Nombre del archivo de preferencias; referenciado también en las reglas de backup. */
    static final String PREFS_NAME = "fitsense_calibration";
    /** Prefijo usado si no hay sesión iniciada (no debería ocurrir: se entra por el login). */
    static final String NO_USER = "sin_sesion";

    private static final String SUFFIX_THRESHOLD = "_threshold";
    private static final String SUFFIX_INTERVAL = "_min_interval_ms";

    private final SharedPreferences prefs;
    private final String keyPrefix;

    public CalibrationStore(Context context, @Nullable String userId) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        keyPrefix = (userId == null || userId.isEmpty() ? NO_USER : userId) + "_";
    }

    /** Almacén del usuario con la sesión iniciada en Firebase en este momento. */
    public static CalibrationStore forCurrentUser(Context context) {
        return new CalibrationStore(context, new AuthManager(context).getCurrentUserId());
    }

    /** Parámetros calibrados del ejercicio o, si nunca se calibró, los valores por defecto. */
    public DetectorParams get(ExerciseType type) {
        if (!isCalibrated(type)) return type.defaultParams;
        float threshold = prefs.getFloat(key(type, SUFFIX_THRESHOLD), type.defaultParams.threshold);
        long interval = prefs.getLong(key(type, SUFFIX_INTERVAL), type.defaultParams.minIntervalMs);
        try {
            return new DetectorParams(threshold, interval);
        } catch (IllegalArgumentException corrupted) {
            return type.defaultParams;
        }
    }

    public boolean isCalibrated(ExerciseType type) {
        return prefs.contains(key(type, SUFFIX_THRESHOLD));
    }

    public void save(ExerciseType type, DetectorParams params) {
        prefs.edit()
                .putFloat(key(type, SUFFIX_THRESHOLD), params.threshold)
                .putLong(key(type, SUFFIX_INTERVAL), params.minIntervalMs)
                .apply();
    }

    public void reset(ExerciseType type) {
        prefs.edit()
                .remove(key(type, SUFFIX_THRESHOLD))
                .remove(key(type, SUFFIX_INTERVAL))
                .apply();
    }

    private String key(ExerciseType type, String suffix) {
        return keyPrefix + type.name() + suffix;
    }
}
