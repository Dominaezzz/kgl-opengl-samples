pluginManagement {
	resolutionStrategy {
		eachPlugin {
			if (requested.id.id == "kotlin-multiplatform") {
				useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
			}
		}
	}
}
rootProject.name = "kgl-opengl-samples"

enableFeaturePreview("GRADLE_METADATA")