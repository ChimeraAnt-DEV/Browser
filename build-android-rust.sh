#!/usr/bin/env bash
# Cross-compiles the Bastion Rust core for the two Android ABIs the universal
# APK ships: 64-bit arm64-v8a and 32-bit armeabi-v7a.
#
# Requires:
#   * Rust toolchain (rustup)
#   * cargo-ndk       (cargo install cargo-ndk --locked)
#   * Android NDK     (ANDROID_HOME with ndk-bundle or an ndk/ folder)
#   * rustup targets  aarch64-linux-android, armv7-linux-androideabi
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGETS=("aarch64-linux-android" "armv7-linux-androideabi")
PLATFORM_LEVEL="${ANDROID_PLATFORM_LEVEL:-26}"

# Locate the NDK.
NDK="${ANDROID_NDK_HOME:-}"
if [ -z "$NDK" ] && [ -n "${ANDROID_HOME:-}" ]; then
  if [ -d "$ANDROID_HOME/ndk-bundle" ]; then
    NDK="$ANDROID_HOME/ndk-bundle"
  elif [ -d "$ANDROID_HOME/ndk" ]; then
    # Pick the newest installed NDK.
    NDK="$(find "$ANDROID_HOME/ndk" -maxdepth 1 -mindepth 1 -type d | sort -V | tail -1)"
  fi
fi
if [ -z "${NDK:-}" ] || [ ! -d "${NDK:-}" ]; then
  echo "error: Android NDK not found. Set ANDROID_NDK_HOME." >&2
  exit 1
fi
export NDK_HOME="$NDK"

# Ensure rustup targets are present.
for t in "${TARGETS[@]}"; do
  rustup target add "$t" 2>/dev/null || true
done

# cargo-ndk maps its -t flags to the rustup android triplets internally;
# the output folder structure uses the ABI names directly.
OUT="$ROOT/app/src/main/jniLibs"
cd "$ROOT/bastion"

cargo ndk -t arm64-v8a -t armeabi-v7a \
  -o "$OUT" --platform "$PLATFORM_LEVEL" \
  build --release --features jni

echo
echo "Built Rust core for:"
find "$OUT" -name '*.so' -exec ls -lh {} \;