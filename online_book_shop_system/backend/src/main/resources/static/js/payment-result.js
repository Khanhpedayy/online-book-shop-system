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
        var payosCancel = params.get("payosCancel") === "1";
        var code = params.get("code");
        var cancel = params.get("cancel") === "true";
        var status = (params.get("status") || "").toUpperCase();

        if (!orderId) {
            return { kind: "invalid", orderId: null, message: "Missing order reference." };
        }

        if (payosCancel || cancel || status === "CANCELLED") {
            return { kind: "cancelled", orderId: orderId, message: "Payment was cancelled." };
        }

        if (code && code !== "00") {
            return { kind: "failed", orderId: orderId, message: "Payment failed (code " + code + ")." };
        }

        if (status === "PAID") {
            return { kind: "success", orderId: orderId, message: "Payment completed successfully." };
        }

        if (status === "PENDING" || status === "PROCESSING") {
            return {
                kind: "pending",
                orderId: orderId,
                message: "Payment is still pending or processing. Refresh your order later or try again."
            };
        }

        if (!code && !status && !cancel) {
            return {
                kind: "unknown",
                orderId: orderId,
                message: "No payment status in the URL. Open your order from Orders if you finished paying."
            };
        }

        return { kind: "failed", orderId: orderId, message: "Payment was not completed." };
    }

    function render() {
        var box = document.getElementById("paymentResult");
        if (!box) {
            return;
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
            return;
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
            return;
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
        render();
    });
})();
