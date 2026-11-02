# App-specific R8/ProGuard rules.

# Keep Kotlin metadata + nullability annotations (DebugMetadata needed for parity).
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeVisibleTypeAnnotations
-keepattributes Signature, InnerClasses, EnclosingMethod, AnnotationDefault

# Keep data classes used with kotlinx.serialization (plugin-generated code).
-keepattributes *Annotation*
-keepclassmembers class com.browseglass.** {
    @kotlinx.serialization.Serializable *;
}
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Coroutines
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# GeckoView is kept whole (see geckoview-proguard.pro) because R8 cannot
# safely analyze its JNI boundaries; shrink surrounding app only.
-keep class com.browseglass.** { *; }

# Biometric
-keep class androidx.biometric.** { *; }