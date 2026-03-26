/* =========================================================
   SEED DATA — DOANH THU / REVENUE TEST DATA
   Chạy SAU seed_100_books.sql + seed_shipping_data.sql
   Tạo ~60 đơn hàng DELIVERED trải đều 30 ngày qua
   ========================================================= */

-- Lấy IDs customer đã tạo
DECLARE @c1 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'customer1@test.com');
DECLARE @c2 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'customer2@test.com');
DECLARE @c3 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'customer3@test.com');

-- Nếu chưa có customer, tạo mới
IF @c1 IS NULL BEGIN
    INSERT INTO users (role_id, email, password_hash, full_name, phone, status) VALUES
    (4, 'customer1@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4H0N0H0N0H0N0H0N0H0N0H0N0', N'Nguyễn Văn An', '0901234567', 'ACTIVE');
    SET @c1 = SCOPE_IDENTITY();
END
IF @c2 IS NULL BEGIN
    INSERT INTO users (role_id, email, password_hash, full_name, phone, status) VALUES
    (4, 'customer2@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4H0N0H0N0H0N0H0N0H0N0H0N0', N'Trần Thị Bình', '0912345678', 'ACTIVE');
    SET @c2 = SCOPE_IDENTITY();
END
IF @c3 IS NULL BEGIN
    INSERT INTO users (role_id, email, password_hash, full_name, phone, status) VALUES
    (4, 'customer3@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4H0N0H0N0H0N0H0N0H0N0H0N0', N'Lê Hoàng Nam', '0923456789', 'ACTIVE');
    SET @c3 = SCOPE_IDENTITY();
END

-- Lấy variant IDs
DECLARE @variants TABLE (idx INT IDENTITY(1,1), vid BIGINT, sku VARCHAR(30), title NVARCHAR(200), price INT);
INSERT INTO @variants (vid, sku, title, price)
SELECT TOP 20 bv.id, bv.sku, b.title, bv.sale_price
FROM book_variants bv JOIN books b ON b.id = bv.book_id
WHERE bv.deleted_at IS NULL AND b.deleted_at IS NULL AND bv.sale_price > 0
ORDER BY bv.id;

-- Helper: tạo đơn DELIVERED cho 1 ngày
DECLARE @day INT = 1;
DECLARE @orderNum INT = 100;
DECLARE @customerId BIGINT;
DECLARE @orderId BIGINT;
DECLARE @vid1 BIGINT, @vid2 BIGINT, @vid3 BIGINT;
DECLARE @price1 INT, @price2 INT, @price3 INT;
DECLARE @sku1 VARCHAR(30), @sku2 VARCHAR(30), @sku3 VARCHAR(30);
DECLARE @title1 NVARCHAR(200), @title2 NVARCHAR(200), @title3 NVARCHAR(200);
DECLARE @subtotal INT, @total INT;
DECLARE @placedDate DATETIME2;
DECLARE @itemCount INT;

WHILE @day <= 30
BEGIN
    SET @placedDate = DATEADD(DAY, -@day, SYSUTCDATETIME());
    
    -- Mỗi ngày tạo 2-3 đơn
    SET @itemCount = 1;
    WHILE @itemCount <= (CASE WHEN @day % 3 = 0 THEN 3 ELSE 2 END)
    BEGIN
        SET @orderNum = @orderNum + 1;
        
        -- Chọn khách hàng xoay vòng
        SET @customerId = CASE @itemCount WHEN 1 THEN @c1 WHEN 2 THEN @c2 ELSE @c3 END;
        
        -- Chọn sách ngẫu nhiên dựa trên ngày + item count
        SELECT @vid1 = vid, @price1 = price, @sku1 = sku, @title1 = title FROM @variants WHERE idx = ((@day + @itemCount) % 20) + 1;
        SELECT @vid2 = vid, @price2 = price, @sku2 = sku, @title2 = title FROM @variants WHERE idx = ((@day + @itemCount + 7) % 20) + 1;
        
        SET @subtotal = @price1 + @price2;
        SET @total = @subtotal + 30000; -- ship fee

        INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
            ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
            carrier, tracking_code,
            placed_at, confirmed_at, packed_at, shipped_at, delivered_at)
        VALUES (
            'REV-' + RIGHT('000' + CAST(@orderNum AS VARCHAR), 4),
            @customerId, 'DELIVERED', 'PAID', @subtotal, @total,
            CASE @customerId WHEN @c1 THEN N'Nguyễn Văn An' WHEN @c2 THEN N'Trần Thị Bình' ELSE N'Lê Hoàng Nam' END,
            CASE @customerId WHEN @c1 THEN '0901234567' WHEN @c2 THEN '0912345678' ELSE '0923456789' END,
            CASE @customerId WHEN @c1 THEN N'123 Nguyễn Huệ' WHEN @c2 THEN N'456 Lê Lợi' ELSE N'789 Trần Hưng Đạo' END,
            N'Phường 1', N'Quận 1', N'TP.HCM', N'TP.HCM', 'STANDARD',
            N'Nhân viên giao hàng', 'SELF-' + CAST(@orderNum AS VARCHAR),
            @placedDate,
            DATEADD(HOUR, 2, @placedDate),
            DATEADD(HOUR, 4, @placedDate),
            DATEADD(HOUR, 6, @placedDate),
            DATEADD(HOUR, 24, @placedDate)
        );

        SET @orderId = SCOPE_IDENTITY();

        -- 2 items mỗi đơn
        INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity)
        VALUES (@orderId, @vid1, @title1, @sku1, @price1, 1);
        INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity)
        VALUES (@orderId, @vid2, @title2, @sku2, @price2, 1);

        SET @itemCount = @itemCount + 1;
    END

    SET @day = @day + 1;
END

-- Thêm vài đơn lớn (3-4 items) cho ngày gần đây để biểu đồ sinh động
DECLARE @bigVid BIGINT, @bigPrice INT, @bigSku VARCHAR(30), @bigTitle NVARCHAR(200);

-- Đơn lớn hôm nay
SELECT @bigVid = vid, @bigPrice = price, @bigSku = sku, @bigTitle = title FROM @variants WHERE idx = 1;
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    placed_at, confirmed_at)
VALUES ('REV-TODAY-1', @c1, 'CONFIRMED', 'PAID', 500000, 530000,
    N'Nguyễn Văn An', '0901234567', N'123 Nguyễn Huệ', N'Bến Nghé', N'Quận 1', N'TP.HCM', N'TP.HCM', 'EXPRESS',
    SYSUTCDATETIME(), SYSUTCDATETIME());
SET @orderId = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity)
SELECT @orderId, vid, title, sku, price, 1 FROM @variants WHERE idx IN (1,2,3,4,5);

-- Đơn lớn hôm qua
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, placed_at, confirmed_at, packed_at, shipped_at, delivered_at)
VALUES ('REV-YEST-1', @c2, 'DELIVERED', 'PAID', 380000, 410000,
    N'Trần Thị Bình', '0912345678', N'456 Lê Lợi', N'Phường 6', N'Quận 3', N'TP.HCM', N'TP.HCM', 'STANDARD',
    N'Nhân viên giao hàng', 'SELF-YEST1',
    DATEADD(DAY, -1, SYSUTCDATETIME()), DATEADD(HOUR, -22, SYSUTCDATETIME()),
    DATEADD(HOUR, -20, SYSUTCDATETIME()), DATEADD(HOUR, -18, SYSUTCDATETIME()),
    DATEADD(HOUR, -6, SYSUTCDATETIME()));
SET @orderId = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity)
SELECT @orderId, vid, title, sku, price, 1 FROM @variants WHERE idx IN (6,7,8,9);

-- Đơn lớn 2 ngày trước
INSERT INTO orders (order_code, user_id, status, payment_status, subtotal_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, placed_at, confirmed_at, packed_at, shipped_at, delivered_at)
VALUES ('REV-2DAY-1', @c3, 'DELIVERED', 'PAID', 290000, 320000,
    N'Lê Hoàng Nam', '0923456789', N'789 Trần Hưng Đạo', N'Phường 1', N'Quận 5', N'TP.HCM', N'TP.HCM', 'STANDARD',
    N'Nhân viên giao hàng', 'SELF-2DAY1',
    DATEADD(DAY, -2, SYSUTCDATETIME()), DATEADD(DAY, -2, DATEADD(HOUR, 2, SYSUTCDATETIME())),
    DATEADD(DAY, -2, DATEADD(HOUR, 4, SYSUTCDATETIME())), DATEADD(DAY, -2, DATEADD(HOUR, 6, SYSUTCDATETIME())),
    DATEADD(DAY, -1, SYSUTCDATETIME()));
SET @orderId = SCOPE_IDENTITY();
INSERT INTO order_items (order_id, variant_id, title_snapshot, sku_snapshot, unit_price, quantity)
SELECT @orderId, vid, title, sku, price, 1 FROM @variants WHERE idx IN (10,11,12);

GO

-- Kiểm tra kết quả
SELECT 
    CONVERT(VARCHAR(10), placed_at, 120) AS ngay,
    COUNT(*) AS so_don,
    SUM(total_amount) AS doanh_thu
FROM orders 
WHERE status != 'CANCELLED' AND deleted_at IS NULL
    AND placed_at >= DATEADD(DAY, -30, GETDATE())
GROUP BY CONVERT(VARCHAR(10), placed_at, 120)
ORDER BY ngay DESC;

PRINT N'';
PRINT N'✅ Revenue seed data created!';
PRINT N'   - ~73 orders DELIVERED across 30 days';
PRINT N'   - 3 large orders for recent days';
PRINT N'   - ~150 order items total';
PRINT N'   - Diverse book variants for top selling chart';
