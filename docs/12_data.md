# 12 - Capa de datos y composición (pasos 6, 7 y 8)

## Fuentes de datos
- Usuarios: Firebase Authentication (email y contraseña).
- Historial: Firebase Realtime Database, nodo `ROUTINES` con `USER_ID`, `EXERCISE_CAMBER` y
  `REPS_CAMBER` (se conservan los nombres para no romper los datos ya guardados).
- Calibración: SharedPreferences `fitsense_calibration`, local y excluida de las copias de seguridad.

## Implementaciones (`data`)
- `repository.AuthRepositoryImp`, `repository.RoutineRepositoryImp` y
  `repository.CalibrationRepositoryImp` implementan los puertos del dominio.
- Firebase es asíncrono: los repositorios esperan con `Tasks.await` (con timeout) y se llaman
  fuera del hilo principal. Los errores se traducen a `AuthException` y `DataUnavailableException`.
- Traducción en la frontera: JSON -> `dto.RoutineDto` -> `Routine`, con `mapper.RoutineDtoMapper`
  y `mapper.ExerciseDbMapper` (nombre del ejercicio en la base de datos).
- Mejora: el historial se consulta filtrando por usuario (`orderByChild(USER_ID).equalTo(uid)`);
  antes se descargaban y mostraban las rutinas de todos los usuarios.

## Composición
`GlobalApplication` + `AppContainer`: el único sitio que crea las implementaciones (composition
root). Las Activities piden ahí sus casos de uso.
