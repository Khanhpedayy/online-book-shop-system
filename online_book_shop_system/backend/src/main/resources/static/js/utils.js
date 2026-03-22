/**
 * Utility functions
 */
const Utils = {
    /** Format number as Vietnamese currency */
    currency(v) {
        if (v == null) return '—';
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);
    },

    /** Format number with commas */
    number(v) {
        if (v == null) return '—';
        return new Intl.NumberFormat('vi-VN').format(v);
    },

    /** Format date string */
    date(v) {
        if (!v) return '—';
        return new Date(v).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
    },

    /** Format datetime string */
    datetime(v) {
        if (!v) return '—';
        return new Date(v).toLocaleString('vi-VN', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    },

    /** Get status badge HTML */
    badge(status) {
        if (!status) return '<span class="badge-status badge-default">—</span>';
        const cls = {
            'ACTIVE': 'badge-active', 'AVAILABLE': 'badge-available',
            'DELIVERED': 'badge-delivered', 'COMPLETED': 'badge-completed',
            'HIDDEN': 'badge-hidden', 'DRAFT': 'badge-draft', 'PENDING': 'badge-pending',
            'INACTIVE': 'badge-inactive', 'CANCELLED': 'badge-cancelled', 'DAMAGED': 'badge-damaged',
            'PROCESSING': 'badge-processing', 'CONFIRMED': 'badge-confirmed', 'SHIPPED': 'badge-shipped',
            'NEW': 'badge-new',
        }[status.toUpperCase()] || 'badge-default';
        return `<span class="badge-status ${cls}">${status}</span>`;
    },

    /** Show Bootstrap toast */
    toast(message, type = 'success') {
        let container = document.querySelector('.toast-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }
        const bg = type === 'success' ? 'bg-success' : type === 'danger' ? 'bg-danger' : 'bg-warning';
        const id = 'toast-' + Date.now();
        container.innerHTML += `
            <div id="${id}" class="toast align-items-center text-white ${bg} border-0 show" role="alert">
                <div class="d-flex">
                    <div class="toast-body">${message}</div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>`;
        setTimeout(() => {
            const t = document.getElementById(id);
            if (t) t.remove();
        }, 4000);
    },

    /** Get query param from URL */
    param(name) {
        return new URLSearchParams(window.location.search).get(name);
    },

    /** Escape HTML to prevent XSS */
    esc(str) {
        if (str == null) return '';
        const div = document.createElement('div');
        div.textContent = String(str);
        return div.innerHTML;
    },

    /** Show loading spinner */
    showLoading(el) {
        el.innerHTML = `<div class="loading-center"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>`;
    },

    /** Show empty state */
    showEmpty(el, msg = 'Không có dữ liệu') {
        el.innerHTML = `<div class="empty-state"><span class="material-symbols-outlined">inbox</span><p>${msg}</p></div>`;
    },

    /** Confirm dialog using Bootstrap modal */
    confirm(title, message) {
        return new Promise((resolve) => {
            let modal = document.getElementById('confirmModal');
            if (!modal) {
                document.body.insertAdjacentHTML('beforeend', `
                    <div class="modal fade" id="confirmModal" tabindex="-1">
                        <div class="modal-dialog modal-dialog-centered">
                            <div class="modal-content">
                                <div class="modal-header"><h5 class="modal-title" id="confirmModalTitle"></h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                <div class="modal-body" id="confirmModalBody"></div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Huỷ</button>
                                    <button type="button" class="btn btn-danger" id="confirmModalOk">Xác nhận</button>
                                </div>
                            </div>
                        </div>
                    </div>`);
                modal = document.getElementById('confirmModal');
            }
            document.getElementById('confirmModalTitle').textContent = title;
            document.getElementById('confirmModalBody').textContent = message;
            const bsModal = new bootstrap.Modal(modal);
            const okBtn = document.getElementById('confirmModalOk');
            const handler = () => { bsModal.hide(); resolve(true); okBtn.removeEventListener('click', handler); };
            okBtn.addEventListener('click', handler);
            modal.addEventListener('hidden.bs.modal', () => resolve(false), { once: true });
            bsModal.show();
        });
    },
};

/* Global aliases — pages call showToast(...) directly */
function showToast(msg, type) { Utils.toast(msg, type); }
function formatVND(v) { return Utils.currency(v); }
function formatDate(v) { return Utils.date(v); }
function formatDateTime(v) { return Utils.datetime(v); }
