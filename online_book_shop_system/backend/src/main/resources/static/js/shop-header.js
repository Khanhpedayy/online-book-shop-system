/**
 * Shared storefront header: session → JWT, auth link visibility, logout, search on non-index pages.
 * Include after auth-session.js on every shop page.
 */
(function () {
    var SHOP_API_BASE = "http://localhost:8080";

    function updateShopHeaderAuth() {
        var token = localStorage.getItem("token");
        var loginLink = document.getElementById("loginLink");
        var logoutLink = document.getElementById("logoutLink");
        var profileLink = document.getElementById("profileLink");
        var ordersLink = document.getElementById("ordersLink");
        var reviewsLink = document.getElementById("reviewsLink");
        var supportLink = document.getElementById("supportLink");
        var notifLink = document.getElementById("notifLink");
        if (!loginLink || !logoutLink) {
            return;
        }
        if (token) {
            loginLink.style.display = "none";
            logoutLink.style.display = "inline";
            if (profileLink) {
                profileLink.style.display = "inline";
            }
            if (ordersLink) {
                ordersLink.style.display = "inline";
            }
            if (reviewsLink) {
                reviewsLink.style.display = "inline";
            }
            if (notifLink) {
                notifLink.style.display = "inline";
            }
            if (supportLink) {
                supportLink.style.display = "inline";
            }
        } else {
            loginLink.style.display = "inline";
            logoutLink.style.display = "none";
            if (profileLink) {
                profileLink.style.display = "none";
            }
            if (ordersLink) {
                ordersLink.style.display = "none";
            }
            if (reviewsLink) {
                reviewsLink.style.display = "none";
            }
            if (notifLink) {
                notifLink.style.display = "none";
            }
            if (supportLink) {
                supportLink.style.display = "none";
            }
        }
    }

    window.updateShopHeaderAuth = updateShopHeaderAuth;

    window.shopLogout = async function shopLogout() {
        if (typeof performLogout === "function") {
            await performLogout(SHOP_API_BASE);
        } else {
            localStorage.removeItem("token");
            localStorage.removeItem("user");
            window.location.href = "login.html";
        }
    };

    window.updateShopHeaderCart = async function updateShopHeaderCart() {
        var el = document.getElementById("headerCartCount");
        if (!el) {
            return;
        }
        var token = localStorage.getItem("token");
        if (!token) {
            el.textContent = "$0.00 (0)";
            return;
        }
        try {
            var r = await fetch(SHOP_API_BASE + "/api/cart/me", {
                headers: { Authorization: "Bearer " + token },
                credentials: "include"
            });
            if (!r.ok) {
                return;
            }
            var items = await r.json();
            if (!Array.isArray(items)) {
                return;
            }
            var total = 0;
            var count = 0;
            items.forEach(function (ci) {
                var price = ci.variant && ci.variant.salePrice != null ? Number(ci.variant.salePrice) : 0;
                var q = ci.quantity || 0;
                total += price * q;
                count += q;
            });
            el.textContent = "$" + total.toFixed(2) + " (" + count + ")";
        } catch (e) {
            /* ignore */
        }
    };

    function bindLogout() {
        var el = document.getElementById("logoutLink");
        if (!el) {
            return;
        }
        el.addEventListener("click", function (e) {
            e.preventDefault();
            shopLogout();
        });
    }

    /** On cart/checkout/book, search sends user to home with ?keyword= */
    function initShopHeaderSearch() {
        var path = (window.location.pathname || "").toLowerCase();
        var file = path.split("/").pop() || "";
        var onIndex = file === "index.html" || file === "" || path.endsWith("/");
        if (onIndex) {
            return;
        }
        var btn = document.getElementById("searchBtn");
        var input = document.getElementById("searchInput");
        if (!btn || !input) {
            return;
        }
        function go() {
            var q = (input.value || "").trim();
            window.location.href = q ? "index.html?keyword=" + encodeURIComponent(q) : "index.html";
        }
        btn.addEventListener("click", go);
        input.addEventListener("keydown", function (e) {
            if (e.key === "Enter") {
                go();
            }
        });
    }

    /* ── Notification badge ── */
    window.updateNotifBadge = async function () {
        var badge = document.getElementById("notifBadge");
        if (!badge) return;
        var token = localStorage.getItem("token");
        if (!token) { badge.textContent = ""; return; }
        try {
            var r = await fetch(SHOP_API_BASE + "/api/me/notifications/unread-count", {
                headers: { Authorization: "Bearer " + token },
                credentials: "include"
            });
            if (!r.ok) return;
            var data = await r.json();
            badge.textContent = (data.count > 0) ? data.count : "";
        } catch (e) { /* ignore */ }
    };

    document.addEventListener("DOMContentLoaded", async function () {
        if (typeof syncAuthFromServerSession === "function") {
            await syncAuthFromServerSession(SHOP_API_BASE);
        }
        updateShopHeaderAuth();
        bindLogout();
        initShopHeaderSearch();
        updateShopHeaderCart();
        updateNotifBadge();
        setInterval(updateNotifBadge, 30000); // poll every 30s
    });
})();
