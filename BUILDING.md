# Building, Running, and Packaging WireShare

This is the practical build and release entrypoint for WireShare contributors and release builders. The canonical desktop payload is `WireShare.jar`; platform packages are built from that jar.

## Requirements

- JDK 21 with `java`, `javac`, `jlink`, and `jpackage` available from the active `JAVA_HOME`.
- The Gradle wrapper included in this repository: `./gradlew`.
- macOS packaging: macOS host with the JDK 21 `jpackage` tool. Signing and notarization are optional.
- Windows packaging: Windows x64 host with the JDK 21 `jpackage` tool. MSI generation may also require WiX tooling if the host JDK/runtime does not provide everything needed.
- Linux Flatpak packaging: Linux x64 or arm64 host with `flatpak` and `flatpak-builder`.

Local release packaging is allowed to produce unsigned artifacts. If signing credentials are missing, Gradle logs a warning and continues where the platform task supports unsigned output.

## Build

Build only the Compose desktop app:

```bash
./gradlew :desktop-compose-app:build
```

Build the full project and show all Gradle warnings:

```bash
./gradlew build --console=plain --warning-mode all
```

Build the canonical runnable jar:

```bash
./gradlew wireShareJar
```

The jar is written to:

```text
WireShare.jar
```

## Run

Run the Compose desktop app through Gradle during development:

```bash
./gradlew :desktop-compose-app:run
```

Run the canonical jar after `wireShareJar` has been built:

```bash
java -jar WireShare.jar
```

On Apple Silicon, prefer the Gradle `run` task for development because the build wires in the runtime payload and JVM options used by the Compose desktop path.

## Package Desktop Releases

Build the host-specific desktop package:

```bash
./gradlew packageDesktopRelease
```

`packageDesktopRelease` dispatches by host:

- macOS builds a DMG.
- Windows x64 builds an MSI.
- Linux x64/arm64 builds a Flatpak bundle.

Package outputs:

```text
build/macos/dmg/WireShare-*.dmg
build/windows/msi/WireShare-*.msi
build/distributions/WireShare-7.0-linux-*.flatpak
```

Direct platform tasks are also available:

```bash
./gradlew packageMacAppImage
./gradlew packageMacDmg
./gradlew packageWindowsMsi
./gradlew signWindowsMsi
./gradlew packageLinuxFlatpak
```

Use `signWindowsMsi` only when Windows signing credentials are configured. macOS signing and notarization are covered in more detail in [MACOS_SIGNING.md](MACOS_SIGNING.md).

## Linux Flatpak

Install the Flatpak tools and Freedesktop runtime/SDK:

```bash
flatpak remote-add --if-not-exists flathub https://dl.flathub.org/repo/flathub.flatpakrepo
flatpak install flathub org.freedesktop.Platform//25.08 org.freedesktop.Sdk//25.08
```

Build the Flatpak bundle:

```bash
./gradlew packageLinuxFlatpak
```

Install and run the local bundle:

```bash
flatpak install --user build/distributions/WireShare-7.0-linux-*.flatpak
flatpak run org.teamhermes.WireShare
```

The Flatpak bundle includes a Java 21 runtime image generated with `jlink` from the active `JAVA_HOME`.

## Signing Notes

Signing is optional for local packaging.

- macOS signing/notarization properties and release flow are documented in [MACOS_SIGNING.md](MACOS_SIGNING.md).
- Windows signing can use either a certificate file or certificate thumbprint plus `signtool`; see [RELEASE_PACKAGING.md](RELEASE_PACKAGING.md) for the property names.
- Unsigned macOS artifacts may require the usual local Gatekeeper override when opened outside a notarized release flow.

## Troubleshooting

- If `java -jar WireShare.jar` runs old code, rebuild with `./gradlew wireShareJar`.
- If packaging cannot find `jpackage` or `jlink`, verify `JAVA_HOME` points at a full JDK 21, not a JRE.
- If `packageDesktopRelease` fails with an unsupported host error, use a supported release host: macOS, Windows x64, or Linux x64/arm64.
- If `packageLinuxFlatpak` cannot resolve the runtime, install `org.freedesktop.Platform//25.08` and `org.freedesktop.Sdk//25.08` from Flathub.
- If an unsigned package is produced, that is expected unless signing credentials are configured.

