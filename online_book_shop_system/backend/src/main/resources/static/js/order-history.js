const API_BASE = 'http://localhost:8080';

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

async function apiPost(path) {
    const headers = {};
    const t = getToken();
    if (t) headers['Authorization'] = 'Bearer ' + t;
    const res = await fetch(API_BASE + path, {
        method: "POST",
        headers,
        credentials: 'include'
    });
    const text = await res.text();
    if (!res.ok) throw new Error(text || res.statusText);
    if (!text.trim()) return {};
    try {
        return JSON.parse(text);
    } catch {
        return {};
    }
}

/* ===== LOAD ORDERS ===== */
async function loadOrders() {
    const container = document.getElementById("orderList");
    container.innerHTML = "Loading...";

    try {
        const keyword = document.getElementById("searchOrder").value;
        const status = document.getElementById("statusFilter").value;

        let url = `/api/orders/me`;

        const params = new URLSearchParams();
        if (status) params.append("status", status);
        if (keyword) params.append("keyword", keyword);

        if (params.toString()) {
            url += "?" + params.toString();
        }

        const orders = await apiGet(url);

        if (!orders.length) {
            container.innerHTML = "No orders found";
            return;
        }

        container.innerHTML = "";

        orders.forEach(o => {
            const date = new Date(o.placedAt || o.createdAt).toLocaleString();

            container.innerHTML += `
                <div class="order-card">
                    <div class="order-header">
                        <div>
                            <b>Order #${o.orderCode || o.id}</b><br>
                            ${date}
                        </div>

                        <div class="status ${o.status}">
                            ${o.status}
                        </div>
                    </div>

                    <div>
                        💰 Total: ${formatMoney(o.totalAmount)} <br>
                        💳 Payment: ${paymentMethodLabel(o)} (${paymentStatusLabel(o)})
                    </div>

                    <div style="margin-top:10px;">
                        <button type="button" class="shop-btn" onclick="viewOrder(${o.id})">View</button>

                        ${o.status === "NEW" ?
                `<button type="button" class="shop-btn shop-btn--secondary" onclick="cancelOrder(${o.id})">Cancel</button>` : ""
            }

                        ${payAgainEligible(o) ?
                `<button type="button" class="shop-btn" onclick="repay(${o.id})">Pay again</button>` : ""
            }
                    </div>
                </div>
            `;
        });

    } catch (e) {
        console.error(e);
        container.innerHTML = "❌ Error: " + e.message;
    }
}

/* ===== FORMAT MONEY ===== */
function formatMoney(value) {
    if (value == null || value === "") {
        return "—";
    }
    const n = Number(value);
    if (!Number.isFinite(n)) {
        return String(value);
    }
    return n.toLocaleString("vi-VN", { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + " VND";
}

function paymentMethodLabel(o) {
    const m = o && o.paymentMethod;
    if (m != null && String(m).trim() !== "") {
        return String(m).trim().toUpperCase();
    }
    return "COD";
}

function paymentStatusLabel(o) {
    const s = o && o.paymentStatus;
    if (s != null && String(s).trim() !== "") {
        return String(s).trim().toUpperCase();
    }
    return "—";
}

function payAgainEligible(o) {
    const ps = paymentStatusLabel(o);
    const pm = paymentMethodLabel(o);
    return pm === "PAYOS" && (ps === "PENDING" || ps === "UNPAID" || ps === "CANCELLED");
}

/* ===== VIEW DETAIL ===== */
function viewOrder(id) {
    window.location = "order-detail.html?id=" + id;
}

/* ===== CANCEL ===== */
async function cancelOrder(id) {
    if (!confirm("Cancel this order?")) return;

    try {
        await apiPost(`/api/orders/${id}/cancel`);
        alert("Cancelled!");
        loadOrders();
    } catch (e) {
        alert(e.message);
    }
}

/* ===== PAY AGAIN ===== */
async function repay(id) {
    try {
        const res = await apiPost(`/api/orders/${id}/repay`);
        if (res.paymentUrl) {
            window.location.href = res.paymentUrl;
        } else {
            alert("Cannot repay");
        }
    } catch (e) {
        alert(e.message);
    }
}

/* ===== INIT ===== */
document.addEventListener("DOMContentLoaded", async () => {
    await syncAuthFromServerSession(API_BASE);
    if (!getToken()) {
        alert("Please login first!");
        window.location = "login.html";
        return;
    }

    loadOrders();
});