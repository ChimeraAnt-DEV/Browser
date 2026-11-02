//! Background tab suspension: state snapshotting to disk.
//!
//! Kotlin's background worker serialises the session state on which this module
//! builds a stable, in-memory-addressable store. `to_json()` is the blob written
//! to disk so a frozen tab can be restored bit-perfectly without keeping its
//! `GeckoSession` alive.

use std::collections::HashMap;

#[derive(Debug, Clone, PartialEq, serde::Serialize, serde::Deserialize)]
pub struct TabSnapshot {
    pub url: String,
    pub title: String,
    pub scroll_x: f64,
    pub scroll_y: f64,
    pub zoom: f64,
    pub desktop_mode: bool,
    pub saved_at_unix: u64,
}

#[derive(Debug, Clone, PartialEq, Eq, serde::Serialize, serde::Deserialize)]
pub struct SuspendedTabPage {
    pub url: String,
    pub title: String,
    pub preview_base64: Option<String>,
    pub desktop_mode: bool,
}

#[derive(Debug, Default)]
pub struct SuspendedTabStore {
    inner: HashMap<i64, TabSnapshot>,
}

impl SuspendedTabStore {
    pub fn save(&mut self, tab_id: i64, snapshot: TabSnapshot) {
        self.inner.insert(tab_id, snapshot);
    }

    pub fn load(&self, tab_id: i64) -> Option<TabSnapshot> {
        self.inner.get(&tab_id).cloned()
    }

    pub fn remove(&mut self, tab_id: i64) {
        self.inner.remove(&tab_id);
    }

    pub fn count(&self) -> usize {
        self.inner.len()
    }

    pub fn all(&self) -> impl Iterator<Item = (&i64, &TabSnapshot)> {
        self.inner.iter()
    }

    pub fn to_json(&self) -> String {
        serde_json::to_string(&self.inner).unwrap_or_else(|_| "{}".to_string())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn json_roundtrip() {
        let mut s = SuspendedTabStore::default();
        s.save(
            1,
            TabSnapshot {
                url: "https://a".into(),
                title: "A".into(),
                scroll_x: 0.0,
                scroll_y: 0.0,
                zoom: 1.0,
                desktop_mode: false,
                saved_at_unix: 0,
            },
        );
        let json = s.to_json();
        let reparsed: HashMap<i64, TabSnapshot> = serde_json::from_str(&json).unwrap();
        assert_eq!(reparsed.get(&1).unwrap().url, "https://a");
    }
}