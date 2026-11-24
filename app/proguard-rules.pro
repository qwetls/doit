# Keep Room entities and DAOs (accessed via generated code and reflection)
-keep class com.xeylabs.doit.data.** { *; }
-keepclassmembers class * {
    @androidx.room.Dao *;
}

# Keep attributes for reflection and serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# R8 DEX compaction and debug log stripping for smaller release APKs
-allowaccessmodification
-repackageclasses 'doit.p'
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
