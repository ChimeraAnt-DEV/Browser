//! DNS pre-connect cache used by the address bar.
//!
//! When the user is typing, we resolve the domain early; recording it here stops
//! duplicate warm-ups within a TTL and lets the app decide whether a brand-new
//! host still sitting in the pre-connect queue is worth a socket warm-up.

use std::collections::HashMap;
use std::time::{Duration, Instant};

#[derive(Debug)]
pub struct PreconnectCache {
    ttl: Duration,
    seen: HashMap<String, Instant>,
}

impl PreconnectCache {
    pub fn new(ttl: Duration) -> Self {
        Self { ttl, seen: HashMap::new() }
    }

    pub fn record(&mut self, host: &str) {
        let now = Instant::now();
        self.seen.retain(|_, at| now.duration_since(*at) < self.ttl);
        self.seen.insert(host.to_string(), now);
    }

    /// True when we should (still) pre-connect: the host is brand new or its
    /// last warm-up has expired (outside the TTL). False when it was recorded
    /// within the TTL (skip the duplicate warm-up). Either way the timestamp is
    /// refreshed so repeated calls within TTL are cheap no-ops.
    pub fn should_preconnect(&mut self, host: &str) -> bool {
        let now = Instant::now();
        self.seen.retain(|_, at| now.duration_since(*at) < self.ttl);
        let due = self.seen.get(host).is_none();
        self.seen.insert(host.to_string(), now);
        due
    }

    pub fn clear(&mut self) {
        self.seen.clear();
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn ttl_eviction() {
        let mut c = PreconnectCache::new(Duration::from_millis(50));
        c.should_preconnect("a.com");
        std::thread::sleep(Duration::from_millis(120));
        c.should_preconnect("a.com"); // expired -> re-record
        assert!(!c.seen.is_empty());
    }
}