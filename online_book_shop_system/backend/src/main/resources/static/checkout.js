const API_BASE = 'http://localhost:8080';
const USER_ID = 1;

const shippingCost = 5; // fixed shipping cost
const vouchers = {
    "SAVE10": 10,       // $10 off
    "HALFPRICE": 0.5,   // 50% off
};
let appliedVoucher = null;
let subtotal = 0;
let discountAmount = 0;

// ===== DOM =====
const orderForm = document.getElementById("orderForm");
const orderResult = document.getElementById("orderResult");
const summaryDiv = document.getElementById("checkoutSummary");

const emailInput = document.getElementById("orderEmail");
const addressInput = document.getElementById("orderAddress");
const recipientInput = document.getElementById("orderRecipient");
const phoneInput = document.getElementById("orderPhone");
const paymentMethodSelect = document.getElementById("paymentMethod");

const voucherInput = document.getElementById("voucherCode"); // new field
const applyVoucherBtn = document.getElementById("applyVoucherBtn");
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

// ===== UPDATE TOTALS =====
function updateTotals() {
    discountAmount = 0;
    if (appliedVoucher) {
        const value = vouchers[appliedVoucher];
        discountAmount = value < 1 ? subtotal * value : value;
    }
    const total = subtotal - discountAmount + shippingCost;

    summaryDiv.querySelector("#subtotalVal").textContent = subtotal.toFixed(2);
    summaryDiv.querySelector("#discountVal").textContent = discountAmount.toFixed(2);
    summaryDiv.querySelector("#shippingVal").textContent = shippingCost.toFixed(2);
    summaryDiv.querySelector("#totalVal").textContent = total.toFixed(2);
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

        subtotal = 0;
        let html = "";

        items.forEach(ci => {
            const v = ci.variant;
            const book = v?.book;

            const title = book?.title || v?.sku || "Unknown";
            const price = v?.salePrice || 0;
            const qty = ci.quantity;
            const lineTotal = price * qty;
            subtotal += lineTotal;

            html += `
                <div class="item">
                    <div><b>${title}</b><br>${qty} x $${price.toFixed(2)}</div>
                    <div>$${lineTotal.toFixed(2)}</div>
                </div>
            `;
        });

        html += `
            <hr>
            <div><span>Subtotal:</span> $<span id="subtotalVal">${subtotal.toFixed(2)}</span></div>
            // <div><span>Discount:</span> -$<span id="discountVal">0.00</span></div>
            // <div><span>Shipping:</span> $<span id="shippingVal">${shippingCost.toFixed(2)}</span></div>
            <div><strong>Total:</strong> $<span id="totalVal">${subtotal.toFixed(2)}</span></div>
        `;
        summaryDiv.innerHTML = html;

        updateTotals();

    } catch (err) {
        console.error(err);
        summaryDiv.innerHTML = "❌ Failed to load cart: " + err.message;
    }
}

// ===== APPLY VOUCHER =====
applyVoucherBtn?.addEventListener("click", () => {
    const code = voucherInput.value.trim().toUpperCase();
    if (!code) { alert("Enter a voucher code!"); return; }

    if (vouchers[code]) {
        appliedVoucher = code;
        alert("Voucher applied!");
    } else {
        appliedVoucher = null;
        alert("Invalid voucher code");
    }
    updateTotals();
});

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
                paymentMethod: paymentMethodSelect.value,
                // voucherCode: appliedVoucher || null,
                // discountAmount: discountAmount
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