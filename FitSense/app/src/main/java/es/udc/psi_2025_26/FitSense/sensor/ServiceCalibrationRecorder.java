package es.udc.psi_2025_26.FitSense.sensor;

import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.detection.SignalRecording;
import es.udc.psi_2025_26.FitSense.domain.model.CaptureState;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.repository.CalibrationRecorder;

// Implementación del puerto CalibrationRecorder con JumpService
// La calibración se graba en el mismo servicio que el entrenamiento para que se capture
// igual: mismo sensor, misma frecuencia y funcionando con la pantalla bloqueada
public class ServiceCalibrationRecorder implements CalibrationRecorder {

    private final Context mContext;
    private final CaptureStateHolder mHolder = CaptureStateHolder.getInstance();

    public ServiceCalibrationRecorder(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void start(ExerciseType exercise, DetectorParams trialParams) {
        // Se pasa ya a "cuenta atrás" para que no se vea el panel anterior mientras arranca el servicio
        mHolder.onCountdown(JumpService.CALIBRATION_COUNTDOWN_S);
        ContextCompat.startForegroundService(mContext,
                JumpService.newCalibrationIntent(mContext, exercise, trialParams));
    }

    @Override
    public void stop() {
        mHolder.finish(Long.MAX_VALUE);
        mContext.stopService(new Intent(mContext, JumpService.class));
    }

    @Override
    public void discard() {
        mHolder.reset();
    }

    @Override
    public CaptureState getState() {
        return mHolder.getState();
    }

    @Override
    public SignalRecording getFinishedRecording() {
        return mHolder.getFinishedRecording();
    }

    @Override
    public void setListener(Listener listener) {
        mHolder.setListener(listener);
    }
}
