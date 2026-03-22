const API_BASE = 'http://localhost:8080';

function getToken() {
    return localStorage.getItem("token");
}

function logout() {
    localStorage.removeItem("token");
    window.location = "login.html";
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
    if (!res.ok) throw new Error(await res.text());
    return res.json();
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
                        💳 Payment: ${o.paymentMethod} (${o.paymentStatus})
                    </div>

                    <div style="margin-top:10px;">
                        <button onclick="viewOrder(${o.id})">View</button>

                        ${o.status === "PENDING" ?
                `<button onclick="cancelOrder(${o.id})">Cancel</button>` : ""
            }

                        ${o.paymentStatus === "UNPAID" && o.paymentMethod === "PAYOS" ?
                `<button onclick="repay(${o.id})">Pay Again</button>` : ""
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
    return Number(value).toLocaleString('vi-VN') + " VND";
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