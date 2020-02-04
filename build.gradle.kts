import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version("1.3.61")
}

repositories {
    maven("https://dl.bintray.com/dominaezzz/kotlin-native")

    jcenter()
    mavenCentral()
}

val kglVersion = "0.1.9-dev-6"

kotlin {
    val os = OperatingSystem.current()

    if (os.isWindows) mingwX64()
    if (os.isLinux) linuxX64()
    if (os.isMacOsX) macosX64()

    val samples = listOf("triangle", "texture")

    targets.withType<KotlinNativeTarget> {
        binaries {
            for (sample in samples) {
                executable(sample) {
                    entryPoint = "$sample.main"
                    runTask!!.workingDir("src/main/resources")
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
                    implementation("com.kgl:kgl-glfw:$kglVersion")
                    implementation("com.kgl:kgl-glfw-static:$kglVersion")
                    implementation("com.kgl:kgl-opengl:$kglVersion")
                    implementation("com.kgl:kgl-stb:$kglVersion")
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
