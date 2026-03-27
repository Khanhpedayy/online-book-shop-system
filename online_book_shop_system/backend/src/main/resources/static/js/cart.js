const API_BASE = 'http://localhost:8080';

const cartContainer = document.getElementById("cartContainer");
const headerCartCount = document.getElementById('headerCartCount');

const subtotalEl = document.getElementById('cartSubtotal');
const shippingEl = document.getElementById('cartShipping');
const totalEl = document.getElementById('cartTotal');
const shippingCost = 5000;

function formatVnd(value) {
    const n = Number(value) || 0;
    return new Intl.NumberFormat('vi-VN').format(Math.round(n));
}

function updateHeaderCart(total, count) {
    if (!headerCartCount) return;
    headerCartCount.textContent = `${formatVnd(total)}₫ (${count || 0})`;
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

async function apiPut(path, data) {
    const headers = { "Content-Type": "application/json" };
    const token = localStorage.getItem("token");
    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }
    const resp = await fetch(`${API_BASE}${path}`, {
        method: "PUT",
        headers,
        credentials: "include",
        body: JSON.stringify(data)
    });
    if (!resp.ok) throw new Error(await resp.text());
    return resp.json();
}

function updateCartTotals(subtotal) {
    const total = subtotal + shippingCost;

    subtotalEl.textContent = formatVnd(subtotal);
    shippingEl.textContent = formatVnd(shippingCost);
    totalEl.textContent = formatVnd(total);
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
            const variantId = ci.variantId;
            const title = ci.title ?? '(unknown)';
            const price = Number(ci.salePrice ?? 0);
            const qty = Number(ci.quantity || 0);
            const lineTotal = price * qty;
            subtotal += lineTotal;
            quantityCount += qty;

            const cover =
                ci.coverImageUrl ||
                "https://via.placeholder.com/60x80?text=Book";

            const div = document.createElement('div');
            div.className = 'cart-item';
            div.innerHTML = `
                <img class="cart-img" src="${cover}" alt="">
                <div class="cart-info">
                    <div class="cart-title">${title.replace(/</g, '&lt;')}</div>
                    <div class="cart-copy">${ci.sku ? String(ci.sku).replace(/</g, '&lt;') : ''}</div>
                </div>
                <div class="cart-quantity">
                    <input type="number" min="1" value="${qty || 1}" data-qty="${variantId != null ? variantId : ''}">
                </div>
                <div class="cart-price">${formatVnd(lineTotal)}₫</div>
                <button type="button" data-remove="${variantId != null ? variantId : ''}">Remove</button>
            `;
            cartContainer.appendChild(div);
        });

        // update quantity
        cartContainer.querySelectorAll('input[data-qty]').forEach(inp => {
            inp.addEventListener('change', async () => {
                const raw = inp.getAttribute('data-qty');
                const variantId = Number(raw);
                const quantity = Number(inp.value || 1);
                if (!raw || !Number.isFinite(variantId) || variantId <= 0) {
                    alert('Không xác định được sản phẩm trong giỏ (variantId). Hãy tải lại trang.');
                    return;
                }
                if (!Number.isFinite(quantity) || quantity <= 0) {
                    inp.value = "1";
                    return;
                }
                try {
                    await apiPut(`/api/cart/items/${variantId}`, { quantity });
                    await loadCart();
                } catch (e) {
                    console.error(e);
                    alert(e.message);
                    await loadCart();
                }
            });
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