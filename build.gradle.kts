import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version("1.3.21")
}
repositories {
    maven(url = "https://dl.bintray.com/dominaezzz/kotlin-native")
    
    jcenter()
    mavenCentral()
}
kotlin {
    val os = org.gradle.internal.os.OperatingSystem.current()
    // For ARM, should be changed to iosArm32 or iosArm64

    if (os.isWindows) mingwX64("mingw")
    if (os.isLinux) linuxX64("linux")
    if (os.isMacOsX) macosX64("macos")

    val samples = listOf("triangle")

    targets.filterIsInstance<KotlinNativeTarget>().forEach { target ->
        target.binaries {
            for (sample in samples) {
                executable(sample) {
                    entryPoint = "$sample.main"
                }
            }
        }
        val main by target.compilations.getting {
            defaultSourceSet {
                kotlin.srcDir("src/nativeMain/kotlin")
                resources.srcDir("src/nativeMain/resources")

                dependencies {
                    implementation("com.kgl:kgl-glfw:0.1.5-dev-1")
                    implementation("com.kgl:kgl-opengl:0.1.5-dev-1")
                }
            }
        }
        val test by target.compilations.getting {
            defaultSourceSet {
                kotlin.srcDir("src/nativeTest/kotlin")
                resources.srcDir("src/nativeTest/resources")
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
