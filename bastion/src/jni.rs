//! JNI exports for `com.browseglass.data.BastionNative`.
//!
//! `BastionNative` is a Kotlin `object`, so its `external` members are
//! *instance* methods: the JNI entry points receive the object instance as the
//! first argument (we ignore it — the classifier is process-global).

#![allow(clippy::not_unsafe_ptr_arg_deref)]

use std::sync::OnceLock;

use jni::objects::{JObject, JString};
use jni::sys::{jboolean, jint, jsize, JNI_FALSE, JNI_TRUE};
use jni::JNIEnv;

use crate::adblock::UrlClassifier;
use crate::version::EngineVersion;

/// One-per-process classifier.
fn classifier() -> &'static UrlClassifier {
    static CLASSIFIER: OnceLock<UrlClassifier> = OnceLock::new();
    CLASSIFIER.get_or_init(|| {
        UrlClassifier::from_rules(crate::adblock::BLOCK_RULES.iter().map(|s| s.to_string()))
    })
}

// NOTE: `_this` is the Kotlin object instance; the Rust side is stateless.

#[no_mangle]
pub extern "system" fn Java_com_browseglass_data_BastionNative_nativeIsBlocked<'a>(
    mut env: JNIEnv<'a>,
    _this: JObject<'a>,
    url: JString<'a>,
) -> jboolean {
    let blocked = env
        .get_string(&url)
        .ok()
        .map(|s| classifier().is_blocked(&s.to_string_lossy()))
        .unwrap_or(false);
    if blocked {
        JNI_TRUE
    } else {
        JNI_FALSE
    }
}

#[no_mangle]
pub extern "system" fn Java_com_browseglass_data_BastionNative_nativeDohServers<'a>(
    mut env: JNIEnv<'a>,
    _this: JObject<'a>,
) -> JObject<'a> {
    string_array(
        &mut env,
        &["https://dns.quad9.net/dns-query", "https://cloudflare-dns.com/dns-query"],
    )
    .unwrap_or_else(|_| JObject::null())
}

#[no_mangle]
pub extern "system" fn Java_com_browseglass_data_BastionNative_nativeEngineVersion<'a>(
    env: JNIEnv<'a>,
    _this: JObject<'a>,
) -> JObject<'a> {
    let v = EngineVersion::current().to_string();
    env.new_string(v)
        .map(JObject::from)
        .unwrap_or_else(|_| JObject::null())
}

#[no_mangle]
pub extern "system" fn Java_com_browseglass_data_BastionNative_nativeEngineMajor(
    _env: JNIEnv<'_>,
    _this: JObject<'_>,
) -> jint {
    EngineVersion::current().major.parse::<i32>().unwrap_or(0)
}

#[no_mangle]
pub extern "system" fn Java_com_browseglass_data_BastionNative_nativeAbiList<'a>(
    mut env: JNIEnv<'a>,
    _this: JObject<'a>,
) -> JObject<'a> {
    string_array(&mut env, &["arm64-v8a", "armeabi-v7a"]).unwrap_or_else(|_| JObject::null())
}

/// Builds a Java `String[]` from the given string slice.
fn string_array<'a>(env: &mut JNIEnv<'a>, items: &[&str]) -> jni::errors::Result<JObject<'a>> {
    let cls = env.find_class("java/lang/String")?;
    let arr = env.new_object_array(items.len() as jsize, &cls, &JObject::null())?;
    for (i, item) in items.iter().enumerate() {
        let s = env.new_string(*item)?;
        env.set_object_array_element(&arr, i as jsize, &s)?;
    }
    Ok(JObject::from(arr))
}