# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keepattributes SourceFile,LineNumberTable

# Keep Glance widget related classes
-keepclassmembers class * extends androidx.glance.** {
  <fields>;
}
-keep public class * extends androidx.glance.appwidget.action.ActionCallback

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.DefaultExecutor { *; }
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }

# Keep all classes in the app package
-keeppackagenames com.darkrockstudios.apps.fasttrack.**
-keep class com.darkrockstudios.apps.fasttrack.** { *; }
