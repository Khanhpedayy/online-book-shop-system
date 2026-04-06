const API_BASE = 'http://localhost:8080';

function getToken() {
    return localStorage.getItem('token');
}

function authHeaders(json) {
    const h = {};
    if (json) {
        h['Content-Type'] = 'application/json';
    }
    const t = getToken();
    if (t) {
        h['Authorization'] = 'Bearer ' + t;
    }
    return h;
}

async function parseError(resp) {
    const text = await resp.text();
    try {
        const j = JSON.parse(text);
        if (j) {
            if (j.message) {
                return j.message;
            }
            if (j.error) {
                return j.error;
            }
        }
    } catch (e) { /* ignore */ }
    return text || resp.statusText;
}

async function apiGet(path) {
    const r = await fetch(API_BASE + path, { headers: authHeaders(false), credentials: 'include' });
    if (!r.ok) {
        throw new Error(await parseError(r));
    }
    return r.json();
}

async function apiPut(path, body) {
    const r = await fetch(API_BASE + path, {
        method: 'PUT',
        headers: authHeaders(true),
        credentials: 'include',
        body: JSON.stringify(body)
    });
    if (!r.ok) {
        throw new Error(await parseError(r));
    }
    return r.json();
}

async function apiPost(path, body) {
    const r = await fetch(API_BASE + path, {
        method: 'POST',
        headers: authHeaders(true),
        credentials: 'include',
        body: body != null ? JSON.stringify(body) : '{}'
    });
    if (!r.ok) {
        throw new Error(await parseError(r));
    }
    if (r.status === 204 || r.headers.get('content-length') === '0') {
        return null;
    }
    const ct = r.headers.get('content-type');
    if (ct && ct.includes('application/json')) {
        const t = await r.text();
        return t ? JSON.parse(t) : null;
    }
    return null;
}

async function apiDelete(path) {
    const r = await fetch(API_BASE + path, {
        method: 'DELETE',
        headers: authHeaders(false),
        credentials: 'include'
    });
    if (!r.ok) {
        throw new Error(await parseError(r));
    }
}

function showMsg(el, text, ok) {
    if (!el) {
        return;
    }
    el.textContent = text || '';
    el.style.color = ok ? '#0a0' : '#c00';
}

async function loadProfile() {
    const data = await apiGet('/api/me/profile');
    document.getElementById('pfEmail').value = data.email || '';
    document.getElementById('pfFullName').value = data.fullName || '';
    document.getElementById('pfPhone').value = data.phone || '';
}

async function saveProfile() {
    const msg = document.getElementById('pfMsg');
    const phoneRaw = document.getElementById('pfPhone').value.trim();
    if (phoneRaw !== '' && !isValidVnPhone(phoneRaw)) {
        showMsg(msg, 'Số điện thoại không hợp lệ (VD: 09xxxxxxxx hoặc +84…).', false);
        return;
    }
    try {
        await apiPut('/api/me/profile', {
            fullName: document.getElementById('pfFullName').value,
            phone: phoneRaw === '' ? '' : normalizeVnPhone(phoneRaw)
        });
        showMsg(msg, 'Đã lưu hồ sơ.', true);
    } catch (e) {
        showMsg(msg, e.message || 'Lỗi', false);
    }
}

async function changePassword() {
    const msg = document.getElementById('pwMsg');
    const cur = document.getElementById('pwCurrent').value;
    const nw = document.getElementById('pwNew').value;
    const cf = document.getElementById('pwConfirm').value;
    if (nw !== cf) {
        showMsg(msg, 'Mật khẩu mới và xác nhận không khớp.', false);
        return;
    }
    try {
        await apiPost('/api/me/change-password', {
            currentPassword: cur,
            newPassword: nw
        });
        document.getElementById('pwCurrent').value = '';
        document.getElementById('pwNew').value = '';
        document.getElementById('pwConfirm').value = '';
        showMsg(msg, 'Đã đổi mật khẩu.', true);
    } catch (e) {
        showMsg(msg, e.message || 'Lỗi', false);
    }
}

function renderAddresses(list) {
    const box = document.getElementById('addrList');
    if (!list || list.length === 0) {
        box.innerHTML = '<p class="muted">Chưa có địa chỉ.</p>';
        return;
    }
    box.innerHTML = list.map(a => `
        <div class="addr-card" data-id="${a.id}">
            <div class="addr-head">
                <strong>${escapeHtml(a.recipientName)}</strong>
                ${a.defaultAddress ? '<span class="badge-def">Mặc định</span>' : ''}
            </div>
            <div>${escapeHtml(a.line1)}${a.line2 ? '<br>' + escapeHtml(a.line2) : ''}</div>
            <div>${escapeHtml(a.city || '')} · ${escapeHtml(a.phone || '')}</div>
            <div class="addr-actions">
                ${!a.defaultAddress ? `<button type="button" class="btn-sm" data-def="${a.id}">Đặt mặc định</button>` : ''}
                <button type="button" class="btn-sm secondary" data-edit="${a.id}">Sửa</button>
                <button type="button" class="btn-sm danger" data-del="${a.id}">Xóa</button>
            </div>
        </div>
    `).join('');

    box.querySelectorAll('[data-def]').forEach(btn => {
        btn.addEventListener('click', () => setDefault(Number(btn.getAttribute('data-def'))));
    });
    box.querySelectorAll('[data-del]').forEach(btn => {
        btn.addEventListener('click', () => removeAddr(Number(btn.getAttribute('data-del'))));
    });
    box.querySelectorAll('[data-edit]').forEach(btn => {
        btn.addEventListener('click', () => startEdit(Number(btn.getAttribute('data-edit'))));
    });
}

function escapeHtml(s) {
    if (!s) {
        return '';
    }
    return String(s)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

let addressesCache = [];

async function refreshAddresses() {
    addressesCache = await apiGet('/api/me/addresses');
    renderAddresses(addressesCache);
}

async function setDefault(id) {
    const msg = document.getElementById('addrMsg');
    try {
        await apiPost('/api/me/addresses/' + id + '/default', null);
        await refreshAddresses();
        showMsg(msg, 'Đã cập nhật địa chỉ mặc định.', true);
    } catch (e) {
        showMsg(msg, e.message || 'Lỗi', false);
    }
}

async function removeAddr(id) {
    if (!confirm('Xóa địa chỉ này?')) {
        return;
    }
    const msg = document.getElementById('addrMsg');
    try {
        await apiDelete('/api/me/addresses/' + id);
        await refreshAddresses();
        showMsg(msg, 'Đã xóa.', true);
        clearAddrForm();
    } catch (e) {
        showMsg(msg, e.message || 'Lỗi', false);
    }
}

let editingId = null;

function clearAddrForm() {
    editingId = null;
    document.getElementById('addrFormTitle').textContent = 'Thêm địa chỉ';
    document.getElementById('addrLabel').value = '';
    document.getElementById('addrRecipient').value = '';
    document.getElementById('addrPhone').value = '';
    document.getElementById('addrLine1').value = '';
    document.getElementById('addrLine2').value = '';
    document.getElementById('addrCity').value = '';
    document.getElementById('addrDefault').checked = false;
}

function startEdit(id) {
    const a = addressesCache.find(x => x.id === id);
    if (!a) {
        return;
    }
    editingId = id;
    document.getElementById('addrFormTitle').textContent = 'Sửa địa chỉ';
    document.getElementById('addrLabel').value = a.label || '';
    document.getElementById('addrRecipient').value = a.recipientName || '';
    document.getElementById('addrPhone').value = a.phone || '';
    document.getElementById('addrLine1').value = a.line1 || '';
    document.getElementById('addrLine2').value = a.line2 || '';
    document.getElementById('addrCity').value = a.city || '';
    document.getElementById('addrDefault').checked = !!a.defaultAddress;
    document.getElementById('addrRecipient').scrollIntoView({ behavior: 'smooth', block: 'center' });
}

async function submitAddress() {
    const msg = document.getElementById('addrMsg');
    const recipientName = document.getElementById('addrRecipient').value.trim();
    const line1 = document.getElementById('addrLine1').value.trim();
    if (!recipientName) {
        showMsg(msg, 'Recipient is required.', false);
        return;
    }
    if (!line1) {
        showMsg(msg, 'Address line 1 is required.', false);
        return;
    }
    const phoneRaw = document.getElementById('addrPhone').value.trim();
    if (phoneRaw !== '' && !isValidVnPhone(phoneRaw)) {
        showMsg(msg, 'Số điện thoại không hợp lệ (VD: 09xxxxxxxx).', false);
        return;
    }
    const body = {
        label: document.getElementById('addrLabel').value.trim(),
        recipientName,
        phone: phoneRaw === '' ? '' : normalizeVnPhone(phoneRaw),
        line1,
        line2: document.getElementById('addrLine2').value.trim(),
        city: document.getElementById('addrCity').value.trim(),
        defaultAddress: document.getElementById('addrDefault').checked
    };
    try {
        if (editingId) {
            await apiPut('/api/me/addresses/' + editingId, body);
            showMsg(msg, 'Đã cập nhật địa chỉ.', true);
        } else {
            await apiPost('/api/me/addresses', body);
            showMsg(msg, 'Đã thêm địa chỉ.', true);
        }
        clearAddrForm();
        await refreshAddresses();
    } catch (e) {
        showMsg(msg, e.message || 'Lỗi', false);
    }
}

async function init() {
    const ok = await syncAuthFromServerSession(API_BASE);
    if (!getToken()) {
        window.location.assign(API_BASE + '/login');
        return;
    }
    try {
        await loadProfile();
        await refreshAddresses();
        await loadWallet();
    } catch (e) {
        document.getElementById('pfMsg').textContent = e.message || 'Không tải được hồ sơ';
        document.getElementById('pfMsg').style.color = '#c00';
    }
    document.getElementById('btnSaveProfile').addEventListener('click', () => saveProfile());
    document.getElementById('btnChangePw').addEventListener('click', () => changePassword());
    document.getElementById('btnAddrSubmit').addEventListener('click', () => submitAddress());
    document.getElementById('btnAddrCancel').addEventListener('click', () => clearAddrForm());
    document.getElementById('btnWithdraw').addEventListener('click', () => submitWithdrawal());
}

// ─── VÍ ẢO ─────────────────────────────────────────────────────────

function formatVnd(amount) {
    if (amount == null) return '0₫';
    return new Intl.NumberFormat('vi-VN').format(Math.round(Number(amount))) + '₫';
}

async function loadWallet() {
    try {
        const data = await apiGet('/api/wallet/me');
        renderWallet(data);
        document.getElementById('walletSection').style.display = '';
    } catch (e) {
        // Nếu lỗi 401/403 thì không hiện section ví
        console.warn('[wallet] loadWallet error:', e.message);
    }
}

function renderWallet(data) {
    // Số dư
    document.getElementById('walletBalance').textContent = formatVnd(data.balance);

    // Pending alert
    const pendingAlert = document.getElementById('walletPendingAlert');
    pendingAlert.style.display = data.hasPendingWithdrawal ? '' : 'none';

    // Disable form nếu có pending
    const wdForm = document.getElementById('walletForm');
    const inputs = wdForm.querySelectorAll('input, button');
    inputs.forEach(el => el.disabled = data.hasPendingWithdrawal);

    // Withdrawal gần nhất
    const withdrawals = data.withdrawals || [];
    const latestWd = withdrawals[0];
    const wdSection = document.getElementById('walletCurrentWithdrawal');
    const wdDetail = document.getElementById('walletWithdrawalDetail');
    if (latestWd) {
        wdSection.style.display = '';
        const statusLabel = { PENDING: '⏳ Đang chờ xử lý', APPROVED: '✅ Đã duyệt', REJECTED: '❌ Đã từ chối' };
        const statusColors = { PENDING: '#d97706', APPROVED: '#16a34a', REJECTED: '#dc2626' };
        const st = latestWd.status || 'PENDING';
        wdDetail.innerHTML = `
            <div style="display:flex;justify-content:space-between;align-items:center;">
                <div>
                    <div style="font-weight:600;">${latestWd.requestCode}</div>
                    <div style="color:#555;font-size:0.85rem;">Số tiền: <strong>${formatVnd(latestWd.amount)}</strong></div>
                    <div style="color:#555;font-size:0.85rem;">${latestWd.bankName} — ${latestWd.bankAccountNumber}</div>
                    ${latestWd.adminNote ? `<div style="color:#888;font-size:0.82rem;margin-top:4px;">Ghi chú: ${escapeHtml(latestWd.adminNote)}</div>` : ''}
                </div>
                <span style="font-weight:600;color:${statusColors[st]}">${statusLabel[st] || st}</span>
            </div>
        `;
    } else {
        wdSection.style.display = 'none';
    }

    // Lịch sử giao dịch
    const txList = document.getElementById('walletTxList');
    const txs = data.transactions || [];
    if (!txs.length) {
        txList.innerHTML = '<p style="color:#888;font-size:0.9rem;">Chưa có giao dịch nào.</p>';
    } else {
        txList.innerHTML = txs.map(tx => {
            const isCredit = tx.type === 'CREDIT';
            return `
                <div style="display:flex;align-items:center;justify-content:space-between;padding:10px 0;border-bottom:1px solid #f1f5f9;">
                    <div>
                        <div style="font-size:0.9rem;color:#374151;">${escapeHtml(tx.note || tx.refType || '')}</div>
                        <div style="font-size:0.78rem;color:#9ca3af;">
                            ${tx.createdAt ? new Date(tx.createdAt).toLocaleString('vi-VN') : ''}
                        </div>
                    </div>
                    <div style="font-weight:700;color:${isCredit ? '#16a34a' : '#dc2626'};">
                        ${isCredit ? '+' : '-'}${formatVnd(tx.amount)}
                    </div>
                </div>
            `;
        }).join('');
    }
}

async function submitWithdrawal() {
    const msg = document.getElementById('wdMsg');
    const amount = Number(document.getElementById('wdAmount').value) || 0;
    const bankName = document.getElementById('wdBank').value.trim();
    const bankAccountNumber = document.getElementById('wdAccNo').value.trim();
    const bankAccountName = document.getElementById('wdAccName').value.trim();

    if (amount <= 0) { showMsg(msg, 'Vui lòng nhập số tiền hợp lệ.', false); return; }
    if (!bankName) { showMsg(msg, 'Vui lòng nhập tên ngân hàng.', false); return; }
    if (!bankAccountNumber) { showMsg(msg, 'Vui lòng nhập số tài khoản.', false); return; }
    if (!bankAccountName) { showMsg(msg, 'Vui lòng nhập tên chủ tài khoản.', false); return; }

    try {
        await apiPost('/api/wallet/withdrawal', {
            amount, bankName, bankAccountNumber, bankAccountName
        });
        showMsg(msg, '✅ Gửi yêu cầu rút tiền thành công! Admin sẽ xem xét trong thời gian sớm nhất.', true);
        // Reload wallet
        await loadWallet();
    } catch (e) {
        showMsg(msg, e.message || 'Lỗi không xác định.', false);
    }
}

document.addEventListener('DOMContentLoaded', init);

