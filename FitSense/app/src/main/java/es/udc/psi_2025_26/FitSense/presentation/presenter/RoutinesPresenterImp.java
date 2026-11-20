package es.udc.psi_2025_26.FitSense.presentation.presenter;

import es.udc.psi_2025_26.FitSense.domain.exception.DataUnavailableException;
import es.udc.psi_2025_26.FitSense.domain.model.Routine;
import es.udc.psi_2025_26.FitSense.domain.repository.RoutineRepository;
import es.udc.psi_2025_26.FitSense.domain.usecase.SignOutUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.WatchRoutineHistoryUseCase;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.RoutineUiModelMapper;

// Presenter del historial. Las rutinas llegan en tiempo real (y ya en el hilo principal)
// por eso aquí no hace falta el TaskRunner
public class RoutinesPresenterImp implements RoutinesPresenter, RoutineRepository.Listener {

    private RoutinesView mView;
    private final WatchRoutineHistoryUseCase mWatchRoutineHistoryUseCase;
    private final SignOutUseCase mSignOutUseCase;

    public RoutinesPresenterImp(RoutinesView view, WatchRoutineHistoryUseCase watchRoutineHistoryUseCase,
                                SignOutUseCase signOutUseCase) {
        mView = view;
        mWatchRoutineHistoryUseCase = watchRoutineHistoryUseCase;
        mSignOutUseCase = signOutUseCase;
    }

    @Override
    public void onCreate() {
        mWatchRoutineHistoryUseCase.execute(this);
    }

    @Override
    public void onRoutineAdded(Routine routine) {
        if (mView != null) {
            mView.addRoutine(RoutineUiModelMapper.map(routine));
        }
    }

    @Override
    public void onError(DataUnavailableException e) {
        if (mView != null) {
            mView.showError(e.getMessage());
        }
    }

    @Override
    public void onClickNewRoutine() {
        mView.navigateToWorkout();
    }

    @Override
    public void onClickLogout() {
        mSignOutUseCase.execute();
        mView.showLoggedOut();
        mView.navigateToLogin();
    }

    @Override
    public void onDestroy() {
        mWatchRoutineHistoryUseCase.execute(null);
        mView = null;
    }
}
