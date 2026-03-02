# ProGuard Rules for STAenTurno

# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Soporte\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Support for generic rules
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep all Serializable classes
-keepnames class * implements java.io.Serializable

# Keep all Parcelable classes
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep all enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

############################################
# ===== GSON & MODELS =====
############################################

# Keep model classes (fields included) - CRITICAL for Gson
-keep class com.sta.staenturno.data.model.** { *; }

# Keep Gson annotations
-keepattributes Signature
-keepattributes *Annotation*

# Prevent stripping of fields used via reflection by Gson
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Gson library classes
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

############################################
# ===== RETROFIT =====
############################################

# Keep API interfaces
-keep interface com.sta.staenturno.data.remote.** { *; }

# Keep Retrofit annotations
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

# Retrofit library classes
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes Exceptions

############################################
# ===== OKHTTP =====
############################################

-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.** 
-dontwarn okio.** 

############################################
# ===== JETPACK COMPOSE & OTHERS =====
############################################

-keep class androidx.compose.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.material.** { *; }
-keep class androidx.biometric.** { *; }

# Keep UI States and ViewModels to avoid issues with StateFlow/LiveCycle in Release
-keep class com.sta.staenturno.ui.login.** { *; }
-keep class com.sta.staenturno.ui.home.** { *; }
-keep class com.sta.staenturno.ui.schedule.** { *; }
-keep class com.sta.staenturno.ui.changePassword.** { *; }
