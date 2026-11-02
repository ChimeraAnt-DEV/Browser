# Bastion

A private, glassmorphic Android browser built on **GeckoView** (Mozilla's
independent rendering engine — not WebView), a **Rust** core for the performance
primitives, and **Jetpack Compose** for the UI.

The universal APK ships **both 64-bit (arm64-v8a) and 32-bit (armeabi-v7a)**
native libraries.

## Architecture

```
app/                     Kotlin + Compose UI, GeckoView integration
  src/main/java/com/browseglass/
    BastionApp.kt        Application: single GeckoRuntime + startup tracing
    MainActivity.kt      Single-activity, edge-to-edge, permission launchers
    engine/              BrowserEngine, SessionManager, Tab, GeckoSessionHolder
    ui/                  glassmorphic screen, toolbar, tab grid, settings, home
    data/                DataStore prefs, native bridge (BastionNative), scripts
    security/            BiometricGate (fingerprint lock for private tabs)
  src/main/assets/       bundled WebExtension (cookie-banner blocker)
  src/main/jniLibs/      built by CI: libbastion.so for arm64-v8a + armeabi-v7a
  src/main/baseline-prof.txt
bastion/                 Rust core (src/) + host-side tests (tests/)
  src/adblock.rs         EasyList-style native content blocker
  src/doh.rs             DNS-over-HTTPS policy
  src/search.rs          custom search engines
  src/tabstore.rs        tab suspension state snapshots
  src/preconnect.rs      address-bar DNS pre-connect cache
  src/jni.rs             JNI bridge (BastionNative)
.github/workflows/build.yml   CI: Rust libs → signed universal APK → upload
```

## Engine

- **GeckoView stable pin** (`159.0.20261022150525`), never nightly
- Fission (site isolation) enabled — a crash on one tab can't take the browser down
- Content-process ceiling (4) via `dom.ipc.processCount.override`
- HTTPS-only, DNS-over-HTTPS (Quad9 + Cloudflare as TRR)
- Native content blocking (tracking/ads/crypto/fingerprinting) via ContentBlocking
- Renderer-crash → silent discard + "Reload" affordance
- Tab suspension (30 min) + LRU eviction on `onTrimMemory`
- Hardware-accelerated `GeckoView` SurfaceView

## Build

```bash
# 1) Rust libraries for both ABIs (requires NDK + cargo-ndk)
./build-android-rust.sh

# 2) Android app (debug or release)
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease   # requires keystore/bastion.jks or -P passwords
```

The release APK is R8-minified, shrunk, ships the baseline profile, and embeds
the native libs for `arm64-v8a` and `armeabi-v7a`.

## CI

`.github/workflows/build.yml` runs on push/PR:

1. setup JDK 17, Gradle, Android SDK (platform 37, NDK 28)
2. cross-compiles the Rust core via cargo-ndk for both ABIs
3. generates a throwaway signing keystore
4. assembles `app-release.apk` + `app-debug.apk` + `app-release.aab`
5. **verifies the APK actually contains both ABIs** (fails otherwise)
6. uploads the artifacts

## Tests

```bash
cd bastion && cargo test   # 16 host-side tests (no Android needed)
```