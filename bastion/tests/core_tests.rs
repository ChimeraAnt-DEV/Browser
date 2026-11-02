//! Host-side unit tests for the pure-Rust core primitives.
//! Run with `cargo test` on any host (no Android/JNI required).

use bastion::{
    DnsRecord, DohResolver, EngineVersion, PreconnectCache, SearchEngine, SearchEngines,
    SuspendedTabStore, TabSnapshot, UrlClassifier, BLOCK_RULES,
};
use std::time::Duration;

fn doubleclick_url() -> String { "https://ad.doubleclick.net/path?x=1".into() }
fn google_fonts_url() -> String { "https://fonts.googleapis.com/css2?family=Sans".into() }
fn example_com_url() -> String { "https://example.com/".into() }

#[test]
fn adblock_matches_doubleclick_and_allows_cdn() {
    let c = UrlClassifier::from_rules(BLOCK_RULES.iter().map(|s| s.to_string()));
    assert!(c.is_blocked(&doubleclick_url()));
    assert!(!c.is_blocked(&google_fonts_url()));
    assert!(!c.is_blocked(&example_com_url()));
}

#[test]
fn url_classifier_is_idempotent_for_https() {
    let c = UrlClassifier::from_rules(BLOCK_RULES.iter().map(|s| s.to_string()));
    assert!(c.is_blocked(&doubleclick_url()));
    assert!(c.is_blocked(&doubleclick_url()));
}

#[test]
fn doh_resolver_returns_canned_record() {
    let mut r = DohResolver::new("1.1.1.1");
    r.set_resolver_url("https://cloudflare-dns.com/dns-query");
    let rec = r.resolve("example.com", "1.2.3.4", 53).unwrap();
    assert_eq!(rec, DnsRecord { host: "example.com".into(), ip: "1.2.3.4".into(), port: 53, ttl: 0 });
}

#[test]
fn search_engines_find_and_default() {
    let se = SearchEngines::defaults();
    assert_eq!(se.default_engine().name, "DuckDuckGo");
    let url = se.query_url(&"cats and dogs".to_string());
    assert!(url.contains("duckduckgo.com"));
    // A brand-new engine replaces nothing but is findable and selectable.
    let mut custom = SearchEngines::defaults();
    let startpage = SearchEngine::custom("Startpage".into(), "https://www.startpage.com/sp/search?query={query}".into(), Some(9u32));
    custom.add(startpage);
    assert_eq!(custom.find("Startpage").unwrap().name, "Startpage");
    custom.set_default("Startpage");
    assert_eq!(custom.default_engine().name, "Startpage");
    let q = custom.query_url(&"test".to_string());
    assert!(q.contains("startpage.com"));
}

#[test]
fn search_engine_url_escapes_query() {
    let se = SearchEngine::custom("Test".into(), "https://example.com/s?q={query}".into(), None);
    assert_eq!(se.do_query("a&b c"), "https://example.com/s?q=a%26b%20c");
}

#[test]
fn suspend_store_roundtrip() {
    let mut store = SuspendedTabStore::default();
    let snapshot = TabSnapshot {
        url: "https://example.com/page/1".into(),
        title: "Example".into(),
        scroll_x: 12.0,
        scroll_y: 340.0,
        zoom: 1.15,
        desktop_mode: false,
        saved_at_unix: 1_700_000_000u64,
    };
    store.save(7, snapshot.clone());
    assert_eq!(store.load(7), Some(snapshot));
    assert_eq!(store.count(), 1);
    let json = store.to_json();
    assert!(json.len() > 10);
    store.remove(99);
    assert_eq!(store.count(), 1);
    store.remove(7);
    assert_eq!(store.count(), 0);
}

#[test]
fn preconnect_cache_dedupes_in_ttl() {
    let mut p = PreconnectCache::new(Duration::from_secs(60));
    p.record("example.com");
    // A fresh record is not yet due for another warm-up.
    assert!(!p.should_preconnect("example.com"));
    // A brand-new host is.
    assert!(p.should_preconnect("fresh.example"));
    // After TTL the old host becomes eligible again.
    let mut evicted = PreconnectCache::new(Duration::from_millis(300));
    evicted.record("stale.example");
    evicted.should_preconnect("stale.example"); // updates its timestamp
    std::thread::sleep(Duration::from_millis(500));
    assert!(evicted.should_preconnect("stale.example"), "expired -> eligible again");
}

#[test]
fn engine_version_is_valid() {
    let v = EngineVersion::current();
    assert!(!v.major.is_empty());
    assert!(!v.build.is_empty());
    let _pretty = v.to_string();
}