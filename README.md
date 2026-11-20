# Q1_1
Q1.1

# FitSense - Memoria y Proyecto Práctico (Hito 1)

Aplicación móvil nativa para Android, desarrollada en Java, que **cuenta automáticamente repeticiones de ejercicios físicos** (saltos a la comba, flexiones, pasos y dominadas) usando el acelerómetro del dispositivo en segundo plano. Incluye un sistema de **autocalibración** que adapta la detección a cada usuario y a cada móvil. Usa Firebase para la autenticación y para guardar las rutinas en la nube.

Esta rama contiene el código refactorizado y ampliado:
- Internacionalización de cadenas (`strings.xml`).
- Inyección de contexto en los componentes.
- Comunicación asíncrona corregida entre el servicio de sensores y la interfaz (`BroadcastReceiver`).
- Soporte para **cuatro actividades físicas**: saltos, flexiones, pasos y dominadas.
- **Autocalibración** individual de cada ejercicio, propia de cada usuario.

## 👥 Autores / Grupo

- **Pablo Pérez Candal**: sensores inerciales, Foreground Service, Broadcasts UI, algoritmo de detección y autocalibración.
- **Daniel Quinteiro Graña**: configuración de Firebase Auth, gestión de sesiones y login.
- **Gabriele Masciandaro**: base de datos Realtime Database, RecyclerView e historial de rutinas.

## ✨ Funcionalidades

| Funcionalidad | Descripción |
|---|---|
| Autenticación | Registro e inicio de sesión con Firebase Authentication. |
| Contador de saltos | Detecta los impactos de cada salto a la comba. |
| Contador de flexiones | Detecta el movimiento de subida y bajada del cuerpo. |
| Contador de pasos | Detecta cada pisada al caminar. |
| Contador de dominadas | Detecta cada repetición en la barra. |
| Funcionamiento en segundo plano | Sigue contando con la pantalla bloqueada o la app minimizada. |
| Autocalibración | Ajusta la detección de cada ejercicio a partir de las repeticiones reales del usuario. Cada usuario tiene su propia calibración. |
| Historial en la nube | Las rutinas se guardan en Firebase Realtime Database y se listan en un RecyclerView. |

## 🏗️ Arquitectura de la aplicación (Clean Architecture)

La aplicación ha pasado de un monolito (Activities que hablaban directamente con Firebase,
SharedPreferences y el servicio de sensores) a una **arquitectura Clean en capas**, siguiendo la
metodología de la asignatura (diseño top-down e implementación por rebanadas verticales con
GitFlow). Cada paso está explicado en [`docs/`](docs).

```
            ┌──────────────────────────── :app (Android) ────────────────────────────┐
            │  presentation (MVP): ui · presenter · uimodel · common (TaskRunner)     │
            │  sensor: JumpService · ServiceWorkoutTracker · ServiceCalibrationRecorder│
            │  GlobalApplication + AppContainer (composition root)                    │
            │      ┌──────────────── :data (Android library) ────────────────┐        │
            │      │  repository (Firebase, SharedPreferences) · dto · mapper │        │
            │      │  RepositoryFactory (única entrada pública)               │        │
            │      │      ┌──────────── :domain (Java puro) ────────────┐     │        │
            │      │      │ model · usecase · repository (puertos)      │     │        │
            │      │      │ exception · detection (algoritmo)           │     │        │
            │      │      └─────────────────────────────────────────────┘     │        │
            │      └──────────────────────────────────────────────────────────┘        │
            └──────────────────────────────────────────────────────────────────────────┘
```

Regla de dependencias: `:app` → `:data` → `:domain`. Al estar en módulos Gradle distintos, el
compilador impide saltársela (no se puede importar Android en el dominio ni Firebase en `:app`).

- **Dominio (`:domain`, Java puro):** entidades (`ExerciseType`, `Routine`, `Calibration`...),
  casos de uso (una clase por acción: `SignInUseCase`, `SaveWorkoutUseCase`,
  `AdjustCalibrationUseCase`...), puertos (`AuthRepository`, `RoutineRepository`,
  `CalibrationRepository`, `WorkoutTracker`, `CalibrationRecorder`) y excepciones del dominio.
  Incluye el algoritmo de detección y el motor de autocalibración.
- **Datos (`:data`):** implementaciones de los repositorios con **Firebase Authentication**,
  **Firebase Realtime Database** (historial en tiempo real) y **SharedPreferences** (calibración por
  usuario y dispositivo, excluida de las copias de seguridad). Traducen JSON → DTO → entidad y los
  errores a excepciones del dominio.
- **Presentación (`:app`, MVP):** cada pantalla tiene su contrato `View` / `Presenter`; las
  Activities son pasivas (pintan y reenvían eventos). Los Presenters llaman a los casos de uso
  fuera del hilo principal con `TaskRunner` y preparan los datos en `UiModel`s. El Presenter de la
  calibración se conserva en un `PresenterHolder` (ViewModel) para sobrevivir a las rotaciones.
- **Sensores (`:app`, paquete `sensor`):** `JumpService` es el único componente que lee el
  acelerómetro (`TYPE_LINEAR_ACCELERATION`, ~50 Hz) y funciona con la pantalla bloqueada. Recibe
  los parámetros del detector en el Intent; `ServiceWorkoutTracker` y `ServiceCalibrationRecorder`
  implementan los puertos del dominio sobre él.

### Estructura del código

```
FitSense
├── domain/  es.udc.psi_2025_26.FitSense.domain
│   ├── model/        ExerciseType, Routine, WorkoutCounts, Calibration, CaptureState...
│   ├── usecase/      Un caso de uso por acción del usuario
│   ├── repository/   Puertos (interfaces) que implementan :data y :app
│   ├── exception/    AuthException, DataUnavailableException...
│   └── detection/    RepetitionDetector, ExponentialSmoother, QuietDetector,
│                     SignalRecording, CalibrationEngine
├── data/    es.udc.psi_2025_26.FitSense.data
│   ├── RepositoryFactory
│   ├── repository/   AuthRepositoryImp, RoutineRepositoryImp, CalibrationRepositoryImp
│   ├── dto/          RoutineDto
│   └── mapper/       RoutineDtoMapper, ExerciseDbMapper
└── app/     es.udc.psi_2025_26.FitSense
    ├── GlobalApplication, AppContainer
    ├── presentation/
    │   ├── ui/        LoginActivity, RoutinesActivity, WorkoutActivity, CalibrationActivity
    │   ├── presenter/ Contratos View/Presenter y sus implementaciones
    │   ├── uimodel/   UiModels y mappers
    │   └── common/    TaskRunner, PresenterHolder, PermissionHelper
    └── sensor/        JumpService, ServiceWorkoutTracker, ServiceCalibrationRecorder
```

## 📈 Algoritmo de detección

Cada muestra del acelerómetro pasa por tres etapas:

1. **Magnitud de la aceleración lineal:** no depende de la orientación del móvil. Si el dispositivo no tiene el
sensor de aceleración lineal, se usa el acea gravedad.
2. **Filtro paso bajo:** suaviza la señal para eliminar temblores y vibraciones. Tiene en cuenta el tiempo real entre
muestras, así que funciona igual aunque cadun ritmo distinto.
3. **Disparador con histéresis y tiempo mínimo entre repeticiones:**
   - Cuenta una repetición cuando la señal
   - No vuelve a contar hasta que la señal baja del 60 % de ese umbral, para que un mismo pico no cuente varias
veces.
   - Exige un tiempo mínimo entre repeticiones, para que los dos picos de una misma flexión o dominada cuenten como
una sola.

Cada ejercicio tiene sus propios valores poimo y suavizado), definidos en
`ExerciseType`.

## 🎯 Autocalibración

Cada persona se mueve de forma distinta y cada móvil tiene un sensor diferente, así que unos valores fijos no sirven
para todos. La autocalibración ajusta cada

**Cómo se usa:**

1. Pulsa **Autocalibrar** y elige el ejercio.
2. Pulsa **Empezar**. Tienes 5 segundos para guardar el móvil y un pitido marca el inicio.
3. Haz las repeticiones contándolas tú (se recomiendan entre 10 y 20).
4. Al terminar, quédate quieto 4 segundos. Un segundo pitido indica que la grabación ha terminado y ya puedes sacar
el móvil.
5. La app muestra cuántas repeticiones ha detectado y tú introduces cuántas hiciste realmente.
6. Pulsa **Ajustar**: la app calcula los nuevos valores.
7. Pulsa **Probar de nuevo** para comprobarlos, o **Guardar** si ya cuenta bien.

**Cómo funciona por dentro:**

- La señal del sensor se graba **sin procesar**. Con el número real de repeticiones, el motor vuelve a pasar la
grabación por el detector con unas 1.300 coo mínimo, y se queda con la mejor. El
detector siempre da el mismo resultado con los mismos datos, así que lo que acierta sobre la grabación también
acierta en vivo.
- **Usa todos los intentos de la sesión** a la vez, no solo el último, para no ajustarse a una única serie.
- Entre las combinaciones que aciertan, eli del rango válido**, porque es la que mejor
tolera series más flojas o más intensas. La app muestra si el margen es amplio, moderado o estrecho.
- La calibración se hace en el **mismo servnes** que el entrenamiento real (pantalla
bloqueada, móvil en el bolsillo).
- La grabación se recorta automáticamente a el movimiento de sacar el móvil no estropee
los datos.
- Los valores ajustados **no se aplican hasar**, y siempre se pueden restaurar los
valores por defecto.

**Dónde se guarda:**

- **Por usuario:** la forma de moverse es personal, así que cada cuenta tiene su propia calibración. Si varias
personas comparten el móvil, cada una usa la suya al iniciar sesión.
- **Por dispositivo:** se guarda en el móvil y no en la nube. Los valores calibrados dependen del sensor de cada
teléfono: cada fabricante calcula y filtra a distinta, y la frecuencia de muestreo real
también varía. Por eso no se pueden pasar de un móvil a otro.
- Por el mismo motivo, el archivo de calibrs de seguridad de Android**
(`backup_rules.xml` y `data_extraction_rules.xml`), para que no se restaure en un móvil nuevo con un sensor distinto.
Al cambiar de móvil hay que volver a calibrar.

## 🔐 Permisos

| Permiso | Motivo |
|---|---|
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICn segundo plano. |
| `ACTIVITY_RECOGNITION` | Obligatorio en Android 14+ para los servicios en primer plano de tipo *health*. |
| `POST_NOTIFICATIONS` | Mostrar la notifico (Android 13+). |

## 🚀 Compilación y ejecución

1. Clonar el repositorio y cambiar a la rama `calibracion_por_usuario`
2. Abrir la carpeta `FitSense/` con Android Studio.
3. Comprobar que existe el archivo `app/google-services.json` del proyecto de Firebase.
4. Ejecutar en un dispositivo físico. Un emulador no tiene acelerómetro real.

**Requisitos:** Android 8.0 (API 26) o superior.
