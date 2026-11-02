package es.udc.psi_2025_26.FitSense;


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
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class JumpService extends Service implements SensorEventListener {

    private static final String TAG = "_TAG_JumpService";

    // Constantes de comunicación
    public static final String ACTION_UPDATE_COUNT = "es.udc.psi_2025_26.FitSense.UPDATE_COUNT";
    public static final String EXTRA_COUNT_SALTOS = "JUMP_COUNT";
    public static final String EXTRA_COUNT_FLEXIONES = "FLEX_COUNT";

    // Tipos de ejercicio
    public static final String EXTRA_EXERCISE_TYPE = "EXERCISE_TYPE";
    public static final int TYPE_SALTOS = 1;
    public static final int TYPE_FLEXIONES = 2;

    private SensorManager sensorManager;
    private Sensor linearAccelSensor;

    // Estado interno
    private int activeExercise = TYPE_SALTOS;
    private boolean isTracking = false;
    public static int jumpCount = 0;
    public static int flexionesCount = 0;

    private long lastJumpTime = 0;
    private long lastFlexionTime = 0;

    // Calibración independiente
    private static final float JUMP_THRESHOLD = 13.0f;
    private static final long JUMP_COOLDOWN_MS = 650;

    private static final float FLEXION_THRESHOLD = 2.5f; // Movimiento más suave
    private static final long FLEXION_COOLDOWN_MS = 1400; // Repeticiones más lentas

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            linearAccelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Leemos qué botón pulsó el usuario
        int reqType = intent.getIntExtra(EXTRA_EXERCISE_TYPE, TYPE_SALTOS);

        // Si el servicio arranca por primera vez, ponemos a 0 y encendemos el sensor
        if (!isTracking) {
            jumpCount = 0;
            flexionesCount = 0;
            isTracking = true;

            NotificationCompat.Builder builder = createNotificationBuilder();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH);
            } else {
                startForeground(1, builder.build());
            }

            if (linearAccelSensor != null) {
                sensorManager.registerListener(this, linearAccelSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        // Actualizamos el ejercicio activo sin borrar los datos anteriores
        activeExercise = reqType;
        Log.d(TAG, "Cambiando a modo ejercicio: " + activeExercise);

        actualizarNotificacion();
        enviarBroadcastUI();

        return START_NOT_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isTracking || event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) return;

        double magnitude = Math.sqrt((event.values[0] * event.values[0]) +
                (event.values[1] * event.values[1]) +
                (event.values[2] * event.values[2]));
        long currentTime = System.currentTimeMillis();

        if (activeExercise == TYPE_SALTOS) {
            if (magnitude > JUMP_THRESHOLD && (currentTime - lastJumpTime > JUMP_COOLDOWN_MS)) {
                jumpCount++;
                lastJumpTime = currentTime;
                Log.d(TAG, "Salto: " + jumpCount);
                actualizarNotificacion();
                enviarBroadcastUI();
            }
        } else if (activeExercise == TYPE_FLEXIONES) {
            if (magnitude > FLEXION_THRESHOLD && (currentTime - lastFlexionTime > FLEXION_COOLDOWN_MS)) {
                flexionesCount++;
                lastFlexionTime = currentTime;
                Log.d(TAG, "Flexion: " + flexionesCount);
                actualizarNotificacion();
                enviarBroadcastUI();
            }
        }
    }

    private void actualizarNotificacion() {
        NotificationCompat.Builder builder = createNotificationBuilder();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(1, builder.build());
            }
        } else {
            notificationManager.notify(1, builder.build());
        }
    }

    private void enviarBroadcastUI() {
        Intent broadcastIntent = new Intent(ACTION_UPDATE_COUNT);
        broadcastIntent.putExtra(EXTRA_COUNT_SALTOS, jumpCount);
        broadcastIntent.putExtra(EXTRA_COUNT_FLEXIONES, flexionesCount);
        sendBroadcast(broadcastIntent);
    }

    private NotificationCompat.Builder createNotificationBuilder() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String textoActivo = (activeExercise == TYPE_SALTOS) ?
                "Contando saltos: " + jumpCount :
                "Contando flexiones: " + flexionesCount;

        return new NotificationCompat.Builder(this, "fitsense_channel")
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(textoActivo)
                .setSmallIcon(android.R.drawable.ic_menu_directions)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("fitsense_channel", "Entrenamiento FitSense", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onDestroy() {
        isTracking = false;
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}