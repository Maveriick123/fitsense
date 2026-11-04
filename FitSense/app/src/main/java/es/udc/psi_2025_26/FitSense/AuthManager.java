package es.udc.psi_2025_26.FitSense;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Clase para comunicación con el servicio firebase-auth
public class AuthManager {
    private final FirebaseAuth mAuth;
    private final Context context;

    public AuthManager(Context context) {
        // Se obtiene la instancia del servicio de autenticación
        this.mAuth = FirebaseAuth.getInstance();
        // Se guarda el contexto de la aplicación para acceder a los recursos (strings)
        this.context = context.getApplicationContext();
    }

    // Retorna el ID del usuario actual, o null si no ha iniciado sesión...
    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    // Método para iniciar sesión de forma desacoplada con un callback
    public void iniciarSesion(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                // Cuando Firebase responda...
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        callback.onSuccess(mAuth.getCurrentUser().getUid());
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() :
                                context.getString(R.string.error_general);
                        callback.onFailure(error);
                    }
                });
    }

    // Función para registrar un nuevo usuario con email y contraseña
    public void registrar(String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        callback.onSuccess(mAuth.getCurrentUser().getUid());
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() :
                                context.getString(R.string.error_registro);
                        callback.onFailure(error);
                    }
                });
    }

    // Interfaz para la respuesta asíncrona de firebase-auth
    // La clase que llame a iniciarSesión deberá implementar estos dos métodos
    public interface AuthCallback {
        void onSuccess(String userId);
        void onFailure(String errorMessage);
    }

    public void cerrarSesion() {
        mAuth.signOut();
    }
}