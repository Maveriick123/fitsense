# 14 - Historial de rutinas con MVP

- `RoutineUiModel` + `RoutineUiModelMapper`: la fila ya preparada para pintarse (nombre del
  ejercicio como recurso de texto y número de repeticiones).
- `RoutinesView` / `RoutinesPresenter`: el Presenter recibe el historial y cierra sesión con
  `SignOutUseCase`. Desde la feature 19 el historial llega en tiempo real (ver docs/19).
- `RoutinesActivity` y `RoutineAdapter` pasan a `presentation.ui`. El adaptador usa ViewBinding
  y un método `bind(RoutineUiModel)` en el ViewHolder. Se elimina `RoutineModel`.
- La Activity ya no usa Firebase directamente ni pasa el `USER_ID` en el Intent: el usuario
  actual lo conoce el dominio a través del puerto `AuthRepository`.
- Textos: "Repeticiones: %1$d" con formato, y mensaje de error en recursos.
