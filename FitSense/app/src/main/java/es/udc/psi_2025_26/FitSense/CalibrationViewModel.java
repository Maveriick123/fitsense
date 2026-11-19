package es.udc.psi_2025_26.FitSense;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.udc.psi_2025_26.FitSense.domain.detection.CalibrationEngine;
import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.detection.SignalRecording;
import es.udc.psi_2025_26.FitSense.domain.model.CaptureState;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;

/**
 * Lógica de la sesión de calibración (patrón MVVM).
 *
 * Mantiene los intentos etiquetados y los parámetros "de prueba" de la sesión. Al vivir en un
 * ViewModel, nada de esto se pierde si la Activity se recrea (rotación, cambio de tema...).
 *
 * Los parámetros ajustados NO se guardan automáticamente: se usan para el siguiente intento y
 * solo se persisten cuando el usuario pulsa "Guardar". Así puede probar sin miedo a estropear
 * una calibración que ya le funcionaba.
 */
public class CalibrationViewModel extends AndroidViewModel {

    /** Con menos repeticiones el ajuste está poco determinado (cualquier umbral "acierta"). */
    public static final int MIN_REAL_REPS = 3;
    public static final int MAX_REAL_REPS = 500;

    private final CalibrationRepository repository = CalibrationRepository.getInstance();
    private final CalibrationStore store;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final List<CalibrationEngine.LabeledRecording> attempts = new ArrayList<>();
    private final MutableLiveData<CalibrationEngine.Result> result = new MutableLiveData<>(null);
    private final MutableLiveData<DetectorParams> trialParams = new MutableLiveData<>();

    private ExerciseType type;
    private DetectorParams paramsBeforeAdjust;
    private int lastRealCount;
    private int lastDetectedCount;
    private boolean computing = false;

    public CalibrationViewModel(@NonNull Application application) {
        super(application);
        // El usuario no puede cambiar mientras esta pantalla vive (cerrar sesión está en otra).
        store = CalibrationStore.forCurrentUser(application);
    }

    /** Idempotente: solo inicializa la primera vez (no en cada recreación de la Activity). */
    public void init(ExerciseType exercise) {
        if (type != null) return;
        type = exercise;
        trialParams.setValue(store.get(exercise));
        if (!repository.isCapturing()) repository.reset();
    }

    public ExerciseType getType() {
        return type;
    }

    public LiveData<CaptureState> getCaptureState() {
        return repository.getState();
    }

    public LiveData<CalibrationEngine.Result> getResult() {
        return result;
    }

    public LiveData<DetectorParams> getTrialParams() {
        return trialParams;
    }

    public DetectorParams getParamsBeforeAdjust() {
        return paramsBeforeAdjust;
    }

    public int getLastRealCount() {
        return lastRealCount;
    }

    public int getLastDetectedCount() {
        return lastDetectedCount;
    }

    public boolean isCalibrationSaved() {
        return store.isCalibrated(type);
    }

    public boolean hasUnsavedChanges() {
        DetectorParams trial = trialParams.getValue();
        return trial != null && !trial.equals(store.get(type));
    }

    // ------------------------------------------------------------------ Captura

    public void startCapture() {
        // Pasamos ya a "cuenta atrás" para que la UI no muestre el panel anterior mientras arranca el servicio.
        repository.onCountdown(JumpService.CALIBRATION_COUNTDOWN_S);
        result.setValue(null);
        DetectorParams p = trialParams.getValue();
        Intent intent = new Intent(getApplication(), JumpService.class)
                .putExtra(JumpService.EXTRA_MODE, JumpService.MODE_CALIBRATION)
                .putExtra(JumpService.EXTRA_EXERCISE_TYPE, type.name())
                .putExtra(JumpService.EXTRA_THRESHOLD, p.threshold)
                .putExtra(JumpService.EXTRA_MIN_INTERVAL_MS, p.minIntervalMs);
        ContextCompat.startForegroundService(getApplication(), intent);
    }

    /** Parada manual (si el usuario no espera al fin automático por quietud). */
    public void stopCapture() {
        repository.finish(Long.MAX_VALUE);
        getApplication().stopService(new Intent(getApplication(), JumpService.class));
    }

    /** Repeticiones que detectan los parámetros de prueba en la grabación recién terminada. */
    public int computeDetectedCount() {
        SignalRecording rec = repository.getFinishedRecording();
        DetectorParams p = trialParams.getValue();
        return (rec == null || p == null) ? 0 : rec.countRepetitions(p, type.smoothingTauSec);
    }

    /** @return false si no hay grabación válida que etiquetar. */
    public boolean submitRealCount(int realCount) {
        SignalRecording rec = repository.getFinishedRecording();
        if (rec == null || rec.size() == 0 || computing || result.getValue() != null) return false;

        lastRealCount = realCount;
        lastDetectedCount = computeDetectedCount();
        attempts.add(new CalibrationEngine.LabeledRecording(rec, realCount));

        final DetectorParams current = trialParams.getValue();
        final List<CalibrationEngine.LabeledRecording> snapshot = new ArrayList<>(attempts);
        computing = true;
        executor.execute(() -> {
            CalibrationEngine.Result r = CalibrationEngine.calibrate(
                    snapshot, current, type.smoothingTauSec, type.calibrationBounds);
            ContextCompat.getMainExecutor(getApplication()).execute(() -> {
                computing = false;
                paramsBeforeAdjust = current;
                trialParams.setValue(r.params);
                result.setValue(r);
            });
        });
        return true;
    }

    // ------------------------------------------------------------------ Persistencia

    public void save() {
        store.save(type, trialParams.getValue());
    }

    public void resetToDefaults() {
        store.reset(type);
        attempts.clear();
        paramsBeforeAdjust = null;
        trialParams.setValue(type.defaultParams);
        result.setValue(null);
        if (!repository.isCapturing()) repository.reset();
    }

    @Override
    protected void onCleared() {
        // La pantalla se cierra definitivamente: no dejamos una captura huérfana en marcha.
        executor.shutdownNow();
        if (repository.isCapturing()) {
            getApplication().stopService(new Intent(getApplication(), JumpService.class));
        }
        repository.reset();
        super.onCleared();
    }
}
