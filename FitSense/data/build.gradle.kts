// Capa de datos: librería Android (Firebase y SharedPreferences necesitan Android).
// Firebase es "implementation": :app no lo ve y solo accede a los puertos del dominio.
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "es.udc.psi_2025_26.FitSense.data"
    compileSdk {
        version = release(37)
    }
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(project(":domain"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
}
