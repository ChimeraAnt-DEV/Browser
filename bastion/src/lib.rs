//! Bastion core engine primitives.
//!
//! Pure-Rust (host-testable) modules for the browser core:
//! * [`adblock`]   – the native content blocker (built-in EasyList-style rules)
//! * [`doh`]       – DNS-over-HTTPS resolver (ultra-fast DNS path)
//! * [`tabstore`]  – background tab suspension (snapshot -> disk state)
//! * [`search`]    – custom search engines
//! * [`preconnect`]– DNS pre-connect cache (address-bar prefetch)
//! * [`version`]   – pinned engine version identity

pub mod adblock;
pub mod doh;
pub mod preconnect;
pub mod search;
pub mod tabstore;
pub mod version;

pub use adblock::{UrlClassifier, BLOCK_RULES};
pub use doh::{DnsRecord, DohResolver};
pub use preconnect::PreconnectCache;
pub use search::{SearchEngine, SearchEngines};
pub use tabstore::{SuspendedTabPage, SuspendedTabStore, TabSnapshot};
pub use version::EngineVersion;

/// Which Android ABI this build is targeting (used by the prefs about 32/64-bit).
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum AndroidAbi {
    Arm64,
    Arm32,
    X86_64,
    Other,
}

impl AndroidAbi {
    pub fn from(bits: u8) -> Self {
        match bits {
            64 => AndroidAbi::Arm64,
            32 => AndroidAbi::Arm32,
            _ => AndroidAbi::Other,
        }
    }

    pub fn name(&self) -> &'static str {
        match self {
            AndroidAbi::Arm64 => "arm64-v8a",
            AndroidAbi::Arm32 => "armeabi-v7a",
            AndroidAbi::X86_64 => "x86_64",
            AndroidAbi::Other => "unknown",
        }
    }
}

#[cfg(feature = "jni")]
pub mod jni;