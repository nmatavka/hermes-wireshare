# Desktop Release Packaging

WireShare desktop releases are built from the canonical `WireShare.jar` produced by the root Gradle build.

For the general contributor guide covering build, run, jar, and package commands, see [BUILDING.md](BUILDING.md). This document focuses on release artifact identity and packaging details.

## Product Identity

- App name: `WireShare`
- App id: `org.teamhermes.WireShare`
- Vendor: `Hermes`
- Version source: Gradle `releaseAppVersion`
- Windows upgrade UUID: `5CAFE0D2-ABB3-4F01-8CFE-C564E00C09EC`

## Local Packaging Tasks

```bash
./gradlew build --console=plain --warning-mode all
./gradlew wireShareJar
./gradlew packageDesktopRelease
```

`packageDesktopRelease` is host-specific:

- macOS builds `build/macos/dmg/WireShare-*.dmg`.
- Windows x64 builds `build/windows/msi/WireShare-*.msi`.
- Linux x64/arm64 builds `build/distributions/WireShare-7.0-linux-*.flatpak`.

## Signing

Signing is optional for local packaging. If credentials are missing, Gradle builds unsigned packages and logs a warning.

macOS:

- `macosCodesignIdentity` or `MACOS_CODESIGN_IDENTITY`
- `macosCodesignKeychain` or `MACOS_CODESIGN_KEYCHAIN`
- `macosNotaryProfile` or `MACOS_NOTARY_PROFILE`

Windows:

- `windowsCodesignCertificatePath` or `WINDOWS_CODESIGN_CERTIFICATE_PATH`
- `windowsCodesignCertificatePassword` or `WINDOWS_CODESIGN_CERTIFICATE_PASSWORD`
- `windowsCodesignCertificateThumbprint` or `WINDOWS_CODESIGN_CERTIFICATE_THUMBPRINT`
- `windowsSignToolPath` or `WINDOWS_SIGNTOOL_PATH`
- `windowsTimestampUrl` or `WINDOWS_TIMESTAMP_URL`

## Linux Flatpak

The Flatpak build uses `org.freedesktop.Platform//25.08` and `org.freedesktop.Sdk//25.08`.

Install local prerequisites:

```bash
flatpak remote-add --if-not-exists flathub https://dl.flathub.org/repo/flathub.flatpakrepo
flatpak install flathub org.freedesktop.Platform//25.08 org.freedesktop.Sdk//25.08
```

The Flatpak bundles a Java 21 runtime image generated from the active `JAVA_HOME`.

## CI

`.github/workflows/desktop-release.yml` builds:

- Windows x64 MSI
- macOS x64 DMG
- macOS arm64 DMG
- Linux x64 Flatpak
- Linux arm64 Flatpak

CI signing is also optional. Add the documented secrets to produce signed/notarized artifacts.
