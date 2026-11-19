# 14 - Historial de rutinas con MVP

- `RoutineUiModel` + `RoutineUiModelMapper`: la fila ya preparada para pintarse (nombre del
  ejercicio como recurso de texto y número de repeticiones).
- `RoutinesView` define los estados de la pantalla: cargando, lista, sin rutinas y error.
- `RoutinesPresenterImp` carga el historial con `GetRoutineHistoryUseCase` (TaskRunner) cada vez
  que la pantalla se hace visible, y cierra sesión con `SignOutUseCase`.
- `RoutinesActivity` y `RoutineAdapter` pasan a `presentation.ui`. El adaptador usa ViewBinding
  y un método `bind(RoutineUiModel)` en el ViewHolder. Se elimina `RoutineModel`.
- La Activity ya no usa Firebase directamente ni pasa el `USER_ID` en el Intent: el usuario
  actual lo conoce el dominio a través del puerto `AuthRepository`.
- Textos: "Repeticiones: %1$d" con formato, y mensajes de lista vacía y de error en recursos.
