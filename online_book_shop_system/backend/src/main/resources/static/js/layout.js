/**
 * Layout — Injects sidebar + header into every page
 * Each page must have: <div id="sidebar"></div> and <div id="topHeader"></div>
 */
const Layout = {
    navGroups: [
        {
            title: 'Tổng quan',
            items: [
                { label: 'Bảng tổng quan', href: 'dashboard.html', icon: 'bi-grid-1x2' },
            ],
        },
        {
            title: 'Quản lý',
            items: [
                { label: 'Sách', href: 'books.html', icon: 'bi-book' },
                { label: 'Danh mục', href: 'categories.html', icon: 'bi-bookmark' },
                { label: 'Phiên bản & Giá', href: 'variants.html', icon: 'bi-tags' },
                { label: 'Nhà cung cấp', href: 'suppliers.html', icon: 'bi-truck' },
            ],
        },
        {
            title: 'Kho hàng',
            items: [
                { label: 'Tồn kho', href: 'stock.html', icon: 'bi-box-seam' },
                { label: 'Nhập hàng', href: 'lots.html', icon: 'bi-box-seam' },
                { label: 'Sắp xếp lên kệ (Bản sao)', href: 'copies.html', icon: 'bi-qr-code-scan' },
                { label: 'Điều chỉnh', href: 'adjustments.html', icon: 'bi-arrow-down-up' },
                { label: 'Kiểm kê', href: 'stocktaking.html', icon: 'bi-clipboard-check' },
            ],
        },
        {
            title: 'Xử lý đơn',
            items: [
                { label: 'Xác nhận đơn', href: 'manager-confirm.html', icon: 'bi-check2-square' },
                { label: 'Phiếu lỗi', href: 'manager-stockout-workspace.html', icon: 'bi-exclamation-triangle' },
            ],
        },
        {
            title: 'Hoạt động Cửa hàng',
            items: [
                { label: 'Bảng điều khiển', href: 'staff-dashboard.html', icon: 'bi-speedometer2' },
                { label: 'Đơn hàng', href: 'staff-orders.html', icon: 'bi-bag' },
                { label: 'Xử lý Trả hàng', href: 'returns.html', icon: 'bi-arrow-return-left' },
            ],
        },
        {
            title: 'Hệ thống',
            items: [
                { label: 'Báo cáo', href: 'reports.html', icon: 'bi-bar-chart' },
                { label: 'Support Tickets', href: 'support-tickets.html', icon: 'bi-headset' },
                { label: 'Yêu cầu rút tiền', href: 'withdrawals.html', icon: 'bi-cash-coin' },
            ],
        },
    ],

    getCurrentPage() {
        const path = window.location.pathname.split('/').pop() || '';
        return path;
    },

    getActiveItem() {
        const page = this.getCurrentPage();
        for (const g of this.navGroups) {
            for (const item of g.items) {
                if (item.href === page) return item;
            }
        }
        return null;
    },

    renderSidebar() {
        const page = this.getCurrentPage();
        let navHtml = '';
        for (const group of this.navGroups) {
            navHtml += `<div class="nav-group">
                <div class="nav-group-title">${group.title}</div>`;
            for (const item of group.items) {
                const active = item.href === page ? 'active' : '';
                navHtml += `<a href="${item.href}" class="nav-link ${active}">
                    <i class="bi ${item.icon}"></i>
                    <span>${item.label}</span>
                </a>`;
            }
            navHtml += '</div>';
        }

        return `
            <div class="sidebar-brand">
                <div class="sidebar-brand-icon">
                    <i class="bi bi-journal-bookmark"></i>
                </div>
                <div class="sidebar-brand-text">
                    <h1>Hệ thống</h1>
                    <p>Quản lý Kho Sách</p>
                </div>
            </div>
            <nav class="sidebar-nav">${navHtml}</nav>
            <div class="sidebar-user">
                <div class="sidebar-user-inner">
                    <div class="sidebar-user-avatar">AM</div>
                    <div class="sidebar-user-info">
                        <p class="sidebar-user-name">Alex Morgan</p>
                        <p class="sidebar-user-role">Quản lý Cửa hàng</p>
                    </div>
                </div>
            </div>`;
    },

    renderHeader() {
        const active = this.getActiveItem();
        const label = active ? active.label : '';
        return `
            <div class="breadcrumb-custom">
                <span class="bc-muted">Quản trị</span>
                <span class="bc-sep">/</span>
                <span class="bc-active">${label}</span>
            </div>
            <div class="header-actions">
                <form id="logout-form" action="/logout" method="post" style="display:inline">
                    <button id="btn-logout" type="submit" title="Đăng xuất">
                        <i class="bi bi-box-arrow-right"></i> Đăng xuất
                    </button>
                </form>
            </div>`;
    },

    init() {
        /* support both old IDs and new -slot IDs */
        const sidebar = document.getElementById('sidebar') || document.getElementById('sidebar-slot');
        const header = document.getElementById('topHeader') || document.getElementById('header-slot');
        const shell = document.getElementById('app-layout') || (sidebar && sidebar.parentElement);

        /* ensure CSS classes are present */
        if (shell) shell.classList.add('app-shell');
        if (sidebar) { sidebar.classList.add('sidebar'); sidebar.innerHTML = this.renderSidebar(); }
        if (header) { header.classList.add('top-header'); header.innerHTML = this.renderHeader(); }
    },
};

document.addEventListener('DOMContentLoaded', () => Layout.init());
