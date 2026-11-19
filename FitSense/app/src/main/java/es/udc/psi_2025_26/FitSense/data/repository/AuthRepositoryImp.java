package es.udc.psi_2025_26.FitSense.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import es.udc.psi_2025_26.FitSense.domain.exception.AuthException;
import es.udc.psi_2025_26.FitSense.domain.repository.AuthRepository;

// Implementación del puerto AuthRepository con Firebase Authentication
// Firebase es asíncrono (Task + listener), y para no llevar callbacks por el dominio aquí se
// espera al resultado con Tasks.await. Por eso hay que llamar a estos métodos fuera del hilo
// principal (el Presenter usa el TaskRunner)
public class AuthRepositoryImp implements AuthRepository {

    private static final long TIMEOUT_S = 30;

    private final FirebaseAuth mAuth;

    public AuthRepositoryImp(FirebaseAuth auth) {
        mAuth = auth;
    }

    @Override
    public String signIn(String email, String password) throws AuthException {
        return awaitUserId(mAuth.signInWithEmailAndPassword(email, password));
    }

    @Override
    public String register(String email, String password) throws AuthException {
        return awaitUserId(mAuth.createUserWithEmailAndPassword(email, password));
    }

    @Override
    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    @Override
    public void signOut() {
        mAuth.signOut();
    }

    // Espera a la tarea de Firebase y pasa sus errores a la excepción del dominio
    private String awaitUserId(Task<AuthResult> task) throws AuthException {
        try {
            Tasks.await(task, TIMEOUT_S, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new AuthException(cause.getMessage(), cause);
        } catch (TimeoutException e) {
            throw new AuthException(e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuthException(e.getMessage(), e);
        }
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new AuthException();
        }
        return userId;
    }
}
