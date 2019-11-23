import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version("1.3.60")
}

repositories {
    maven("https://dl.bintray.com/dominaezzz/kotlin-native")

    jcenter()
    mavenCentral()
}

kotlin {
    val os = OperatingSystem.current()

    if (os.isWindows) mingwX64()
    if (os.isLinux) linuxX64()
    if (os.isMacOsX) macosX64()

    val samples = listOf("triangle")

    targets.withType<KotlinNativeTarget> {
        binaries {
            for (sample in samples) {
                executable(sample) {
                    entryPoint = "$sample.main"
                }
            }
        }
        compilations {
            "main" {
                defaultSourceSet {
                    kotlin.srcDir("src/main/kotlin")
                    resources.srcDir("src/main/resources")
                }
                dependencies {
                    implementation("com.kgl:kgl-glfw:0.1.8-dev-9")
                    implementation("com.kgl:kgl-opengl:0.1.8-dev-9")
                }
            }
            "test" {
                defaultSourceSet {
                    kotlin.srcDir("src/test/kotlin")
                    resources.srcDir("src/test/resources")
                }
            }
        }
    }

    sourceSets {
        // Note: To enable common source sets please comment out "kotlin.import.noCommonSourceSets" property
        // in gradle.properties file and re-import your project in IDE.
    }

    sourceSets.all {
        languageSettings.apply {
            enableLanguageFeature("InlineClasses")
            useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
        }
    }
}
