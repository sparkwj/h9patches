# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep class android.hardware.dsp.** { *; }
-keep class android.hidl.** { *; }
-keep class com.android.dx.rop.** { *; }
# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int d(...);
    public static int w(...);
    public static int v(...);
    public static int i(...);
}
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn android.service.notification.ZenModeConfig
-dontwarn android.text.FontConfig$Family
-dontwarn android.util.Pools$SynchronizedPool
-dontwarn android.util.Singleton
-dontwarn android.view.DisplayAdjustments
-dontwarn android.view.ViewHierarchyEncoder
-dontwarn android.view.ViewRootImpl$ActivityConfigCallback
-dontwarn android.view.Window$OnWindowDismissedCallback
-dontwarn android.view.Window$WindowControllerCallback
-dontwarn android.view.autofill.AutofillManager$AutofillClient
-dontwarn android.view.autofill.AutofillPopupWindow
-dontwarn android.view.autofill.IAutofillWindowPresenter
-dontwarn com.android.internal.app.IAppOpsService
-dontwarn com.android.internal.app.IVoiceInteractor
-dontwarn com.android.internal.location.ProviderProperties
-dontwarn com.android.internal.util.AsyncChannel
-dontwarn com.android.internal.util.FunctionalUtils$ThrowingRunnable
-dontwarn com.android.internal.util.FunctionalUtils$ThrowingSupplier
-dontwarn com.android.internal.util.IndentingPrintWriter
-dontwarn com.android.internal.util.NotificationColorUtil
