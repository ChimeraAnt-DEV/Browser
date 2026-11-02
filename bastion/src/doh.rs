//! DNS-over-HTTPS resolver.
//!
//! On Android the actual network call is performed by Gecko's network stack; this
//! module owns the *policy* (which DoH server, cache TTL, bootstrap) and exposes a
//! host-side testable `resolve` that returns a DNS record for a given IP (produced
//! by the caller, e.g. a recently-seen A record) so pre-connect can warm the
//! sockets before the user hits Enter.

use std::time::Instant;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DnsRecord {
    pub host: String,
    pub ip: String,
    pub port: u16,
    pub ttl: u64,
}

pub struct DohResolver {
    resolver_url: String,
    bootstrap_ip: String,
    last_answered: Option<(Instant, DnsRecord)>,
}

impl Default for DohResolver {
    fn default() -> Self {
        Self::new("9.9.9.9")
    }
}

impl DohResolver {
    pub fn new(bootstrap_ip: &str) -> Self {
        Self {
            resolver_url: "https://dns.quad9.net/dns-query".to_string(),
            bootstrap_ip: bootstrap_ip.to_string(),
            last_answered: None,
        }
    }

    pub fn set_resolver_url(&mut self, url: &str) {
        self.resolver_url = url.to_string();
    }

    pub fn resolver_url(&self) -> &str {
        &self.resolver_url
    }

    pub fn bootstrap_ip(&self) -> &str {
        &self.bootstrap_ip
    }

    /// Produces a DNS record. `observed_ip` is what the caller most recently saw
    /// for this host (e.g. from an existing connection); in production this is
    /// replaced by a true DoH query result (Gecko performs the I/O).
    pub fn resolve(&mut self, host: &str, observed_ip: &str, port: u16) -> Option<DnsRecord> {
        let rec = DnsRecord {
            host: host.to_string(),
            ip: observed_ip.to_string(),
            port,
            ttl: 0,
        };
        self.last_answered = Some((Instant::now(), rec.clone()));
        Some(rec)
    }

    /// The most recently cached record, if still within 5 minutes.
    pub fn cached(&self) -> Option<&DnsRecord> {
        match &self.last_answered {
            Some((at, rec)) if at.elapsed().as_secs() < 300 => Some(rec),
            _ => None,
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn defaults_to_quad9() {
        let r = DohResolver::default();
        assert_eq!(r.resolver_url(), "https://dns.quad9.net/dns-query");
        assert_eq!(r.bootstrap_ip(), "9.9.9.9");
    }
}