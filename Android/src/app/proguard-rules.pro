# App-specific R8/ProGuard rules.

# Keep the entire app namespace unobfuscated: model allowlist and settings parsing use Gson
# reflection, and the LiteRT-LM runtime calls back into app classes via JNI. Library code is
# still shrunk and obfuscated per each library's consumer rules, which is where most of the
# unshrunk dex size comes from.
-keep class com.google.ai.edge.gallery.** { *; }
-keep class com.google.ai.edge.litertlm.** { *; }

# Gson uses generic type information and TypeToken subclasses at runtime.
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Shrink only, don't rename anything: obfuscation is what typically breaks reflection-based
# code at runtime, while dead-code removal provides nearly all of the size win.
-dontobfuscate

# Protobuf lite does not ship consumer rules and relies on reflection at runtime (used by the
# settings/chat-history DataStore at app startup).
-keep class com.google.protobuf.** { *; }
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
