package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.exception.AuthException;
import es.udc.psi_2025_26.FitSense.domain.repository.AuthRepository;

// Registra un usuario nuevo y deja su sesión iniciada
public class RegisterUseCase {

    private final AuthRepository mAuthRepository;

    public RegisterUseCase(AuthRepository authRepository) {
        mAuthRepository = authRepository;
    }

    // Retorna el ID del usuario creado
    public String execute(String email, String password) throws AuthException {
        if (SignInUseCase.isBlank(email) || SignInUseCase.isBlank(password)) {
            throw new IllegalArgumentException("El email y la contraseña son obligatorios");
        }
        return mAuthRepository.register(email.trim(), password.trim());
    }
}
