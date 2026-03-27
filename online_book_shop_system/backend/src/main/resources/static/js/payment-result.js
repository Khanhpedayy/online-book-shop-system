/**
 * PayOS return/cancel page: reads query params, shows outcome, offers repay with JWT.
 */
(function () {
    var API_BASE = "http://localhost:8080";

    function getToken() {
        return localStorage.getItem("token");
    }

    async function apiPost(path, body) {
        var headers = { "Content-Type": "application/json" };
        var t = getToken();
        if (t) {
            headers.Authorization = "Bearer " + t;
        }
        var res = await fetch(API_BASE + path, {
            method: "POST",
            headers: headers,
            credentials: "include",
            body: body ? JSON.stringify(body) : "{}"
        });
        if (!res.ok) {
            var text = await res.text();
            throw new Error(text || res.statusText);
        }
        return res.json();
    }

    function parseOutcome(params) {
        var orderId = params.get("orderId");
        var paymentLinkId = params.get("id");
        var payosCancel = params.get("payosCancel") === "1";
        var code = params.get("code");
        var cancel = params.get("cancel") === "true";
        var status = (params.get("status") || "").toUpperCase();

        if (!orderId) {
            return { kind: "invalid", orderId: null, paymentLinkId: null, message: "Missing order reference." };
        }

        if (payosCancel || cancel || status === "CANCELLED") {
            return { kind: "cancelled", orderId: orderId, paymentLinkId: paymentLinkId, message: "Payment was cancelled." };
        }

        if (code && code !== "00") {
            return { kind: "failed", orderId: orderId, paymentLinkId: paymentLinkId, message: "Payment failed (code " + code + ")." };
        }

        if (status === "PAID") {
            return { kind: "success", orderId: orderId, paymentLinkId: paymentLinkId, message: "Payment completed successfully." };
        }

        if (status === "PENDING" || status === "PROCESSING") {
            return {
                kind: "pending",
                orderId: orderId,
                paymentLinkId: paymentLinkId,
                message: "Payment is still pending or processing. Refresh your order later or try again."
            };
        }

        if (!code && !status && !cancel) {
            return {
                kind: "unknown",
                orderId: orderId,
                paymentLinkId: paymentLinkId,
                message: "No payment status in the URL. Open your order from Orders if you finished paying."
            };
        }

        return { kind: "failed", orderId: orderId, paymentLinkId: paymentLinkId, message: "Payment was not completed." };
    }

    function render() {
        var box = document.getElementById("paymentResult");
        if (!box) {
            return { kind: "invalid", orderId: null, paymentLinkId: null, message: "paymentResult element not found." };
        }

        var params = new URLSearchParams(window.location.search);
        var out = parseOutcome(params);

        var actions = document.getElementById("paymentActions");
        if (actions) {
            actions.innerHTML = "";
        }

        if (out.kind === "invalid") {
            box.className = "payment-banner payment-banner--error";
            box.textContent = out.message;
            return out;
        }

        var title =
            out.kind === "success"
                ? "Success"
                : out.kind === "cancelled"
                  ? "Cancelled"
                  : out.kind === "pending"
                    ? "Pending"
                    : "Payment issue";

        var detail =
            "<strong>Order #" +
            out.orderId +
            "</strong><p class=\"payment-detail\">" +
            out.message +
            "</p>";

        if (out.kind === "success") {
            box.className = "payment-banner payment-banner--ok";
            box.innerHTML = "<h2>" + title + "</h2>" + detail;
            if (actions) {
                actions.innerHTML =
                    '<a class="shop-btn shop-btn--secondary" href="order-detail.html?id=' +
                    encodeURIComponent(out.orderId) +
                    '">View order</a> <a class="shop-btn" href="order-history.html">Order history</a>';
            }
            return out;
        }

        box.className =
            out.kind === "pending" ? "payment-banner payment-banner--pending" : "payment-banner payment-banner--error";
        box.innerHTML = "<h2>" + title + "</h2>" + detail;

        var canRetry = out.kind === "cancelled" || out.kind === "failed" || out.kind === "pending" || out.kind === "unknown";
        if (canRetry && actions) {
            var retryBtn = document.createElement("button");
            retryBtn.type = "button";
            retryBtn.className = "shop-btn";
            retryBtn.textContent = "Pay again";
            retryBtn.addEventListener("click", function () {
                payAgain(out.orderId);
            });
            actions.appendChild(retryBtn);

            var od = document.createElement("a");
            od.className = "shop-btn shop-btn--secondary";
            od.href = "order-detail.html?id=" + encodeURIComponent(out.orderId);
            od.textContent = "View order";
            actions.appendChild(od);
        }

        return out;
    }

    async function trySyncPayosPayment(out) {
        // Only do the fallback sync when we see PAID on return URL.
        // Webhook is the source of truth; this is only to avoid "paid UI but PENDING DB" in local/dev.
        if (!out || out.kind !== "success") return;
        if (!out.paymentLinkId) return;
        if (!getToken()) return; // require authenticated customer

        var payload = {
            paymentLinkId: out.paymentLinkId,
            targetStatus: "PAID"
        };

        try {
            await apiPost("/api/payos/orders/" + encodeURIComponent(out.orderId) + "/sync-return", payload);
        } catch (e) {
            // Don't break the page; just log. User can always refresh order detail later.
            console.warn("PayOS fallback sync failed:", e);
        }
    }

    async function payAgain(orderId) {
        if (!getToken()) {
            window.location.href = "login.html?next=" + encodeURIComponent(window.location.pathname + window.location.search);
            return;
        }
        try {
            var res = await apiPost("/api/orders/" + orderId + "/repay", {});
            if (res.paymentUrl) {
                window.location.href = res.paymentUrl;
            } else {
                alert("Could not start payment.");
            }
        } catch (e) {
            console.error(e);
            alert("Pay again failed: " + e.message);
        }
    }

    window.payAgain = payAgain;

    document.addEventListener("DOMContentLoaded", async function () {
        await syncAuthFromServerSession(API_BASE);
        if (typeof updateShopHeaderAuth === "function") {
            updateShopHeaderAuth();
        }
        if (typeof updateShopHeaderCart === "function") {
            updateShopHeaderCart();
        }
        var out = render();
        await trySyncPayosPayment(out);
    });
})();
