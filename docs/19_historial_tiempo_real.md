# 19 - Historial en tiempo real

En las features 12 y 14 el historial pasó a consultarse una sola vez (`Tasks.await` sobre
`get()`), filtrado por el usuario actual y recargado al volver a la pantalla. Eso cambiaba el
comportamiento del monolito, que muestra las rutinas de **todos** los usuarios y las actualiza
**en tiempo real**. Se recupera ese comportamiento sin salir de la arquitectura:

- Puerto `RoutineRepository`: `setListener(Listener)` sustituye a `getRoutines(userId)`. El
  `Listener` recibe cada rutina (`onRoutineAdded`) y los errores (`onError`), como los puertos
  `WorkoutTracker` y `CalibrationRecorder`.
- Caso de uso `WatchRoutineHistoryUseCase` (sustituye a `GetRoutineHistoryUseCase`).
- `RoutineRepositoryImp` escucha el nodo `ROUTINES` con un `ChildEventListener`: Firebase
  entrega primero las rutinas existentes y después cada nueva, en el hilo principal. Al soltar el
  listener se quita de Firebase (el monolito nunca lo quitaba).
- Las rutinas con un ejercicio desconocido (por ejemplo las de las primeras versiones de prueba)
  ya no se descartan: se muestran con el nombre con el que se guardaron, como antes.
- `RoutinesPresenterImp` empieza a escuchar al crearse la pantalla y deja de hacerlo al
  destruirse; `RoutineAdapter` añade cada rutina al final de la lista.
