/* === admin-review-reports.js — Review Reports Page === */

/* Inject page HTML once */
(function buildReviewReportsPage() {
    const html = `
    <div id="pageReviewReports" class="page-section" style="display:none;">
        <div class="topbar">
            <div>
                <h1>🚩 Report &amp; Support</h1>
                <div class="breadcrumb">Admin / Report &amp; Support</div>
            </div>
        </div>
        <div class="content">
            <div class="toolbar" style="margin-bottom:16px;">
                <span style="font-size:13px;color:var(--text2);">Lọc trạng thái:</span>
                <select id="rrStatusFilter" class="filter-select" onchange="loadReviewReports()">
                    <option value="">Tất cả</option>
                    <option value="PENDING" selected>Chờ xử lý</option>
                    <option value="APPROVED">Đã xoá review</option>
                    <option value="REJECTED">Đã từ chối</option>
                </select>
                <button class="btn btn-accent" onclick="loadReviewReports()">🔄 Tải lại</button>
            </div>
            <div class="table-wrapper">
                <table>
                    <thead><tr>
                        <th>#</th><th>Review (bị báo)</th><th>Người viết</th><th>Người báo cáo</th><th>Lý do</th><th>Trạng thái</th><th>Ngày</th><th>Hành động</th>
                    </tr></thead>
                    <tbody id="rrTableBody"></tbody>
                </table>
                <div id="rrEmpty" class="empty-state" style="display:none;"><div class="icon">🚩</div><p>Không có báo cáo nào.</p></div>
            </div>
        </div>
    </div>

    <!-- Report detail modal -->
    <div id="rrModal" class="modal-overlay">
        <div class="modal" style="max-width:600px;">
            <h2 id="rrModalTitle">Chi tiết báo cáo</h2>

            <div style="background:rgba(255,255,255,0.04);border-radius:10px;padding:14px 16px;margin-bottom:16px;border:1px solid var(--border);">
                <div style="font-size:11px;color:var(--text3);margin-bottom:6px;">📝 NỘI DUNG REVIEW BỊ BÁO CÁO</div>
                <div id="rrReviewText" style="font-size:13.5px;color:var(--text);line-height:1.6;"></div>
                <div style="margin-top:8px;color:var(--text2);font-size:12px;">Người viết: <span id="rrReviewAuthor"></span> &bull; <span id="rrReviewStars"></span></div>
            </div>

            <div style="margin-bottom:16px;">
                <div style="font-size:11px;color:var(--text3);margin-bottom:6px;">📋 LÝ DO BÁO CÁO</div>
                <div id="rrReasonText" style="font-size:13px;color:var(--text2);white-space:pre-wrap;"></div>
                <div style="margin-top:6px;font-size:12px;color:var(--text3);">Người báo cáo: <span id="rrReporterName"></span></div>
            </div>

            <div id="rrRejectGroup" style="display:none;margin-bottom:12px;">
                <label style="display:block;font-size:12px;color:var(--text2);margin-bottom:6px;">Lý do từ chối (bắt buộc)</label>
                <textarea id="rrAdminNote" placeholder="Nhập lý do từ chối..." style="width:100%;padding:10px 12px;background:var(--input-bg);border:1px solid var(--border);border-radius:10px;color:var(--text);font-family:inherit;font-size:13px;min-height:80px;resize:vertical;"></textarea>
            </div>

            <div id="rrModalMsg" style="color:var(--red);font-size:12px;min-height:1.2em;margin-bottom:8px;"></div>

            <div class="modal-actions">
                <button class="btn-ghost" onclick="closeRRModal()">Đóng</button>
                <button class="btn" style="background:var(--red);color:#fff;" id="btnRRReject" onclick="showRejectInput()">❌ Từ chối</button>
                <button class="btn btn-accent" id="btnRRApprove" onclick="decideReport(true)">🗑 Xoá review</button>
                <button class="btn" style="background:var(--yellow);color:#000;display:none;" id="btnRRSendReject" onclick="decideReport(false)">📨 Gửi từ chối</button>
            </div>
        </div>
    </div>`;

    document.querySelector('.main').insertAdjacentHTML('beforeend', html);
})();

var _currentReportId = null;

async function loadReviewReports() {
    const status = document.getElementById('rrStatusFilter')?.value || '';
    const token = localStorage.getItem('token');
    try {
        const url = '/api/admin/review-reports' + (status ? '?status=' + status : '');
        const res = await fetch(url, {
            headers: { Authorization: 'Bearer ' + token },
            credentials: 'include'
        });
        const data = await res.json();
        renderRRTable(data);
    } catch(e) {
        console.error(e);
    }
}

function renderRRTable(data) {
    const tbody = document.getElementById('rrTableBody');
    const empty = document.getElementById('rrEmpty');
    if (!data || data.length === 0) {
        tbody.innerHTML = '';
        empty.style.display = 'block';
        return;
    }
    empty.style.display = 'none';
    tbody.innerHTML = data.map(r => {
        const stars = '★'.repeat(r.reviewRating || 0) + '☆'.repeat(5 - (r.reviewRating || 0));
        const badgeClass = r.status === 'PENDING' ? 'badge-role' : r.status === 'APPROVED' ? 'badge-active' : 'badge-inactive';
        const date = r.createdAt ? new Date(r.createdAt).toLocaleDateString('vi-VN') : '';
        const actions = r.status === 'PENDING' ?
            `<button class="btn-sm" onclick="openRRModal(${r.id})">🔍 Xem xét</button>` :
            `<span style="color:var(--text3);font-size:12px;">${r.status === 'APPROVED' ? '✅ Đã xoá' : '❌ Đã từ chối'}</span>`;
        return `<tr>
            <td style="font-size:12px;color:var(--text3);">#${r.id}</td>
            <td style="max-width:160px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title="${esc(r.reviewContent || '')}">${esc((r.reviewTitle || r.reviewContent || '').substring(0, 50))}</td>
            <td>${esc(r.reviewerName || '')}</td>
            <td>${esc(r.reporterName || '')}</td>
            <td style="max-width:160px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title="${esc(r.reason || '')}">${esc((r.reason || '').substring(0, 50))}</td>
            <td><span class="badge ${badgeClass}">${r.status}</span></td>
            <td class="date-text">${date}</td>
            <td class="actions">${actions}</td>
        </tr>`;
    }).join('');
}

function openRRModal(reportId) {
    _currentReportId = reportId;
    const token = localStorage.getItem('token');
    fetch('/api/admin/review-reports', {
        headers: { Authorization: 'Bearer ' + token }, credentials: 'include'
    }).then(r => r.json()).then(data => {
        const r = data.find(x => x.id === reportId);
        if (!r) return;
        document.getElementById('rrModalTitle').textContent = 'Chi tiết báo cáo #' + r.id;
        document.getElementById('rrReviewText').textContent = (r.reviewTitle ? '"' + r.reviewTitle + '" — ' : '') + (r.reviewContent || '');
        document.getElementById('rrReviewAuthor').textContent = r.reviewerName || '';
        document.getElementById('rrReviewStars').textContent = '★'.repeat(r.reviewRating || 0);
        document.getElementById('rrReasonText').textContent = r.reason || '';
        document.getElementById('rrReporterName').textContent = r.reporterName || '';
        document.getElementById('rrRejectGroup').style.display = 'none';
        document.getElementById('btnRRReject').style.display = 'inline-flex';
        document.getElementById('btnRRApprove').style.display = 'inline-flex';
        document.getElementById('btnRRSendReject').style.display = 'none';
        document.getElementById('rrAdminNote').value = '';
        document.getElementById('rrModalMsg').textContent = '';
        document.getElementById('rrModal').classList.add('active');
    });
}

function closeRRModal() {
    document.getElementById('rrModal').classList.remove('active');
}

function showRejectInput() {
    document.getElementById('rrRejectGroup').style.display = 'block';
    document.getElementById('btnRRReject').style.display = 'none';
    document.getElementById('btnRRSendReject').style.display = 'inline-flex';
}

async function decideReport(approve) {
    const token = localStorage.getItem('token');
    const note = approve ? '' : (document.getElementById('rrAdminNote').value || '').trim();
    if (!approve && !note) {
        document.getElementById('rrModalMsg').textContent = '⚠️ Vui lòng nhập lý do từ chối.';
        return;
    }
    try {
        const res = await fetch('/api/admin/review-reports/' + _currentReportId + '/decide', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
            credentials: 'include',
            body: JSON.stringify({ approve, adminNote: note })
        });
        const data = await res.json();
        if (res.ok) {
            closeRRModal();
            showToast(data.message || 'Thành công', 'success');
            loadReviewReports();
        } else {
            document.getElementById('rrModalMsg').textContent = '❌ ' + (data.message || 'Lỗi');
        }
    } catch(e) {
        document.getElementById('rrModalMsg').textContent = '❌ ' + e.message;
    }
}
