package es.udc.psi_2025_26.FitSense;

import android.os.Bundle;
import android.text.TextUtils;
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

import es.udc.psi_2025_26.FitSense.databinding.ActivityCalibrationBinding;
import es.udc.psi_2025_26.FitSense.detection.CalibrationEngine;
import es.udc.psi_2025_26.FitSense.detection.DetectorParams;

/**
 * Pantalla de autocalibración de un ejercicio.
 *
 * Bucle: Empezar -> (cuenta atrás) -> el usuario hace N repeticiones -> se queda quieto (fin
 * automático) -> la app muestra cuántas detectó -> el usuario introduce las reales -> se ajustan
 * los parámetros -> "Probar de nuevo" con los nuevos valores ... -> "Guardar".
 *
 * La Activity solo pinta estado; toda la lógica está en {@link CalibrationViewModel}.
 * La interfaz es una función del estado (fase de captura + resultado), de modo que tras una
 * rotación o al volver desde la notificación se reconstruye siempre correctamente.
 */
public class CalibrationActivity extends AppCompatActivity {

    public static final String EXTRA_EXERCISE_TYPE = "es.udc.psi_2025_26.FitSense.CALIB_EXERCISE";

    private ActivityCalibrationBinding binding;
    private CalibrationViewModel viewModel;

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

        ExerciseType type = ExerciseType.fromName(
                getIntent().getStringExtra(EXTRA_EXERCISE_TYPE), ExerciseType.SALTOS);
        viewModel = new ViewModelProvider(this).get(CalibrationViewModel.class);
        viewModel.init(type);

        String exerciseName = getString(viewModel.getType().nameRes);
        binding.tvCalibTitle.setText(getString(R.string.calib_title, exerciseName));
        binding.tvCalibInstructions.setText(viewModel.getType().calibrationInstructionsRes);

        binding.btnCalibStart.setOnClickListener(v -> startCapture());
        binding.btnCalibRetry.setOnClickListener(v -> startCapture());
        binding.btnCalibStop.setOnClickListener(v -> viewModel.stopCapture());
        binding.btnCalibAdjust.setOnClickListener(v -> submitRealCount());
        binding.etRealCount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitRealCount();
                return true;
            }
            return false;
        });
        binding.btnCalibDiscard.setOnClickListener(v -> CalibrationRepository.getInstance().reset());
        binding.btnCalibSave.setOnClickListener(v -> {
            viewModel.save();
            Toast.makeText(this, R.string.calib_saved, Toast.LENGTH_SHORT).show();
            finish();
        });
        binding.btnCalibReset.setOnClickListener(v -> confirmReset());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmExit();
            }
        });

        viewModel.getTrialParams().observe(this, p -> renderParams());
        viewModel.getCaptureState().observe(this, s -> render());
        viewModel.getResult().observe(this, r -> render());
    }

    // ------------------------------------------------------------------ Acciones

    private void startCapture() {
        if (!PermissionHelper.hasServicePermissions(this)) {
            Toast.makeText(this, R.string.permiso_denegado, Toast.LENGTH_LONG).show();
            return;
        }
        binding.etRealCount.setText("");
        viewModel.startCapture();
    }

    private void submitRealCount() {
        String text = binding.etRealCount.getText().toString().trim();
        int real;
        try {
            real = TextUtils.isEmpty(text) ? -1 : Integer.parseInt(text);
        } catch (NumberFormatException e) {
            real = -1;
        }
        if (real < CalibrationViewModel.MIN_REAL_REPS || real > CalibrationViewModel.MAX_REAL_REPS) {
            binding.etRealCount.setError(getString(R.string.calib_error_real_count,
                    CalibrationViewModel.MIN_REAL_REPS, CalibrationViewModel.MAX_REAL_REPS));
            return;
        }
        if (!viewModel.submitRealCount(real)) {
            Toast.makeText(this, R.string.calib_error_no_data, Toast.LENGTH_LONG).show();
        }
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.calib_reset_title)
                .setMessage(R.string.calib_reset_message)
                .setPositiveButton(R.string.calib_btn_reset, (d, w) -> {
                    viewModel.resetToDefaults();
                    Toast.makeText(this, R.string.calib_reset_done, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmExit() {
        if (!viewModel.hasUnsavedChanges() && !CalibrationRepository.getInstance().isCapturing()) {
            finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.calib_exit_title)
                .setMessage(R.string.calib_exit_message)
                .setPositiveButton(R.string.calib_exit_discard, (d, w) -> finish())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // ------------------------------------------------------------------ Render

    private void renderParams() {
        DetectorParams p = viewModel.getTrialParams().getValue();
        if (p == null) return;
        int origin = viewModel.hasUnsavedChanges() ? R.string.calib_origin_trial
                : viewModel.isCalibrationSaved() ? R.string.calib_origin_saved
                : R.string.calib_origin_default;
        binding.tvCalibParams.setText(getString(R.string.calib_params,
                p.threshold, p.minIntervalMs, getString(origin)));
    }

    private void render() {
        CalibrationRepository.CaptureState state = viewModel.getCaptureState().getValue();
        CalibrationEngine.Result result = viewModel.getResult().getValue();
        if (state == null) return;

        boolean idle = state.phase == CalibrationRepository.Phase.IDLE;
        boolean capturing = state.phase == CalibrationRepository.Phase.COUNTDOWN
                || state.phase == CalibrationRepository.Phase.RECORDING;
        boolean finished = state.phase == CalibrationRepository.Phase.FINISHED;

        binding.panelIntro.setVisibility(idle ? View.VISIBLE : View.GONE);
        binding.panelCapture.setVisibility(capturing ? View.VISIBLE : View.GONE);
        binding.panelReview.setVisibility(finished && result == null ? View.VISIBLE : View.GONE);
        binding.panelResult.setVisibility(finished && result != null ? View.VISIBLE : View.GONE);
        binding.btnCalibReset.setEnabled(!capturing);

        if (state.phase == CalibrationRepository.Phase.COUNTDOWN) {
            binding.tvCaptureStatus.setText(getString(R.string.calib_status_countdown, state.secondsLeft));
            binding.tvCaptureHint.setText(R.string.calib_hint_countdown);
        } else if (state.phase == CalibrationRepository.Phase.RECORDING) {
            binding.tvCaptureStatus.setText(getString(R.string.calib_status_recording, state.liveCount));
            binding.tvCaptureHint.setText(R.string.calib_hint_recording);
        } else if (finished && result == null) {
            binding.tvReviewDetected.setText(
                    getString(R.string.calib_review_detected, viewModel.computeDetectedCount()));
        } else if (finished) {
            renderResult(result);
        }
        renderParams();
    }

    private void renderResult(CalibrationEngine.Result r) {
        int real = viewModel.getLastRealCount();
        boolean exact = r.totalError == 0;
        binding.tvResultSummary.setText(exact
                ? getString(R.string.calib_result_exact, viewModel.getLastDetectedCount(), real, r.lastAttemptPredicted)
                : getString(R.string.calib_result_approx, viewModel.getLastDetectedCount(), real, r.lastAttemptPredicted));

        DetectorParams before = viewModel.getParamsBeforeAdjust();
        if (before != null) {
            binding.tvResultChanges.setText(getString(R.string.calib_result_changes,
                    before.threshold, r.params.threshold, before.minIntervalMs, r.params.minIntervalMs, r.attempts));
        }

        float ratio = r.robustnessRatio();
        int robustness = ratio >= 1.5f ? R.string.calib_robust_high
                : ratio >= 1.15f ? R.string.calib_robust_medium
                : R.string.calib_robust_low;
        binding.tvResultRobustness.setText(getString(robustness, r.stableMinThreshold, r.stableMaxThreshold));
    }
}
