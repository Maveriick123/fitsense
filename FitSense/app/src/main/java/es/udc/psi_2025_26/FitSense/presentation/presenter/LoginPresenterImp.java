package es.udc.psi_2025_26.FitSense.presentation.presenter;

import es.udc.psi_2025_26.FitSense.domain.usecase.RegisterUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.SignInUseCase;
import es.udc.psi_2025_26.FitSense.presentation.common.TaskRunner;

public class LoginPresenterImp implements LoginPresenter {

    private LoginView mView;
    private final SignInUseCase mSignInUseCase;
    private final RegisterUseCase mRegisterUseCase;
    private final TaskRunner mTaskRunner;

    public LoginPresenterImp(LoginView view, SignInUseCase signInUseCase,
                             RegisterUseCase registerUseCase, TaskRunner taskRunner) {
        mView = view;
        mSignInUseCase = signInUseCase;
        mRegisterUseCase = registerUseCase;
        mTaskRunner = taskRunner;
    }

    @Override
    public void onLogin(String email, String password) {
        authenticate(email, password, false);
    }

    @Override
    public void onRegister(String email, String password) {
        authenticate(email, password, true);
    }

    @Override
    public void onDestroy() {
        mView = null;
    }

    private void authenticate(String email, String password, boolean register) {
        if (isBlank(email) || isBlank(password)) {
            // Si los campos están vacíos se avisa y no se llama a Firebase
            mView.showMissingCredentials();
            return;
        }
        mView.showLoading(true);
        mTaskRunner.execute(
                () -> register
                        ? mRegisterUseCase.execute(email, password)
                        : mSignInUseCase.execute(email, password),
                new TaskRunner.Callback<String>() {
                    @Override
                    public void onSuccess(String userId) {
                        if (mView == null) return;
                        mView.showLoading(false);
                        mView.showWelcome();
                        mView.navigateToRoutines();
                    }

                    @Override
                    public void onError(Exception e) {
                        if (mView == null) return;
                        mView.showLoading(false);
                        if (register) {
                            mView.showRegisterError(e.getMessage());
                        } else {
                            mView.showLoginError(e.getMessage());
                        }
                    }
                });
    }

    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
