package es.udc.psi_2025_26.FitSense;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import es.udc.psi_2025_26.FitSense.data.repository.AuthRepositoryImp;
import es.udc.psi_2025_26.FitSense.data.repository.CalibrationRepositoryImp;
import es.udc.psi_2025_26.FitSense.data.repository.RoutineRepositoryImp;
import es.udc.psi_2025_26.FitSense.domain.repository.AuthRepository;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRepository;
import es.udc.psi_2025_26.FitSense.domain.repository.RoutineRepository;
import es.udc.psi_2025_26.FitSense.domain.usecase.GetCalibrationUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.GetRoutineHistoryUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.RegisterUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.ResetCalibrationUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.SaveCalibrationUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.SignInUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.SignOutUseCase;

// Clase donde se crean todas las dependencias de la app (repositorios, casos de uso...)
// Es el único sitio que conoce las implementaciones
// Las Activities lo piden con ((GlobalApplication) getApplication()).getContainer()
public class AppContainer {

    private final AuthRepository mAuthRepository;
    private final RoutineRepository mRoutineRepository;
    private final CalibrationRepository mCalibrationRepository;

    public AppContainer(Context context) {
        Context appContext = context.getApplicationContext();
        mAuthRepository = new AuthRepositoryImp(FirebaseAuth.getInstance());
        mRoutineRepository = new RoutineRepositoryImp(FirebaseDatabase.getInstance());
        mCalibrationRepository = new CalibrationRepositoryImp(appContext.getSharedPreferences(
                CalibrationRepositoryImp.PREFS_NAME, Context.MODE_PRIVATE));
    }

    // Casos de uso

    public SignInUseCase signInUseCase() {
        return new SignInUseCase(mAuthRepository);
    }

    public RegisterUseCase registerUseCase() {
        return new RegisterUseCase(mAuthRepository);
    }

    public SignOutUseCase signOutUseCase() {
        return new SignOutUseCase(mAuthRepository);
    }

    public GetRoutineHistoryUseCase getRoutineHistoryUseCase() {
        return new GetRoutineHistoryUseCase(mAuthRepository, mRoutineRepository);
    }

    public GetCalibrationUseCase getCalibrationUseCase() {
        return new GetCalibrationUseCase(mAuthRepository, mCalibrationRepository);
    }

    public SaveCalibrationUseCase saveCalibrationUseCase() {
        return new SaveCalibrationUseCase(mAuthRepository, mCalibrationRepository);
    }

    public ResetCalibrationUseCase resetCalibrationUseCase() {
        return new ResetCalibrationUseCase(mAuthRepository, mCalibrationRepository);
    }
}
