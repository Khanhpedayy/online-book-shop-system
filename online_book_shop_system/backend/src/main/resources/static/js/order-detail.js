const API_BASE = 'http://localhost:8080';

function getToken(){
    return localStorage.getItem("token");
}

function getQueryParam(name){
    return new URLSearchParams(window.location.search).get(name);
}

async function apiGet(path){
    const res = await fetch(API_BASE + path,{
        headers:{
            "Authorization":"Bearer " + getToken()
        }
    });
    if(!res.ok) throw new Error(await res.text());
    return res.json();
}

async function apiPost(path){
    const res = await fetch(API_BASE + path,{
        method:"POST",
        headers:{
            "Authorization":"Bearer " + getToken()
        }
    });

    if(!res.ok) throw new Error(await res.text());

    try {
        return await res.json();
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
        renderItems(o.items);
        renderTimeline(o.status);

    }catch(e){
        console.error(e);
        document.getElementById("orderInfo").innerHTML = "Error: " + e.message;
    }
}

/* ===== ORDER INFO ===== */
function renderOrderInfo(o){
    const div = document.getElementById("orderInfo");

    div.innerHTML = `
        <h3>Order #${o.orderCode || o.id}</h3>
        <p>Status: <b>${o.status}</b></p>
        <p>Payment: ${o.paymentMethod} (${o.paymentStatus})</p>
        <p>Total: ${formatMoney(o.totalAmount)}</p>

        <hr>

        <p><b>Recipient:</b> ${o.shipName}</p>
        <p><b>Phone:</b> ${o.shipPhone}</p>
        <p><b>Address:</b> ${o.shipLine1}</p>

        <div style="margin-top:10px;">
            ${o.status === "NEW" ?
        `<button class="btn" onclick="cancelOrder(${o.id})">Cancel</button>` : ""
    }

            ${o.paymentStatus === "UNPAID" && o.paymentMethod === "PAYOS" ?
        `<button class="btn" onclick="repay(${o.id})">Pay Again</button>` : ""
    }
        </div>
    `;
}

/* ===== ITEMS ===== */
function renderItems(items){
    const container = document.getElementById("orderItems");
    container.innerHTML = "";

    if (!items || items.length === 0) {
        container.innerHTML = "<p>No items found</p>";
        return;
    }

    items.forEach(i => {
        container.innerHTML += `
            <div class="item">
                <div>
                    ${i.titleSnapshot || "Book"} <br>
                    x${i.quantity}
                </div>
                <div>
                    ${formatMoney(i.unitPrice)}
                </div>
            </div>
        `;
    });
}

/* ===== TIMELINE ===== */
function renderTimeline(status){
    const steps = ["NEW","CONFIRMED","SHIPPING","COMPLETED"];
    const container = document.getElementById("timeline");

    container.innerHTML = "";

    steps.forEach(s => {
        const active = steps.indexOf(s) <= steps.indexOf(status);

        container.innerHTML += `
            <div class="step">
                <div class="circle ${active ? "active" : ""}"></div>
                <div>${s}</div>
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
    return Number(v).toLocaleString('vi-VN') + " VND";
}

/* ===== INIT ===== */
document.addEventListener("DOMContentLoaded", () => {
    if(!getToken()){
        alert("Login first!");
        window.location = "login.html";
        return;
    }

    loadOrder();
});