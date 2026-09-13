import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// La firma de release vive fuera del repo: keystore.properties (ver README).
// Si el archivo no esta, el release se arma sin firmar y sirve igual para probar.
val propiedadesFirma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
val hayFirmaDeRelease = propiedadesFirma.getProperty("storeFile") != null

android {
    namespace = "uy.horacio.recetariocriollo"
    compileSdk = 37

    defaultConfig {
        applicationId = "uy.horacio.recetariocriollo"
        minSdk = 26 // Android 8.0
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hayFirmaDeRelease) {
            create("release") {
                storeFile = rootProject.file(propiedadesFirma.getProperty("storeFile"))
                storePassword = propiedadesFirma.getProperty("storePassword")
                keyAlias = propiedadesFirma.getProperty("keyAlias")
                keyPassword = propiedadesFirma.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hayFirmaDeRelease) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            // Para poder tener instaladas la de desarrollo y la de Play a la vez.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // Los esquemas exportados viajan al APK de test para probar las migraciones.
    sourceSets {
        getByName("androidTest").assets.directories.add("$projectDir/schemas")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

// Los esquemas de Room quedan versionados para poder escribir migraciones.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Base de datos local
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Cronometros persistidos como JSON en SharedPreferences
    implementation(libs.kotlinx.serialization.json)

    // Compose UI trae graphics-path 1.0.1, cuya .so no está alineada a páginas de 16 KB (#54).
    constraints {
        implementation(libs.androidx.graphics.path) { because("1.1.0 sale alineada a 16 KB") }
    }

    // Ajustes del usuario (tema, modo cocina, unidades), sin red
    implementation(libs.androidx.datastore.preferences)

    // Fotos de recetas guardadas en el almacenamiento interno
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
