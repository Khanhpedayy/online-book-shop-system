/* notifications.js */
const API_BASE = 'http://localhost:8080';
function getToken() { return localStorage.getItem('token'); }

async function apiFetch(path, options = {}) {
    const t = getToken();
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (t) headers['Authorization'] = 'Bearer ' + t;
    const res = await fetch(API_BASE + path, { ...options, headers, credentials: 'include' });
    if (!res.ok) throw new Error(await res.text());
    try { return res.json(); } catch(_) { return {}; }
}

const ICONS = {
    REVIEW_DELETED: '🗑️',
    REPORT_APPROVED: '✅',
    REPORT_REJECTED: '❌',
    GENERAL: '📢'
};

async function loadNotifications() {
    const listEl = document.getElementById('notifList');
    listEl.innerHTML = '<p class="empty-state">Đang tải...</p>';
    try {
        const items = await apiFetch('/api/me/notifications');
        if (!items.length) {
            listEl.innerHTML = '<p class="empty-state">Không có thông báo nào.</p>';
            return;
        }
        listEl.innerHTML = items.map(n => {
            const icon = ICONS[n.type] || ICONS.GENERAL;
            const date = n.createdAt ? new Date(n.createdAt).toLocaleString('vi-VN') : '';
            return `
            <div class="notif-card ${n.read ? '' : 'unread'}" onclick="markRead(${n.id}, this)">
                <div class="notif-icon">${icon}</div>
                <div class="notif-body">
                    <h4>${esc(n.title)}</h4>
                    <p>${esc(n.body)}</p>
                    <div class="notif-date">${date}</div>
                </div>
                ${n.read ? '' : '<div class="unread-dot"></div>'}
            </div>`;
        }).join('');
    } catch(e) {
        listEl.innerHTML = `<p class="empty-state">❌ ${e.message}</p>`;
    }
}

async function markRead(id, el) {
    try {
        await apiFetch(`/api/me/notifications/${id}/read`, { method: 'POST' });
        el.classList.remove('unread');
        const dot = el.querySelector('.unread-dot');
        if (dot) dot.remove();
    } catch(_) {}
}

async function markAllRead() {
    await apiFetch('/api/me/notifications/read-all', { method: 'POST' });
    document.querySelectorAll('.notif-card').forEach(el => {
        el.classList.remove('unread');
        const dot = el.querySelector('.unread-dot');
        if (dot) dot.remove();
    });
    const badge = document.getElementById('notifBadge');
    if (badge) badge.textContent = '';
}

function esc(s) {
    return String(s || '').replace(/[&<>"']/g,
        c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}

document.addEventListener('DOMContentLoaded', async () => {
    await syncAuthFromServerSession(API_BASE);
    if (!getToken()) { window.location.href = 'login.html'; return; }
    loadNotifications();
});
