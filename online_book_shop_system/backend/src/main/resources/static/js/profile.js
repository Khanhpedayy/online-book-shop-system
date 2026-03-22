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
        if (j && j.error) {
            return j.error;
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
    document.getElementById('pfAvatar').value = data.avatarUrl || '';
}

async function saveProfile() {
    const msg = document.getElementById('pfMsg');
    try {
        await apiPut('/api/me/profile', {
            fullName: document.getElementById('pfFullName').value,
            phone: document.getElementById('pfPhone').value,
            avatarUrl: document.getElementById('pfAvatar').value
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
    const body = {
        label: document.getElementById('addrLabel').value,
        recipientName: document.getElementById('addrRecipient').value,
        phone: document.getElementById('addrPhone').value,
        line1: document.getElementById('addrLine1').value,
        line2: document.getElementById('addrLine2').value,
        city: document.getElementById('addrCity').value,
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
    } catch (e) {
        document.getElementById('pfMsg').textContent = e.message || 'Không tải được hồ sơ';
        document.getElementById('pfMsg').style.color = '#c00';
    }
    document.getElementById('btnSaveProfile').addEventListener('click', () => saveProfile());
    document.getElementById('btnChangePw').addEventListener('click', () => changePassword());
    document.getElementById('btnAddrSubmit').addEventListener('click', () => submitAddress());
    document.getElementById('btnAddrCancel').addEventListener('click', () => clearAddrForm());
}

document.addEventListener('DOMContentLoaded', init);
