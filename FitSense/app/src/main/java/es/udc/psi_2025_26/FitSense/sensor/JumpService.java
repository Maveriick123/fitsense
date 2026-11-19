package es.udc.psi_2025_26.FitSense.sensor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

import es.udc.psi_2025_26.FitSense.CalibrationActivity;
import es.udc.psi_2025_26.FitSense.R;
import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.detection.ExponentialSmoother;
import es.udc.psi_2025_26.FitSense.domain.detection.QuietDetector;
import es.udc.psi_2025_26.FitSense.domain.detection.RepetitionDetector;
import es.udc.psi_2025_26.FitSense.domain.model.CapturePhase;
import es.udc.psi_2025_26.FitSense.domain.model.CaptureState;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.presentation.ui.WorkoutActivity;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.ExerciseUiModelMapper;

// Servicio en primer plano que lee el acelerómetro y cuenta repeticiones
// Tiene dos modos:
//  - ENTRENAMIENTO: cuenta las repeticiones del ejercicio activo con los parámetros que le
//    llegan en el Intent (los decide el dominio: calibración del usuario o por defecto) y las
//    avisa por Broadcast. Lo arranca ServiceWorkoutTracker, que implementa el puerto WorkoutTracker
//  - CALIBRACIÓN: después de una cuenta atrás graba la señal en CalibrationRepository con unos
//    parámetros de prueba y se para solo cuando el usuario se queda quieto
// La calibración se hace en este mismo servicio (y no con un listener en la Activity) para que
// se capture IGUAL que en el entrenamiento: mismo sensor, misma frecuencia, mismo filtrado y
// funcionando con la pantalla bloqueada y el móvil en el bolsillo
// Una Activity deja de recibir eventos del sensor en cuanto se bloquea la pantalla
public class JumpService extends Service implements SensorEventListener {

    private static final String TAG = "_TAG_JumpService";
    private static final int NOTIFICATION_ID = 1;

    // Claves para comunicarse con el servicio (las ve el sistema, llevan el nombre del paquete)
    public static final String ACTION_UPDATE_COUNT = "es.udc.psi_2025_26.FitSense.UPDATE_COUNT";
    /** int[] con el contador de cada ejercicio, indexado por {@link ExerciseType#ordinal()}. */
    public static final String EXTRA_COUNTS = "es.udc.psi_2025_26.FitSense.EXTRA_COUNTS";
    /** {@link ExerciseType#name()} del ejercicio a contar o calibrar. */
    public static final String EXTRA_EXERCISE_TYPE = "es.udc.psi_2025_26.FitSense.EXTRA_EXERCISE_TYPE";
    public static final String EXTRA_MODE = "es.udc.psi_2025_26.FitSense.EXTRA_MODE";
    public static final int MODE_WORKOUT = 0;
    public static final int MODE_CALIBRATION = 1;
    // Parámetros del detector: los calibrados (entrenamiento) o los de prueba (calibración)
    public static final String EXTRA_THRESHOLD = "es.udc.psi_2025_26.FitSense.EXTRA_THRESHOLD";
    public static final String EXTRA_MIN_INTERVAL_MS = "es.udc.psi_2025_26.FitSense.EXTRA_MIN_INTERVAL_MS";

    // Método para crear el Intent que cuenta un ejercicio con los parámetros que se le pasan
    public static Intent newWorkoutIntent(Context context, ExerciseType type, DetectorParams params) {
        return newIntent(context, MODE_WORKOUT, type, params);
    }

    // Método para crear el Intent que graba un intento de calibración con parámetros de prueba
    public static Intent newCalibrationIntent(Context context, ExerciseType type, DetectorParams params) {
        return newIntent(context, MODE_CALIBRATION, type, params);
    }

    private static Intent newIntent(Context context, int mode, ExerciseType type, DetectorParams params) {
        return new Intent(context, JumpService.class)
                .putExtra(EXTRA_MODE, mode)
                .putExtra(EXTRA_EXERCISE_TYPE, type.name())
                .putExtra(EXTRA_THRESHOLD, params.threshold)
                .putExtra(EXTRA_MIN_INTERVAL_MS, params.minIntervalMs);
    }

    // --- Calibración ---
    /** Tiempo para guardar el móvil en el bolsillo antes de empezar a grabar. */
    public static final int CALIBRATION_COUNTDOWN_S = 5;
    /** Fin automático de la serie: 4 s por debajo de 0.6 m/s² tras haber superado 1.5 m/s². */
    private static final float QUIET_LEVEL = 0.6f;
    private static final float ACTIVE_LEVEL = 1.5f;
    private static final long QUIET_DURATION_MS = 4000;
    private static final float QUIET_TAU_S = 0.3f;

    /** Constante de tiempo para estimar la gravedad si no existe TYPE_LINEAR_ACCELERATION. */
    private static final float GRAVITY_TAU_S = 0.4f;

    // --- Estado compartido con la UI (mismo proceso) ---
    private static final int[] counts = new int[ExerciseType.values().length];
    private static volatile boolean workoutRunning = false;

    public static int[] getCounts() {
        return counts.clone();
    }

    public static void resetCounts() {
        Arrays.fill(counts, 0);
    }

    public static boolean isWorkoutRunning() {
        return workoutRunning;
    }

    // --- Estado interno ---
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final CalibrationRepository repository = CalibrationRepository.getInstance();

    private SensorManager sensorManager;
    private Sensor sensor;
    private boolean usingLinearAcceleration;
    private final ExponentialSmoother[] gravity = new ExponentialSmoother[3];

    private boolean foregroundStarted = false;
    private boolean listening = false;
    private boolean calibrating = false;
    private ExerciseType activeExercise = ExerciseType.SALTOS;
    private RepetitionDetector detector;
    private QuietDetector quietDetector;
    private ToneGenerator toneGenerator;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            usingLinearAcceleration = sensor != null;
            if (sensor == null) {
                // Algunos móviles baratos no tienen el sensor virtual de aceleración lineal:
                // usamos el acelerómetro y restamos la gravedad con un filtro paso bajo.
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                for (int i = 0; i < 3; i++) gravity[i] = new ExponentialSmoother(GRAVITY_TAU_S);
            }
        }
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        ensureForeground();

        ExerciseType type = ExerciseType.fromName(
                intent.getStringExtra(EXTRA_EXERCISE_TYPE), ExerciseType.SALTOS);

        DetectorParams params = new DetectorParams(
                intent.getFloatExtra(EXTRA_THRESHOLD, type.defaultParams.threshold),
                intent.getLongExtra(EXTRA_MIN_INTERVAL_MS, type.defaultParams.minIntervalMs));
        if (intent.getIntExtra(EXTRA_MODE, MODE_WORKOUT) == MODE_CALIBRATION) {
            startCalibration(type, params);
        } else {
            startWorkout(type, params);
        }
        return START_NOT_STICKY;
    }

    // ------------------------------------------------------------------ Entrenamiento

    private void startWorkout(ExerciseType type, DetectorParams params) {
        if (calibrating) {
            cancelCalibration();
        }
        if (!workoutRunning) {
            resetCounts();
            workoutRunning = true;
        }
        // Al cambiar de ejercicio se conservan los contadores de los demás, como antes.
        activeExercise = type;
        detector = new RepetitionDetector(params, type.smoothingTauSec);
        Log.d(TAG, "Modo entrenamiento: " + type + " con " + params);

        startListening();
        updateNotification();
        sendCountsBroadcast();
    }

    // ------------------------------------------------------------------ Calibración

    private void startCalibration(ExerciseType type, DetectorParams params) {
        if (workoutRunning) {
            // La UI no lo permite; defensa por si acaso: no mezclamos una rutina con una calibración.
            Log.w(TAG, "Calibración ignorada: hay una rutina en curso");
            return;
        }
        if (sensor == null) {
            Log.e(TAG, "El dispositivo no tiene acelerómetro");
            repository.reset();
            stopSelf();
            return;
        }
        stopListening();
        handler.removeCallbacksAndMessages(null);

        calibrating = true;
        activeExercise = type;
        detector = new RepetitionDetector(params, type.smoothingTauSec);
        quietDetector = new QuietDetector(QUIET_LEVEL, ACTIVE_LEVEL, QUIET_DURATION_MS, QUIET_TAU_S);
        Log.d(TAG, "Calibrando " + type + " con " + params);

        countdownTick(CALIBRATION_COUNTDOWN_S);
    }

    private void countdownTick(int secondsLeft) {
        if (secondsLeft <= 0) {
            beginRecording();
            return;
        }
        repository.onCountdown(secondsLeft);
        updateNotification();
        handler.postDelayed(() -> countdownTick(secondsLeft - 1), 1000);
    }

    private void beginRecording() {
        repository.onRecordingStarted();
        // Aviso sonoro y NO vibración: la vibración la captaría el propio acelerómetro.
        beep();
        startListening();
        updateNotification();
    }

    private void finishCalibration(long trimFromNs) {
        stopListening();
        repository.finish(trimFromNs);
        beep();
        stopSelf();
    }

    private void cancelCalibration() {
        handler.removeCallbacksAndMessages(null);
        stopListening();
        calibrating = false;
        repository.reset();
    }

    // ------------------------------------------------------------------ Sensor

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!listening) return;
        // event.timestamp (ns) es el instante real de la muestra, inmune al batching.
        long timestampNs = event.timestamp;
        float magnitude = linearMagnitude(event);

        if (calibrating) {
            if (repository.getPhase() != CapturePhase.RECORDING) return;
            repository.onSample(timestampNs, magnitude);
            if (detector.process(timestampNs, magnitude)) {
                repository.onRepetition(detector.getCount());
                updateNotification();
            }
            long quietStart = quietDetector.process(timestampNs, magnitude);
            if (quietStart != QuietDetector.NOT_FINISHED) {
                finishCalibration(quietStart);
            }
        } else if (detector.process(timestampNs, magnitude)) {
            counts[activeExercise.ordinal()]++;
            Log.d(TAG, activeExercise + ": " + counts[activeExercise.ordinal()]);
            updateNotification();
            sendCountsBroadcast();
        }
    }

    private float linearMagnitude(SensorEvent event) {
        float x = event.values[0], y = event.values[1], z = event.values[2];
        if (!usingLinearAcceleration) {
            x -= gravity[0].filter(event.timestamp, x);
            y -= gravity[1].filter(event.timestamp, y);
            z -= gravity[2].filter(event.timestamp, z);
        }
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private void startListening() {
        if (listening || sensor == null) return;
        // SENSOR_DELAY_GAME (~50 Hz). Antes se usaba SENSOR_DELAY_NORMAL (~5 Hz): una muestra cada
        // 200 ms, demasiado lenta para captar el pico de un salto o un paso (duran ~100-200 ms).
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        listening = true;
    }

    private void stopListening() {
        if (!listening) return;
        sensorManager.unregisterListener(this);
        listening = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // ------------------------------------------------------------------ UI / notificación

    private void sendCountsBroadcast() {
        Intent broadcastIntent = new Intent(ACTION_UPDATE_COUNT);
        broadcastIntent.putExtra(EXTRA_COUNTS, getCounts());
        broadcastIntent.setPackage(getPackageName());
        sendBroadcast(broadcastIntent);
    }

    private void ensureForeground() {
        if (foregroundStarted) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotificationBuilder().build(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH);
        } else {
            startForeground(NOTIFICATION_ID, createNotificationBuilder().build());
        }
        foregroundStarted = true;
    }

    private void updateNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, createNotificationBuilder().build());
    }

    private NotificationCompat.Builder createNotificationBuilder() {
        Class<?> target = calibrating ? CalibrationActivity.class : WorkoutActivity.class;
        Intent intent = new Intent(this, target);
        intent.putExtra(CalibrationActivity.EXTRA_EXERCISE_TYPE, activeExercise.name());
        // SINGLE_TOP | CLEAR_TOP: vuelve a la pantalla existente en vez de crear otra encima.
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String title;
        String text;
        String exerciseName = getString(ExerciseUiModelMapper.map(activeExercise).nameRes);
        if (calibrating) {
            title = getString(R.string.notif_calib_title);
            CaptureState s = repository.getState().getValue();
            text = (s != null && s.phase == CapturePhase.COUNTDOWN)
                    ? getString(R.string.notif_calib_countdown, exerciseName, s.secondsLeft)
                    : getString(R.string.notif_calib_recording, exerciseName,
                            s == null ? 0 : s.liveCount);
        } else {
            title = getString(R.string.notif_title);
            text = getString(ExerciseUiModelMapper.map(activeExercise).notificationRes, counts[activeExercise.ordinal()]);
        }

        return new NotificationCompat.Builder(this, getString(R.string.channel_id))
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_directions)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    getString(R.string.channel_id),
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.channel_desc));
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void beep() {
        try {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            }
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 400);
        } catch (RuntimeException e) {
            // ToneGenerator puede fallar si el sistema no tiene recursos de audio libres.
            Log.w(TAG, "No se pudo reproducir el aviso sonoro", e);
        }
    }

    // ------------------------------------------------------------------ Ciclo de vida

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        stopListening();
        if (calibrating && repository.isCapturing()) {
            // Detenido desde fuera (botón "Detener" o el sistema): cerramos sin recortar.
            repository.finish(Long.MAX_VALUE);
        }
        calibrating = false;
        workoutRunning = false;
        if (toneGenerator != null) {
            // Se retrasa la liberación para no cortar el pitido final.
            ToneGenerator tg = toneGenerator;
            toneGenerator = null;
            new Handler(Looper.getMainLooper()).postDelayed(tg::release, 600);
        }
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
