# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontobfuscate

# retrolambda
-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*

# dependencies
-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn org.apache.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn com.google.android.gms.**
-dontwarn com.caverock.**
-dontwarn java.rmi.**
-dontwarn org.w3c.**

# embedded actionbar tabs reflection
-keep class android.support.** { *; }

# serialized over network
-keep class info.snoha.matej.linkeddatamap.api.** { *; }
