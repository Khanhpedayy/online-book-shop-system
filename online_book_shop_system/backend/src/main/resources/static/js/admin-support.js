/* === admin-support.js — Support Tickets Page === */

/* Inject page HTML once */
(function buildSupportPage() {
    const html = `
    <div id="pageSupport" class="page-section" style="display:none;">
        <div class="topbar">
            <div>
                <h1>🎧 Support Tickets</h1>
                <div class="breadcrumb">Admin / Support Tickets</div>
            </div>
        </div>
        <div class="content">
            <div class="toolbar" style="margin-bottom:16px;">
                <span style="font-size:13px;color:var(--text2);">Lọc:</span>
                <select id="spStatusFilter" class="filter-select" onchange="loadSupportTickets()">
                    <option value="">Tất cả</option>
                    <option value="OPEN" selected>OPEN</option>
                    <option value="IN_PROGRESS">IN_PROGRESS</option>
                    <option value="WAITING">WAITING</option>
                    <option value="RESOLVED">RESOLVED</option>
                    <option value="CLOSED">CLOSED</option>
                </select>
                <button class="btn btn-accent" onclick="loadSupportTickets()">🔄 Tải lại</button>
            </div>
            <div class="table-wrapper">
                <table>
                    <thead><tr>
                        <th>Mã ticket</th><th>Khách hàng</th><th>Loại</th><th>Tiêu đề</th><th>Trạng thái</th><th>Ngày tạo</th><th>Hành động</th>
                    </tr></thead>
                    <tbody id="spTableBody"></tbody>
                </table>
                <div id="spEmpty" class="empty-state" style="display:none;"><div class="icon">🎧</div><p>Không có support ticket nào.</p></div>
            </div>
        </div>
    </div>

    <!-- Support detail + reply modal -->
    <div id="spModal" class="modal-overlay">
        <div class="modal" style="max-width:640px;">
            <h2 id="spModalTitle">Chi tiết ticket</h2>
            <div id="spModalMeta" style="font-size:12px;color:var(--text3);margin-bottom:12px;"></div>

            <!-- Thread -->
            <div id="spThread" style="background:rgba(255,255,255,0.03);border-radius:10px;padding:12px 14px;margin-bottom:16px;max-height:240px;overflow-y:auto;border:1px solid var(--border);"></div>

            <!-- Reply form -->
            <div style="margin-bottom:12px;">
                <label style="display:block;font-size:12px;color:var(--text2);margin-bottom:6px;">Phản hồi của admin *</label>
                <textarea id="spReplyMsg" placeholder="Nhập nội dung phản hồi..." style="width:100%;padding:10px 12px;background:var(--input-bg);border:1px solid var(--border);border-radius:10px;color:var(--text);font-family:inherit;font-size:13px;min-height:90px;resize:vertical;"></textarea>
            </div>
            <div style="margin-bottom:14px;">
                <label style="display:block;font-size:12px;color:var(--text2);margin-bottom:6px;">Cập nhật trạng thái</label>
                <select id="spResolution" style="width:100%;padding:10px 12px;background:var(--input-bg);border:1px solid var(--border);border-radius:10px;color:var(--text);font-family:inherit;font-size:13px;">
                    <option value="IN_PROGRESS">Đang xử lý (IN_PROGRESS)</option>
                    <option value="WAITING">Chờ phản hồi (WAITING)</option>
                    <option value="RESOLVED">✅ Đã giải quyết (RESOLVED)</option>
                    <option value="CLOSED">❌ Đóng/Từ chối (CLOSED)</option>
                </select>
            </div>

            <div id="spModalMsg" style="color:var(--red);font-size:12px;min-height:1.2em;margin-bottom:8px;"></div>

            <div class="modal-actions">
                <button class="btn-ghost" onclick="closeSpModal()">Đóng</button>
                <button class="btn btn-accent" onclick="sendSupportReply()">📨 Gửi phản hồi</button>
            </div>
        </div>
    </div>`;

    document.querySelector('.main').insertAdjacentHTML('beforeend', html);
})();

// Add nav item for Support under Report & Support
(function addSupportNav() {
    const rrNav = document.getElementById('navReviewReports');
    if (!rrNav) return;
    const newNav = document.createElement('a');
    newNav.className = 'nav-item';
    newNav.id = 'navSupport';
    newNav.innerHTML = '<span class="emoji">🎧</span> Support Tickets';
    newNav.setAttribute('onclick', "switchPage('support')");
    rrNav.after(newNav);
})();

var _currentTicketId = null;

async function loadSupportTickets() {
    const status = document.getElementById('spStatusFilter')?.value || '';
    const token = localStorage.getItem('token');
    try {
        const url = '/api/admin/support-tickets' + (status ? '?status=' + status : '');
        const res = await fetch(url, { headers: { Authorization: 'Bearer ' + token }, credentials: 'include' });
        const data = await res.json();
        renderSpTable(data);
    } catch(e) { console.error(e); }
}

function renderSpTable(data) {
    const tbody = document.getElementById('spTableBody');
    const empty = document.getElementById('spEmpty');
    if (!data || data.length === 0) {
        tbody.innerHTML = '';
        empty.style.display = 'block';
        return;
    }
    empty.style.display = 'none';
    const STATUS_COLORS = { OPEN: 'badge-role', IN_PROGRESS: 'badge-role', RESOLVED: 'badge-active', CLOSED: 'badge-inactive', WAITING: '' };
    tbody.innerHTML = data.map(t => {
        const badge = STATUS_COLORS[t.status] || '';
        const date = t.createdAt ? new Date(t.createdAt).toLocaleDateString('vi-VN') : '';
        return `<tr>
            <td style="font-family:monospace;font-size:12px;">${esc(t.ticketCode)}</td>
            <td>${esc(t.userName || '')}</td>
            <td><span class="badge badge-role" style="font-size:10px;">${esc(t.category)}</span></td>
            <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title="${esc(t.subject)}">${esc((t.subject||'').substring(0,50))}</td>
            <td><span class="badge ${badge}">${t.status}</span></td>
            <td class="date-text">${date}</td>
            <td class="actions"><button class="btn-sm" onclick="openSpModal(${t.id})">🔍 Xem & Trả lời</button></td>
        </tr>`;
    }).join('');
}

async function openSpModal(ticketId) {
    _currentTicketId = ticketId;
    const token = localStorage.getItem('token');
    document.getElementById('spModal').classList.add('active');
    document.getElementById('spModalTitle').textContent = 'Đang tải...';
    document.getElementById('spThread').innerHTML = '';
    document.getElementById('spReplyMsg').value = '';
    document.getElementById('spModalMsg').textContent = '';
    try {
        const res = await fetch('/api/admin/support-tickets/' + ticketId, {
            headers: { Authorization: 'Bearer ' + token }, credentials: 'include'
        });
        const t = await res.json();
        document.getElementById('spModalTitle').textContent = esc(t.subject);
        document.getElementById('spModalMeta').textContent =
            t.ticketCode + ' · ' + t.category + ' · ' + t.status +
            (t.orderId ? ' · Order #' + t.orderId : '');

        const thread = document.getElementById('spThread');
        thread.innerHTML = (t.messages || []).map(m => {
            const isAdmin = m.from === 'admin';
            const label = isAdmin ? '🧑‍💼 Admin' : '👤 Khách hàng';
            const bg = isAdmin ? 'rgba(108,99,255,0.12)' : 'rgba(255,255,255,0.05)';
            const time = m.at ? new Date(m.at).toLocaleString('vi-VN') : '';
            return `<div style="background:${bg};border-radius:8px;padding:10px 12px;margin-bottom:8px;">
                <div style="font-size:11px;color:var(--text3);margin-bottom:4px;">${label} · ${time}</div>
                <div style="font-size:13px;">${esc(m.message)}</div>
            </div>`;
        }).join('') || '<div style="color:var(--text3);font-size:12px;">Không có tin nhắn.</div>';

        thread.scrollTop = thread.scrollHeight;
    } catch(e) {
        document.getElementById('spModalTitle').textContent = 'Lỗi tải dữ liệu';
    }
}

function closeSpModal() {
    document.getElementById('spModal').classList.remove('active');
}

async function sendSupportReply() {
    const token = localStorage.getItem('token');
    const message = (document.getElementById('spReplyMsg').value || '').trim();
    const resolution = document.getElementById('spResolution').value;
    const msgEl = document.getElementById('spModalMsg');
    if (!message) { msgEl.textContent = '⚠️ Vui lòng nhập nội dung phản hồi.'; return; }
    try {
        const res = await fetch('/api/admin/support-tickets/' + _currentTicketId + '/reply', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
            credentials: 'include',
            body: JSON.stringify({ message, resolution })
        });
        const data = await res.json();
        if (res.ok) {
            closeSpModal();
            showToast(data.message || 'Đã gửi phản hồi!', 'success');
            loadSupportTickets();
        } else {
            msgEl.textContent = '❌ ' + (data.message || 'Lỗi');
        }
    } catch(e) {
        msgEl.textContent = '❌ ' + e.message;
    }
}
