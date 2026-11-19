package es.udc.psi_2025_26.FitSense.presentation.common;

import java.util.concurrent.Executor;

// Clase para ejecutar una tarea en segundo plano y devolver el resultado en el hilo principal
// Sustituye a la AsyncTask (obsoleta desde Android 11). Los dos Executor llegan por el
// constructor, así en los tests se puede usar uno síncrono (Runnable::run) para los dos
public class TaskRunner {

    public interface Task<R> {
        R call() throws Exception;
    }

    public interface Callback<R> {
        void onSuccess(R result);

        void onError(Exception e);
    }

    private final Executor mBackgroundExecutor;
    private final Executor mMainExecutor;

    public TaskRunner(Executor backgroundExecutor, Executor mainExecutor) {
        mBackgroundExecutor = backgroundExecutor;
        mMainExecutor = mainExecutor;
    }

    public <R> void execute(Task<R> task, Callback<R> callback) {
        mBackgroundExecutor.execute(() -> {
            try {
                R result = task.call();
                mMainExecutor.execute(() -> callback.onSuccess(result));
            } catch (Exception e) {
                mMainExecutor.execute(() -> callback.onError(e));
            }
        });
    }
}
