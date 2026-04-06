/* === admin-core.js — Auth, Users, Dashboard, Reports, Helpers === */

const API = '';
let allUsers = [];
let sortField = 'createdAt';
let sortDir = 'desc';
let editingId = null;
let searchTimeout = null;

// === Auth check ===
function getToken() { return localStorage.getItem('token'); }
function getUser() {
    try { return JSON.parse(localStorage.getItem('user')); } catch { return null; }
}
function authHeaders() {
    const token = getToken();
    const headers = { 'Content-Type': 'application/json' };
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    return headers;
}

(function initTheme() {
    const savedTheme = localStorage.getItem('adminTheme');
    if (savedTheme === 'light') {
        document.body.classList.add('light-mode');
    }
})();

function toggleTheme() {
    document.body.classList.toggle('light-mode');
    if (document.body.classList.contains('light-mode')) {
        localStorage.setItem('adminTheme', 'light');
    } else {
        localStorage.setItem('adminTheme', 'dark');
    }
}

(function checkAdmin() {
    const user = getUser();
    const token = getToken();

    if (!token || !user || user.role !== 'ADMIN') {
        window.location.href = '/login';
        return;
    }

    document.getElementById('adminName').textContent = user.fullName || 'Admin';
    document.getElementById('adminAvatar').textContent = (user.fullName || 'A').charAt(0).toUpperCase();
})();

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/logout';
}

// === Load users ===
async function loadUsers() {
    const search = document.getElementById('searchInput').value.trim();
    const role = document.getElementById('filterRole').value;
    const status = document.getElementById('filterStatus').value;

    const params = new URLSearchParams();
    if (search) params.append('search', search);
    if (role) params.append('role', role);
    if (status) params.append('status', status);

    try {
        const res = await fetch(API + '/api/admin/users?' + params.toString(), {
            headers: authHeaders()
        });
        if (res.status === 403) { toast('error', 'Bạn không có quyền truy cập'); return; }
        if (res.status === 401) { logout(); return; }
        allUsers = await res.json();
        sortAndRender();
    } catch (e) {
        toast('error', 'Không thể kết nối server');
    }
}

// === Sort ===
function sortBy(field) {
    if (sortField === field) {
        sortDir = sortDir === 'asc' ? 'desc' : 'asc';
    } else {
        sortField = field;
        sortDir = 'asc';
    }
    sortAndRender();
}
function sortAndRender() {
    const sorted = [...allUsers].sort((a, b) => {
        let va = a[sortField] || '';
        let vb = b[sortField] || '';
        if (typeof va === 'string') va = va.toLowerCase();
        if (typeof vb === 'string') vb = vb.toLowerCase();
        if (va < vb) return sortDir === 'asc' ? -1 : 1;
        if (va > vb) return sortDir === 'asc' ? 1 : -1;
        return 0;
    });
    renderTable(sorted);
    updateStats();
}

// === Render ===
function renderTable(users) {
    const tbody = document.getElementById('userTableBody');
    const empty = document.getElementById('emptyState');

    if (users.length === 0) {
        tbody.innerHTML = '';
        empty.style.display = 'block';
        return;
    }
    empty.style.display = 'none';

    tbody.innerHTML = users.map(u => `
    <tr>
        <td style="color:var(--text3); font-size:12px;">#${u.id}</td>
        <td>
            <div class="user-cell">
                <div class="avatar">${(u.fullName || '?').charAt(0).toUpperCase()}</div>
                <div class="info">
                    <div class="name">${esc(u.fullName)}</div>
                    <div class="email">${esc(u.email)}</div>
                </div>
            </div>
        </td>
        <td><span class="badge badge-role ${u.roleCode?.toLowerCase()}">${esc(u.roleName || u.roleCode)}</span></td>
        <td><span class="badge ${u.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'}">
            ${u.status === 'ACTIVE' ? '● Hoạt động' : '○ Vô hiệu hóa'}</span></td>
        <td style="color:var(--text2); font-size:13px;">${esc(u.phone || '—')}</td>
        <td><span class="date-text">${formatDate(u.createdAt)}</span></td>
        <td>
            <div class="actions">
                <button class="btn-sm" onclick="openEdit(${u.id})" title="Chỉnh sửa">✏️</button>
                ${u.status === 'ACTIVE'
            ? `<button class="btn-sm danger" onclick="toggleStatus(${u.id})" title="Vô hiệu hóa">🔒</button>`
            : `<button class="btn-sm success" onclick="toggleStatus(${u.id})" title="Kích hoạt">🔓</button>`
        }
            </div>
        </td>
    </tr>
    `).join('');
}

function updateStats() {
    document.getElementById('statTotal').textContent = allUsers.length;
    document.getElementById('statActive').textContent = allUsers.filter(u => u.status === 'ACTIVE').length;
    document.getElementById('statInactive').textContent = allUsers.filter(u => u.status !== 'ACTIVE').length;
    document.getElementById('statStaff').textContent = allUsers.filter(u => ['ADMIN', 'STAFF', 'MANAGER'].includes(u.roleCode)).length;
}

// === Search debounce ===
function debounceSearch() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => loadUsers(), 350);
}

// === Modal ===
function resetValidation() {
    ['mFullName', 'mEmail', 'mPhone', 'mPassword'].forEach(id => {
        const el = document.getElementById(id);
        const err = document.getElementById('err-' + id);
        el.classList.remove('valid', 'invalid');
        if (err) {
            err.textContent = '';
            err.style.display = 'none';
        }
    });
}

function validateField(fieldId) {
    const el = document.getElementById(fieldId);
    const err = document.getElementById('err-' + fieldId);
    const val = el.value.trim();
    let isValid = true;
    let msg = '';

    // Reset
    el.classList.remove('valid', 'invalid');
    if (err) { err.style.display = 'none'; err.innerHTML = ''; }

    if (fieldId === 'mFullName') {
        if (!val) {
            isValid = false; msg = 'Họ tên không được để trống';
        } else {
            const isDup = allUsers.some(u => String(u.id) !== String(editingId) && u.fullName && u.fullName.trim().toLowerCase() === val.toLowerCase());
            if (isDup) {
                isValid = false; msg = 'Họ tên này đã tồn tại trong hệ thống';
            }
        }
    }

    if (fieldId === 'mEmail') {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!val) {
            isValid = false; msg = 'Email không được để trống';
        } else if (!emailRegex.test(val)) {
            isValid = false; msg = 'Email không đúng định dạng';
        } else {
            const isDup = allUsers.some(u => String(u.id) !== String(editingId) && u.email && u.email.trim().toLowerCase() === val.toLowerCase());
            if (isDup) {
                isValid = false; msg = 'Email này đã được sử dụng';
            }
        }
    }

    if (fieldId === 'mPhone') {
        const phoneRegex = /^[0-9]{10,11}$/;
        if (val && !phoneRegex.test(val)) {
            isValid = false; msg = 'Số điện thoại phải từ 10-11 chữ số';
        } else if (val) {
            const cleanPhone = val.replace(/\D/g, '');
            const isDup = allUsers.some(u => String(u.id) !== String(editingId) && u.phone && u.phone.replace(/\D/g, '') === cleanPhone);
            if (isDup) {
                isValid = false; msg = 'Số điện thoại này đã được sử dụng';
            }
        }
    }

    if (fieldId === 'mPassword') {
        if (!val && !editingId) {
            // Creating new user: required
            isValid = false; msg = 'Mật khẩu không được để trống';
        } else if (val) {
            // MUST meet strong criteria
            const hasUpper = /[A-Z]/.test(val);
            const hasLower = /[a-z]/.test(val);
            const hasNumber = /[0-9]/.test(val);
            // Standard regex for special chars:
            const hasSpecial = /[!@#$%^&*(),.?":{}|<>\-_+=\[\]\\/;'~`]/.test(val);

            if (val.length < 8 || !hasUpper || !hasLower || !hasNumber || !hasSpecial) {
                isValid = false;
                msg = 'Mật khẩu yếu. Yêu cầu:<br>• Tối thiểu 8 ký tự<br>• Gồm chữ in HOA, in thường<br>• Gồm số và ký tự đặc biệt';
            }
        } else if (!val && editingId) {
            // Editing, leave empty -> ignore
            isValid = true;
        }
    }

    if (isValid) {
        if (val || (!val && fieldId === 'mPhone')) el.classList.add('valid');
    } else {
        el.classList.add('invalid');
        if (err) {
            err.innerHTML = msg;
            err.style.display = 'block';
        }
    }

    return isValid;
}

function openModal() {
    editingId = null;
    resetValidation();
    document.getElementById('modalTitle').textContent = 'Thêm tài khoản mới';
    document.getElementById('modalSubmitBtn').textContent = 'Tạo tài khoản';
    document.getElementById('mFullName').value = '';
    document.getElementById('mEmail').value = '';
    document.getElementById('mPhone').value = '';
    document.getElementById('mRole').value = 'CUSTOMER';
    document.getElementById('mPassword').value = '';
    document.getElementById('passwordLabel').textContent = 'Mật khẩu * (≥ 8 ký tự, hoa, thường, số, đặc biệt)';

    document.getElementById('mFullName').disabled = false;
    document.getElementById('mEmail').disabled = false;
    document.getElementById('mPhone').disabled = false;
    document.getElementById('mRole').disabled = false;
    document.getElementById('mPassword').style.display = 'block';
    document.getElementById('passwordLabel').style.display = 'block';
    document.getElementById('modalSubmitBtn').style.display = 'inline-flex';
    const rpBtn = document.getElementById('btnResetCustomerPassword');
    if (rpBtn) rpBtn.style.display = 'none';

    document.getElementById('modalOverlay').classList.add('active');
}

function openEdit(id) {
    const user = allUsers.find(u => u.id === id);
    if (!user) return;
    editingId = id;
    resetValidation();
    document.getElementById('modalTitle').textContent = 'Chi tiết tài khoản';
    document.getElementById('modalSubmitBtn').textContent = 'Cập nhật';
    document.getElementById('mFullName').value = user.fullName || '';
    document.getElementById('mEmail').value = user.email || '';
    document.getElementById('mPhone').value = user.phone || '';
    document.getElementById('mRole').value = user.roleCode || 'CUSTOMER';
    document.getElementById('mPassword').value = '';
    document.getElementById('passwordLabel').textContent = 'Mật khẩu mới (bỏ trống nếu không đổi, nếu đổi phải ≥ 8 ký tự...)';

    const isCustomer = user.roleCode === 'CUSTOMER';
    document.getElementById('mFullName').disabled = isCustomer;
    document.getElementById('mEmail').disabled = isCustomer;
    document.getElementById('mPhone').disabled = isCustomer;
    document.getElementById('mRole').disabled = isCustomer;

    const rpBtn = document.getElementById('btnResetCustomerPassword');
    if (isCustomer) {
        document.getElementById('modalSubmitBtn').style.display = 'none';
        document.getElementById('mPassword').style.display = 'none';
        document.getElementById('passwordLabel').style.display = 'none';
        if (rpBtn) rpBtn.style.display = 'inline-flex';
    } else {
        document.getElementById('modalSubmitBtn').style.display = 'inline-flex';
        document.getElementById('mPassword').style.display = 'block';
        document.getElementById('passwordLabel').style.display = 'block';
        if (rpBtn) rpBtn.style.display = 'none';
    }

    // Mark existing valid fields as green
    validateField('mFullName');
    validateField('mEmail');
    if (user.phone) validateField('mPhone');

    document.getElementById('modalOverlay').classList.add('active');
}

async function resetCustomerPassword() {
    if (!editingId) return;
    const user = allUsers.find(u => u.id === editingId);
    if (!user) return;
    if (!confirm(`Bạn có chắc muốn cấp lại mật khẩu cho khách hàng "${user.fullName}"? Mật khẩu mới sẽ được gửi về email.`)) return;

    const btn = document.getElementById('btnResetCustomerPassword');
    const originalText = btn.textContent;
    btn.disabled = true;
    btn.textContent = 'Đang xử lý...';

    try {
        const res = await fetch(API + '/api/admin/users/' + editingId + '/reset-password-email', {
            method: 'POST', headers: authHeaders()
        });
        const data = await res.json();
        if (!res.ok) { toast('error', data.error || 'Thao tác thất bại'); return; }
        toast('success', 'Đã cấp lại mật khẩu và gửi email thành công');
        closeModal();
    } catch (err) {
        toast('error', 'Không thể kết nối server');
    } finally {
        btn.disabled = false;
        btn.textContent = originalText;
    }
}

function closeModal() {
    document.getElementById('modalOverlay').classList.remove('active');
}

async function handleSubmit(e) {
    e.preventDefault();

    const isNameValid = validateField('mFullName');
    const isEmailValid = validateField('mEmail');
    const isPhoneValid = validateField('mPhone');
    const isPasswordValid = validateField('mPassword');

    if (!isNameValid || !isEmailValid || !isPhoneValid || !isPasswordValid) {
        toast('error', 'Vui lòng kiểm tra lại thông tin không hợp lệ');
        return;
    }

    const body = {
        fullName: document.getElementById('mFullName').value.trim(),
        email: document.getElementById('mEmail').value.trim(),
        phone: document.getElementById('mPhone').value.trim() || null,
        roleCode: document.getElementById('mRole').value,
        password: document.getElementById('mPassword').value || null
    };

    const btn = document.getElementById('modalSubmitBtn');
    const originalText = btn.textContent;
    btn.disabled = true;
    btn.textContent = 'Đang xử lý...';

    try {
        const url = editingId
            ? API + '/api/admin/users/' + editingId
            : API + '/api/admin/users';
        const method = editingId ? 'PUT' : 'POST';

        const res = await fetch(url, { method, headers: authHeaders(), body: JSON.stringify(body) });
        const data = await res.json();

        if (!res.ok) {
            toast('error', data.error || 'Thao tác thất bại');
            btn.disabled = false;
            btn.textContent = originalText;
            return;
        }

        toast('success', editingId ? 'Cập nhật thành công!' : 'Tạo tài khoản thành công!');
        closeModal();
        loadUsers();
    } catch (err) {
        toast('error', 'Không thể kết nối server');
        btn.disabled = false;
        btn.textContent = originalText;
    } finally {
        btn.disabled = false;
        btn.textContent = originalText;
    }
}

// === Toggle status ===
async function toggleStatus(id) {
    const user = allUsers.find(u => u.id === id);
    const name = user ? user.fullName : ('ID: ' + id);
    const action = user?.status === 'ACTIVE' ? 'vô hiệu hóa' : 'kích hoạt';
    if (!confirm(`Bạn có chắc muốn ${action} tài khoản "${name}"?`)) return;

    try {
        const res = await fetch(API + '/api/admin/users/' + id + '/status', {
            method: 'PUT', headers: { 'Authorization': 'Bearer ' + getToken() }
        });
        const data = await res.json();
        if (!res.ok) { toast('error', data.error || 'Thao tác thất bại'); return; }
        toast('success', `Đã ${action} tài khoản "${name}"`);
        loadUsers();
    } catch (err) {
        toast('error', 'Không thể kết nối server');
    }
}

// === Helpers ===
function esc(s) { if (!s) return ''; const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
function formatDate(dt) {
    if (!dt) return '—';
    const d = new Date(dt);
    return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}
function toast(type, msg) {
    const c = document.getElementById('toastContainer');
    const t = document.createElement('div');
    t.className = 'toast ' + type;
    t.innerHTML = `<span>${type === 'success' ? '✓' : '⚠'}</span> ${esc(msg)}`;
    c.appendChild(t);
    setTimeout(() => t.remove(), 3200);
}
/* Alias used by review-reports & support modules */
function showToast(msg, type) { toast(type, msg); }

// === Init ===
loadDashboard();

// === Page Switching ===
let currentPage = 'dashboard';
function switchPage(page) {
    currentPage = page;
    document.querySelectorAll('.page-section').forEach(s => s.style.display = 'none');
    const rrPage = document.getElementById('pageReviewReports');
    if (rrPage) rrPage.style.display = 'none';
    const spPage = document.getElementById('pageSupport');
    if (spPage) spPage.style.display = 'none';
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

    if (page === 'dashboard') {
        document.getElementById('pageDashboard').style.display = '';
        document.getElementById('navDashboard').classList.add('active');
        loadDashboard();
    } else if (page === 'accounts') {
        document.getElementById('pageAccounts').style.display = '';
        document.getElementById('navAccounts').classList.add('active');
        loadUsers();
    } else if (page === 'reports') {
        document.getElementById('pageReports').style.display = '';
        document.getElementById('navReports').classList.add('active');
        loadReport('daily');
    } else if (page === 'reviewReports') {
        const p = document.getElementById('pageReviewReports');
        if (p) p.style.display = '';
        document.getElementById('navReviewReports').classList.add('active');
        loadReviewReports();
    } else if (page === 'support') {
        const p = document.getElementById('pageSupport');
        if (p) p.style.display = '';
        const nav = document.getElementById('navSupport');
        if (nav) nav.classList.add('active');
        loadSupportTickets();
    } else if (page === 'withdrawals') {
        const p = document.getElementById('pageWithdrawals');
        if (p) p.style.display = '';
        const nav = document.getElementById('navWithdrawals');
        if (nav) nav.classList.add('active');
        loadWithdrawals();
    } else if (page === 'codDeposits') {
        const p = document.getElementById('pageCodDeposits');
        if (p) p.style.display = '';
        const nav = document.getElementById('navCodDeposits');
        if (nav) nav.classList.add('active');
        loadCodDeposits();
    }
}

// === Dashboard ===
let dailyChart = null, catChart = null, reportChart = null;

async function loadDashboard() {
    try {
        const [summaryRes, dailyRes, dashRes, topRes] = await Promise.all([
            fetch(API + '/api/reports/summary', { headers: authHeaders() }),
            fetch(API + '/api/reports/sales/daily', { headers: authHeaders() }),
            fetch(API + '/api/management/dashboard', { headers: authHeaders() }),
            fetch(API + '/api/reports/sales/top-selling?limit=10', { headers: authHeaders() })
        ]);

        if (summaryRes.ok) {
            const s = await summaryRes.json();
            document.getElementById('dTotalBooks').textContent = s.totalBooks || 0;
            document.getElementById('dTotalOrders').textContent = s.totalOrders || 0;
            document.getElementById('dTotalRevenue').textContent = formatVND(s.totalRevenue || 0);
            document.getElementById('dLowStock').textContent = s.lowStockCount || 0;
            document.getElementById('dBooksDetail').textContent = (s.totalVariants || 0) + ' variants • ' + (s.totalCopiesAvailable || 0) + ' copies';
        }

        if (dailyRes.ok) {
            const data = await dailyRes.json();
            renderDailyChart(data.slice(-14));
        }

        if (dashRes.ok) {
            const d = await dashRes.json();
            if (d.categoryStats) renderCatChart(d.categoryStats);
        }

        if (topRes.ok) {
            const top = await topRes.json();
            renderTopSelling(top, 'topSellingBody', 'topSellingEmpty');
        }
    } catch (e) {
        console.error('Dashboard load error:', e);
    }
}

function renderDailyChart(data) {
    const ctx = document.getElementById('chartDailyRevenue');
    if (dailyChart) dailyChart.destroy();
    dailyChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.map(d => d.period),
            datasets: [{
                label: 'Doanh thu (VNĐ)',
                data: data.map(d => d.totalRevenue || 0),
                backgroundColor: 'rgba(108, 99, 255, 0.6)',
                borderColor: '#6c63ff',
                borderWidth: 1,
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: {
                y: { ticks: { color: '#6b6b80', callback: v => formatVND(v) }, grid: { color: 'rgba(255,255,255,0.05)' } },
                x: { ticks: { color: '#6b6b80', maxRotation: 45 }, grid: { display: false } }
            }
        }
    });
}

function renderCatChart(stats) {
    const ctx = document.getElementById('chartCategoryDist');
    if (catChart) catChart.destroy();
    const colors = ['#6c63ff','#63b3ff','#2ed573','#ffa502','#ff4757','#a29bfe','#fd79a8','#00cec9','#e17055','#81ecec'];
    catChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: stats.map(s => s.categoryName),
            datasets: [{
                data: stats.map(s => s.bookCount),
                backgroundColor: colors.slice(0, stats.length),
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { position: 'right', labels: { color: '#a0a0b8', font: { size: 11 }, padding: 8, usePointStyle: true } } }
        }
    });
}

function renderTopSelling(data, tbodyId, emptyId) {
    const tbody = document.getElementById(tbodyId);
    const empty = document.getElementById(emptyId);
    if (!data || data.length === 0) {
        tbody.innerHTML = '';
        if (empty) empty.style.display = 'block';
        return;
    }
    if (empty) empty.style.display = 'none';
    tbody.innerHTML = data.map((b, i) => `
        <tr>
            <td style="color:var(--text3)">${i + 1}</td>
            <td>${esc(b.title)}</td>
            <td style="font-family:monospace; color:var(--text2)">${esc(b.sku)}</td>
            <td><span class="badge badge-active">${b.totalSold}</span></td>
            <td style="color:var(--green); font-weight:600">${formatVND(b.totalRevenue || 0)}</td>
        </tr>
    `).join('');
}

// === Reports ===
let reportMode = 'daily';
async function loadReport(mode) {
    reportMode = mode;
    const btnD = document.getElementById('btnDaily');
    const btnM = document.getElementById('btnMonthly');
    btnD.className = mode === 'daily' ? 'btn btn-accent' : 'btn-ghost';
    btnM.className = mode === 'monthly' ? 'btn btn-accent' : 'btn-ghost';
    if (mode !== 'daily') { btnD.style.cssText = 'padding:10px 18px; border:1px solid var(--border); border-radius:10px; background:transparent; color:var(--text2); font-family:inherit; font-size:13px; cursor:pointer;'; }
    else { btnD.style.cssText = ''; }
    if (mode !== 'monthly') { btnM.style.cssText = 'padding:10px 18px; border:1px solid var(--border); border-radius:10px; background:transparent; color:var(--text2); font-family:inherit; font-size:13px; cursor:pointer;'; }
    else { btnM.style.cssText = ''; }

    try {
        const [salesRes, topRes] = await Promise.all([
            fetch(API + '/api/reports/sales/' + mode, { headers: authHeaders() }),
            fetch(API + '/api/reports/sales/top-selling?limit=20', { headers: authHeaders() })
        ]);

        if (salesRes.ok) {
            const data = await salesRes.json();
            renderReportChart(data, mode);
            renderReportTable(data);
        }
        if (topRes.ok) {
            const top = await topRes.json();
            renderTopSelling(top, 'reportTopBody', null);
        }
    } catch (e) {
        console.error('Report load error:', e);
    }
}

function renderReportChart(data, mode) {
    const ctx = document.getElementById('chartReport');
    if (reportChart) reportChart.destroy();
    reportChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.map(d => d.period),
            datasets: [
                {
                    label: 'Doanh thu (VNĐ)',
                    data: data.map(d => d.totalRevenue || 0),
                    borderColor: '#6c63ff',
                    backgroundColor: 'rgba(108, 99, 255, 0.1)',
                    fill: true,
                    tension: 0.3,
                    yAxisID: 'y'
                },
                {
                    label: 'Số đơn',
                    data: data.map(d => d.totalOrders || 0),
                    borderColor: '#2ed573',
                    backgroundColor: 'rgba(46, 213, 115, 0.1)',
                    fill: false,
                    tension: 0.3,
                    yAxisID: 'y1'
                }
            ]
        },
        options: {
            responsive: true,
            interaction: { mode: 'index', intersect: false },
            plugins: { legend: { labels: { color: '#a0a0b8' } } },
            scales: {
                y: { type: 'linear', position: 'left', ticks: { color: '#6b6b80', callback: v => formatVND(v) }, grid: { color: 'rgba(255,255,255,0.05)' } },
                y1: { type: 'linear', position: 'right', ticks: { color: '#2ed573' }, grid: { display: false } },
                x: { ticks: { color: '#6b6b80', maxRotation: 45 }, grid: { display: false } }
            }
        }
    });
}

function renderReportTable(data) {
    const tbody = document.getElementById('reportTableBody');
    const empty = document.getElementById('reportEmpty');
    if (!data || data.length === 0) {
        tbody.innerHTML = '';
        empty.style.display = 'block';
        return;
    }
    empty.style.display = 'none';
    tbody.innerHTML = data.map(d => `
        <tr>
            <td style="font-family:monospace">${esc(d.period)}</td>
            <td><span class="badge badge-role">${d.totalOrders}</span></td>
            <td>${d.totalItemsSold}</td>
            <td style="color:var(--green); font-weight:600">${formatVND(d.totalRevenue || 0)}</td>
        </tr>
    `).join('');
}

function formatVND(n) {
    if (n == null) return '0đ';
    if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M';
    if (n >= 1000) return (n / 1000).toFixed(0) + 'K';
    return n.toLocaleString('vi-VN') + 'đ';
}
