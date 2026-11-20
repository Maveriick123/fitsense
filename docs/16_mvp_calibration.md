# 16 - Calibración con MVP

La pantalla de calibración usaba MVVM (`CalibrationViewModel` + `LiveData`) y hablaba
directamente con el servicio, con `CalibrationStore` y con `AuthManager`.

- `CalibrationPresenterImp` contiene la lógica de la sesión y solo usa casos de uso
  (`StartCalibrationCaptureUseCase`, `LabelAttemptUseCase`, `AdjustCalibrationUseCase`...).
  El ajuste, que es costoso, se calcula fuera del hilo principal con el TaskRunner.
- La interfaz es una función del estado (fase de captura + resultado): `render()` decide qué
  panel se muestra. `CalibrationParamsUiModel` y `CalibrationResultUiModel` llevan los datos ya
  preparados (origen de los parámetros, margen de robustez...).
- Rotación: el Presenter se guarda en un `PresenterHolder` (un `ViewModel` de Jetpack que solo lo
  conserva). La Activity se engancha con `attachView` y se suelta con `detachView`; al cerrar la
  pantalla definitivamente `onFinish()` descarta la captura en curso.
- Sensores: `CaptureStateHolder` sustituye al `CalibrationRepository` con LiveData (ahora con un
  listener del puerto) y `ServiceCalibrationRecorder` implementa el puerto `CalibrationRecorder`.
- `CalibrationActivity` pasa a `presentation.ui` con un `newIntent(context, tipo)`.
- Se eliminan `CalibrationViewModel`, `CalibrationStore` y `AuthManager`.
