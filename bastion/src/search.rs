//! Custom search engines.
//!
//! Users can add, remove, reorder and pick a default; the `{query}` placeholder
//! in the URL template is percent-encoded per RFC 3986.

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct SearchEngine {
    pub name: String,
    pub url_template: String,
    pub priority: Option<u32>,
}

impl SearchEngine {
    pub fn custom(name: String, url_template: String, priority: Option<u32>) -> Self {
        Self { name, url_template, priority }
    }

    /// Renders the search URL for a raw user query.
    pub fn do_query(&self, raw: &str) -> String {
        self.url_template.replace("{query}", &percent_encode(raw))
    }
}

fn percent_encode(raw: &str) -> String {
    let mut out = String::with_capacity(raw.len() + 16);
    for b in raw.bytes() {
        match b {
            b'A'..=b'Z' | b'a'..=b'z' | b'0'..=b'9' | b'-' | b'_' | b'.' | b'~' => {
                out.push(b as char)
            }
            _ => out.push_str(&format!("%{b:02X}")),
        }
    }
    out
}

#[derive(Debug, Clone)]
pub struct SearchEngines {
    pub engines: Vec<SearchEngine>,
    pub default_name: String,
}

impl Default for SearchEngines {
    fn default() -> Self {
        Self::defaults()
    }
}

impl SearchEngines {
    pub fn defaults() -> Self {
        let engines = vec![
            SearchEngine::custom(
                "DuckDuckGo".to_string(),
                "https://duckduckgo.com/?q={query}".to_string(),
                Some(0),
            ),
            SearchEngine::custom(
                "Google".to_string(),
                "https://www.google.com/search?q={query}".to_string(),
                Some(1),
            ),
            SearchEngine::custom(
                "Bing".to_string(),
                "https://www.bing.com/search?q={query}".to_string(),
                Some(2),
            ),
            SearchEngine::custom(
                "Brave".to_string(),
                "https://search.brave.com/search?q={query}".to_string(),
                Some(3),
            ),
        ];
        Self { engines, default_name: "DuckDuckGo".to_string() }
    }

    pub fn default_engine(&self) -> &SearchEngine {
        self.find(&self.default_name).unwrap_or(&self.engines[0])
    }

    pub fn find(&self, name: &str) -> Option<&SearchEngine> {
        self.engines.iter().find(|e| e.name == name)
    }

    pub fn add(&mut self, engine: SearchEngine) {
        if !self.engines.iter().any(|e| e.name == engine.name) {
            self.engines.push(engine);
        }
    }

    pub fn remove(&mut self, name: &str) -> bool {
        let before = self.engines.len();
        self.engines.retain(|e| e.name != name);
        let removed = self.engines.len() != before;
        if removed && self.default_name == name {
            self.default_name = self.engines.first().map(|e| e.name.clone()).unwrap_or_default();
        }
        removed
    }

    pub fn set_default(&mut self, name: &str) -> bool {
        if self.find(name).is_some() {
            self.default_name = name.to_string();
            true
        } else {
            false
        }
    }

    /// Search URL for a raw query via the default engine.
    pub fn query_url(&self, raw: &str) -> String {
        self.default_engine().do_query(raw)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn percent_encodes() {
        assert_eq!(percent_encode("a b&c"), "a%20b%26c");
    }
}