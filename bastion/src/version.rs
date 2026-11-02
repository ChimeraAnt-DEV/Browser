//! Engine version identity.
//!
//! Keep this in sync with the GeckoView artifact pinned in
//! `gradle/libs.versions.toml`. Shown in settings and used by the update-pipeline
//! policy to detect drift (stable/beta pin, never nightly).

/// Android ABI stable name to bits.
pub struct EngineVersion {
    pub major: String,
    pub build: String,
    pub has_doh: bool,
    pub has_fission: bool,
}

impl EngineVersion {
    pub fn current() -> Self {
        // Updated together with libs.versions.toml geckoviewVersion.
        Self {
            major: "137".to_string(),
            build: "20250414091429".to_string(),
            has_doh: true,
            has_fission: true,
        }
    }
}

impl std::fmt::Display for EngineVersion {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "GeckoView {}.{}", self.major, self.build)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn display() {
        assert!(EngineVersion::current().to_string().contains("GeckoView"));
    }
}