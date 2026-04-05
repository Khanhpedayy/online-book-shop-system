const API_BASE = 'http://localhost:8080';

const booksContainer = document.getElementById('booksContainer');
const headerCartCount = document.getElementById('headerCartCount');
const productCount = document.getElementById('productCount');
const sortSelect = document.getElementById('sortSelect');
const searchInput = document.getElementById('searchInput');

let currentBooks = [];
let currentPage = 1;
let booksPerPage = 8;

function formatVnd(value) {
    const n = Number(value) || 0;
    return new Intl.NumberFormat("vi-VN").format(Math.round(n)) + "₫";
}

/* ===== JWT ===== */
function getToken() {
    return localStorage.getItem("token");
}

function isLoggedIn() {
    return !!getToken();
}

async function logout() {
    await performLogout(API_BASE);
}

/* ===== API ===== */
async function readApiErrorMessage(res) {
    const text = await res.text();
    if (!text || !text.trim()) return res.statusText || "Request failed";
    try {
        const j = JSON.parse(text);
        if (j && j.message) return String(j.message);
    } catch (_) {}
    return text;
}

async function apiGet(path) {
    const headers = {};

    if (getToken()) {
        headers["Authorization"] = "Bearer " + getToken();
    }

    const resp = await fetch(API_BASE + path, { headers, credentials: 'include' });

    if (!resp.ok) throw new Error(await readApiErrorMessage(resp));
    return resp.json();
}

async function apiPost(path, body) {
    const headers = { 'Content-Type': 'application/json' };
    if (getToken()) {
        headers['Authorization'] = 'Bearer ' + getToken();
    }
    const resp = await fetch(API_BASE + path, {
        method: 'POST',
        headers,
        credentials: 'include',
        body: JSON.stringify(body)
    });
    if (!resp.ok) throw new Error(await readApiErrorMessage(resp));
    return resp.json();
}

/* ===== BOOKS ===== */
function sortBooks(books, value) {
    const list = [...books];
    switch (value) {
        case 'title-asc': list.sort((a, b) => a.title.localeCompare(b.title)); break;
        case 'title-desc': list.sort((a, b) => b.title.localeCompare(a.title)); break;
        case 'price-asc': list.sort((a, b) => (a.salePrice ?? 0) - (b.salePrice ?? 0)); break;
        case 'price-desc': list.sort((a, b) => (b.salePrice ?? 0) - (a.salePrice ?? 0)); break;
    }
    return list;
}

function renderBooks(list = currentBooks) {
    if (!booksContainer) return;

    let sorted = sortBooks(list, sortSelect?.value);
    let start = (currentPage - 1) * booksPerPage;
    let end = start + booksPerPage;
    let books = sorted.slice(start, end);

    booksContainer.innerHTML = "";
    console.log("RENDER:", list);
    books.forEach(b => {
        const price = b.salePrice ?? 0;
        const coverUrl = b.coverImageUrl || ('https://covers.openlibrary.org/b/isbn/' + (b.isbn || '0385533229') + '-M.jpg');
        const variantId = b.variantId || b.id;

        booksContainer.innerHTML += `
        <div class="book">
            <img class="book-cover" src="${coverUrl}" onerror="this.src='https://via.placeholder.com/220x330?text=Book'">
            <div class="book-info">
                <div class="book-title">${b.title}</div>
                <div class="book-price">${formatVnd(price)}</div>
                <div class="book-actions">
                    <input type="number" value="1" min="1" id="qty-${variantId}">
                    <button class="btn" onclick="addToCart(${variantId})">Add to Cart</button>
                    <button class="btn btn-secondary" onclick="openBook(${variantId})">View</button>
                </div>
            </div>
        </div>`;
    });

    createPagination(sorted.length);

    if (productCount) productCount.textContent = sorted.length + " products";
}

/* ===== PAGINATION ===== */
function createPagination(totalBooks) {
    const pagination = document.getElementById("pagination");
    if (!pagination) return;

    let totalPages = Math.ceil(totalBooks / booksPerPage);
    pagination.innerHTML = "";

    for (let i = 1; i <= totalPages; i++) {
        pagination.innerHTML += `
        <button class="page-btn ${i === currentPage ? 'active' : ''}" onclick="changePage(${i})">${i}</button>`;
    }
}

function changePage(page) {
    currentPage = page;
    renderBooks();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

/* ===== LOAD BOOKS ===== */
async function loadBooks(filters = {}) {
    if (!booksContainer) return;

    booksContainer.innerHTML = "Loading...";
    console.log("LOAD BOOKS CALLED");
    try {
        let url = "/api/books/filter";
        const params = new URLSearchParams();

        for (const key in filters) {
            if (filters[key] === undefined || filters[key] === null || filters[key] === "") {
                continue;
            }
            if (key === "minPrice" || key === "maxPrice" || key === "categoryId") {
                params.append(key, Number(filters[key]));
            } else {
                params.append(key, filters[key]);
            }
        }

        if (params.toString()) url += "?" + params.toString();

        const books = await apiGet(url);

        if (!books.length) {
            booksContainer.innerHTML = "No books found";
            return;
        }

        // Populate publisher dropdown from current result set (simple, no extra API).
        const pubSel = document.getElementById("publisherFilter");
        if (pubSel) {
            const current = pubSel.value || "";
            const pubs = Array.from(
                new Set(
                    books
                        .map(b => (b.publisherName || "").trim())
                        .filter(Boolean)
                )
            ).sort((a, b) => a.localeCompare(b));
            pubSel.innerHTML =
                '<option value="">All Publishers</option>' +
                pubs.map(p => `<option value="${escapeHtmlAttr(p)}">${escapeHtmlAttr(p)}</option>`).join("");
            pubSel.value = pubs.includes(current) ? current : "";
        }

        currentBooks = books;
        currentPage = 1;
        renderBooks(currentBooks);

    } catch (e) {
        booksContainer.innerHTML = "Error loading books";
        console.error(e);
    }
}

/* ===== SEARCH / FILTERS ===== */
function escapeHtmlAttr(s) {
    if (s == null) {
        return "";
    }
    return String(s)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}

function collectFiltersFromForm() {
    const filters = {
        keyword: document.getElementById("searchInput")?.value?.trim(),
        publisherName: document.getElementById("publisherFilter")?.value,
        minPrice: document.getElementById("minPrice")?.value,
        maxPrice: document.getElementById("maxPrice")?.value,
        format: document.getElementById("formatFilter")?.value,
        condition: document.getElementById("conditionFilter")?.value
    };
    const cf = document.getElementById("categoryFilter");
    if (cf && cf.value) {
        const cid = Number(cf.value);
        if (Number.isFinite(cid)) {
            filters.categoryId = cid;
        }
    }
    Object.keys(filters).forEach(key => {
        const v = filters[key];
        if (v === undefined || v === null || v === "") {
            delete filters[key];
        }
    });
    return filters;
}

function applyFilters() {
    currentPage = 1;
    loadBooks(collectFiltersFromForm());
}

async function loadCategoryFilters() {
    const sel = document.getElementById("categoryFilter");
    const icons = document.getElementById("categoryIcons");
    if (!sel || !icons) {
        return;
    }
    let cats = [];
    try {
        cats = await apiGet("/api/categories");
    } catch (e) {
        console.warn("loadCategoryFilters", e);
    }
    sel.innerHTML = '<option value="">All categories</option>' +
        cats.map(c => `<option value="${c.id}">${escapeHtmlAttr(c.name || ("Category " + c.id))}</option>`).join("");

    const chipIcons = ["📖", "💼", "💻", "🔬", "🧠", "📚", "🎨", "🌍", "📕", "📗"];
    icons.innerHTML =
        '<div class="cat active" data-id="" role="button" tabindex="0">📚<span>All</span></div>' +
        cats.map((c, i) =>
            `<div class="cat" data-id="${c.id}" role="button" tabindex="0">${chipIcons[i % chipIcons.length]}<span>${escapeHtmlAttr(c.name || "")}</span></div>`
        ).join("");

    function markActiveChip() {
        const v = sel.value;
        icons.querySelectorAll(".cat").forEach(el => {
            const id = el.getAttribute("data-id");
            const match = (v === "" && id === "") || (v !== "" && id === v);
            el.classList.toggle("active", match);
        });
    }

    icons.querySelectorAll(".cat").forEach(el => {
        el.addEventListener("click", () => {
            sel.value = el.getAttribute("data-id") || "";
            markActiveChip();
            applyFilters();
        });
    });

    sel.addEventListener("change", () => {
        markActiveChip();
        applyFilters();
    });
}

/* ===== CART ===== */
async function addToCart(id) {
    await syncAuthFromServerSession(API_BASE);
    if (!isLoggedIn()) {
        alert("Please login first!");
        window.location = "login.html";
        return;
    }

    const qty = Number(document.getElementById("qty-" + id)?.value || 1);

    apiPost(`/api/cart/items`, {   // ✅ FIX Ở ĐÂY
        variantId: id,
        quantity: qty
    })
        .then(() => loadCart())
        .catch(err => alert(err.message));
}

function updateHeaderCart(total, count) {
    if (!headerCartCount) return;
    headerCartCount.textContent = `${formatVnd(total)} (${count})`;
}

async function loadCart() {
    if (!isLoggedIn()) return;

    try {
        const items = await apiGet(`/api/cart/me`);

        let total = 0, count = 0;

        items.forEach(ci => {
            const price = ci.salePrice ?? ci.variant?.salePrice ?? 0;
            total += price * ci.quantity;
            count += ci.quantity;
        });

        updateHeaderCart(total, count);

    } catch (e) {
        console.error(e);
    }
}

/* ===== NAV ===== */
function openBook(bookId) {
    window.location = "book.html?id=" + bookId;
}

/* ===== SORT ===== */
if (sortSelect) {
    sortSelect.addEventListener("change", () => {
        currentPage = 1;
        renderBooks(currentBooks);
    });
}

/* ===== BANNER ===== */
let currentSlide = 0;
let slides, dots;

function showSlide(index) {
    slides.forEach(s => s.classList.remove("active"));
    dots.forEach(d => d.classList.remove("active"));
    slides[index]?.classList.add("active");
    dots[index]?.classList.add("active");
}

function nextSlide() {
    currentSlide = (currentSlide + 1) % slides.length;
    showSlide(currentSlide);
}

function prevSlide() {
    currentSlide = (currentSlide - 1) % slides.length;
    showSlide(currentSlide);
}

/* ===== INIT ===== */
document.addEventListener("DOMContentLoaded", async () => {
    await syncAuthFromServerSession(API_BASE);
    if (typeof updateShopHeaderAuth === "function") {
        updateShopHeaderAuth();
    }

    document.getElementById("searchBtn")?.addEventListener("click", applyFilters);

    document.getElementById("searchInput")?.addEventListener("keyup", (e) => {
        if (e.key === "Enter") applyFilters();
    });

    slides = document.querySelectorAll(".slide");
    dots = document.querySelectorAll(".dot");

    if (slides.length) {
        setInterval(nextSlide, 4000);
    }

    const kw = new URLSearchParams(window.location.search).get("keyword");
    const si = document.getElementById("searchInput");
    if (kw && si) {
        si.value = kw;
    }

    await loadCategoryFilters();
    if (kw) {
        applyFilters();
    } else {
        loadBooks();
    }

    if (isLoggedIn()) {
        loadCart();
    }
});