package es.udc.psi_2025_26.FitSense;

import android.app.Application;

// Android crea la GlobalApplication una sola vez, antes que cualquier Activity o Service
// Aquí se guarda el AppContainer con las dependencias de toda la app
public class GlobalApplication extends Application {

    private AppContainer mContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        mContainer = new AppContainer(this);
    }

    public AppContainer getContainer() {
        return mContainer;
    }
}
