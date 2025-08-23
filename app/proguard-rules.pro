# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keepattributes SourceFile,LineNumberTable

# Keep Glance widget related classes
-keep public class * extends androidx.glance.appwidget.GlanceAppWidget
-keep public class * extends androidx.glance.appwidget.action.ActionCallback


# Android Preferences
-keep class android.preference.PreferenceManager { *; }
-keepclassmembers class android.preference.PreferenceManager { 
    public static android.content.SharedPreferences getDefaultSharedPreferences(android.content.Context);
}

# Glide
-keep class * extends com.bumptech.glide.module.AppGlideModule
