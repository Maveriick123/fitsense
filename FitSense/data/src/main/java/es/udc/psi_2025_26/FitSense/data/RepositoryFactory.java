package es.udc.psi_2025_26.FitSense.data;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import es.udc.psi_2025_26.FitSense.data.repository.AuthRepositoryImp;
import es.udc.psi_2025_26.FitSense.data.repository.CalibrationRepositoryImp;
import es.udc.psi_2025_26.FitSense.data.repository.RoutineRepositoryImp;
import es.udc.psi_2025_26.FitSense.domain.repository.AuthRepository;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRepository;
import es.udc.psi_2025_26.FitSense.domain.repository.RoutineRepository;

// Única entrada al módulo :data
// Firebase es una dependencia "implementation" de :data, así que :app no la ve. Aquí se crean
// las implementaciones y solo se devuelven los puertos del dominio
public final class RepositoryFactory {

    private RepositoryFactory() {}

    public static AuthRepository createAuthRepository() {
        return new AuthRepositoryImp(FirebaseAuth.getInstance());
    }

    public static RoutineRepository createRoutineRepository() {
        return new RoutineRepositoryImp(FirebaseDatabase.getInstance());
    }

    public static CalibrationRepository createCalibrationRepository(Context context) {
        return new CalibrationRepositoryImp(context.getApplicationContext().getSharedPreferences(
                CalibrationRepositoryImp.PREFS_NAME, Context.MODE_PRIVATE));
    }
}
