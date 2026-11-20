package es.udc.psi_2025_26.FitSense.data.repository;

import android.content.SharedPreferences;

import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRepository;

// Implementación del puerto CalibrationRepository con SharedPreferences
// Se guarda por usuario y por móvil:
//  - Por usuario porque cada uno se mueve a su manera, si varios comparten el móvil cada uno
//    tiene su calibración
//  - Por móvil porque los umbrales dependen del acelerómetro, así que se guarda en local y no
//    en Firebase, y el archivo no entra en las copias de seguridad
//    (res/xml/backup_rules.xml y data_extraction_rules.xml)
// Las claves son <userId>_<EJERCICIO>_<campo>. El UID de Firebase solo tiene letras y números
// así que se puede usar sin problema como prefijo
public class CalibrationRepositoryImp implements CalibrationRepository {

    /** Nombre del archivo de preferencias; referenciado también en las reglas de backup. */
    public static final String PREFS_NAME = "fitsense_calibration";
    /** Prefijo usado si no hay sesión iniciada (no debería ocurrir: se entra por el login). */
    private static final String NO_USER = "sin_sesion";
    private static final String SEPARATOR = "_";
    private static final String SUFFIX_THRESHOLD = "_threshold";
    private static final String SUFFIX_INTERVAL = "_min_interval_ms";

    private final SharedPreferences mPrefs;

    public CalibrationRepositoryImp(SharedPreferences prefs) {
        mPrefs = prefs;
    }

    @Override
    public DetectorParams find(String userId, ExerciseType exercise) {
        String thresholdKey = key(userId, exercise, SUFFIX_THRESHOLD);
        if (!mPrefs.contains(thresholdKey)) return null;
        float threshold = mPrefs.getFloat(thresholdKey, exercise.defaultParams.threshold);
        long interval = mPrefs.getLong(key(userId, exercise, SUFFIX_INTERVAL),
                exercise.defaultParams.minIntervalMs);
        try {
            return new DetectorParams(threshold, interval);
        } catch (IllegalArgumentException corrupted) {
            return null;
        }
    }

    @Override
    public void save(String userId, ExerciseType exercise, DetectorParams params) {
        mPrefs.edit()
                .putFloat(key(userId, exercise, SUFFIX_THRESHOLD), params.threshold)
                .putLong(key(userId, exercise, SUFFIX_INTERVAL), params.minIntervalMs)
                .apply();
    }

    @Override
    public void delete(String userId, ExerciseType exercise) {
        mPrefs.edit()
                .remove(key(userId, exercise, SUFFIX_THRESHOLD))
                .remove(key(userId, exercise, SUFFIX_INTERVAL))
                .apply();
    }

    private static String key(String userId, ExerciseType exercise, String suffix) {
        String prefix = (userId == null || userId.isEmpty()) ? NO_USER : userId;
        return prefix + SEPARATOR + exercise.name() + suffix;
    }
}
