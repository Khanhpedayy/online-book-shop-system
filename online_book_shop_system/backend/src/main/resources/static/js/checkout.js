const API_BASE = 'http://localhost:8080';

let subtotal = 0;
let shippingFeeQuote = 0;

function formatVnd(value) {
    const n = Number(value) || 0;
    return new Intl.NumberFormat("vi-VN").format(Math.round(n));
}

// ===== DOM =====
const orderForm = document.getElementById("orderForm");
const orderResult = document.getElementById("orderResult");
const summaryDiv = document.getElementById("checkoutSummary");

const emailInput = document.getElementById("orderEmail");
const addressInput = document.getElementById("orderAddress");
const recipientInput = document.getElementById("orderRecipient");
const phoneInput = document.getElementById("orderPhone");
const paymentMethodSelect = document.getElementById("paymentMethod");
// ===== API =====
function getToken() {
    return localStorage.getItem("token");
}
async function apiGet(path) {
    const headers = {};
    const t = getToken();
    if (t) headers['Authorization'] = 'Bearer ' + t;
    const res = await fetch(API_BASE + path, {
        headers,
        credentials: 'include'
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
}

async function apiPost(path, body){
    const headers = { 'Content-Type': 'application/json' };
    const t = getToken();
    if (t) headers['Authorization'] = 'Bearer ' + t;
    const res = await fetch(API_BASE + path,{
        method:'POST',
        headers,
        credentials: 'include',
        body:JSON.stringify(body)
    });
    if(!res.ok) throw new Error(await res.text());
    return res.json();
}


async function logout() {
    await performLogout(API_BASE);
}

// ===== UPDATE TOTALS =====
function updateTotals() {
    const total = subtotal + shippingFeeQuote;

    summaryDiv.querySelector("#subtotalVal").textContent = formatVnd(subtotal);
    const shipEl = summaryDiv.querySelector("#shippingVal");
    if (shipEl) {
        if (shippingFeeQuote <= 0 && subtotal > 0) {
            shipEl.textContent = "Miễn phí";
        } else {
            shipEl.textContent = formatVnd(shippingFeeQuote) + "₫";
        }
    }
    summaryDiv.querySelector("#totalVal").textContent = formatVnd(total);
}

// ===== LOAD SUMMARY =====
async function loadCheckoutSummary() {
    if (!summaryDiv) return;
    summaryDiv.innerHTML = "Loading...";

    try {
        const items = await apiGet(`/api/cart/me`);
        if (!items || items.length === 0) {
            summaryDiv.innerHTML = "Cart is empty.";
            return;
        }

        subtotal = 0;
        let html = "";

        items.forEach(ci => {
            const title = ci.title || ci.sku || "Unknown";
            const price = Number(ci.salePrice || 0);
            const qty = ci.quantity;
            const lineTotal = price * qty;
            subtotal += lineTotal;

            html += `
                <div class="item">
                    <div><b>${title}</b><br>${qty} x ${formatVnd(price)}₫</div>
                    <div>${formatVnd(lineTotal)}₫</div>
                </div>
            `;
        });

        try {
            const quote = await apiGet(`/api/shipping/quote?subtotal=${encodeURIComponent(subtotal)}`);
            shippingFeeQuote = Number(quote.shippingFee) || 0;
        } catch (e) {
            console.warn("shipping quote", e);
            shippingFeeQuote = 0;
        }

        html += `
            <hr>
            <div><span>Subtotal:</span> <span id="subtotalVal"></span>₫</div>
            <div><span>Shipping:</span> <span id="shippingVal"></span></div>
            <div><strong>Total:</strong> <span id="totalVal"></span>₫</div>
        `;
        summaryDiv.innerHTML = html;

        updateTotals();

    } catch (err) {
        console.error(err);
        summaryDiv.innerHTML = "❌ Failed to load cart: " + err.message;
    }
}

// ===== SUBMIT ORDER (SINGLE HANDLER) =====
if (orderForm) {
    orderForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        orderResult.className = "";
        orderResult.textContent = "Placing order...";

        try {
            const body = {
                email: emailInput.value,
                shippingAddress: addressInput.value,
                recipientName: recipientInput.value,
                phone: phoneInput.value,
                paymentMethod: paymentMethodSelect.value,
            };

            // 🔥 QUAN TRỌNG: dùng API đúng
            const res = await apiPost(`/api/orders/from-cart`, body);
            // ===== COD =====
            if (!res.paymentUrl) {
                orderResult.textContent =
                    `✅ Order #${res.orderId} placed successfully (COD)`;
                orderResult.className = "success";

                await loadCheckoutSummary(); // refresh cart
                return;
            }

            // ===== PAYOS =====
            orderResult.textContent = "Redirecting to payment...";
            window.location.href = res.paymentUrl;

        } catch (err) {
            console.error(err);
            orderResult.textContent = "❌ " + err.message;
            orderResult.className = "error";
        }
    });
}

// ===== INIT =====
document.addEventListener("DOMContentLoaded", async () => {
    await syncAuthFromServerSession(API_BASE);
    if (typeof updateShopHeaderAuth === "function") {
        updateShopHeaderAuth();
    }
    loadCheckoutSummary();
});