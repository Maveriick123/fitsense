# 17 - Recursos y constantes

- Todo lo de la GUI está en recursos: textos en `strings.xml` (con formato `%1$d`, sin
  concatenar), tamaños y márgenes en `dimens.xml` y colores en `colors.xml`.
- Claves internas en constantes: nodos y campos de Firebase (`RoutineDto`), nombre del archivo y
  claves de SharedPreferences (`CalibrationRepositoryImp`), nombres de ejercicio en la base de
  datos (`ExerciseDbMapper`), TAG de log.
- Claves externas (las ven otros componentes o el sistema) en constantes precedidas del nombre
  del paquete: acción y extras de `JumpService`, extra de `CalibrationActivity` y el canal de
  notificaciones (`es.udc.psi_2025_26.FitSense.CHANNEL_WORKOUT`), que antes estaba en strings.xml.
- Se eliminan las cadenas que ya no se usan.
