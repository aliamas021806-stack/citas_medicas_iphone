# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson (modelos)
-keep class com.tuempresa.citasmedicas.model.** { *; }
-keep class com.tuempresa.citasmedicas.data.remote.** { *; }
