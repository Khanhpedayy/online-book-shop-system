/* =========================================================
   SEED DATA — SHIPPING TEST DATA
   Chạy SAU seed_100_books.sql (cần categories, books, book_variants)
   Tạo: 2 users (customer), 1 supplier, lots, copies,
         10 đơn hàng ở các trạng thái khác nhau
   ========================================================= */

-- ===================== CUSTOMER USERS =====================
-- (roles đã được tạo bởi DataInitializer, id: 1=ADMIN, 2=MANAGER, 3=STAFF, 4=CUSTOMER)

INSERT INTO users (role_id, email, password_hash, full_name, phone, status) VALUES
(4, 'customer1@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4H0N0H0N0H0N0H0N0H0N0H0N0', N'Nguyễn Văn An', '0901234567', 'ACTIVE'),
(4, 'customer2@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4H0N0H0N0H0N0H0N0H0N0H0N0', N'Trần Thị Bình', '0912345678', 'ACTIVE'),
(4, 'customer3@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4H0N0H0N0H0N0H0N0H0N0H0N0', N'Lê Hoàng Nam', '0923456789', 'ACTIVE'),
(3, 'staff1@test.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4H0N0H0N0H0N0H0N0H0N0H0N0', N'Phạm Minh Tuấn', '0934567890', 'ACTIVE');

-- Lấy ID các user vừa tạo
DECLARE @c1 BIGINT = (SELECT id FROM users WHERE email = 'customer1@test.com');
DECLARE @c2 BIGINT = (SELECT id FROM users WHERE email = 'customer2@test.com');
DECLARE @c3 BIGINT = (SELECT id FROM users WHERE email = 'customer3@test.com');

-- ===================== SUPPLIER + LOTS (để có inventory) =====================

INSERT INTO suppliers (name, code, email, phone, contact_person, is_active) VALUES
(N'NXB Trẻ', 'NXB-TRE', 'nxbtre@test.com', '02838223456', N'Nguyễn Văn Sách', 1);

DECLARE @supId BIGINT = SCOPE_IDENTITY();

-- Lấy variant IDs của 10 cuốn sách đầu tiên
DECLARE @v1 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE sku = 'VH-VN-001');
DECLARE @v2 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE sku = 'VH-VN-002');
DECLARE @v3 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE sku = 'VH-NN-001');
DECLARE @v4 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE sku = 'VH-NN-003');
DECLARE @v5 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE sku = 'KT-001');
DECLARE @v6 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE sku = 'KN-001');
DECLARE @v7 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE sku = 'KN-003');
DECLARE @v8 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE sku = 'MG-001');
DECLARE @v9 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE sku = 'MG-003');
DECLARE @v10 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE sku = 'TN-002');

-- Nhập kho 50 cuốn cho mỗi variant (lots)
INSERT INTO lots (lot_code, supplier_id, variant_id, received_at, unit_cost, qty_received, qty_available, condition_default, status) VALUES
('LOT-001', @supId, @v1,  SYSUTCDATETIME(), 50000, 50, 50, 'NEW', 'RELEASED'),
('LOT-002', @supId, @v2,  SYSUTCDATETIME(), 55000, 50, 50, 'NEW', 'RELEASED'),
('LOT-003', @supId, @v3,  SYSUTCDATETIME(), 70000, 50, 50, 'NEW', 'RELEASED'),
('LOT-004', @supId, @v4,  SYSUTCDATETIME(), 40000, 50, 50, 'NEW', 'RELEASED'),
('LOT-005', @supId, @v5,  SYSUTCDATETIME(), 60000, 50, 50, 'NEW', 'RELEASED'),
('LOT-006', @supId, @v6,  SYSUTCDATETIME(), 45000, 50, 50, 'NEW', 'RELEASED'),
('LOT-007', @supId, @v7,  SYSUTCDATETIME(), 55000, 50, 50, 'NEW', 'RELEASED'),
('LOT-008', @supId, @v8,  SYSUTCDATETIME(), 15000, 50, 50, 'NEW', 'RELEASED'),
('LOT-009', @supId, @v9,  SYSUTCDATETIME(), 15000, 50, 50, 'NEW', 'RELEASED'),
('LOT-010', @supId, @v10, SYSUTCDATETIME(), 40000, 50, 50, 'NEW', 'RELEASED');

-- ===================== 10 ĐƠN HÀNG =====================

-- Đơn 1: CONFIRMED — chờ đóng gói
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method, placed_at, confirmed_at)
VALUES ('ORD-20260301', @c1, 'CONFIRMED', 'PAID', 167000, 197000,
    N'Nguyễn Văn An', '0901234567', N'123 Nguyễn Huệ', N'Bến Nghé', N'Quận 1', N'TP.HCM', N'TP.HCM', 'STANDARD',
    DATEADD(DAY, -3, SYSUTCDATETIME()), DATEADD(DAY, -2, SYSUTCDATETIME()));

DECLARE @o1 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity) VALUES
(@o1, @v1, N'Tôi thấy hoa vàng trên cỏ xanh', 'VH-VN-001', 78000, 1),
(@o1, @v4, N'Nhà giả kim', 'VH-NN-003', 59000, 1);

-- Đơn 2: CONFIRMED — chờ đóng gói
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method, placed_at, confirmed_at)
VALUES ('ORD-20260302', @c2, 'CONFIRMED', 'PAID', 189000, 219000,
    N'Trần Thị Bình', '0912345678', N'456 Lê Lợi', N'Phường 6', N'Quận 3', N'TP.HCM', N'TP.HCM', 'EXPRESS',
    DATEADD(DAY, -2, SYSUTCDATETIME()), DATEADD(DAY, -1, SYSUTCDATETIME()));

DECLARE @o2 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity) VALUES
(@o2, @v5, N'Cha giàu cha nghèo', 'KT-001', 89000, 1),
(@o2, @v3, N'Rừng Na Uy', 'VH-NN-001', 99000, 1);

-- Đơn 3: CONFIRMED — đơn lớn
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method, placed_at, confirmed_at)
VALUES ('ORD-20260303', @c3, 'CONFIRMED', 'PAID', 250000, 280000,
    N'Lê Hoàng Nam', '0923456789', N'789 Trần Hưng Đạo', N'Phường 1', N'Quận 5', N'TP.HCM', N'TP.HCM', 'STANDARD',
    DATEADD(DAY, -2, SYSUTCDATETIME()), DATEADD(DAY, -1, SYSUTCDATETIME()));

DECLARE @o3 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity) VALUES
(@o3, @v6, N'Đắc nhân tâm', 'KN-001', 72000, 1),
(@o3, @v7, N'Nghệ thuật tinh tế của việc đếch quan tâm', 'KN-003', 85000, 1),
(@o3, @v2, N'Mắt biếc', 'VH-VN-002', 85000, 1);

-- Đơn 4: PACKED — đã đóng gói, chờ giao
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method, placed_at, confirmed_at, packed_at)
VALUES ('ORD-20260304', @c1, 'PACKED', 'PAID', 144000, 174000,
    N'Nguyễn Văn An', '0901234567', N'123 Nguyễn Huệ', N'Bến Nghé', N'Quận 1', N'TP.HCM', N'TP.HCM', 'STANDARD',
    DATEADD(DAY, -5, SYSUTCDATETIME()), DATEADD(DAY, -4, SYSUTCDATETIME()), DATEADD(DAY, -3, SYSUTCDATETIME()));

DECLARE @o4 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity) VALUES
(@o4, @v8, N'One Piece - Tập 1', 'MG-001', 22000, 1),
(@o4, @v9, N'Conan - Tập 1', 'MG-003', 22000, 1),
(@o4, @v10, N'Totto-chan bên cửa sổ', 'TN-002', 65000, 1);

-- Đơn 5: PACKED — đã đóng gói, chờ giao
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_line2, ship_ward, ship_district, ship_city, ship_province, ship_method, placed_at, confirmed_at, packed_at)
VALUES ('ORD-20260305', @c2, 'PACKED', 'PAID', 85000, 115000,
    N'Trần Thị Bình', '0912345678', N'456 Lê Lợi', N'Tầng 3, Block A', N'Phường 6', N'Quận 3', N'TP.HCM', N'TP.HCM', 'EXPRESS',
    DATEADD(DAY, -4, SYSUTCDATETIME()), DATEADD(DAY, -3, SYSUTCDATETIME()), DATEADD(DAY, -2, SYSUTCDATETIME()));

DECLARE @o5 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity) VALUES
(@o5, @v7, N'Nghệ thuật tinh tế của việc đếch quan tâm', 'KN-003', 85000, 1);

-- Đơn 6: SHIPPED — đang giao hàng
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, placed_at, confirmed_at, packed_at, shipped_at)
VALUES ('ORD-20260306', @c3, 'SHIPPED', 'PAID', 159000, 189000,
    N'Lê Hoàng Nam', '0923456789', N'789 Trần Hưng Đạo', N'Phường 1', N'Quận 5', N'TP.HCM', N'TP.HCM', 'STANDARD',
    N'GHN Express', 'GHN123456789',
    DATEADD(DAY, -6, SYSUTCDATETIME()), DATEADD(DAY, -5, SYSUTCDATETIME()), DATEADD(DAY, -4, SYSUTCDATETIME()), DATEADD(DAY, -2, SYSUTCDATETIME()));

DECLARE @o6 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity) VALUES
(@o6, @v3, N'Rừng Na Uy', 'VH-NN-001', 99000, 1),
(@o6, @v4, N'Nhà giả kim', 'VH-NN-003', 59000, 1);

-- Đơn 7: SHIPPED — đang giao hàng
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, placed_at, confirmed_at, packed_at, shipped_at)
VALUES ('ORD-20260307', @c1, 'SHIPPED', 'PAID', 72000, 102000,
    N'Nguyễn Văn An', '0901234567', N'123 Nguyễn Huệ', N'Bến Nghé', N'Quận 1', N'TP.HCM', N'TP.HCM', 'STANDARD',
    N'Viettel Post', 'VTP987654321',
    DATEADD(DAY, -7, SYSUTCDATETIME()), DATEADD(DAY, -6, SYSUTCDATETIME()), DATEADD(DAY, -5, SYSUTCDATETIME()), DATEADD(DAY, -3, SYSUTCDATETIME()));

DECLARE @o7 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity) VALUES
(@o7, @v6, N'Đắc nhân tâm', 'KN-001', 72000, 1);

-- Đơn 8: DELIVERED — đã giao thành công
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, placed_at, confirmed_at, packed_at, shipped_at, delivered_at)
VALUES ('ORD-20260308', @c2, 'DELIVERED', 'PAID', 78000, 108000,
    N'Trần Thị Bình', '0912345678', N'456 Lê Lợi', N'Phường 6', N'Quận 3', N'TP.HCM', N'TP.HCM', 'STANDARD',
    N'J&T Express', 'JT111222333',
    DATEADD(DAY, -10, SYSUTCDATETIME()), DATEADD(DAY, -9, SYSUTCDATETIME()), DATEADD(DAY, -8, SYSUTCDATETIME()),
    DATEADD(DAY, -7, SYSUTCDATETIME()), DATEADD(DAY, -5, SYSUTCDATETIME()));

DECLARE @o8 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity) VALUES
(@o8, @v1, N'Tôi thấy hoa vàng trên cỏ xanh', 'VH-VN-001', 78000, 1);

-- Đơn 9: NEW — vừa đặt, chưa xác nhận
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method, placed_at)
VALUES ('ORD-20260309', @c3, 'NEW', 'PENDING', 44000, 74000,
    N'Lê Hoàng Nam', '0923456789', N'100 Hai Bà Trưng', N'Đa Kao', N'Quận 1', N'TP.HCM', N'TP.HCM', 'STANDARD',
    DATEADD(HOUR, -2, SYSUTCDATETIME()));

DECLARE @o9 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity) VALUES
(@o9, @v8, N'One Piece - Tập 1', 'MG-001', 22000, 1),
(@o9, @v9, N'Conan - Tập 1', 'MG-003', 22000, 1);

-- Đơn 10: CANCELLED — đã hủy
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    cancel_reason, placed_at, cancelled_at)
VALUES ('ORD-20260310', @c1, 'CANCELLED', 'REFUNDED', 99000, 129000,
    N'Nguyễn Văn An', '0901234567', N'123 Nguyễn Huệ', N'Bến Nghé', N'Quận 1', N'TP.HCM', N'TP.HCM', 'STANDARD',
    N'Khách đổi ý', DATEADD(DAY, -8, SYSUTCDATETIME()), DATEADD(DAY, -7, SYSUTCDATETIME()));

DECLARE @o10 BIGINT = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity) VALUES
(@o10, @v3, N'Rừng Na Uy', 'VH-NN-001', 99000, 1);

GO

PRINT N'✅ Shipping test data seeded:';
PRINT N'   - 4 users (3 customer + 1 staff)';
PRINT N'   - 1 supplier + 10 lots';
PRINT N'   - 10 orders: 3 CONFIRMED, 2 PACKED, 2 SHIPPED, 1 DELIVERED, 1 NEW, 1 CANCELLED';
PRINT N'   - 15 order items total';
