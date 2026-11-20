# 18 - Módulos Gradle

Hasta aquí la arquitectura era correcta solo por disciplina: todo estaba en `:app`. Se separan las
capas en tres módulos para que romper la regla de dependencias sea un error de compilación:

- `:domain`: librería Java pura (`java-library`), sin dependencias.
- `:data`: librería Android (`com.android.library`), porque Firebase y SharedPreferences
  necesitan Android. Depende de `:domain` con `api` y de Firebase con `implementation`.
  `RepositoryFactory` es su única entrada pública y devuelve solo los puertos del dominio.
- `:app`: presentación, sensores, `GlobalApplication` y `AppContainer`. Depende de `:data`.

Los paquetes no cambian: los módulos no crean la arquitectura, la protegen.

Diferencia entre `api` e `implementation` en `data/build.gradle.kts`: con `api(:domain)` quien
use `:data` ve también el dominio (los puertos que devuelve `RepositoryFactory`); con
`implementation(firebase)` Firebase queda oculto, así que `:app` no puede importarlo.
