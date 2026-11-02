# GeckoView ships a mix of Java (GeckoSession etc.) and huge native libs
# (libxul.so). Its Java↔native boundary relies on exact method names, so R8
# must never rename or strip GeckoView classes. We keep GeckoView unshrunk.
-keep class org.mozilla.geckoview.** { *; }
-keep class org.mozilla.gecko.** { *; }
-keep interface org.mozilla.geckoview.** { *; }

# MozillaGeckoResult is used from native back into Java.
-keep class org.mozilla.geckoview.GeckoResult { *; }

# JNI entrypoints (declared in Java, called from C++).
-keepclasseswithmembernames class * {
    native <methods>;
}

# GeckoView loads *all* its classes via Class.forName-like reflection from the
# profile engine; never mark them dead.
-dontwarn org.mozilla.geckoview.**
-dontwarn org.mozilla.gecko.**

# Keep annotations consumed at runtime.
-keepattributes *Annotation*, RuntimeVisible*Annotations

# Web extension messaging classes.
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**