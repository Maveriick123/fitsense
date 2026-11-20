package es.udc.psi_2025_26.FitSense.presentation.presenter;

import java.util.List;

import es.udc.psi_2025_26.FitSense.domain.detection.CalibrationEngine;
import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.exception.WorkoutInProgressException;
import es.udc.psi_2025_26.FitSense.domain.model.Calibration;
import es.udc.psi_2025_26.FitSense.domain.model.CalibrationSession;
import es.udc.psi_2025_26.FitSense.domain.model.CaptureState;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRecorder;
import es.udc.psi_2025_26.FitSense.domain.usecase.AdjustCalibrationUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.CountRecordedRepetitionsUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.DiscardCalibrationCaptureUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.GetCalibrationUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.LabelAttemptUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.ResetCalibrationUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.SaveCalibrationUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.StartCalibrationCaptureUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.StopCalibrationCaptureUseCase;
import es.udc.psi_2025_26.FitSense.domain.usecase.WatchCalibrationCaptureUseCase;
import es.udc.psi_2025_26.FitSense.presentation.common.TaskRunner;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.CalibrationUiModelMapper;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.ExerciseUiModelMapper;

// Presenter de la sesión de calibración
// Funciona así: Empezar -> cuenta atrás -> el usuario hace N repeticiones -> se queda quieto
// (fin automático) -> se muestran las detectadas -> el usuario mete las reales -> se ajustan
// los parámetros -> "Probar de nuevo" con los valores nuevos... -> "Guardar"
// Los parámetros ajustados NO se guardan solos, se usan en el siguiente intento y solo se
// guardan al pulsar "Guardar". La pantalla se pinta según el estado (fase + resultado),
// por eso al volver a engancharse la vista siempre se pinta bien
public class CalibrationPresenterImp implements CalibrationPresenter, CalibrationRecorder.Listener {

    private CalibrationView mView;
    private final ExerciseType mType;

    private final GetCalibrationUseCase mGetCalibrationUseCase;
    private final SaveCalibrationUseCase mSaveCalibrationUseCase;
    private final ResetCalibrationUseCase mResetCalibrationUseCase;
    private final StartCalibrationCaptureUseCase mStartCaptureUseCase;
    private final StopCalibrationCaptureUseCase mStopCaptureUseCase;
    private final DiscardCalibrationCaptureUseCase mDiscardCaptureUseCase;
    private final WatchCalibrationCaptureUseCase mWatchCaptureUseCase;
    private final CountRecordedRepetitionsUseCase mCountRepetitionsUseCase;
    private final LabelAttemptUseCase mLabelAttemptUseCase;
    private final AdjustCalibrationUseCase mAdjustCalibrationUseCase;
    private final TaskRunner mTaskRunner;

    private final CalibrationSession mSession = new CalibrationSession();
    private Calibration mSaved;
    private DetectorParams mTrialParams;
    private DetectorParams mParamsBeforeAdjust;
    private CalibrationEngine.Result mResult;
    private CaptureState mState;
    private int mLastRealCount;
    private int mLastDetectedCount;
    private boolean mComputing = false;

    public CalibrationPresenterImp(ExerciseType type,
                                   GetCalibrationUseCase getCalibrationUseCase,
                                   SaveCalibrationUseCase saveCalibrationUseCase,
                                   ResetCalibrationUseCase resetCalibrationUseCase,
                                   StartCalibrationCaptureUseCase startCaptureUseCase,
                                   StopCalibrationCaptureUseCase stopCaptureUseCase,
                                   DiscardCalibrationCaptureUseCase discardCaptureUseCase,
                                   WatchCalibrationCaptureUseCase watchCaptureUseCase,
                                   CountRecordedRepetitionsUseCase countRepetitionsUseCase,
                                   LabelAttemptUseCase labelAttemptUseCase,
                                   AdjustCalibrationUseCase adjustCalibrationUseCase,
                                   TaskRunner taskRunner) {
        mType = type;
        mGetCalibrationUseCase = getCalibrationUseCase;
        mSaveCalibrationUseCase = saveCalibrationUseCase;
        mResetCalibrationUseCase = resetCalibrationUseCase;
        mStartCaptureUseCase = startCaptureUseCase;
        mStopCaptureUseCase = stopCaptureUseCase;
        mDiscardCaptureUseCase = discardCaptureUseCase;
        mWatchCaptureUseCase = watchCaptureUseCase;
        mCountRepetitionsUseCase = countRepetitionsUseCase;
        mLabelAttemptUseCase = labelAttemptUseCase;
        mAdjustCalibrationUseCase = adjustCalibrationUseCase;
        mTaskRunner = taskRunner;

        mSaved = mGetCalibrationUseCase.execute(type);
        mTrialParams = mSaved.getParams();
        mState = mWatchCaptureUseCase.execute(null);
        if (!mState.isCapturing()) {
            // Si quedó algo de una sesión anterior se empieza desde cero
            mDiscardCaptureUseCase.execute();
            mState = CaptureState.IDLE;
        }
    }

    // ------------------------------------------------------------------ Ciclo de vida

    @Override
    public void attachView(CalibrationView view) {
        mView = view;
        mState = mWatchCaptureUseCase.execute(this);
        mView.showExercise(ExerciseUiModelMapper.map(mType));
        render();
    }

    @Override
    public void detachView() {
        mWatchCaptureUseCase.execute(null);
        mView = null;
    }

    @Override
    public void onFinish() {
        // La pantalla se cierra definitivamente: no dejamos una captura huérfana en marcha.
        mWatchCaptureUseCase.execute(null);
        mDiscardCaptureUseCase.execute();
    }

    @Override
    public void onCaptureStateChanged(CaptureState state) {
        mState = state;
        render();
    }

    // ------------------------------------------------------------------ Acciones

    @Override
    public void onClickStart() {
        mView.clearRealCount();
        mResult = null;
        try {
            mStartCaptureUseCase.execute(mType, mTrialParams);
        } catch (WorkoutInProgressException e) {
            mView.showWorkoutInProgress();
        }
    }

    @Override
    public void onClickStop() {
        mStopCaptureUseCase.execute();
    }

    @Override
    public void onSubmitRealCount(String text) {
        int realCount = parse(text);
        if (!LabelAttemptUseCase.isValidRealCount(realCount)) {
            mView.showRealCountError(LabelAttemptUseCase.MIN_REAL_REPS, LabelAttemptUseCase.MAX_REAL_REPS);
            return;
        }
        if (mComputing || mResult != null) return;

        int detected = mCountRepetitionsUseCase.execute(mType, mTrialParams);
        if (!mLabelAttemptUseCase.execute(mSession, realCount)) {
            mView.showNoDataError();
            return;
        }
        mLastRealCount = realCount;
        mLastDetectedCount = detected;

        final DetectorParams current = mTrialParams;
        final List<CalibrationEngine.LabeledRecording> attempts = mSession.snapshot();
        mComputing = true;
        mTaskRunner.execute(() -> mAdjustCalibrationUseCase.execute(mType, attempts, current),
                new TaskRunner.Callback<CalibrationEngine.Result>() {
                    @Override
                    public void onSuccess(CalibrationEngine.Result result) {
                        mComputing = false;
                        mParamsBeforeAdjust = current;
                        mTrialParams = result.params;
                        mResult = result;
                        render();
                    }

                    @Override
                    public void onError(Exception e) {
                        mComputing = false;
                        if (mView != null) mView.showNoDataError();
                    }
                });
    }

    @Override
    public void onClickDiscard() {
        mDiscardCaptureUseCase.execute();
    }

    @Override
    public void onClickSave() {
        mSaved = mSaveCalibrationUseCase.execute(mType, mTrialParams);
        mView.showSaved();
        mView.close();
    }

    @Override
    public void onClickReset() {
        mView.confirmReset();
    }

    @Override
    public void onResetConfirmed() {
        mSaved = mResetCalibrationUseCase.execute(mType);
        mSession.clear();
        mParamsBeforeAdjust = null;
        mTrialParams = mSaved.getParams();
        mResult = null;
        if (!mState.isCapturing()) {
            mDiscardCaptureUseCase.execute();
        }
        mView.showResetDone();
        render();
    }

    @Override
    public void onBackPressed() {
        if (!hasUnsavedChanges() && !mState.isCapturing()) {
            mView.close();
        } else {
            mView.confirmExit();
        }
    }

    @Override
    public void onExitConfirmed() {
        mView.close();
    }

    // ------------------------------------------------------------------ Render

    private void render() {
        if (mView == null) return;
        mView.showParams(CalibrationUiModelMapper.mapParams(
                mTrialParams, hasUnsavedChanges(), mSaved.isCustom()));
        mView.setResetEnabled(!mState.isCapturing());
        switch (mState.phase) {
            case COUNTDOWN:
                mView.showCountdown(mState.secondsLeft);
                break;
            case RECORDING:
                mView.showRecording(mState.liveCount);
                break;
            case FINISHED:
                if (mResult == null) {
                    mView.showReview(mCountRepetitionsUseCase.execute(mType, mTrialParams));
                } else {
                    mView.showResult(CalibrationUiModelMapper.mapResult(
                            mResult, mParamsBeforeAdjust, mLastDetectedCount, mLastRealCount));
                }
                break;
            case IDLE:
            default:
                mView.showIntro();
                break;
        }
    }

    private boolean hasUnsavedChanges() {
        return !mTrialParams.equals(mSaved.getParams());
    }

    private static int parse(String text) {
        try {
            return text == null ? -1 : Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
