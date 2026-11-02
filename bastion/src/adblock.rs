//! Native content blocker.
//!
//! A compact EasyList-style matcher. Rules are plain subdomain/path filters with
//! `||` meaning "domain boundary". Kept intentionally dependency-free so we stay
//! tiny, fast, and host-testable. The JNI bridge exposes the same verdict so the
//! Kotlin layer can ask once per navigation and apply a per-site exception.

pub const BLOCK_RULES: &[&str] = &[
    "||doubleclick.net^",
    "||googlesyndication.com^",
    "||googleadservices.com^",
    "||adservice.google.com^",
    "||connect.facebook.net^",
    "||facebook.net^/tr",
    "||ads.yahoo.com^",
    "||adnxs.com^",
    "||rubiconproject.com^",
    "||criteo.com^",
    "||taboola.com^",
    "||outbrain.com^",
    "||amazon-adsystem.com^",
    "||scorecardresearch.com^",
];

/// Understands the small rule syntax above:
/// - `||domain^`   -> any host ending in `.domain` or equal to `domain`, any path
/// - `||domain^/p` -> same host match, path must start with `/p`
/// Everything else ("literal") matches if the URL simply contains the text.
#[derive(Debug, Clone, Default)]
pub struct UrlClassifier {
    rules: Vec<Rule>,
}

#[derive(Debug, Clone)]
struct Rule {
    host: Option<String>,
    path_prefix: Option<String>,
    literal: Option<String>,
}

impl UrlClassifier {
    pub fn from_rules<I: IntoIterator<Item = String>>(rules: I) -> Self {
        let rules = rules.into_iter().filter_map(|r| Self::parse(&r)).collect();
        Self { rules }
    }

    fn parse(raw: &str) -> Option<Rule> {
        let raw = raw.trim();
        if raw.is_empty() || raw.starts_with('!') {
            return None;
        }
        if let Some(rest) = raw.strip_prefix("||") {
            let (host, path) = match rest.find('^') {
                Some(i) => (&rest[..i], &rest[i + 1..]),
                None => (rest, ""),
            };
            let path_prefix = if path.is_empty() { None } else { Some(path.to_string()) };
            return Some(Rule { host: Some(host.to_string()), path_prefix, literal: None });
        }
        Some(Rule { host: None, path_prefix: None, literal: Some(raw.to_string()) })
    }

    /// True when the URL should be blocked.
    pub fn is_blocked(&self, url: &str) -> bool {
        let lower = url.to_ascii_lowercase();
        let host = extract_host(&lower);
        self.rules.iter().any(|r| {
            if let Some(h) = &r.host {
                let host_matches = match &host {
                    Some(hh) => hh == h || hh.ends_with(&format!(".{h}")),
                    None => false,
                };
                if !host_matches {
                    return false;
                }
                match &r.path_prefix {
                    Some(p) => path_prefix_matches(url, p),
                    None => true,
                }
            } else if let Some(l) = &r.literal {
                lower.contains(&l.to_ascii_lowercase())
            } else {
                false
            }
        })
    }
}

fn path_prefix_matches(url: &str, prefix: &str) -> bool {
    let no_query = url.split('?').next().unwrap_or(url);
    match no_query.split_once("://") {
        Some((_, rest)) => {
            // Path is the portion (starting at the first '/') after the host;
            // `rest` strips authority so all indices are relative to it.
            match rest.find('/') {
                Some(i) => rest[i..].starts_with(prefix),
                None => prefix == "/",
            }
        }
        None => no_query.starts_with(prefix),
    }
}

fn extract_host(url: &str) -> Option<String> {
    let after = url.split_once("://").map(|(_, r)| r).unwrap_or(url);
    let hostport = after.splitn(2, '/').next().unwrap_or(after);
    let host = hostport.split('@').last().unwrap_or(hostport);
    let host = host.split(':').next().unwrap_or(host);
    (!host.is_empty()).then(|| host.to_string())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parses_rules() {
        let c = UrlClassifier::from_rules(BLOCK_RULES.iter().map(|s| s.to_string()));
        assert!(c.rules.len() >= 10);
    }

    #[test]
    fn host_variants_match() {
        let c = UrlClassifier::from_rules(vec!["||example.net^".to_string()]);
        assert!(c.is_blocked("https://example.net/"));
        assert!(c.is_blocked("https://ads.example.net/path"));
        assert!(!c.is_blocked("https://notexample.net/"));
    }

    #[test]
    fn path_rule_matches() {
        let c = UrlClassifier::from_rules(vec!["||tracker.io^/collect".to_string()]);
        assert!(c.is_blocked("https://tracker.io/collect?x=1"));
        assert!(!c.is_blocked("https://tracker.io/home"));
    }
}