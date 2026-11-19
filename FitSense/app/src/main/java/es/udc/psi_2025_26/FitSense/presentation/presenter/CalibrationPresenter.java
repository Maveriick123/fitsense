package es.udc.psi_2025_26.FitSense.presentation.presenter;

import es.udc.psi_2025_26.FitSense.presentation.common.PresenterHolder;

// Interfaz con los eventos de la pantalla de calibración
// El Presenter sobrevive a la recreación de la Activity (rotación...): la vista se engancha
// con attachView, se suelta con detachView y onFinish se llama cuando la pantalla se cierra del todo
public interface CalibrationPresenter extends PresenterHolder.RetainedPresenter {

    void attachView(CalibrationView view);

    void detachView();

    // Los permisos ya los comprueba la Activity antes
    void onClickStart();

    void onClickStop();

    void onSubmitRealCount(String realCount);

    void onClickDiscard();

    void onClickSave();

    void onClickReset();

    void onResetConfirmed();

    void onBackPressed();

    void onExitConfirmed();

    @Override
    void onFinish();
}
