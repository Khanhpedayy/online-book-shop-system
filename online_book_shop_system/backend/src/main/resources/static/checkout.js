const API_BASE = 'http://localhost:8080';
const USER_ID = 1;

// ===== DOM =====
const orderForm = document.getElementById("orderForm");
const orderResult = document.getElementById("orderResult");
const summaryDiv = document.getElementById("checkoutSummary");

const emailInput = document.getElementById("orderEmail");
const addressInput = document.getElementById("orderAddress");
const recipientInput = document.getElementById("orderRecipient");
const phoneInput = document.getElementById("orderPhone");
const noteInput = document.getElementById("orderNote");
const paymentMethodSelect = document.getElementById("paymentMethod");

// ===== API =====
async function apiGet(path) {
    const res = await fetch(API_BASE + path);
    if (!res.ok) throw new Error(await res.text());
    return res.json();
}

async function apiPost(path, body){
    const res = await fetch(API_BASE + path,{
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify(body)
    });
    if(!res.ok) throw new Error(await res.text());
    return res.json();
}

// ===== LOAD SUMMARY =====
async function loadCheckoutSummary() {
    if (!summaryDiv) return;

    summaryDiv.innerHTML = "Loading...";

    try {
        const items = await apiGet(`/api/cart/user/${USER_ID}`);

        if (!items || items.length === 0) {
            summaryDiv.innerHTML = "Cart is empty.";
            return;
        }

        let total = 0;
        let html = "";

        items.forEach(ci => {
            const v = ci.variant;
            const book = v?.book;

            const title = book?.title || v?.sku || "Unknown";
            const price = v?.salePrice || 0;
            const qty = ci.quantity;

            const lineTotal = price * qty;
            total += lineTotal;

            html += `
                <div class="item">
                    <div>
                        <b>${title}</b><br>
                        ${qty} x $${price.toFixed(2)}
                    </div>
                    <div>$${lineTotal.toFixed(2)}</div>
                </div>
            `;
        });

        html += `<hr><b>Total: $${total.toFixed(2)}</b>`;
        summaryDiv.innerHTML = html;

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
                customerId: USER_ID,
                email: emailInput.value,
                shippingAddress: addressInput.value,
                recipientName: recipientInput.value,
                phone: phoneInput.value,
                paymentMethod: paymentMethodSelect.value
            };

            // 🔥 QUAN TRỌNG: dùng API đúng
            const res = await apiPost(`/api/orders/from-cart/${USER_ID}`, body);

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

// ===== PAYMENT RESULT =====
function getQueryParam(name) {
    return new URLSearchParams(window.location.search).get(name);
}

function handlePaymentResult() {
    const orderId = getQueryParam("orderId");
    const status = getQueryParam("status");

    const resultBox = document.getElementById("paymentResult");
    if (!resultBox) return;

    if (!orderId) {
        resultBox.innerHTML = "Invalid payment result.";
        return;
    }

    if (status === "success") {
        resultBox.innerHTML = `✅ Payment successful for Order #${orderId}`;
        return;
    }

    if (status === "failed" || status === "cancelled") {
        resultBox.innerHTML = `
            ❌ Payment ${status} for Order #${orderId}
            <br><br>
            <button onclick="payAgain(${orderId})">Pay Again</button>
        `;
    }
}

// ===== PAY AGAIN =====
async function payAgain(orderId) {
    try {
        const res = await apiPost(`/api/orders/${orderId}/repay`, {});

        if (res.paymentUrl) {
            window.location.href = res.paymentUrl;
        } else {
            alert("Cannot retry payment");
        }
    } catch (e) {
        console.error(e);
        alert("Error retrying payment: " + e.message);
    }
}

// ===== INIT =====
document.addEventListener("DOMContentLoaded", () => {
    loadCheckoutSummary();
    handlePaymentResult();
});