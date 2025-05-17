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

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.darkrockstudios.apps.fasttrack.**$$serializer { *; }
-keepclassmembers class com.darkrockstudios.apps.fasttrack.** {
    *** Companion;
}
-keepclasseswithmembers class com.darkrockstudios.apps.fasttrack.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# KotlinX DateTime
-keep class kotlinx.datetime.** { *; }
-keepclassmembers class kotlinx.datetime.** { *; }
-keep class kotlinx.datetime.Clock { *; }
-keep class kotlinx.datetime.Clock$System { *; }
-keepclassmembers class kotlinx.datetime.Clock$System {
    public static ** INSTANCE;
    public ** now();
}

# Android Preferences
-keep class android.preference.PreferenceManager { *; }
-keepclassmembers class android.preference.PreferenceManager { 
    public static android.content.SharedPreferences getDefaultSharedPreferences(android.content.Context);
}

# Koin
-keepnames class org.koin.** { *; }
-keep class org.koin.** { *; }
-keep class org.koin.core.qualifier.** { *; }
-keep class org.koin.core.annotation.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Markwon (Markdown)
-keep class io.noties.markwon.** { *; }
-keep class org.commonmark.** { *; }

# Satchel (Encrypted Storage)
-keep class com.github.adrielcafe.satchel.** { *; }

# FastAdapter
-keep class com.mikepenz.fastadapter.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

-dontwarn com.caverock.androidsvg.SVG
-dontwarn com.caverock.androidsvg.SVGParseException
-dontwarn kotlin.Experimental$Level
-dontwarn kotlin.Experimental
-dontwarn okhttp3.Call$Factory
-dontwarn okhttp3.Call
-dontwarn okhttp3.OkHttpClient
-dontwarn okhttp3.Request$Builder
-dontwarn okhttp3.Request
-dontwarn okhttp3.Response
-dontwarn okhttp3.ResponseBody
-dontwarn pl.droidsonroids.gif.GifDrawable

# Keep all classes in the app package
-keeppackagenames com.darkrockstudios.apps.fasttrack.**
-keep class com.darkrockstudios.apps.fasttrack.** { *; }
