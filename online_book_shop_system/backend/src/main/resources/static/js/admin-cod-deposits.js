/* === admin-cod-deposits.js — COD Deposits (Shipper nộp tiền) Page === */

(function buildCodDepositsPage() {
    const html = `
    <div id="pageCodDeposits" class="page-section" style="display:none;">
        <div class="topbar">
            <div>
                <h1>💵 Nộp tiền COD</h1>
                <div class="breadcrumb">Admin / Nộp tiền COD</div>
            </div>
            <button class="btn btn-accent" onclick="loadCodDeposits()" style="align-self:center;">🔄 Tải lại</button>
        </div>
        <div class="content">
            <!-- Stats -->
            <div class="stats" style="margin-bottom:20px;">
                <div class="stat-card">
                    <div class="label">⏳ Chờ nộp</div>
                    <div class="value" id="cdStatPending" style="color:var(--yellow);">—</div>
                </div>
                <div class="stat-card">
                    <div class="label">✅ Đã nộp</div>
                    <div class="value" id="cdStatPaid" style="color:var(--green);">—</div>
                </div>
                <div class="stat-card">
                    <div class="label">💰 Tổng tiền chờ</div>
                    <div class="value" id="cdStatTotal" style="color:var(--accent);">—</div>
                </div>
                <div class="stat-card">
                    <div class="label">✅ Tổng đã nhận</div>
                    <div class="value" id="cdStatReceived" style="color:var(--green);">—</div>
                </div>
            </div>

            <!-- Filter -->
            <div class="toolbar" style="margin-bottom:16px;">
                <span style="font-size:13px;color:var(--text2);">Lọc:</span>
                <select id="cdStatusFilter" class="filter-select" onchange="loadCodDeposits()">
                    <option value="">Tất cả</option>
                    <option value="PENDING" selected>⏳ PENDING — Chờ nộp</option>
                    <option value="PAID">✅ PAID — Đã nộp</option>
                    <option value="CANCELLED">❌ CANCELLED</option>
                </select>
            </div>

            <!-- Table -->
            <div class="table-wrapper">
                <table>
                    <thead><tr>
                        <th>Mã nộp</th>
                        <th>Shipper</th>
                        <th>Số tiền</th>
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                        <th>Ngày nộp</th>
                    </tr></thead>
                    <tbody id="cdTableBody"></tbody>
                </table>
                <div id="cdEmpty" class="empty-state" style="display:none;">
                    <div class="icon">💵</div>
                    <p>Không có giao dịch nộp tiền nào.</p>
                </div>
            </div>
        </div>
    </div>`;

    document.querySelector('.main').insertAdjacentHTML('beforeend', html);
})();

/* Add nav item after Yêu cầu rút tiền */
(function addCodDepositsNav() {
    const wdNav = document.getElementById('navWithdrawals');
    const ref = wdNav || document.getElementById('navSupport');
    if (!ref) return;
    const nav = document.createElement('a');
    nav.className = 'nav-item';
    nav.id = 'navCodDeposits';
    nav.innerHTML = '<span class="emoji">💵</span> Nộp tiền COD';
    nav.setAttribute('onclick', "switchPage('codDeposits')");
    ref.after(nav);
})();

/* ─── Load & Render ─────────────────────────────────────────── */

async function loadCodDeposits() {
    const status = document.getElementById('cdStatusFilter')?.value || '';
    const token = localStorage.getItem('token');
    const url = '/staff/wallet/admin/cod-deposits' + (status ? '?status=' + encodeURIComponent(status) : '');
    try {
        const res = await fetch(url, {
            headers: { Authorization: 'Bearer ' + token },
            credentials: 'include'
        });
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const data = await res.json();
        renderCdTable(data);
        updateCdStats(data);
    } catch(e) {
        console.error('[cod-deposits]', e);
        showToast('Lỗi tải dữ liệu: ' + e.message, 'error');
    }
}

function updateCdStats(data) {
    const pending  = data.filter(d => d.status === 'PENDING');
    const paid     = data.filter(d => d.status === 'PAID');
    const totalAmt = pending.reduce((s, d) => s + (Number(d.amount) || 0), 0);
    const receivedAmt = paid.reduce((s, d) => s + (Number(d.amount) || 0), 0);
    document.getElementById('cdStatPending').textContent  = pending.length;
    document.getElementById('cdStatPaid').textContent     = paid.length;
    document.getElementById('cdStatTotal').textContent    = fmtVndCd(totalAmt);
    document.getElementById('cdStatReceived').textContent = fmtVndCd(receivedAmt);
}

function renderCdTable(data) {
    const tbody = document.getElementById('cdTableBody');
    const empty = document.getElementById('cdEmpty');
    if (!data || data.length === 0) {
        tbody.innerHTML = '';
        empty.style.display = 'block';
        return;
    }
    empty.style.display = 'none';

    const ST_STYLE = {
        PENDING:   'background:rgba(234,179,8,0.18);color:#ca8a04;',
        PAID:      'background:rgba(34,197,94,0.15);color:#16a34a;',
        CANCELLED: 'background:rgba(239,68,68,0.15);color:#dc2626;',
    };
    const ST_LABEL = { PENDING: '⏳ Chờ nộp', PAID: '✅ Đã nộp', CANCELLED: '❌ Huỷ' };

    tbody.innerHTML = data.map(d => {
        const st = d.status || 'PENDING';
        const date = d.createdAt ? new Date(d.createdAt).toLocaleDateString('vi-VN') : '—';
        const paidDate = d.paidAt ? new Date(d.paidAt).toLocaleDateString('vi-VN') : '—';
        return `<tr>
            <td style="font-family:monospace;font-size:12px;">${esc(d.depositCode)}</td>
            <td>
                <div style="font-weight:600;font-size:13px;">${esc(d.staffName || '—')}</div>
                <div style="font-size:11px;color:var(--text3);">${esc(d.staffEmail || '')}</div>
            </td>
            <td style="font-weight:700;color:var(--yellow);font-size:14px;">${fmtVndCd(d.amount)}</td>
            <td><span class="badge" style="${ST_STYLE[st]}padding:4px 10px;border-radius:6px;font-size:11px;font-weight:700;">${ST_LABEL[st] || st}</span></td>
            <td class="date-text">${date}</td>
            <td class="date-text">${paidDate}</td>
        </tr>`;
    }).join('');
}

/* ─── Helpers ────────────────────────────────────────────────── */
function fmtVndCd(v) {
    return new Intl.NumberFormat('vi-VN').format(Math.round(Number(v) || 0)) + '₫';
}
function esc(s) {
    if (!s) return '';
    return String(s)
        .replace(/&/g,'&amp;').replace(/</g,'&lt;')
        .replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');
}
