// Bastion cookie-banner blocker.
// Removes common consent UI ("Accept cookies" popups) without activating them,
// by hiding the banner nodes. The browser chrome has a per-site toggle to
// disable this (Phase 5.31).
(function () {
  'use strict';
  var ATTEMPTS = 0;
  var MAX_ATTEMPTS = 8;

  function looksLikeConsentBanner(node) {
    if (!node || node.nodeType !== 1) return false;
    var text = (node.innerText || node.textContent || '').slice(0, 500).toLowerCase();
    if (!text) return false;
    var markers = ['accept cookies', 'accept all cookies', 'cookie policy', 'accept all', 'agree and continue', 'consent', 'gdpr', 'ccpa', 'we use cookies', 'allow all cookies', 'accept privacy', 'agree'];
    var hit = markers.some(function (m) { return text.indexOf(m) !== -1; });
    if (!hit) return false;
    // Prefer the smallest container that looks like a banner/dialog.
    var role = node.getAttribute('role') || '';
    var id = node.id || '';
    var cls = node.className || '';
    return role.indexOf('dialog') !== -1 || /cookie|consent|gdpr|banner|notice/i.test(id + ' ' + cls) || node.tagName === 'DIALOG';
  }

  function scanAndHide() {
    ATTEMPTS++;
    var roots = document.querySelectorAll('div, section, aside, dialog, iframe');
    for (var i = 0; i < roots.length; i++) {
      var el = roots[i];
      if (el.dataset.bastionHidden === '1') continue;
      if (looksLikeConsentBanner(el)) {
        var prev = el.previousElementSibling;
        // Only hide when it is a leaf-ish banner (small enough) to avoid nuking
        // large page sections.
        var rect = el.getBoundingClientRect ? el.getBoundingClientRect() : null;
        if (rect && rect.height < 600 && rect.width < Math.max(document.body ? document.body.clientWidth : 400)) {
          el.style.display = 'none';
          el.style.visibility = 'hidden';
          el.dataset.bastionHidden = '1';
          try { browser.runtime.sendMessage({ bastion: true, type: 'cookieBannerHidden' }); } catch (e) {}
        }
      }
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () { setTimeout(scanAndHide, 120); });
  } else {
    setTimeout(scanAndHide, 120);
  }

  var observer = new MutationObserver(function () { if (ATTEMPTS < MAX_ATTEMPTS) setTimeout(scanAndHide, 250); });
  observer.observe(document.documentElement, { childList: true, subtree: true });
  setTimeout(function () { observer.disconnect(); }, 15000);
})();