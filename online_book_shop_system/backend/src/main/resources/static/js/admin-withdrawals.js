/* === admin-withdrawals.js — Withdrawal Requests Page === */

/* Inject page HTML once */
(function buildWithdrawalsPage() {
    const html = `
    <div id="pageWithdrawals" class="page-section" style="display:none;">
        <div class="topbar">
            <div>
                <h1>💸 Yêu cầu rút tiền</h1>
                <div class="breadcrumb">Admin / Yêu cầu rút tiền</div>
            </div>
            <button class="btn btn-accent" onclick="loadWithdrawals()" style="align-self:center;">🔄 Tải lại</button>
        </div>
        <div class="content">
            <!-- Stats -->
            <div class="stats" id="wdStats" style="margin-bottom:20px;">
                <div class="stat-card">
                    <div class="label">⏳ Đang chờ</div>
                    <div class="value" id="wdStatPending" style="color:var(--yellow);">—</div>
                </div>
                <div class="stat-card">
                    <div class="label">✅ Đã duyệt</div>
                    <div class="value" id="wdStatApproved" style="color:var(--green);">—</div>
                </div>
                <div class="stat-card">
                    <div class="label">❌ Đã từ chối</div>
                    <div class="value" id="wdStatRejected" style="color:var(--red);">—</div>
                </div>
                <div class="stat-card">
                    <div class="label">💰 Tổng cần xử lý</div>
                    <div class="value" id="wdStatTotal" style="color:var(--accent);">—</div>
                </div>
            </div>

            <!-- Filter -->
            <div class="toolbar" style="margin-bottom:16px;">
                <span style="font-size:13px;color:var(--text2);">Lọc trạng thái:</span>
                <select id="wdStatusFilter" class="filter-select" onchange="loadWithdrawals()">
                    <option value="">Tất cả</option>
                    <option value="PENDING" selected>⏳ PENDING — Chờ xử lý</option>
                    <option value="APPROVED">✅ APPROVED — Đã duyệt</option>
                    <option value="REJECTED">❌ REJECTED — Đã từ chối</option>
                </select>
            </div>

            <!-- Table -->
            <div class="table-wrapper">
                <table>
                    <thead><tr>
                        <th>Mã yêu cầu</th>
                        <th>Khách hàng</th>
                        <th>Số tiền</th>
                        <th>Ngân hàng</th>
                        <th>Số TK / Tên TK</th>
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                        <th>Hành động</th>
                    </tr></thead>
                    <tbody id="wdTableBody"></tbody>
                </table>
                <div id="wdEmpty" class="empty-state" style="display:none;">
                    <div class="icon">💸</div>
                    <p>Không có yêu cầu rút tiền nào.</p>
                </div>
            </div>
        </div>
    </div>

    <!-- Reject modal -->
    <div id="wdRejectModal" class="modal-overlay">
        <div class="modal" style="max-width:480px;">
            <h2>❌ Từ chối yêu cầu rút tiền</h2>
            <p id="wdRejectInfo" style="font-size:13px;color:var(--text2);margin-bottom:16px;"></p>
            <div style="margin-bottom:12px;padding:12px;background:rgba(255,165,0,0.1);border-radius:8px;font-size:13px;color:var(--yellow);">
                ⚠️ Số tiền sẽ được hoàn tự động về ví ảo của khách sau khi từ chối.
            </div>
            <div style="margin-bottom:16px;">
                <label style="display:block;font-size:12px;color:var(--text2);margin-bottom:6px;">
                    Lý do từ chối <span style="color:var(--red)">*</span>
                </label>
                <textarea id="wdRejectReason"
                    placeholder="VD: Thông tin tài khoản không hợp lệ, số tài khoản không tồn tại…"
                    style="width:100%;padding:10px 12px;background:var(--input-bg);border:1px solid var(--border);border-radius:10px;color:var(--text);font-family:inherit;font-size:13px;min-height:90px;resize:vertical;"></textarea>
            </div>
            <div id="wdRejectMsg" style="color:var(--red);font-size:12px;min-height:1.2em;margin-bottom:8px;"></div>
            <div class="modal-actions">
                <button class="btn-ghost" onclick="closeWdRejectModal()">Hủy</button>
                <button class="btn" onclick="confirmReject()" style="background:var(--red);color:#fff;">Xác nhận từ chối</button>
            </div>
        </div>
    </div>`;

    document.querySelector('.main').insertAdjacentHTML('beforeend', html);
})();

/* Add nav item after Support Tickets */
(function addWithdrawalsNav() {
    const supportNav = document.getElementById('navSupport');
    const ref = supportNav || document.getElementById('navReviewReports');
    if (!ref) return;
    const nav = document.createElement('a');
    nav.className = 'nav-item';
    nav.id = 'navWithdrawals';
    nav.innerHTML = '<span class="emoji">💸</span> Yêu cầu rút tiền';
    nav.setAttribute('onclick', "switchPage('withdrawals')");
    ref.after(nav);
})();

/* State */
var _wdRejectingId = null;

/* ─── Load & Render ─────────────────────────────────────────── */

async function loadWithdrawals() {
    const status = document.getElementById('wdStatusFilter')?.value || '';
    const token = localStorage.getItem('token');
    // Dùng 1 endpoint duy nhất với query param ?status= (rỗng = lấy tất cả)
    const url = '/management/withdrawals' + (status ? '?status=' + encodeURIComponent(status) : '/all');
    try {
        const res = await fetch(url, {
            headers: { Authorization: 'Bearer ' + token },
            credentials: 'include'
        });
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const data = await res.json();
        renderWdTable(data);
        updateWdStats(data, status);
    } catch(e) {
        console.error('[withdrawals]', e);
        showToast('Lỗi tải dữ liệu: ' + e.message, 'error');
    }
}

function updateWdStats(data, currentStatus) {
    if (currentStatus === 'PENDING' || currentStatus === '') {
        const pending  = data.filter(d => d.status === 'PENDING').length;
        const approved = data.filter(d => d.status === 'APPROVED').length;
        const rejected = data.filter(d => d.status === 'REJECTED').length;
        const totalAmt = data.filter(d => d.status === 'PENDING')
                             .reduce((s, d) => s + (Number(d.amount) || 0), 0);
        document.getElementById('wdStatPending').textContent   = pending;
        document.getElementById('wdStatApproved').textContent  = approved;
        document.getElementById('wdStatRejected').textContent  = rejected;
        document.getElementById('wdStatTotal').textContent     = fmtVnd(totalAmt);
    }
}

function renderWdTable(data) {
    const tbody = document.getElementById('wdTableBody');
    const empty = document.getElementById('wdEmpty');
    if (!data || data.length === 0) {
        tbody.innerHTML = '';
        empty.style.display = 'block';
        return;
    }
    empty.style.display = 'none';

    const STATUS_STYLE = {
        PENDING:  'background:rgba(234,179,8,0.18);color:#ca8a04;',
        APPROVED: 'background:rgba(34,197,94,0.15);color:#16a34a;',
        REJECTED: 'background:rgba(239,68,68,0.15);color:#dc2626;',
    };
    const STATUS_LABEL = { PENDING: '⏳ PENDING', APPROVED: '✅ APPROVED', REJECTED: '❌ REJECTED' };

    tbody.innerHTML = data.map(w => {
        const st = w.status || 'PENDING';
        const stStyle = STATUS_STYLE[st] || '';
        const stLabel = STATUS_LABEL[st] || st;
        const date = w.createdAt ? new Date(w.createdAt).toLocaleDateString('vi-VN') : '—';
        const actionBtns = st === 'PENDING' ? `
            <button class="btn-sm" style="background:var(--green);color:#fff;margin-right:4px;"
                onclick="approveWithdrawal(${w.id}, '${esc(w.requestCode)}', '${fmtVnd(w.amount)}')">
                ✅ Duyệt
            </button>
            <button class="btn-sm" style="background:var(--red);color:#fff;"
                onclick="openWdRejectModal(${w.id}, '${esc(w.requestCode)}', '${fmtVnd(w.amount)}')">
                ❌ Từ chối
            </button>` : `<span style="color:var(--text3);font-size:12px;">—</span>`;

        return `<tr>
            <td style="font-family:monospace;font-size:12px;">${esc(w.requestCode)}</td>
            <td>
                <div style="font-weight:600;font-size:13px;">${esc(w.userName || '—')}</div>
                <div style="font-size:11px;color:var(--text3);">${esc(w.userEmail || '')}</div>
            </td>
            <td style="font-weight:700;color:var(--accent);font-size:14px;">${fmtVnd(w.amount)}</td>
            <td>${esc(w.bankName || '—')}</td>
            <td>
                <div style="font-family:monospace;">${esc(w.bankAccountNumber || '')}</div>
                <div style="font-size:11px;color:var(--text3);">${esc(w.bankAccountName || '')}</div>
            </td>
            <td><span class="badge" style="${stStyle}padding:4px 10px;border-radius:6px;font-size:11px;font-weight:700;">${stLabel}</span></td>
            <td class="date-text">${date}</td>
            <td class="actions">${actionBtns}</td>
        </tr>`;
    }).join('');
}

/* ─── Approve ────────────────────────────────────────────────── */

async function approveWithdrawal(id, code, amountStr) {
    if (!confirm(`✅ Xác nhận duyệt yêu cầu ${code} (${amountStr})?\n\nBạn đã chuyển khoản thành công cho khách?`)) return;
    const token = localStorage.getItem('token');
    try {
        const res = await fetch(`/management/withdrawals/${id}/approve`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
            credentials: 'include',
            body: JSON.stringify({})
        });
        const data = await res.json();
        if (res.ok) {
            showToast('✅ Đã duyệt yêu cầu rút tiền!', 'success');
            loadWithdrawals();
        } else {
            showToast('❌ ' + (data.message || 'Lỗi'), 'error');
        }
    } catch(e) {
        showToast('❌ ' + e.message, 'error');
    }
}

/* ─── Reject modal ───────────────────────────────────────────── */

function openWdRejectModal(id, code, amountStr) {
    _wdRejectingId = id;
    document.getElementById('wdRejectInfo').textContent =
        `Yêu cầu ${code} · Số tiền: ${amountStr}`;
    document.getElementById('wdRejectReason').value = '';
    document.getElementById('wdRejectMsg').textContent = '';
    document.getElementById('wdRejectModal').classList.add('active');
}

function closeWdRejectModal() {
    document.getElementById('wdRejectModal').classList.remove('active');
}

async function confirmReject() {
    const reason = (document.getElementById('wdRejectReason').value || '').trim();
    const msgEl  = document.getElementById('wdRejectMsg');
    if (!reason) { msgEl.textContent = '⚠️ Vui lòng nhập lý do từ chối.'; return; }

    const token = localStorage.getItem('token');
    try {
        const res = await fetch(`/management/withdrawals/${_wdRejectingId}/reject`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
            credentials: 'include',
            body: JSON.stringify({ reason })
        });
        const data = await res.json();
        if (res.ok) {
            closeWdRejectModal();
            showToast('Đã từ chối và hoàn tiền về ví khách.', 'success');
            loadWithdrawals();
        } else {
            msgEl.textContent = '❌ ' + (data.message || 'Lỗi');
        }
    } catch(e) {
        msgEl.textContent = '❌ ' + e.message;
    }
}

/* ─── Helpers ────────────────────────────────────────────────── */

function fmtVnd(v) {
    return new Intl.NumberFormat('vi-VN').format(Math.round(Number(v) || 0)) + '₫';
}

function esc(s) {
    if (!s) return '';
    return String(s)
        .replace(/&/g,'&amp;').replace(/</g,'&lt;')
        .replace(/>/g,'&gt;').replace(/"/g,'&quot;')
        .replace(/'/g,'&#39;');
}
