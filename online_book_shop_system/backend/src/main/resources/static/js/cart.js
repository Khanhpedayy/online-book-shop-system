const API_BASE = 'http://localhost:8080';

const cartContainer = document.getElementById("cartContainer");
const headerCartCount = document.getElementById('headerCartCount');

const subtotalEl = document.getElementById('cartSubtotal');
const discountEl = document.getElementById('cartDiscount');
const shippingEl = document.getElementById('cartShipping');
const totalEl = document.getElementById('cartTotal');
const voucherInput = document.getElementById('voucherCode');
const applyVoucherBtn = document.getElementById('applyVoucherBtn');

let appliedVoucher = null;
const shippingCost = 5;
const vouchers = {
    "SAVE10": 10,       // $10 off
    "HALFPRICE": 0.5,   // 50% off
};

console.log("TOKEN:", localStorage.getItem("token"));

function updateHeaderCart(total, count) {
    if (!headerCartCount) return;
    headerCartCount.textContent =
        '$ ' + (total != null ? Number(total).toFixed(2) : '0.00') +
        ' (' + (count || 0) + ')';
}

function isLoggedIn() {
    return !!localStorage.getItem("token");
}

async function apiGet(path){
    const headers = {};

    const token = localStorage.getItem("token");
    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }

    const resp = await fetch(`${API_BASE}${path}`, { headers });

    if(!resp.ok) throw new Error(await resp.text());
    return resp.json();
}

async function apiDelete(path) {
    const headers = {};

    const token = localStorage.getItem("token");
    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }

    const resp = await fetch(`${API_BASE}${path}`, {
        method: "DELETE",
        headers
    });

    if (!resp.ok && resp.status !== 204) {
        const text = await resp.text();
        throw new Error(`DELETE ${path} failed: ${resp.status} ${text}`);
    }
}

function updateCartTotals(subtotal) {
    let discount = 0;
    if (appliedVoucher) {
        const value = vouchers[appliedVoucher];
        if (value < 1) discount = subtotal * value; // percentage
        else discount = value;                      // fixed
    }
    const total = subtotal - discount + shippingCost;

    subtotalEl.textContent = subtotal.toFixed(2);
    discountEl.textContent = discount.toFixed(2);
    shippingEl.textContent = shippingCost.toFixed(2);
    totalEl.textContent = total.toFixed(2);
}

async function loadCart() {
    if (cartContainer) cartContainer.innerHTML = 'Loading…';

    try {
        const items = await apiGet(`/api/cart/me`);

        if (!Array.isArray(items) || items.length === 0) {
            cartContainer.innerHTML = 'Your cart is empty.';
            updateHeaderCart(0, 0);
            updateCartTotals(0);
            return;
        }

        let subtotal = 0;
        cartContainer.innerHTML = '';

        items.forEach(ci => {
            const v = ci.variant;
            const title = v?.book?.title ?? v?.sku ?? '(unknown)';
            const price = v?.salePrice ?? 0;
            const lineTotal = price * ci.quantity;
            subtotal += lineTotal;

            const div = document.createElement('div');
            div.className = 'cart-item';
            div.innerHTML = `
                <span>${title.replace(/</g, '&lt;')} × ${ci.quantity} @ $${price.toFixed(2)} = $${lineTotal.toFixed(2)}</span>
                <button type="button" class="btn btn-secondary" data-remove="${v?.id}">Remove</button>
            `;
            cartContainer.appendChild(div);
        });

        // remove item
        cartContainer.querySelectorAll('button[data-remove]').forEach(btn => {
            btn.addEventListener('click', async () => {
                const variantId = Number(btn.getAttribute('data-remove'));
                try {
                    await apiDelete(`/api/cart/items/${variantId}`);
                    await loadCart();
                } catch (e) {
                    console.error(e);
                    alert(e.message);
                }
            });
        });

        updateHeaderCart(subtotal, items.length);
        updateCartTotals(subtotal);

    } catch (e) {
        console.error(e);
        cartContainer.innerHTML = 'Error loading cart: ' + e.message;
    }
}

applyVoucherBtn?.addEventListener('click', () => {
    const code = voucherInput.value.trim().toUpperCase();
    if (!code) {
        alert("Enter a voucher code!");
        return;
    }
    if (vouchers[code]) {
        appliedVoucher = code;
        alert("Voucher applied!");
    } else {
        appliedVoucher = null;
        alert("Invalid voucher code");
    }
    const subtotal = parseFloat(subtotalEl.textContent) || 0;
    updateCartTotals(subtotal);
});

function goToCheckout() {
    window.location.href = "checkout.html";
}

document.addEventListener("DOMContentLoaded", () => {
    if (!isLoggedIn()) {
        alert("Please login first!");
        window.location = "login.html";
        return;
    }

    loadCart();
});