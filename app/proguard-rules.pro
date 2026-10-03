# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson data models
-keep class now.abfahrt.transit.data.model.** { *; }

# ORS request bodies are Gson-serialized from data.api. Keep field names in release/R8 builds.
-keep class now.abfahrt.transit.data.api.OrsDirectionsRequest { *; }
-keep class now.abfahrt.transit.data.api.OrsMatrixRequest { *; }

# Hilt
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
