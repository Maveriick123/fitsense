package es.udc.psi_2025_26.FitSense.presentation.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import es.udc.psi_2025_26.FitSense.AppContainer;
import es.udc.psi_2025_26.FitSense.GlobalApplication;
import es.udc.psi_2025_26.FitSense.R;
import es.udc.psi_2025_26.FitSense.databinding.ActivityCalibrationBinding;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.presentation.common.PermissionHelper;
import es.udc.psi_2025_26.FitSense.presentation.common.PresenterHolder;
import es.udc.psi_2025_26.FitSense.presentation.presenter.CalibrationPresenter;
import es.udc.psi_2025_26.FitSense.presentation.presenter.CalibrationView;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.CalibrationParamsUiModel;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.CalibrationResultUiModel;
import es.udc.psi_2025_26.FitSense.presentation.uimodel.ExerciseUiModel;

// Pantalla de autocalibración de un ejercicio. Solo pinta, toda la lógica está en el
// Presenter, que se guarda en un PresenterHolder para que sobreviva a las rotaciones
public class CalibrationActivity extends AppCompatActivity implements CalibrationView {

    private static final String EXTRA_EXERCISE_TYPE = "es.udc.psi_2025_26.FitSense.EXTRA_CALIBRATION_EXERCISE";

    private ActivityCalibrationBinding binding;
    private CalibrationPresenter presenter;

    // Método para crear el Intent que abre la calibración de un ejercicio
    public static Intent newIntent(Context context, ExerciseType type) {
        return new Intent(context, CalibrationActivity.class).putExtra(EXTRA_EXERCISE_TYPE, type.name());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCalibrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.calibrationRoot, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        PresenterHolder holder = new ViewModelProvider(this).get(PresenterHolder.class);
        presenter = holder.get();
        if (presenter == null) {
            ExerciseType type = ExerciseType.fromName(
                    getIntent().getStringExtra(EXTRA_EXERCISE_TYPE), ExerciseType.SALTOS);
            AppContainer container = ((GlobalApplication) getApplication()).getContainer();
            presenter = container.calibrationPresenter(type);
            holder.set(presenter);
        }

        binding.btnCalibStart.setOnClickListener(v -> startCapture());
        binding.btnCalibRetry.setOnClickListener(v -> startCapture());
        binding.btnCalibStop.setOnClickListener(v -> presenter.onClickStop());
        binding.btnCalibAdjust.setOnClickListener(v -> submitRealCount());
        binding.etRealCount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitRealCount();
                return true;
            }
            return false;
        });
        binding.btnCalibDiscard.setOnClickListener(v -> presenter.onClickDiscard());
        binding.btnCalibSave.setOnClickListener(v -> presenter.onClickSave());
        binding.btnCalibReset.setOnClickListener(v -> presenter.onClickReset());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                presenter.onBackPressed();
            }
        });

        presenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    private void startCapture() {
        if (PermissionHelper.hasServicePermissions(this)) {
            presenter.onClickStart();
        } else {
            Toast.makeText(this, R.string.permiso_denegado, Toast.LENGTH_LONG).show();
        }
    }

    private void submitRealCount() {
        presenter.onSubmitRealCount(binding.etRealCount.getText().toString());
    }

    // Se muestra solo uno de los cuatro paneles
    private void showPanel(View panel) {
        binding.panelIntro.setVisibility(panel == binding.panelIntro ? View.VISIBLE : View.GONE);
        binding.panelCapture.setVisibility(panel == binding.panelCapture ? View.VISIBLE : View.GONE);
        binding.panelReview.setVisibility(panel == binding.panelReview ? View.VISIBLE : View.GONE);
        binding.panelResult.setVisibility(panel == binding.panelResult ? View.VISIBLE : View.GONE);
    }

    // Métodos de CalibrationView

    @Override
    public void showExercise(ExerciseUiModel exercise) {
        binding.tvCalibTitle.setText(getString(R.string.calib_title, getString(exercise.nameRes)));
        binding.tvCalibInstructions.setText(exercise.calibrationInstructionsRes);
    }

    @Override
    public void showParams(CalibrationParamsUiModel params) {
        binding.tvCalibParams.setText(getString(R.string.calib_params,
                params.threshold, params.minIntervalMs, getString(params.originRes)));
    }

    @Override
    public void showIntro() {
        showPanel(binding.panelIntro);
    }

    @Override
    public void showCountdown(int secondsLeft) {
        showPanel(binding.panelCapture);
        binding.tvCaptureStatus.setText(getString(R.string.calib_status_countdown, secondsLeft));
        binding.tvCaptureHint.setText(R.string.calib_hint_countdown);
    }

    @Override
    public void showRecording(int liveCount) {
        showPanel(binding.panelCapture);
        binding.tvCaptureStatus.setText(getString(R.string.calib_status_recording, liveCount));
        binding.tvCaptureHint.setText(R.string.calib_hint_recording);
    }

    @Override
    public void showReview(int detectedCount) {
        showPanel(binding.panelReview);
        binding.tvReviewDetected.setText(getString(R.string.calib_review_detected, detectedCount));
    }

    @Override
    public void showResult(CalibrationResultUiModel r) {
        showPanel(binding.panelResult);
        binding.tvResultSummary.setText(getString(
                r.exact ? R.string.calib_result_exact : R.string.calib_result_approx,
                r.detectedBefore, r.realCount, r.predictedAfter));
        if (r.hasChanges) {
            binding.tvResultChanges.setText(getString(R.string.calib_result_changes,
                    r.thresholdBefore, r.thresholdAfter, r.intervalBefore, r.intervalAfter, r.attempts));
        }
        binding.tvResultRobustness.setText(getString(r.robustnessRes,
                r.stableMinThreshold, r.stableMaxThreshold));
    }

    @Override
    public void setResetEnabled(boolean enabled) {
        binding.btnCalibReset.setEnabled(enabled);
    }

    @Override
    public void clearRealCount() {
        binding.etRealCount.setText("");
    }

    @Override
    public void showRealCountError(int min, int max) {
        binding.etRealCount.setError(getString(R.string.calib_error_real_count, min, max));
    }

    @Override
    public void showNoDataError() {
        Toast.makeText(this, R.string.calib_error_no_data, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showWorkoutInProgress() {
        Toast.makeText(this, R.string.calib_error_workout_running, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showSaved() {
        Toast.makeText(this, R.string.calib_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showResetDone() {
        Toast.makeText(this, R.string.calib_reset_done, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.calib_reset_title)
                .setMessage(R.string.calib_reset_message)
                .setPositiveButton(R.string.calib_btn_reset, (d, w) -> presenter.onResetConfirmed())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void confirmExit() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.calib_exit_title)
                .setMessage(R.string.calib_exit_message)
                .setPositiveButton(R.string.calib_exit_discard, (d, w) -> presenter.onExitConfirmed())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void close() {
        finish();
    }
}
