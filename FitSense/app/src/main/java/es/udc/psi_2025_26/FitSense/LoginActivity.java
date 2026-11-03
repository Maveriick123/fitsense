package es.udc.psi_2025_26.FitSense;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Assicurati che l'importazione del binding sia corretta. Se è rossa, fai Build > Make Project
import es.udc.psi_2025_26.FitSense.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthManager authManager; // 1. Dichiariamo l'oggetto per accedere alla classe di Daniel

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Inizializziamo l'AuthManager
        authManager = new AuthManager();

        // 3. Logica del bottone ACCEDI
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {

                // Chiamiamo il metodo esatto di Daniel passando la Callback
                authManager.iniciarSesion(email, password, new AuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String userId) {
                        Toast.makeText(LoginActivity.this, R.string.acceso_exitoso, Toast.LENGTH_SHORT).show();

                        // Passa alla schermata delle routine
                        Intent intent = new Intent(LoginActivity.this, RoutinesActivity.class);
                        startActivity(intent);
                        finish(); // Chiude laLoginActivity così l'utente non può tornare indietro col tasto "Indietro"
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(LoginActivity.this, getString(R.string.error_inicio_sesion) + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });

            } else {
                Toast.makeText(this, R.string.usuario_requerido, Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Logica del bottone REGISTRATI
        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                // Daniel si è dimenticato di implementare questo metodo in AuthManager!
                //Toast.makeText(this, "Avvisa Daniel: Manca il metodo per registrarsi in AuthManager!", Toast.LENGTH_LONG).show();

                /* Il codice futuro sarà simile a questo:
                authManager.registrarUsuario(email, password, new AuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String userId) { ... }
                    @Override
                    public void onFailure(String errorMessage) { ... }
                });
                */

                authManager.registrar(email, password, new AuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String userId) {
                        Toast.makeText(getApplicationContext(), R.string.acceso_exitoso, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, RoutinesActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // Si ocurre un error (ej. email ya en uso, contraseña débil, sin internet)
                        Toast.makeText(getApplicationContext(), R.string.error_general + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, R.string.usuario_requerido, Toast.LENGTH_SHORT).show();
            }
        });
    }
}