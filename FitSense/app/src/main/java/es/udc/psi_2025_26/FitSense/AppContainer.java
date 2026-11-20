package es.udc.psi_2025_26.FitSense;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executors;

import es.udc.psi_2025_26.FitSense.data.RepositoryFactory;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.repository.AuthRepository;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRecorder;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRepository;
import es.udc.psi_2025_26.FitSense.domain.repository.RoutineRepository;
import es.udc.psi_2025_26.FitSense.domain.repository.WorkoutTracker;
import es.udc.psi_2025_26.FitSense.domain.usecase.AdjustCalibrationUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.CountRecordedRepetitionsUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.DiscardCalibrationCaptureUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.GetCalibrationUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.LabelAttemptUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.ListCalibrationsUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.RegisterUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.ResetCalibrationUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.SaveCalibrationUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.SaveWorkoutUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.SignInUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.SignOutUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.StartCalibrationCaptureUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.StartExerciseUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.StopCalibrationCaptureUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.WatchCalibrationCaptureUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.WatchRoutineHistoryUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.WatchWorkoutUseCase;
import es.udc.psi_2025_26.FitSense.presentation.common.TaskRunner;
import es.udc.psi_2025_26.FitSense.presentation.presenter.CalibrationPresenter;
import es.udc.psi_2025_26.FitSense.presentation.presenter.CalibrationPresenterImp;
import es.udc.psi_2025_26.FitSense.sensor.ServiceCalibrationRecorder;
import es.udc.psi_2025_26.FitSense.sensor.ServiceWorkoutTracker;

// Clase donde se crean todas las dependencias de la app (repositorios, casos de uso...)
// Es el único sitio que conoce las implementaciones
// Las Activities lo piden con ((GlobalApplication) getApplication()).getContainer()
public class AppContainer {

    // Hilos para las llamadas a Firebase y para los cálculos de la calibración
    private static final int BACKGROUND_THREADS = 2;

    private final AuthRepository mAuthRepository;
    private final RoutineRepository mRoutineRepository;
    private final CalibrationRepository mCalibrationRepository;
    private final WorkoutTracker mWorkoutTracker;
    private final CalibrationRecorder mCalibrationRecorder;
    private final TaskRunner mTaskRunner;

    public AppContainer(Context context) {
        Context appContext = context.getApplicationContext();
        mAuthRepository = RepositoryFactory.createAuthRepository();
        mRoutineRepository = RepositoryFactory.createRoutineRepository();
        mCalibrationRepository = RepositoryFactory.createCalibrationRepository(appContext);
        mWorkoutTracker = new ServiceWorkoutTracker(appContext);
        mCalibrationRecorder = new ServiceCalibrationRecorder(appContext);
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mTaskRunner = new TaskRunner(Executors.newFixedThreadPool(BACKGROUND_THREADS), mainHandler::post);
    }

    public TaskRunner taskRunner() {
        return mTaskRunner;
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

    public WatchRoutineHistoryUseCase watchRoutineHistoryUseCase() {
        return new WatchRoutineHistoryUseCase(mRoutineRepository);
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

    public StartExerciseUseCase startExerciseUseCase() {
        return new StartExerciseUseCase(getCalibrationUseCase(), mWorkoutTracker);
    }

    public WatchWorkoutUseCase watchWorkoutUseCase() {
        return new WatchWorkoutUseCase(mWorkoutTracker);
    }

    public SaveWorkoutUseCase saveWorkoutUseCase() {
        return new SaveWorkoutUseCase(mAuthRepository, mRoutineRepository, mWorkoutTracker);
    }

    public ListCalibrationsUseCase listCalibrationsUseCase() {
        return new ListCalibrationsUseCase(getCalibrationUseCase(), mWorkoutTracker);
    }

    // Presenters

    // El presenter de la calibración se crea aquí entero porque guarda el estado de la sesión
    public CalibrationPresenter calibrationPresenter(ExerciseType type) {
        return new CalibrationPresenterImp(type,
                getCalibrationUseCase(), saveCalibrationUseCase(), resetCalibrationUseCase(),
                new StartCalibrationCaptureUseCase(mCalibrationRecorder, mWorkoutTracker),
                new StopCalibrationCaptureUseCase(mCalibrationRecorder),
                new DiscardCalibrationCaptureUseCase(mCalibrationRecorder),
                new WatchCalibrationCaptureUseCase(mCalibrationRecorder),
                new CountRecordedRepetitionsUseCase(mCalibrationRecorder),
                new LabelAttemptUseCase(mCalibrationRecorder),
                new AdjustCalibrationUseCase(),
                mTaskRunner);
    }
}
