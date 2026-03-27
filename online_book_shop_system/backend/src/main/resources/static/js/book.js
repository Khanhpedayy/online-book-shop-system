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
    await loadReviews(book.id);
}

/* ── REVIEWS ── */
async function loadReviews(bookId) {
    const listEl    = document.getElementById("reviewsList");
    const summaryEl = document.getElementById("reviewSummary");
    if (!listEl) return;
    try {
        const reviews = await apiGet(API + "/books/" + bookId + "/reviews");

        if (!reviews || reviews.length === 0) {
            listEl.innerHTML = `<p class="rv-empty">Chưa có đánh giá nào cho cuốn sách này.</p>`;
            return;
        }

        /* Summary: avg rating */
        const avg = (reviews.reduce((s, r) => s + r.rating, 0) / reviews.length).toFixed(1);
        const fullStars = Math.round(avg);
        document.getElementById("avgScore").textContent = avg;
        document.getElementById("avgStars").textContent = "★".repeat(fullStars) + "☆".repeat(5 - fullStars);
        document.getElementById("avgCount").textContent = reviews.length + " đánh giá";
        summaryEl.style.display = "flex";

        /* Render cards */
        listEl.innerHTML = reviews.map(r => {
            const stars = "★".repeat(r.rating) + "☆".repeat(5 - r.rating);
            const date  = r.createdAt ? new Date(r.createdAt).toLocaleDateString("vi-VN") : "";
            const name  = esc(r.reviewerName || "Ẩn danh");
            const title = r.title ? `<div class="rv-title">${esc(r.title)}</div>` : "";
            const text  = esc(r.content || "");
            return `
                <div class="rv-card">
                    <div class="rv-card-header">
                        <span class="rv-stars">${stars}</span>
                        <span class="rv-author">${name}</span>
                        <span class="rv-date">${date}</span>
                        <button class="rv-report-btn" onclick="openReportModal(${r.id})" title="Báo cáo đánh giá này">🚩 Báo cáo</button>
                    </div>
                    ${title}
                    <div class="rv-content">${text}</div>
                </div>`;
        }).join("") +
        `<!-- Report Modal -->
        <div id="reportOverlay" style="display:none;position:fixed;inset:0;background:rgba(0,0,0,0.45);z-index:2000;align-items:center;justify-content:center;">
          <div style="background:#fff;border-radius:14px;padding:26px 28px;width:90%;max-width:460px;box-shadow:0 20px 60px rgba(0,0,0,0.25);">
            <h3 style="margin:0 0 6px;font-size:1.1rem;">🚩 Báo cáo đánh giá</h3>
            <p style="color:#64748b;font-size:0.88rem;margin-bottom:14px;">Nhập lý do để chúng tôi xem xét đánh giá này.</p>
            <textarea id="reportReason" placeholder="Lý do báo cáo..." maxlength="500"
              style="width:100%;min-height:100px;padding:10px 12px;border:1.5px solid #e2e8f0;border-radius:8px;font:inherit;resize:vertical;box-sizing:border-box;margin-bottom:12px;"></textarea>
            <div id="reportMsg" style="color:#dc2626;font-size:0.88rem;min-height:1.2em;margin-bottom:8px;"></div>
            <div style="display:flex;gap:10px;justify-content:flex-end;">
              <button onclick="closeReportModal()" style="padding:9px 18px;border:1.5px solid #e2e8f0;border-radius:8px;background:#fff;cursor:pointer;font-weight:600;">Huỷ</button>
              <button id="btnReport" onclick="submitReport()" style="padding:9px 20px;border:none;border-radius:8px;background:#dc2626;color:#fff;cursor:pointer;font-weight:700;">Gửi báo cáo</button>
            </div>
          </div>
        </div>`;

    } catch(e) {
        console.warn("Could not load reviews:", e);
        listEl.innerHTML = `<p class="rv-empty">Không thể tải đánh giá.</p>`;
    }
}

function esc(str) {
    return String(str || "").replace(/[&<>"']/g,
        c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
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
            const bid = b.bookId != null ? b.bookId : b.id;
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
            const bid = b.bookId != null ? b.bookId : b.id;
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

/* ── REPORT MODAL ── */
var _reportReviewId = null;

function openReportModal(reviewId) {
    _reportReviewId = reviewId;
    var overlay = document.getElementById("reportOverlay");
    if (!overlay) { alert("Không tìm thấy form báo cáo"); return; }
    overlay.style.display = "flex";
    document.getElementById("reportReason").value = "";
    document.getElementById("reportMsg").textContent = "";
    var btn = document.getElementById("btnReport");
    if (btn) btn.disabled = false;
}

function closeReportModal() {
    var overlay = document.getElementById("reportOverlay");
    if (overlay) overlay.style.display = "none";
}

async function submitReport() {
    const token = getToken();
    if (!token) { alert("Vui lòng đăng nhập để báo cáo."); return; }
    const reason = (document.getElementById("reportReason").value || "").trim();
    const msgEl = document.getElementById("reportMsg");
    if (!reason) { msgEl.textContent = "⚠️ Vui lòng nhập lý do."; return; }
    const btn = document.getElementById("btnReport");
    btn.disabled = true;
    msgEl.textContent = "";
    try {
        const res = await fetch(API_BASE + "/api/me/review-reports", {
            method: "POST",
            headers: { "Content-Type": "application/json", "Authorization": "Bearer " + token },
            credentials: "include",
            body: JSON.stringify({ reviewId: _reportReviewId, reason })
        });
        const data = await res.json();
        if (res.ok) {
            closeReportModal();
            alert("✅ " + (data.message || "Đã gửi báo cáo!"));
        } else {
            msgEl.textContent = "❌ " + (data.message || "Lỗi gửi báo cáo");
            btn.disabled = false;
        }
    } catch(e) {
        msgEl.textContent = "❌ " + e.message;
        btn.disabled = false;
    }
}

