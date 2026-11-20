# 15 - Entrenamiento con MVP y sensores desacoplados

## Sensores (`sensor`)
- `JumpService` y el estado de la captura pasan al paquete `sensor`.
- El servicio ya no lee la calibración: el dominio (`StartExerciseUseCase`) decide los
  parámetros y se los pasa en el Intent con los métodos de factoría `newWorkoutIntent(...)` y
  `newCalibrationIntent(...)`.
- `ServiceWorkoutTracker` implementa el puerto `WorkoutTracker`: arranca/para el servicio y
  traduce el Broadcast de los contadores a un listener. Usa Android, así que vive en `:app`.
- Las claves externas (acción y extras del Intent) llevan el nombre del paquete.

## Presentación
- `MainActivity` pasa a ser `presentation.ui.WorkoutActivity` (layout `activity_workout.xml`).
- `WorkoutView` / `WorkoutPresenter`, con `CounterUiModel`, `CalibrationItemUiModel` y su mapper.
- Reglas en el dominio: guardar sin sesión (`SessionRequiredException`) y calibrar con una
  rutina en curso (`WorkoutInProgressException`).
- `PermissionHelper` pasa a `presentation.common`. Se elimina `DatabaseManager`.
