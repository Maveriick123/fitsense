package es.udc.psi_2025_26.FitSense.presentation.common;

import androidx.lifecycle.ViewModel;

// Clase para guardar un Presenter mientras exista la pantalla, aunque la Activity se recree
// (rotación, cambio de tema...). Cuando la pantalla se cierra del todo avisa al Presenter
public class PresenterHolder extends ViewModel {

    // Interfaz para los Presenters que tienen que saber cuándo se cierra su pantalla del todo
    public interface RetainedPresenter {
        void onFinish();
    }

    private RetainedPresenter mPresenter;

    @SuppressWarnings("unchecked")
    public <P extends RetainedPresenter> P get() {
        return (P) mPresenter;
    }

    public void set(RetainedPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    protected void onCleared() {
        if (mPresenter != null) {
            mPresenter.onFinish();
        }
        super.onCleared();
    }
}
