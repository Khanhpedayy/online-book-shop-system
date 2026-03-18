const params = new URLSearchParams(window.location.search);
const status = params.get("status");
const orderId = params.get("orderId");

const statusEl = document.getElementById("status");

if (status === "success") {
    statusEl.innerText = "✅ Payment Successful!";
    localStorage.removeItem("cart");
} else if (status === "cancel") {
    statusEl.innerText = "⚠️ Payment Cancelled";
} else {
    statusEl.innerText = "❌ Payment Failed";
}

async function payAgain() {
    const res = await fetch(`http://localhost:8080/api/orders/${orderId}/repay`, {
        method: "POST"
    });

    const data = await res.json();
    window.location.href = data.paymentUrl;
}