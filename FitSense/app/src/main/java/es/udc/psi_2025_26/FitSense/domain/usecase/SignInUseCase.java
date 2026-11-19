package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.exception.AuthException;
import es.udc.psi_2025_26.FitSense.domain.repository.AuthRepository;

// Inicia la sesión de un usuario con email y contraseña
public class SignInUseCase {

    private final AuthRepository mAuthRepository;

    public SignInUseCase(AuthRepository authRepository) {
        mAuthRepository = authRepository;
    }

    // Retorna el ID del usuario
    public String execute(String email, String password) throws AuthException {
        if (isBlank(email) || isBlank(password)) {
            throw new IllegalArgumentException("El email y la contraseña son obligatorios");
        }
        return mAuthRepository.signIn(email.trim(), password.trim());
    }

    static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
