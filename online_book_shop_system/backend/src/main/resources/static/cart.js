const API_BASE = 'http://localhost:8080';
const USER_ID = 1;

const cartContainer = document.getElementById("cartContainer");
const headerCartCount = document.getElementById('headerCartCount');

function updateHeaderCart(total, count) {
    if (!headerCartCount) return;
    headerCartCount.textContent =
        '$ ' + (total != null ? Number(total).toFixed(2) : '0.00') +
        ' (' + (count || 0) + ')';
}

async function apiGet(path){
    const resp = await fetch(`${API_BASE}${path}`);
    if(!resp.ok) throw new Error(await resp.text());
    return resp.json();
}

async function apiDelete(path) {
    const resp = await fetch(`${API_BASE}${path}`, {
        method: "DELETE"
    });

    if (!resp.ok && resp.status !== 204) {
        const text = await resp.text();
        throw new Error(`DELETE ${path} failed: ${resp.status} ${text}`);
    }
}

async function loadCart() {
    if (cartContainer) cartContainer.innerHTML = 'Loading…';

    try {
        const items = await apiGet(`/api/cart/user/${USER_ID}`);

        if (!Array.isArray(items) || items.length === 0) {
            if (cartContainer) cartContainer.innerHTML = 'Your cart is empty.';
            updateHeaderCart(0, 0);
            return;
        }

        let total = 0;
        cartContainer.innerHTML = '';

        items.forEach((ci) => {
            const v = ci.variant;
            const title = v?.book?.title ?? v?.sku ?? '(unknown)';
            const price = v?.salePrice ?? 0;
            const lineTotal = price * ci.quantity;
            total += lineTotal;

            const div = document.createElement('div');
            div.className = 'cart-item';
            div.innerHTML = `
                <span>${title.replace(/</g, '&lt;')} × ${ci.quantity} @ $${Number(price).toFixed(2)} = $${lineTotal.toFixed(2)}</span>
                <button type="button" class="btn btn-secondary" data-remove="${v?.id}">Remove</button>
            `;

            cartContainer.appendChild(div);
        });

        // remove item
        cartContainer.querySelectorAll('button[data-remove]').forEach((btn) => {
            btn.addEventListener('click', async () => {
                const variantId = Number(btn.getAttribute('data-remove'));
                try {
                    await apiDelete(`/api/cart/user/${USER_ID}/items/${variantId}`);
                    await loadCart();
                } catch (e) {
                    console.error(e);
                    alert(e.message);
                }
            });
        });

        updateHeaderCart(total, items.length);

    } catch (e) {
        console.error(e);
        cartContainer.innerHTML = 'Error loading cart: ' + e.message;
    }
}

function goToCheckout() {
    window.location.href = "checkout.html";
}

// load khi vào trang
document.addEventListener("DOMContentLoaded", () => {
    loadCart();
});