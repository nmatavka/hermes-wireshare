# WireShare Android

This module contains the imported Android application shell for WireShare.

The code was copied into this repository from FrostWire Android so the build does not reference `frostwire-master/`. FrostWire-derived source retains its original GPL notices and attribution, while WireShare-owned package names, Android identity, runtime labels, and service defaults are moved under the `org.team_hermes.wireshare` namespace.

## Build Scope

- Root desktop builds remain Java 21 and do not require the Android SDK.
- Android builds are explicit: run `./gradlew :android-app:assembleDebug` or `./gradlew buildAndroid`.
- Android uses Android Gradle Plugin 9.0.1, compile SDK 36, min SDK 26, and Java/Kotlin toolchain 17.
- The APK application id and namespace are `org.team_hermes.wireshare.android`.

## Android SDK Setup

Install the Android SDK with platform 36 and build tools through Android Studio or command-line tools, then point Gradle at it:

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"
```

Alternatively, create a root `local.properties` file with:

```properties
sdk.dir=/absolute/path/to/android/sdk
```

## Build Commands

```bash
./gradlew build
./gradlew buildAndroid
./gradlew :android-app:assembleDebug
./gradlew :android-app:testPlus1DebugUnitTest
```

The debug APK is produced under `android-app/build/outputs/apk/plus1/debug/`.

## Feature Flags

Google services, Crashlytics, billing, ads, and release signing are not enabled by default. Debug builds should not require FrostWire credentials or contact FrostWire-owned update and promotion services.

Chaquopy remains part of the Android task path because the copied search/runtime code still imports the Python bridge. The Python-backed flows can be split behind a property later once the Java call sites have no hard Chaquopy dependency.

## Backend Direction

The current Android runtime keeps FrostWire's torrent stack as the first concrete backend. New protocol-neutral interfaces live under `org.team_hermes.wireshare.android.backend`, with disabled Gnutella stubs in place so the UI can later become protocol-composite without compiling the desktop Gnutella core directly into Android.
