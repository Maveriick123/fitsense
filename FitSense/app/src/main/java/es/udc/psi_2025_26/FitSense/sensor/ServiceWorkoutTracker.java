package es.udc.psi_2025_26.FitSense.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.core.content.ContextCompat;

import es.udc.psi_2025_26.FitSense.domain.detection.DetectorParams;
import es.udc.psi_2025_26.FitSense.domain.model.ExerciseType;
import es.udc.psi_2025_26.FitSense.domain.model.WorkoutCounts;
import es.udc.psi_2025_26.FitSense.domain.repository.WorkoutTracker;

// Implementación del puerto WorkoutTracker con el servicio en primer plano JumpService
// Usa Android, por eso está en :app
// Los cambios de los contadores llegan por Broadcast y se le pasan al listener en el hilo principal
public class ServiceWorkoutTracker implements WorkoutTracker {

    private final Context mContext;
    private Listener mListener;
    private boolean mReceiverRegistered = false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mListener != null && JumpService.ACTION_UPDATE_COUNT.equals(intent.getAction())) {
                mListener.onCountsChanged(
                        new WorkoutCounts(intent.getIntArrayExtra(JumpService.EXTRA_COUNTS)));
            }
        }
    };

    public ServiceWorkoutTracker(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void start(ExerciseType exercise, DetectorParams params) {
        ContextCompat.startForegroundService(mContext,
                JumpService.newWorkoutIntent(mContext, exercise, params));
    }

    @Override
    public void stop() {
        mContext.stopService(new Intent(mContext, JumpService.class));
    }

    @Override
    public boolean isRunning() {
        return JumpService.isWorkoutRunning();
    }

    @Override
    public WorkoutCounts getCounts() {
        return new WorkoutCounts(JumpService.getCounts());
    }

    @Override
    public void resetCounts() {
        JumpService.resetCounts();
    }

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
        if (listener != null && !mReceiverRegistered) {
            ContextCompat.registerReceiver(mContext, mReceiver,
                    new IntentFilter(JumpService.ACTION_UPDATE_COUNT), ContextCompat.RECEIVER_NOT_EXPORTED);
            mReceiverRegistered = true;
        } else if (listener == null && mReceiverRegistered) {
            mContext.unregisterReceiver(mReceiver);
            mReceiverRegistered = false;
        }
    }
}
