/* ────────────────────────────────────────────────
   reviews.js  — My Reviews page logic
   ──────────────────────────────────────────────── */
const API_BASE = 'http://localhost:8080';

function getToken() { return localStorage.getItem('token'); }

async function apiFetch(path, options = {}) {
    const t = getToken();
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (t) headers['Authorization'] = 'Bearer ' + t;
    const res = await fetch(API_BASE + path, { ...options, headers, credentials: 'include' });
    if (!res.ok) {
        const text = await res.text();
        let msg = text;
        try { msg = JSON.parse(text).message || text; } catch(_) {}
        throw new Error(msg);
    }
    try { return await res.json(); } catch(_) { return {}; }
}

/* ── state ── */
let _currentBookId = null;
let _currentOrderId = null;
let _currentReviewId = null;   // null = create, non-null = edit

/* ── Render star string ── */
function starsHtml(n) {
    return '★'.repeat(n) + '☆'.repeat(5 - n);
}

/* ── Load list ── */
async function loadBooks() {
    const pendingEl  = document.getElementById('pendingList');
    const reviewedEl = document.getElementById('reviewedList');
    pendingEl.innerHTML  = '<p class="empty-state">Đang tải...</p>';
    reviewedEl.innerHTML = '<p class="empty-state">Đang tải...</p>';

    try {
        const books = await apiFetch('/api/me/reviews/purchased');

        const pending  = books.filter(b => !b.reviewed);
        const reviewed = books.filter(b => b.reviewed);

        document.getElementById('pendingCount').textContent  = pending.length;
        document.getElementById('reviewedCount').textContent = reviewed.length;

        renderBooks(pendingEl,  pending,  false);
        renderBooks(reviewedEl, reviewed, true);
    } catch(e) {
        pendingEl.innerHTML  = `<p class="empty-state">❌ ${e.message}</p>`;
        reviewedEl.innerHTML = '';
    }
}

function renderBooks(container, books, isReviewed) {
    if (!books.length) {
        container.innerHTML = `<p class="empty-state">${isReviewed ? 'Chưa có đánh giá nào.' : 'Tất cả sách đã được đánh giá! 🎉'}</p>`;
        return;
    }
    container.innerHTML = books.map(b => `
        <div class="book-card">
            ${b.bookCoverUrl
                ? `<img class="book-cover" src="${b.bookCoverUrl}" alt="${escHtml(b.bookTitle)}" loading="lazy">`
                : `<div class="book-cover-placeholder">📖</div>`
            }
            <div class="book-info">
                <h3 title="${escHtml(b.bookTitle)}">${escHtml(b.bookTitle)}</h3>
                <small>Đơn: ${escHtml(b.orderCode || '#' + b.orderId)}</small>
            </div>
            <div style="flex-shrink:0;">
                ${isReviewed
                    ? `<div class="already-reviewed">
                           <span class="stars-display" title="${b.reviewRating} sao">${starsHtml(b.reviewRating || 0)}</span>
                           <button class="shop-btn" onclick="openEditModal(${b.bookId}, ${b.orderId}, ${b.reviewId}, ${b.reviewRating}, '${escHtml(b.bookTitle)}')">Sửa</button>
                       </div>`
                    : `<button class="shop-btn" onclick="openCreateModal(${b.bookId}, ${b.orderId}, '${escHtml(b.bookTitle)}')">⭐ Đánh giá</button>`
                }
            </div>
        </div>
    `).join('');
}

function escHtml(str) {
    return String(str || '').replace(/[&<>"']/g, c =>
        ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}

/* ── Modal open/close ── */
function openCreateModal(bookId, orderId, bookTitle) {
    _currentBookId  = bookId;
    _currentOrderId = orderId;
    _currentReviewId = null;

    document.getElementById('modalTitle').textContent    = '✍️ Viết đánh giá';
    document.getElementById('modalBookName').textContent = bookTitle;
    document.getElementById('reviewTitle').value   = '';
    document.getElementById('reviewContent').value = '';
    document.getElementById('modalMsg').textContent = '';
    document.querySelectorAll('.star-group input').forEach(r => r.checked = false);
    document.getElementById('btnSubmit').disabled = false;
    document.getElementById('reviewOverlay').classList.add('open');
}

async function openEditModal(bookId, orderId, reviewId, rating, bookTitle) {
    _currentBookId   = bookId;
    _currentOrderId  = orderId;
    _currentReviewId = reviewId;

    document.getElementById('modalTitle').textContent    = '✏️ Sửa đánh giá';
    document.getElementById('modalBookName').textContent = bookTitle;
    document.getElementById('modalMsg').textContent = '';
    document.getElementById('btnSubmit').disabled = false;

    /* Pre-select rating */
    document.querySelectorAll('.star-group input').forEach(r => {
        r.checked = parseInt(r.value) === rating;
    });

    /* Load existing content */
    try {
        const existing = await apiFetch(`/api/me/reviews/purchased`);
        // We already have title/content only if we stored it — reload from backend by bookId
        // Simple approach: leave fields blank and let user re-fill
        document.getElementById('reviewTitle').value   = '';
        document.getElementById('reviewContent').value = '';
    } catch(_) {}

    document.getElementById('reviewOverlay').classList.add('open');
}

function closeModal() {
    document.getElementById('reviewOverlay').classList.remove('open');
}

/* ── Submit ── */
async function submitReview() {
    const rating  = parseInt(document.querySelector('.star-group input:checked')?.value || 0);
    const title   = document.getElementById('reviewTitle').value.trim();
    const content = document.getElementById('reviewContent').value.trim();
    const msgEl   = document.getElementById('modalMsg');

    if (!rating) { msgEl.textContent = '⚠️ Vui lòng chọn số sao đánh giá.'; return; }
    if (!content) { msgEl.textContent = '⚠️ Vui lòng nhập nội dung đánh giá.'; return; }

    const btn = document.getElementById('btnSubmit');
    btn.disabled = true;
    msgEl.textContent = '';

    const body = { bookId: _currentBookId, orderId: _currentOrderId, rating, title, content };

    try {
        if (_currentReviewId) {
            await apiFetch(`/api/me/reviews/${_currentReviewId}`, { method: 'PUT', body: JSON.stringify(body) });
        } else {
            await apiFetch('/api/me/reviews', { method: 'POST', body: JSON.stringify(body) });
        }
        closeModal();
        loadBooks();   // refresh list
    } catch(e) {
        msgEl.textContent = '❌ ' + e.message;
        btn.disabled = false;
    }
}

/* ── Close on overlay click ── */
document.getElementById('reviewOverlay').addEventListener('click', function(e) {
    if (e.target === this) closeModal();
});

/* ── Keyboard close ── */
document.addEventListener('keydown', e => { if (e.key === 'Escape') closeModal(); });

/* ── Init ── */
document.addEventListener('DOMContentLoaded', async () => {
    await syncAuthFromServerSession(API_BASE);
    if (!getToken()) {
        alert('Vui lòng đăng nhập!');
        window.location.href = 'login.html';
        return;
    }
    loadBooks();
});
