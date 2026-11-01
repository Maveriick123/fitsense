package es.udc.psi_2025_26.FitSense;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import es.udc.psi_2025_26.FitSense.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "_TAG";

    private ActivityMainBinding binding;
    private DatabaseManager dbManager;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Instanciar la clases (propias) a la que se le delega la comunicación con la nube
        dbManager = new DatabaseManager();
        authManager = new AuthManager();


        // #############################################################
        // Hay que implementar el sign in del usuario o registro
        // En el app bar, un menu de opciones y usar los métodos del AuthManager
        // ###############################################################
        // @PRUEBA !! --> Después quitar
        String currentUserId = authManager.getCurrentUserId();
        if (currentUserId == null) {
            Log.d(TAG, "onCreate() -> Sin sesión activa. Intentando iniciar sesión de prueba...");
            authManager.iniciarSesion("dani.quingra2005@gmail.com", "1234567", new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(String userId) {
                    Log.d(TAG, "Sesión de prueba iniciada con ID: " + userId);
                    dbManager.escucharRutina(userId);
                }
                @Override
                public void onFailure(String errorMessage) {
                    Log.d(TAG, "Error en sesión de prueba: " + errorMessage);
                }
            });
        }

        // ###################################################
        // Hacer un recycler view o similar como el de la P24
        // Para mostrar las últimas notificaciones de rutinas subidas por los usuarios
        // Como se muestran todas las existentes mostrar solo diez ultimas
        // Además se podrá ir hacia arriba
        // ###################################################

        currentUserId = authManager.getCurrentUserId();
        if (currentUserId != null) {
            dbManager.escucharRutina(currentUserId);
        }

        binding.btnPublishRutine.setOnClickListener(v -> {
            String userId = authManager.getCurrentUserId();
            if (userId != null) {
                // Sube la rutina asociada al ID autenticado
                dbManager.registrarRutina(userId, DatabaseManager.KEY_EXERCISE_CAMBER, 30);
                Log.d(TAG, "Rutina subida");
            } else {
                Log.d(TAG, "Hay que iniciar sesión");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Para @pruebas de cambiar usuario...
        //authManager.cerrarSesion();
        //Log.d(TAG, "onDestroy() Sesión cerrada");
    }
}