/* support.js */
const API_BASE = 'http://localhost:8080';

function getToken() { return localStorage.getItem('token'); }

async function apiFetch(path, options = {}) {
    const t = getToken();
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (t) headers['Authorization'] = 'Bearer ' + t;
    const res = await fetch(API_BASE + path, { ...options, headers, credentials: 'include' });
    const text = await res.text();
    let data;
    try { data = JSON.parse(text); } catch(_) { data = text; }
    if (!res.ok) throw { status: res.status, message: (data && data.message) ? data.message : text };
    return data;
}

function esc(s) {
    return String(s || '').replace(/[&<>"']/g,
        c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}

const STATUS_LABEL = {
    OPEN: 'Mới', IN_PROGRESS: 'Đang xử lý', WAITING: 'Chờ phản hồi',
    RESOLVED: '✅ Đã giải quyết', CLOSED: '❌ Đã đóng'
};

async function loadTickets() {
    const listEl = document.getElementById('ticketList');
    listEl.innerHTML = '<p class="empty-state">Đang tải...</p>';
    try {
        const tickets = await apiFetch('/api/me/support-tickets');
        const countEl = document.getElementById('ticketCount');
        if (!tickets.length) {
            countEl.textContent = '';
            listEl.innerHTML = `<p class="empty-state">Bạn chưa có yêu cầu hỗ trợ nào.<br>Nhấn "+ Tạo yêu cầu mới" để bắt đầu.</p>`;
            return;
        }
        countEl.textContent = tickets.length + ' yêu cầu';
        listEl.innerHTML = tickets.map(t => {
            const date = t.createdAt ? new Date(t.createdAt).toLocaleDateString('vi-VN') : '';
            const statusLabel = STATUS_LABEL[t.status] || t.status;
            return `
            <div class="ticket-card status-${t.status}" onclick="openDetailModal(${t.id})">
                <div class="tc-head">
                    <span class="tc-code">${esc(t.ticketCode)}</span>
                    <span class="tc-category">${esc(t.category)}</span>
                    <span class="tc-status ${t.status}">${statusLabel}</span>
                </div>
                <div class="tc-subject">${esc(t.subject)}</div>
                <div class="tc-date">Tạo ngày ${date}</div>
            </div>`;
        }).join('');
    } catch(e) {
        listEl.innerHTML = `<p class="empty-state">❌ ${esc(e.message || String(e))}</p>`;
    }
}

async function openDetailModal(ticketId) {
    const overlay = document.getElementById('tdOverlay');
    overlay.classList.add('open');
    document.getElementById('tdTitle').textContent = 'Đang tải...';
    document.getElementById('tdMeta').textContent = '';
    document.getElementById('tdThread').innerHTML = '';
    document.getElementById('tdClosed').textContent = '';
    try {
        const t = await apiFetch('/api/me/support-tickets/' + ticketId);
        document.getElementById('tdTitle').textContent = esc(t.subject);
        document.getElementById('tdMeta').textContent =
            t.ticketCode + ' · ' + t.category + ' · ' + (STATUS_LABEL[t.status] || t.status);

        const thread = document.getElementById('tdThread');
        thread.innerHTML = (t.messages || []).map(m => {
            const side = m.from === 'admin' ? 'admin' : 'customer';
            const label = m.from === 'admin' ? '🧑‍💼 Bộ phận hỗ trợ' : '👤 Bạn';
            const time = m.at ? new Date(m.at).toLocaleString('vi-VN') : '';
            return `<div class="msg-bubble ${side}">
                        <div class="msg-from">${label}</div>
                        <div>${esc(m.message)}</div>
                        <div class="msg-time">${time}</div>
                    </div>`;
        }).join('') || '<p style="color:#94a3b8;font-size:0.88rem;">Không có tin nhắn.</p>';

        if (t.status === 'RESOLVED' || t.status === 'CLOSED') {
            document.getElementById('tdClosed').textContent =
                t.status === 'RESOLVED' ? '✅ Yêu cầu đã được giải quyết.' : '❌ Yêu cầu đã bị đóng.';
        }
    } catch(e) {
        document.getElementById('tdTitle').textContent = 'Lỗi tải dữ liệu';
    }
}

function closeDetailModal() {
    document.getElementById('tdOverlay').classList.remove('open');
}

function openCreateModal() {
    document.getElementById('ctOverlay').classList.add('open');
    document.getElementById('ctCategory').value = '';
    document.getElementById('ctSubject').value = '';
    document.getElementById('ctMessage').value = '';
    document.getElementById('ctOrderId').value = '';
    document.getElementById('ctError').textContent = '';
    document.getElementById('btnSubmit').disabled = false;
}

function closeCreateModal() {
    document.getElementById('ctOverlay').classList.remove('open');
}

async function submitTicket() {
    const category = document.getElementById('ctCategory').value.trim();
    const subject  = document.getElementById('ctSubject').value.trim();
    const message  = document.getElementById('ctMessage').value.trim();
    const orderId  = document.getElementById('ctOrderId').value.trim();
    const errEl    = document.getElementById('ctError');

    if (!category) { errEl.textContent = '⚠️ Vui lòng chọn loại vấn đề.'; return; }
    if (!subject)  { errEl.textContent = '⚠️ Vui lòng nhập tiêu đề.'; return; }
    if (!message)  { errEl.textContent = '⚠️ Vui lòng nhập nội dung.'; return; }

    const btn = document.getElementById('btnSubmit');
    btn.disabled = true;
    errEl.textContent = '';
    try {
        await apiFetch('/api/me/support-tickets', {
            method: 'POST',
            body: JSON.stringify({
                category, subject, message,
                orderId: orderId ? Number(orderId) : null
            })
        });
        closeCreateModal();
        await loadTickets();
        alert('✅ Đã gửi yêu cầu hỗ trợ thành công!');
    } catch(e) {
        errEl.textContent = '❌ ' + (e.message || 'Lỗi gửi yêu cầu');
        btn.disabled = false;
    }
}

// Close modals on overlay click
document.addEventListener('DOMContentLoaded', async () => {
    await syncAuthFromServerSession(API_BASE);
    if (!getToken()) { window.location.href = 'login.html'; return; }
    loadTickets();

    document.getElementById('tdOverlay').addEventListener('click', e => {
        if (e.target === e.currentTarget) closeDetailModal();
    });
    document.getElementById('ctOverlay').addEventListener('click', e => {
        if (e.target === e.currentTarget) closeCreateModal();
    });
});
