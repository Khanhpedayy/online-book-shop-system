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
        renderTimeline(o);

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

function normalizeFulfillmentStatus(status) {
    return (status == null ? "" : String(status)).trim().toUpperCase();
}

const FULFILLMENT_LABELS = {
    NEW: "New — awaiting shop confirmation",
    CONFIRMED: "Confirmed — preparing your order",
    PACKED: "Packed — ready to ship",
    SHIPPED: "Shipped — on the way",
    DELIVERED: "Delivered",
    COMPLETED: "Completed",
    CANCELLED: "Cancelled"
};

function fulfillmentLabel(code) {
    const c = normalizeFulfillmentStatus(code);
    return FULFILLMENT_LABELS[c] || c || "—";
}

function isPayOsPaymentComplete(o) {
    const ps = displayPaymentStatus(o);
    return ps === "PAID";
}

function canCancelCustomerOrder(o) {
    return normalizeFulfillmentStatus(o.status) === "NEW";
}

function canRepayPayOs(o) {
    if (normalizeFulfillmentStatus(o.status) === "CANCELLED") {
        return false;
    }
    if (displayPaymentMethod(o) !== "PAYOS") {
        return false;
    }
    const ps = displayPaymentStatus(o);
    return ps === "PENDING" || ps === "UNPAID" || ps === "CANCELLED" || ps === "FAILED";
}

/* ===== ORDER INFO ===== */
function renderOrderInfo(o){
    const div = document.getElementById("orderInfo");
    const addr = [o.shipLine1, o.shipLine2].filter(Boolean).join(", ");
    const fulfillCode = normalizeFulfillmentStatus(o.status);
    const payMethod = displayPaymentMethod(o);
    const payNote =
        payMethod === "PAYOS" && !isPayOsPaymentComplete(o) && fulfillCode !== "CANCELLED"
            ? `<p class="shop-muted" style="margin-top:8px;font-size:0.9rem;">Complete online payment below. Shipment steps unlock after payment is <b>PAID</b>.</p>`
            : "";

    div.innerHTML = `
        <h3>Order #${escapeHtml(String(o.orderCode || o.id))}</h3>
        <p>Shipment / fulfillment: <b>${escapeHtml(fulfillmentLabel(o.status))}</b> <span class="shop-muted">(${escapeHtml(fulfillCode || "—")})</span></p>
        <p>Payment: <b>${escapeHtml(payMethod)}</b> (${escapeHtml(displayPaymentStatus(o))})</p>
        ${payNote}
        <p>Total: ${formatMoney(o.totalAmount)}</p>

        <hr>

        <p><b>Recipient:</b> ${escapeHtml(String(o.shipName || "—"))}</p>
        <p><b>Phone:</b> ${escapeHtml(String(o.shipPhone || "—"))}</p>
        <p><b>Address:</b> ${escapeHtml(addr || "—")}</p>

        <div style="margin-top:10px;">
            ${canCancelCustomerOrder(o) ?
        `<button type="button" class="shop-btn shop-btn--secondary" onclick="cancelOrder(${o.id})">Cancel order</button>` : ""
    }

            ${canRepayPayOs(o) ?
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
function renderTimeline(o) {
    const s = normalizeFulfillmentStatus(o && o.status);
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

    const payMethod = displayPaymentMethod(o);
    const payComplete = payMethod !== "PAYOS" || isPayOsPaymentComplete(o);
    const payFailed = displayPaymentStatus(o) === "FAILED";

    /* PayOS: payment step must align with payment_status before fulfillment steps */
    if (payMethod === "PAYOS") {
        let payCircleClass = "muted";
        let payLabel = "Payment pending";
        if (isPayOsPaymentComplete(o)) {
            payCircleClass = "active";
            payLabel = "Paid online";
        } else if (payFailed) {
            payCircleClass = "pending";
            payLabel = "Payment failed";
        } else {
            payCircleClass = "pending";
        }
        container.innerHTML += `
            <div class="step">
                <div class="circle ${payCircleClass}"></div>
                <div>${payLabel}</div>
            </div>`;
    }

    /* Align with staff workflow: NEW → CONFIRMED → PACKED → SHIPPED → DELIVERED → COMPLETED */
    const steps = [
        { code: "NEW", label: "Order placed" },
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

    steps.forEach((step, i) => {
        const active = payComplete && i <= idx;

        container.innerHTML += `
            <div class="step">
                <div class="circle ${active ? "active" : "muted"}"></div>
                <div>${step.label}</div>
            </div>
        `;
    });

    if (!payComplete && payMethod === "PAYOS") {
        container.innerHTML += `
            <p class="shop-muted" style="flex-basis:100%;width:100%;margin:12px 0 0;font-size:0.88rem;">
                Shipment steps stay inactive until payment is successful (PAID).
            </p>`;
    }
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
