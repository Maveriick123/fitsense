# 13 - Login con MVP (paso 5: presentación)

- `presentation.common.TaskRunner`: ejecuta el caso de uso en otro hilo y entrega el resultado en
  el principal. Recibe los `Executor` por constructor para poder probarlo sin emulador.
- Contratos `LoginView` (lo que sabe hacer la pantalla) y `LoginPresenter` (los eventos).
- `LoginPresenterImp` llama a `SignInUseCase` / `RegisterUseCase` a través del TaskRunner y
  aplica la regla de no llamar a Firebase con los campos vacíos.
- `LoginActivity` pasa a `presentation.ui` y es pasiva: pinta y reenvía eventos. Ya no usa
  `AuthManager`.
- Mientras se espera a Firebase se muestra un `ProgressBar` y se desactivan los botones.
- Los mensajes de error usan cadenas con formato (`%1$s`) en vez de concatenar textos.
