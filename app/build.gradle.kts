plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.ksp)
	alias(libs.plugins.kotlin.compose)
}

android {
	namespace = "com.darkrockstudios.apps.fasttrack"
	compileSdk = libs.versions.compileSdk.get().toInt()

	defaultConfig {
		applicationId = "com.darkrockstudios.apps.fasttrack"
		minSdk = libs.versions.minSdk.get().toInt()
		targetSdk = libs.versions.targetSdk.get().toInt()
		versionCode = libs.versions.versionCode.get().toInt()
		versionName = libs.versions.versionName.get()

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
	signingConfigs {
		create("release") {
			storeFile = file(System.getenv("KEYSTORE_FILE") ?: "keystore.jks")
			storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
			keyAlias = System.getenv("KEY_ALIAS") ?: ""
			keyPassword = System.getenv("KEY_PASSWORD") ?: ""
		}
	}
	buildFeatures {
		buildConfig = true
		compose = true
	}

	composeOptions {
		kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
	}
	buildTypes {
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
			signingConfig = signingConfigs.getByName("release")
		}
		debug {
			applicationIdSuffix = ".dev"
			resValue("string", "app_name", "FastTrackDev")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
		targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
	}
	kotlinOptions {
		jvmTarget = libs.versions.javaVersion.get()
		freeCompilerArgs = listOf(
			"-opt-in=kotlin.time.ExperimentalTime",
			"-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
		)
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	dependenciesInfo {
		includeInApk = false
		includeInBundle = false
	}
}

dependencies {
	implementation(libs.kotlinx.datetime)
	implementation(libs.kotlinx.serialization.core)
	implementation(libs.core.ktx)
	implementation(libs.activity.ktx)
	implementation(libs.collection.ktx)
	implementation(libs.fragment.ktx)
	implementation(libs.recyclerview)

	implementation(libs.appcompat)
	implementation(libs.constraintlayout)

	implementation(libs.navigation.runtime.ktx)
	implementation(libs.androidx.navigation.fragment.ktx)
	implementation(libs.androidx.navigation.ui.ktx)

	implementation(libs.androidx.datastore.preferences)

	implementation(libs.androidx.legacy.support.v4)
	implementation(libs.androidx.lifecycle.extensions)
	implementation(libs.androidx.lifecycle.viewmodel.ktx)
	implementation(libs.androidx.lifecycle.common.java8)

	implementation(libs.kotlinx.coroutines.android)

	implementation(libs.androidx.room.runtime)
	implementation(libs.androidx.cardview)
	implementation(libs.androidx.lifecycle.livedata.ktx)
	implementation(libs.androidx.material3.android)
	implementation(libs.androidx.material3.window.sizeclass)
	implementation(libs.androidx.material3.adaptive.navigation.suite)
	implementation(libs.androidx.adaptive.navigation)

	ksp(libs.androidx.room.compiler)
	implementation(libs.androidx.room.ktx)

	implementation(libs.napier)

	implementation(libs.fastadapter)
	implementation(libs.fastadapter.extensions.binding)

	implementation(libs.glide)
	ksp(libs.compiler)

	implementation(project.dependencies.platform(libs.koin.bom))
	implementation(libs.koin.core)
	implementation(libs.koin.android)
	implementation(libs.koin.core.coroutines)
	implementation(libs.koin.androidx.compose)
	implementation(libs.koin.androidx.workmanager)

	implementation(libs.customdatetimepicker)

	implementation(libs.satchel.core)
	implementation(libs.satchel.storer.encrypted.file)
	implementation(libs.satchel.serializer.base64.android)

	implementation(libs.compose.markdown)

	implementation(libs.materialabout)

	implementation(libs.core)
	implementation(libs.ext.latex)
	implementation(libs.ext.strikethrough)
	implementation(libs.ext.tables)
	implementation(libs.image)
	implementation(libs.image.glide)
	implementation(libs.linkify)

	implementation(libs.compose.ui)
	implementation(libs.compose.ui.tooling.preview)
	implementation(libs.compose.foundation)
	implementation(libs.activity.compose)
	implementation(libs.compose.material.icons.extended)
	debugImplementation(libs.compose.ui.tooling)

	implementation(libs.accompanist.pager)
	implementation(libs.accompanist.pager.indicators)

	implementation(libs.glance)
	implementation(libs.glance.appwidget)
	implementation(libs.glance.material)
	debugImplementation(libs.glance.preview)
	debugImplementation(libs.glance.appwidget.preview)

	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)

	androidTestImplementation(libs.androidx.compose.ui.test)
	debugImplementation(libs.androidx.compose.ui.test.manifest)
}
