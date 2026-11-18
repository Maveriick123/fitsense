package es.udc.psi_2025_26.FitSense;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import es.udc.psi_2025_26.FitSense.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "_TAG";

    public static final String USER_ID = "es.udc.psi_2025_26.FitSense.USER_ID";

    private ActivityMainBinding binding;
    private DatabaseManager dbManager;
    private AuthManager authManager;

    /** Contador de pantalla de cada ejercicio, indexado por {@link ExerciseType#ordinal()}. */
    private TextView[] counterViews;
    /** Texto con formato de cada contador ("Saltos: %1$d"...), mismo índice. */
    private int[] counterFormats;

    // --- RECEPTOR DE SENSORES INYECTADO ---
    private final BroadcastReceiver jumpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (JumpService.ACTION_UPDATE_COUNT.equals(intent.getAction())) {
                int[] counts = intent.getIntArrayExtra(JumpService.EXTRA_COUNTS);
                if (counts != null) actualizarPantalla(counts);
            }
        }
    };

    // --- PERMISOS INYECTADOS ---
    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Inicializar Firebase con el contexto actual
        dbManager = new DatabaseManager(this);
        authManager = new AuthManager(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(USER_ID)) {
            String currentUserId = bundle.getString(USER_ID);
            if (currentUserId == null) {
                Toast.makeText(getApplicationContext(), R.string.error_general, Toast.LENGTH_SHORT).show();
            } else {
                dbManager.escucharRutina(currentUserId);
            }
        }

        // Vistas y botones por ejercicio (mismo orden que ExerciseType)
        counterViews = new TextView[ExerciseType.values().length];
        counterFormats = new int[ExerciseType.values().length];
        bindExercise(ExerciseType.SALTOS, binding.tvContadorSaltos, R.string.tv_saltos_default, binding.btnStartSaltos);
        bindExercise(ExerciseType.FLEXIONES, binding.tvContadorFlexiones, R.string.tv_flexiones_default, binding.btnStartFlexiones);
        bindExercise(ExerciseType.PASOS, binding.tvContadorPasos, R.string.tv_pasos_default, binding.btnStartPasos);
        bindExercise(ExerciseType.DOMINADAS, binding.tvContadorDominadas, R.string.tv_dominadas_default, binding.btnStartDominadas);

        actualizarPantalla(JumpService.getCounts());
        solicitarPermisosIniciales();

        binding.btnAutocalibrar.setOnClickListener(v -> elegirEjercicioACalibrar());

        binding.btnPublishRutine.setOnClickListener(v -> {
            stopService(new Intent(this, JumpService.class));

            String userId = authManager.getCurrentUserId();
            if (userId != null) {
                // Subir rutinas detectadas (nombre fijo de BD por integridad de los datos)
                int[] counts = JumpService.getCounts();
                for (ExerciseType type : ExerciseType.values()) {
                    if (counts[type.ordinal()] > 0) {
                        dbManager.registrarRutina(userId, type.dbName, counts[type.ordinal()]);
                    }
                }

                Toast.makeText(this, R.string.rutinas_guardadas, Toast.LENGTH_SHORT).show();

                // Reiniciar interfaz para el próximo set
                JumpService.resetCounts();
                actualizarPantalla(JumpService.getCounts());
            } else {
                Toast.makeText(this, R.string.falta_sesion, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindExercise(ExerciseType type, TextView counter, int format, View startButton) {
        counterViews[type.ordinal()] = counter;
        counterFormats[type.ordinal()] = format;
        startButton.setOnClickListener(v -> comprobarPermisosYArrancar(type));
    }

    private void actualizarPantalla(int[] counts) {
        for (int i = 0; i < counterViews.length; i++) {
            counterViews[i].setText(getString(counterFormats[i], counts[i]));
        }
    }

    private void elegirEjercicioACalibrar() {
        if (JumpService.isWorkoutRunning()) {
            // Calibrar mientras se cuenta mezclaría ambas cosas en el mismo sensor/servicio.
            Toast.makeText(this, R.string.calib_error_workout_running, Toast.LENGTH_LONG).show();
            return;
        }
        CalibrationStore calibrationStore = CalibrationStore.forCurrentUser(this);
        ExerciseType[] types = ExerciseType.values();
        String[] labels = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            String name = getString(types[i].nameRes);
            labels[i] = calibrationStore.isCalibrated(types[i])
                    ? getString(R.string.calib_item_calibrated, name)
                    : name;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.calib_choose_title)
                .setItems(labels, (dialog, which) -> {
                    Intent intent = new Intent(this, CalibrationActivity.class);
                    intent.putExtra(CalibrationActivity.EXTRA_EXERCISE_TYPE, types[which].name());
                    startActivity(intent);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void solicitarPermisosIniciales() {
        if (!PermissionHelper.hasServicePermissions(this)) {
            requestPermissionsLauncher.launch(PermissionHelper.REQUIRED_PERMISSIONS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(JumpService.ACTION_UPDATE_COUNT);
        ContextCompat.registerReceiver(this, jumpReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        actualizarPantalla(JumpService.getCounts());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(jumpReceiver);
    }

    private void comprobarPermisosYArrancar(ExerciseType tipoEjercicio) {
        if (PermissionHelper.hasServicePermissions(this)) {
            iniciarServicioRutina(tipoEjercicio);
        } else {
            Toast.makeText(this, R.string.permiso_denegado, Toast.LENGTH_LONG).show();
        }
    }

    private void iniciarServicioRutina(ExerciseType tipoEjercicio) {
        Intent serviceIntent = new Intent(this, JumpService.class);
        serviceIntent.putExtra(JumpService.EXTRA_MODE, JumpService.MODE_WORKOUT);
        serviceIntent.putExtra(JumpService.EXTRA_EXERCISE_TYPE, tipoEjercicio.name());
        ContextCompat.startForegroundService(this, serviceIntent);
    }
}
