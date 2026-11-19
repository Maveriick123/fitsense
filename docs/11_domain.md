# 11 - Dominio (pasos 2, 3 y 4 del enfoque top-down)

Se diseña de dentro afuera: primero el dominio, que no depende de nada (Java puro, sin Android).

## Modelo (`domain.model`)
- `ExerciseType`: catálogo de ejercicios con sus valores por defecto y límites de calibración.
  Ya no conoce `R.string` (pasan a `presentation.uimodel.ExerciseUiModel`) ni el nombre de la BD.
- `Routine`, `WorkoutCounts`, `Calibration`, `CaptureState`/`CapturePhase`, `CalibrationSession`.
- `domain.detection`: algoritmo de detección y motor de autocalibración (ya era Java puro).

## Excepciones (`domain.exception`)
`AuthException`, `DataUnavailableException`, `SessionRequiredException`, `WorkoutInProgressException`.

## Puertos (`domain.repository`)
Lo que el dominio necesita del exterior y no puede hacer él. Hablan del negocio, no de Firebase:
`AuthRepository`, `RoutineRepository`, `CalibrationRepository`, `WorkoutTracker` (contar en
segundo plano) y `CalibrationRecorder` (grabar un intento de calibración).

## Casos de uso (`domain.usecase`)
Una clase por acción del usuario, con las reglas de la aplicación:
- Sesión: `SignInUseCase`, `RegisterUseCase`, `SignOutUseCase`.
- Historial: `GetRoutineHistoryUseCase`.
- Entrenamiento: `StartExerciseUseCase`, `WatchWorkoutUseCase`, `SaveWorkoutUseCase`.
- Calibración: `GetCalibrationUseCase`, `ListCalibrationsUseCase`, `SaveCalibrationUseCase`,
  `ResetCalibrationUseCase`, `StartCalibrationCaptureUseCase`, `StopCalibrationCaptureUseCase`,
  `DiscardCalibrationCaptureUseCase`, `WatchCalibrationCaptureUseCase`,
  `CountRecordedRepetitionsUseCase`, `LabelAttemptUseCase`, `AdjustCalibrationUseCase`.
