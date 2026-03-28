const API_BASE = "http://localhost:8080";
const API = API_BASE + "/api";

function formatVnd(value) {
    const n = Number(value) || 0;
    return new Intl.NumberFormat("vi-VN").format(Math.round(n)) + "₫";
}

function getToken() {
    return localStorage.getItem("token");
}

async function apiGet(path) {
    const headers = {};
    const t = getToken();
    if (t) {
        headers["Authorization"] = "Bearer " + t;
    }
    const r = await fetch(path, { headers, credentials: "include" });
    if (!r.ok) {
        throw new Error(await r.text());
    }
    return r.json();
}

async function apiPost(path, data) {
    const headers = { "Content-Type": "application/json" };
    const t = getToken();
    if (t) {
        headers["Authorization"] = "Bearer " + t;
    }
    const r = await fetch(path, {
        method: "POST",
        headers,
        credentials: "include",
        body: JSON.stringify(data)
    });
    if (!r.ok) {
        throw new Error(await r.text());
    }
    return r.json();
}

async function loadBook() {
    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    if (!id) {
        return;
    }

    const book = await apiGet(API + "/books/" + id);
    document.getElementById("title").textContent = book.title;
    document.getElementById("breadcrumbTitle").textContent = book.title;
    document.getElementById("isbn").textContent = book.isbn13 || book.isbn10 || "N/A";
    document.getElementById("publisherName").textContent = book.publisherName || "N/A";
    document.getElementById("publicationYear").textContent = book.publicationYear || "N/A";
    document.getElementById("description").textContent = book.description || "";

    const variants = book.variants || [];
    const variantSelect = document.getElementById("variantSelect");
    variantSelect.innerHTML = "";

    let selectedVariant = null;
    variants.forEach(v => {
        const option = document.createElement("option");
        option.value = v.id;
        option.textContent = (v.sku || "SKU") + " — " + formatVnd(v.salePrice || 0);
        variantSelect.appendChild(option);
        if (!selectedVariant) {
            selectedVariant = v;
        }
    });

    if (selectedVariant) {
        document.getElementById("price").textContent = formatVnd(selectedVariant.salePrice || 0);
        const sq = selectedVariant.stockQuantity;
        document.getElementById("stockStatus").textContent =
            sq != null && sq > 0 ? "In stock (" + sq + ")" : "In stock";
    }

    variantSelect.onchange = () => {
        const v = variants.find(x => String(x.id) === variantSelect.value);
        if (v) {
            document.getElementById("price").textContent = formatVnd(v.salePrice || 0);
        }
    };

    const img = book.coverImageUrl ||
        ("https://covers.openlibrary.org/b/isbn/" +
            (book.isbn13 || "0385533229") +
            "-L.jpg");
    document.getElementById("bookImage").src = img;

    document.getElementById("addCartBtn").onclick = addToCart;

    await loadRelated(book.id);
}

async function addToCart() {
    await syncAuthFromServerSession(API_BASE);
    if (!getToken()) {
        alert("Please login first.");
        window.location.href = "login.html";
        return;
    }
    const variantId = document.getElementById("variantSelect").value;
    const qty = document.getElementById("qty").value;
    if (!variantId) {
        alert("Please select a format.");
        return;
    }
    try {
        await apiPost(API + "/cart/items", {
            variantId: Number(variantId),
            quantity: Number(qty) || 1
        });
        alert("Added to cart");
        await loadCartCount();
    } catch (e) {
        alert(e.message || "Could not add to cart");
    }
}

async function loadCartCount() {
    if (typeof updateShopHeaderCart === "function") {
        await updateShopHeaderCart();
    }
}

async function loadRelated(currentBookId) {
    const grid = document.getElementById("relatedBooks");
    if (!grid) {
        return;
    }
    grid.innerHTML = "";
    try {
        const books = await apiGet(API + "/books");
        const seen = new Set();
        const picks = [];
        for (const b of books) {
            const bid = b.bookId;
            if (bid == null || bid === Number(currentBookId) || seen.has(bid)) {
                continue;
            }
            seen.add(bid);
            picks.push(b);
            if (picks.length >= 4) {
                break;
            }
        }
        picks.forEach(b => {
            const bid = b.bookId;
            const card = document.createElement("div");
            card.className = "card shop-card";
            const cardImage = b.coverImageUrl || ("https://covers.openlibrary.org/b/isbn/" + (b.isbn || "0385533229") + "-M.jpg");
            card.innerHTML =
                "<img src=\"" + cardImage + "\" alt=\"\">" +
                "<p style=\"margin:8px 0 0;font-size:0.9rem;font-weight:600;\">" +
                String(b.title || "").replace(/</g, "&lt;") +
                "</p>";
            card.onclick = () => {
                window.location = "book.html?id=" + bid;
            };
            grid.appendChild(card);
        });
    } catch (e) {
        console.warn(e);
    }
}

document.addEventListener("DOMContentLoaded", async () => {
    await syncAuthFromServerSession(API_BASE);
    if (typeof updateShopHeaderAuth === "function") {
        updateShopHeaderAuth();
    }
    try {
        await loadBook();
    } catch (e) {
        console.error(e);
        document.getElementById("title").textContent = "Error loading book";
    }
    await loadCartCount();
});
