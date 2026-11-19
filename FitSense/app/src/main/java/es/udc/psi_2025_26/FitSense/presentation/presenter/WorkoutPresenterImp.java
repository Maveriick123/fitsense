package es.udc.psi_2025_26.FitSense.presentation.presenter;

import es.udc.psi_2025_26.FitSense.domain.exception.SessionRequiredException;
import es.udc.psi_2025_26.FitSense.domain.exception.WorkoutInProgressException;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.model.WorkoutCounts;
import es.udc.psi_2025_26.FitSense.domain.repository.WorkoutTracker;
import es.udc.psi_2025_26.FitSense.domain.usecase.ListCalibrationsUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.SaveWorkoutUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.StartExerciseUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.WatchWorkoutUseCase;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.WorkoutUiModelMapper;

// Presenter de la pantalla de entrenamiento
// Los casos de uso de aquí son rápidos (arrancar el servicio, leer preferencias, encolar
// escrituras en Firebase...) así que se llaman directamente sin TaskRunner
public class WorkoutPresenterImp implements WorkoutPresenter {

    private WorkoutView mView;
    private final StartExerciseUseCase mStartExerciseUseCase;
    private final WatchWorkoutUseCase mWatchWorkoutUseCase;
    private final SaveWorkoutUseCase mSaveWorkoutUseCase;
    private final ListCalibrationsUseCase mListCalibrationsUseCase;

    private final WorkoutTracker.Listener mCountsListener = this::showCounts;

    public WorkoutPresenterImp(WorkoutView view, StartExerciseUseCase startExerciseUseCase,
                               WatchWorkoutUseCase watchWorkoutUseCase,
                               SaveWorkoutUseCase saveWorkoutUseCase,
                               ListCalibrationsUseCase listCalibrationsUseCase) {
        mView = view;
        mStartExerciseUseCase = startExerciseUseCase;
        mWatchWorkoutUseCase = watchWorkoutUseCase;
        mSaveWorkoutUseCase = saveWorkoutUseCase;
        mListCalibrationsUseCase = listCalibrationsUseCase;
    }

    @Override
    public void onResume() {
        showCounts(mWatchWorkoutUseCase.execute(mCountsListener));
    }

    @Override
    public void onPause() {
        mWatchWorkoutUseCase.execute(null);
    }

    @Override
    public void onClickStart(ExerciseType type) {
        mStartExerciseUseCase.execute(type);
    }

    @Override
    public void onClickSaveRoutine() {
        try {
            mSaveWorkoutUseCase.execute();
            mView.showRoutineSaved();
            // Contadores a 0 para el siguiente set
            showCounts(WorkoutCounts.empty());
        } catch (SessionRequiredException e) {
            mView.showSessionRequired();
        }
    }

    @Override
    public void onClickCalibrate() {
        try {
            mView.showCalibrationChooser(
                    WorkoutUiModelMapper.mapCalibrations(mListCalibrationsUseCase.execute()));
        } catch (WorkoutInProgressException e) {
            mView.showWorkoutInProgress();
        }
    }

    @Override
    public void onCalibrationChosen(ExerciseType type) {
        mView.navigateToCalibration(type);
    }

    @Override
    public void onDestroy() {
        mView = null;
    }

    private void showCounts(WorkoutCounts counts) {
        if (mView != null) {
            mView.showCounters(WorkoutUiModelMapper.mapCounters(counts));
        }
    }
}
