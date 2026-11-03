package es.udc.psi_2025_26.FitSense;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    // --- RECEPTOR DE SENSORES INYECTADO ---
    private final BroadcastReceiver jumpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (JumpService.ACTION_UPDATE_COUNT.equals(intent.getAction())) {
                int saltos = intent.getIntExtra(JumpService.EXTRA_COUNT_SALTOS, 0);
                int flexiones = intent.getIntExtra(JumpService.EXTRA_COUNT_FLEXIONES, 0);
                actualizarPantalla(saltos, flexiones);
            }
        }
    };

    // --- PERMISOS INYECTADOS ---
    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                result.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false);
                result.getOrDefault(Manifest.permission.ACTIVITY_RECOGNITION, false);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        //setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase
        dbManager = new DatabaseManager();
        authManager = new AuthManager();

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            String currentUserId = bundle.getString(USER_ID);
            if(currentUserId == null){
                Toast.makeText(getApplicationContext(), R.string.error_general, Toast.LENGTH_SHORT).show();
            }else{
                dbManager.escucharRutina(currentUserId);
            }
        }

        // Inicializar UI Sensores
        actualizarPantalla(JumpService.jumpCount, JumpService.flexionesCount);
        solicitarPermisosIniciales();

        binding.btnStartSaltos.setOnClickListener(v -> comprobarPermisosYArrancar(JumpService.TYPE_SALTOS));
        binding.btnStartFlexiones.setOnClickListener(v -> comprobarPermisosYArrancar(JumpService.TYPE_FLEXIONES));

        binding.btnPublishRutine.setOnClickListener(v -> {
            stopService(new Intent(this, JumpService.class));

            String userId = authManager.getCurrentUserId();
            if (userId != null) {
                // Subir rutinas detectadas
                if (JumpService.jumpCount > 0) {
                    dbManager.registrarRutina(userId, "Saltos", JumpService.jumpCount);
                }
                if (JumpService.flexionesCount > 0) {
                    dbManager.registrarRutina(userId, "Flexiones", JumpService.flexionesCount);
                }
                Toast.makeText(this, "Rutinas guardadas en la nube", Toast.LENGTH_SHORT).show();

                // Reiniciar interfaz para el próximo set
                JumpService.jumpCount = 0;
                JumpService.flexionesCount = 0;
                actualizarPantalla(0, 0);
            } else {
                Toast.makeText(this, "Falta inicio de sesión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarPantalla(int saltos, int flexiones) {
        binding.tvContadorSaltos.setText(getString(R.string.tv_saltos_default, saltos));
        binding.tvContadorFlexiones.setText(getString(R.string.tv_flexiones_default, flexiones));
    }

    private void solicitarPermisosIniciales() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean hasNotif = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
            boolean hasActivity = ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
            if (!hasNotif || !hasActivity) {
                requestPermissionsLauncher.launch(new String[]{ Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACTIVITY_RECOGNITION });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(JumpService.ACTION_UPDATE_COUNT);
        ContextCompat.registerReceiver(this, jumpReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        actualizarPantalla(JumpService.jumpCount, JumpService.flexionesCount);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(jumpReceiver);
    }

    private void comprobarPermisosYArrancar(int tipoEjercicio) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean hasNotif = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
            boolean hasActivity = ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
            if (hasNotif && hasActivity) {
                iniciarServicioRutina(tipoEjercicio);
            } else {
                Toast.makeText(this, R.string.permiso_denegado, Toast.LENGTH_LONG).show();
            }
        } else {
            iniciarServicioRutina(tipoEjercicio);
        }
    }

    private void iniciarServicioRutina(int tipoEjercicio) {
        Intent serviceIntent = new Intent(this, JumpService.class);
        serviceIntent.putExtra(JumpService.EXTRA_EXERCISE_TYPE, tipoEjercicio);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
}