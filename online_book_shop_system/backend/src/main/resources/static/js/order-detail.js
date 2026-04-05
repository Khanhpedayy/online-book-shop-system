const API_BASE = 'http://localhost:8080';

function getToken(){
    return localStorage.getItem("token");
}

function getQueryParam(name){
    return new URLSearchParams(window.location.search).get(name);
}

async function apiGet(path){
    const headers = {};
    const t = getToken();
    if (t) headers['Authorization'] = 'Bearer ' + t;
    const res = await fetch(API_BASE + path,{
        headers,
        credentials: 'include'
    });
    if(!res.ok) throw new Error(await res.text());
    return res.json();
}

async function apiPost(path){
    const headers = {};
    const t = getToken();
    if (t) headers['Authorization'] = 'Bearer ' + t;
    const res = await fetch(API_BASE + path,{
        method:"POST",
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

/* ===== LOAD DETAIL ===== */
async function loadOrder(){
    const id = getQueryParam("id");

    if(!id){
        alert("Invalid order");
        return;
    }

    try{
        const o = await apiGet(`/api/orders/${id}/me`);

        renderOrderInfo(o);
        const rawItems = o.items != null ? o.items : [];
        renderItems(rawItems);
        renderTimeline(o.status);

    }catch(e){
        console.error(e);
        document.getElementById("orderInfo").innerHTML = "Error: " + e.message;
    }
}

function displayPaymentMethod(o) {
    const m = o.paymentMethod;
    if (m != null && String(m).trim() !== "") {
        return String(m).trim().toUpperCase();
    }
    return "COD";
}

function displayPaymentStatus(o) {
    const s = o.paymentStatus;
    if (s != null && String(s).trim() !== "") {
        return String(s).trim().toUpperCase();
    }
    return "—";
}

/* ===== ORDER INFO ===== */
function renderOrderInfo(o){
    const div = document.getElementById("orderInfo");
    const addr = [o.shipLine1, o.shipLine2].filter(Boolean).join(", ");

    div.innerHTML = `
        <h3>Order #${escapeHtml(String(o.orderCode || o.id))}</h3>
        <p>Status: <b>${escapeHtml(String(o.status || "—"))}</b></p>
        <p>Payment: <b>${escapeHtml(displayPaymentMethod(o))}</b> (${escapeHtml(displayPaymentStatus(o))})</p>
        <p>Total: ${formatMoney(o.totalAmount)}</p>

        <hr>

        <p><b>Recipient:</b> ${escapeHtml(String(o.shipName || "—"))}</p>
        <p><b>Phone:</b> ${escapeHtml(String(o.shipPhone || "—"))}</p>
        <p><b>Address:</b> ${escapeHtml(addr || "—")}</p>

        <div style="margin-top:10px;">
            ${o.status === "NEW" ?
        `<button type="button" class="shop-btn shop-btn--secondary" onclick="cancelOrder(${o.id})">Cancel order</button>` : ""
    }

            ${String(o.status || "").toUpperCase() !== "CANCELLED"
        && (displayPaymentStatus(o) === "PENDING" || displayPaymentStatus(o) === "UNPAID" || displayPaymentStatus(o) === "CANCELLED")
        && displayPaymentMethod(o) === "PAYOS" ?
        `<button type="button" class="shop-btn" onclick="repay(${o.id})">Pay again</button>` : ""
    }
        </div>
    `;
}

function escapeHtml(s) {
    if (s == null) return "";
    return String(s)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}

/* ===== ITEMS ===== */
function renderItems(items){
    const container = document.getElementById("orderItems");
    container.innerHTML = "";

    if (!items || items.length === 0) {
        container.innerHTML = "<p>No line items on this order.</p>";
        return;
    }

    items.forEach(i => {
        const title = i.titleSnapshot || (i.book && i.book.title) || (i.variant && i.variant.sku) || "Item";
        const qty = i.quantity != null ? i.quantity : 0;
        const unit = i.unitPrice;
        container.innerHTML += `
            <div class="item">
                <div>
                    ${escapeHtml(String(title))} <br>
                    <span class="shop-muted">×${qty}</span>
                </div>
                <div>
                    ${formatMoney(unit)} <br>
                    <span class="shop-muted">Line: ${formatMoney(lineTotal(i))}</span>
                </div>
            </div>
        `;
    });
}

function lineTotal(i) {
    if (i.lineTotal != null && i.lineTotal !== "") {
        return i.lineTotal;
    }
    const u = Number(i.unitPrice);
    const q = Number(i.quantity) || 0;
    if (Number.isFinite(u) && Number.isFinite(q)) {
        return u * q;
    }
    return null;
}

/* ===== TIMELINE ===== */
function renderTimeline(status){
    const s = (status || "").toString().toUpperCase();
    const container = document.getElementById("timeline");

    container.innerHTML = "";

    if (s === "CANCELLED") {
        container.innerHTML = `
            <div class="step" style="flex:1;">
                <div class="circle active" style="background:#b91c1c;"></div>
                <div>Cancelled</div>
            </div>`;
        return;
    }

    /* Align with staff workflow: NEW → CONFIRMED → PACKED → SHIPPED → DELIVERED → COMPLETED */
    const steps = [
        { code: "NEW", label: "New" },
        { code: "CONFIRMED", label: "Confirmed" },
        { code: "PACKED", label: "Packed" },
        { code: "SHIPPED", label: "Shipping" },
        { code: "DELIVERED", label: "Delivered" },
        { code: "COMPLETED", label: "Completed" }
    ];
    const orderCodes = steps.map(x => x.code);
    let idx = orderCodes.indexOf(s);
    if (idx < 0) {
        idx = 0;
    }
    const activeIdx = idx;

    steps.forEach((step, i) => {
        const active = i <= activeIdx;

        container.innerHTML += `
            <div class="step">
                <div class="circle ${active ? "active" : ""}"></div>
                <div>${step.label}</div>
            </div>
        `;
    });
}

/* ===== CANCEL ===== */
async function cancelOrder(id){
    if(!confirm("Cancel this order?")) return;

    try{
        await apiPost(`/api/orders/${id}/cancel`);
        alert("Cancelled!");
        loadOrder();
    }catch(e){
        alert(e.message);
    }
}

/* ===== REPAY ===== */
async function repay(id){
    try{
        const res = await apiPost(`/api/orders/${id}/repay`);
        if(res.paymentUrl){
            window.location.href = res.paymentUrl;
        }
    }catch(e){
        alert(e.message);
    }
}

/* ===== UTIL ===== */
function formatMoney(v){
    if (v == null || v === "") {
        return "—";
    }
    const n = Number(v);
    if (!Number.isFinite(n)) {
        return String(v);
    }
    return n.toLocaleString("vi-VN", { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + " VND";
}

/* ===== INIT ===== */
document.addEventListener("DOMContentLoaded", async () => {
    await syncAuthFromServerSession(API_BASE);
    if(!getToken()){
        alert("Login first!");
        window.location = "login.html";
        return;
    }

    loadOrder();
});
