package es.udc.psi_2025_26.FitSense.presentation.presenter;

import java.util.List;

import es.udc.psi_2025_26.FitSense.domain.exception.SessionRequiredException;
import es.udc.psi_2025_26.FitSense.domain.model.Routine;
import es.udc.psi_2025_26.FitSense.domain.usecase.GetRoutineHistoryUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.SignOutUseCase;
import es.udc.psi_2025_26.FitSense.presentation.common.TaskRunner;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.RoutineUiModelMapper;

public class RoutinesPresenterImp implements RoutinesPresenter {

    private RoutinesView mView;
    private final GetRoutineHistoryUseCase mGetRoutineHistoryUseCase;
    private final SignOutUseCase mSignOutUseCase;
    private final TaskRunner mTaskRunner;

    public RoutinesPresenterImp(RoutinesView view, GetRoutineHistoryUseCase getRoutineHistoryUseCase,
                                SignOutUseCase signOutUseCase, TaskRunner taskRunner) {
        mView = view;
        mGetRoutineHistoryUseCase = getRoutineHistoryUseCase;
        mSignOutUseCase = signOutUseCase;
        mTaskRunner = taskRunner;
    }

    @Override
    public void onStart() {
        mView.showLoading();
        mTaskRunner.execute(mGetRoutineHistoryUseCase::execute, new TaskRunner.Callback<List<Routine>>() {
            @Override
            public void onSuccess(List<Routine> routines) {
                if (mView == null) return;
                if (routines.isEmpty()) {
                    mView.showNoRoutines();
                } else {
                    mView.showRoutines(RoutineUiModelMapper.map(routines));
                }
            }

            @Override
            public void onError(Exception e) {
                if (mView == null) return;
                if (e instanceof SessionRequiredException) {
                    mView.navigateToLogin();
                } else {
                    mView.showError();
                }
            }
        });
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
        mView = null;
    }
}
