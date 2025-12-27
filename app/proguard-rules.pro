# Add project specific ProGuard rules here.

# Keep Bluetooth classes
-keep class android.bluetooth.** { *; }
-keep class com.augment.tuner.** { *; }

# Keep manufacturer data parsing
-keepclassmembers class * {
    *** parseManufacturerData(...);
}
