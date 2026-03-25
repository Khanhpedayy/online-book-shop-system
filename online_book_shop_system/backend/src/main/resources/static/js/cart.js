const API_BASE = 'http://localhost:8080';

const cartContainer = document.getElementById("cartContainer");
const headerCartCount = document.getElementById('headerCartCount');

const subtotalEl = document.getElementById('cartSubtotal');
const shippingEl = document.getElementById('cartShipping');
const totalEl = document.getElementById('cartTotal');
const shippingCost = 5;

function updateHeaderCart(total, count) {
    if (!headerCartCount) return;
    headerCartCount.textContent =
        "$" +
        (total != null ? Number(total).toFixed(2) : "0.00") +
        " (" +
        (count || 0) +
        ")";
}

function isLoggedIn() {
    return !!localStorage.getItem("token");
}

async function logout() {
    await performLogout(API_BASE);
}

async function apiGet(path){
    const headers = {};

    const token = localStorage.getItem("token");
    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }

    const resp = await fetch(`${API_BASE}${path}`, { headers, credentials: 'include' });

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
        headers,
        credentials: 'include'
    });

    if (!resp.ok && resp.status !== 204) {
        const text = await resp.text();
        throw new Error(`DELETE ${path} failed: ${resp.status} ${text}`);
    }
}

function updateCartTotals(subtotal) {
    const total = subtotal + shippingCost;

    subtotalEl.textContent = subtotal.toFixed(2);
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
        let quantityCount = 0;
        cartContainer.innerHTML = '';

        items.forEach(ci => {
            const v = ci.variant;
            const variantId = ci.variantId ?? v?.id;
            const title = v?.book?.title ?? v?.sku ?? '(unknown)';
            const price = v?.salePrice ?? 0;
            const lineTotal = price * ci.quantity;
            subtotal += lineTotal;
            quantityCount += ci.quantity || 0;

            const div = document.createElement('div');
            div.className = 'cart-item';
            div.innerHTML = `
                <span>${title.replace(/</g, '&lt;')} × ${ci.quantity} @ $${price.toFixed(2)} = $${lineTotal.toFixed(2)}</span>
                <button type="button" class="btn btn-secondary" data-remove="${variantId != null ? variantId : ''}">Remove</button>
            `;
            cartContainer.appendChild(div);
        });

        // remove item
        cartContainer.querySelectorAll('button[data-remove]').forEach(btn => {
            btn.addEventListener('click', async () => {
                const raw = btn.getAttribute('data-remove');
                const variantId = Number(raw);
                if (!raw || !Number.isFinite(variantId) || variantId <= 0) {
                    alert('Không xác định được sản phẩm trong giỏ (variantId). Hãy tải lại trang.');
                    return;
                }
                try {
                    await apiDelete(`/api/cart/items/${variantId}`);
                    await loadCart();
                } catch (e) {
                    console.error(e);
                    alert(e.message);
                }
            });
        });

        updateHeaderCart(subtotal, quantityCount);
        updateCartTotals(subtotal);

    } catch (e) {
        console.error(e);
        cartContainer.innerHTML = 'Error loading cart: ' + e.message;
    }
}

function goToCheckout() {
    window.location.href = "checkout.html";
}

document.addEventListener("DOMContentLoaded", async () => {
    await syncAuthFromServerSession(API_BASE);
    if (typeof updateShopHeaderAuth === "function") {
        updateShopHeaderAuth();
    }
    if (!isLoggedIn()) {
        alert("Please login first!");
        window.location = "login.html";
        return;
    }

    loadCart();
});