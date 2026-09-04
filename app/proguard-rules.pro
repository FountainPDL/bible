# Add project specific ProGuard rules here.
# Debug builds (used by the CI workflow) don't run ProGuard at all.
-keepattributes *Annotation*
-keep class com.fountainpdl.bible.models.** { *; }
