# macOS Signing and Notarization

This repo now has an opt-in macOS signing pipeline for desktop release artifacts.

## Goals

- Keep local development builds easy.
- Let release builders sign all staged macOS native libraries.
- Produce a signed `WireShare.app` app image and DMG from the canonical `WireShare.jar`.
- Support notarization and stapling with Apple's `notarytool`.

## jlibtorrent Mode

Desktop packaging now defaults to the latest published `frostwire-jlibtorrent` release, not source-head mode.

Default behavior:

- Resolve the latest published GitHub release for `frostwire/frostwire-jlibtorrent`
- Check out the matching source tag for the Java code
- Download the matching host native jar asset for packaging/runtime

Optional overrides:

- `-PfrostwireJlibtorrentSourceHead=true`
  Use the managed source checkout on the upstream default branch instead of the latest published release.

- `-PfrostwireJlibtorrentVersion=2.0.12.7`
  Force a specific published release version.

- `-PfrostwireJlibtorrentDir=/path/to/frostwire-jlibtorrent`
  Use an explicit local checkout.

## Signing Properties

The Gradle build reads these from either Gradle properties or environment variables.

- `macosCodesignIdentity` or `MACOS_CODESIGN_IDENTITY`
  Example: `Developer ID Application: Your Name (TEAMID)`

- `macosCodesignKeychain` or `MACOS_CODESIGN_KEYCHAIN`
  Example: `$HOME/Library/Keychains/login.keychain-db`

- `macosNotaryProfile` or `MACOS_NOTARY_PROFILE`
  Name of a `notarytool` keychain profile previously configured with `xcrun notarytool store-credentials`.

## Main Tasks

- `stageMacRuntimePayload`
  Collects macOS native runtime payloads into `build/macos/runtime-payload`.

- `signMacRuntimePayload`
  Signs staged `.dylib` and `.jnilib` files when a Developer ID identity is configured.

- `verifyMacRuntimePayloadSignatures`
  Verifies codesign state for staged native binaries.

- `wireShareJar`
  Builds the canonical fat jar and embeds the staged macOS native payload.

- `packageMacAppImage`
  Uses `jpackage` to build `build/macos/app-image/WireShare.app`.
  If signing properties are present, `jpackage` signs the app image too.

- `notarizeMacAppImage`
  Zips the generated app image, submits it with `notarytool`, waits for completion, then staples the ticket.

- `packageMacDmg`
  Builds `build/macos/dmg/WireShare-*.dmg` from the signed or unsigned app image.

## Typical Release Flow

Build a signed app image:

```bash
./gradlew packageMacAppImage \
  -PmacosCodesignIdentity="Developer ID Application: Your Name (TEAMID)" \
  -PmacosCodesignKeychain="$HOME/Library/Keychains/login.keychain-db"
```

Build a DMG:

```bash
./gradlew packageMacDmg \
  -PmacosCodesignIdentity="Developer ID Application: Your Name (TEAMID)" \
  -PmacosCodesignKeychain="$HOME/Library/Keychains/login.keychain-db"
```

Verify staged native signatures:

```bash
./gradlew verifyMacRuntimePayloadSignatures \
  -PmacosCodesignIdentity="Developer ID Application: Your Name (TEAMID)" \
  -PmacosCodesignKeychain="$HOME/Library/Keychains/login.keychain-db"
```

Notarize and staple:

```bash
./gradlew notarizeMacAppImage \
  -PmacosCodesignIdentity="Developer ID Application: Your Name (TEAMID)" \
  -PmacosCodesignKeychain="$HOME/Library/Keychains/login.keychain-db" \
  -PmacosNotaryProfile="wireShare-notary"
```

## Notes

- If signing properties are not present, the build still works, but macOS natives remain unsigned.
- The macOS bundle/package identifier is `org.teamhermes.WireShare`.
- The build prefers freshly compiled helper dylibs over older checked-in copies when both exist.
- `WireShare.jar` is marked as a multi-release jar so Java 21 can see versioned classes from bundled dependencies like `dnsjava`.
- For reproducible release behavior, prefer the default published `jlibtorrent` mode over source-head mode.
