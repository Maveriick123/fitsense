package es.udc.psi_2025_26.FitSense.domain.usecase;

import es.udc.psi_2025_26.FitSense.domain.repository.AuthRepository;

// Cierra la sesión del usuario actual
public class SignOutUseCase {

    private final AuthRepository mAuthRepository;

    public SignOutUseCase(AuthRepository authRepository) {
        mAuthRepository = authRepository;
    }

    public void execute() {
        mAuthRepository.signOut();
    }
}
