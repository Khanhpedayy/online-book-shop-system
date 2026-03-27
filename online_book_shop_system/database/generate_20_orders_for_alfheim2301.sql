-- Kịch bản tạo 20 đơn hàng (đã giao thành công) cho tài khoản: alfheim2301@gmail.com
-- Chạy trên SQL Server (hoạt động cho database online_book_shop)

SET NOCOUNT ON;

DECLARE @TargetEmail VARCHAR(255) = 'alfheim2301@gmail.com';
DECLARE @TargetPhone VARCHAR(30) = '0962126466';

-- 1. Tìm hoặc tạo user
DECLARE @UserId BIGINT = (SELECT TOP 1 id FROM users WHERE email = @TargetEmail);

IF @UserId IS NULL
BEGIN
    PRINT N'Không tìm thấy user với email ' + @TargetEmail + N', tiến hành tạo mới...';
    
    DECLARE @RoleId INT = (SELECT TOP 1 id FROM roles WHERE code = 'CUSTOMER');
    
    IF @RoleId IS NULL 
    BEGIN
        PRINT N'Chưa có Role CUSTOMER, vui lòng chạy dữ liệu seed cho Role trước.';
        RETURN;
    END

    INSERT INTO users (role_id, email, password_hash, full_name, phone, status, created_at)
    VALUES (
        @RoleId,
        @TargetEmail,
        '$2a$10$xyz...', -- Placeholder password, user should probably verify qua OTP or update
        N'Phạm Hùng Tiến',
        @TargetPhone,
        'ACTIVE',
        GETDATE()
    );
    SET @UserId = SCOPE_IDENTITY();
END
ELSE
BEGIN
    PRINT N'Đã tìm thấy user với email ' + @TargetEmail + N' (ID: ' + CAST(@UserId AS VARCHAR) + ')';
END

-- 2. Tạo 20 đơn hàng
DECLARE @i INT = 1;

WHILE @i <= 20
BEGIN
    -- Tạo mã đơn hàng random (VD: ORD-... )
    DECLARE @OrderCode VARCHAR(60) = 'ORD-ALF-' + FORMAT(GETDATE(), 'yyMM') + '-' + RIGHT('000' + CAST(@i AS VARCHAR), 3) + '-' + RIGHT(CAST(NEWID() AS VARCHAR(36)), 6);
    
    -- Tạo ngẫu nhiên ngày đặt hàng trong khoảng 5 đến 60 ngày trước
    DECLARE @RandomDaysBack INT = (ABS(CHECKSUM(NEWID())) % 60) + 5; 
    DECLARE @PlacedAt DATETIME2 = DATEADD(DAY, -@RandomDaysBack, GETDATE());
    DECLARE @ConfirmedAt DATETIME2 = DATEADD(HOUR, 2, @PlacedAt);
    DECLARE @PackedAt DATETIME2 = DATEADD(HOUR, 12, @PlacedAt);
    DECLARE @ShippedAt DATETIME2 = DATEADD(DAY, 1, @PlacedAt);
    DECLARE @DeliveredAt DATETIME2 = DATEADD(DAY, 3, @PlacedAt);
    DECLARE @CompletedAt DATETIME2 = DATEADD(DAY, 4, @PlacedAt);
    
    -- Phí vận chuyển mặc định
    DECLARE @ShippingFee DECIMAL(18,2) = 30000;
    
    -- Insert vào bảng orders
    INSERT INTO orders (
        order_code, user_id, status, payment_status, currency,
        subtotal_amount, shipping_fee, discount_amount, total_amount,
        ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
        placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at
    )
    VALUES (
        @OrderCode, @UserId, 'COMPLETED', 'PAID', 'VND',
        0, @ShippingFee, 0, 0, -- sẽ cập nhật tổng tiền (subtotal, total) bằng UPDATE bên dưới
        N'Phạm Hùng Tiến', @TargetPhone, N'Số 1 Đại Cồ Việt', N'Bách Khoa', N'Hai Bà Trưng', N'Hà Nội', N'Hà Nội', 'STANDARD',
        @PlacedAt, @ConfirmedAt, @PackedAt, @ShippedAt, @DeliveredAt, @CompletedAt
    );
    
    DECLARE @OrderId BIGINT = SCOPE_IDENTITY();
    
    -- Số loại sách (order items) trong đơn hàng này: random từ 1 đến 5 loại
    DECLARE @NumItems INT = (ABS(CHECKSUM(NEWID())) % 5) + 1;
    
    -- Gắn các cuốn sách random (đa dạng thể loại - do dùng ORDER BY NEWID())
    -- Lấy thông tin sách từ books và book_variants
    INSERT INTO order_items (
        order_id, variant_id, title_snapshot, sku_snapshot, condition_snapshot,
        unit_price, quantity
    )
    SELECT TOP (@NumItems)
        @OrderId,
        bv.id,
        b.title,
        bv.sku,
        'NEW',
        bv.sale_price,
        (ABS(CHECKSUM(NEWID())) % 3) + 1  -- Số lượng mua của mỗi cuốn: random 1 đến 3
    FROM book_variants bv
    JOIN books b ON b.id = bv.book_id
    WHERE b.status = 'ACTIVE' AND bv.is_active = 1
    ORDER BY NEWID();

    -- Tính toán lại giá trị Subtotal và Total (Subtotal + Shipping - Discount)
    DECLARE @Subtotal DECIMAL(18,2) = (
        SELECT ISNULL(SUM(unit_price * quantity), 0)
        FROM order_items
        WHERE order_id = @OrderId
    );

    UPDATE orders
    SET subtotal_amount = @Subtotal,
        total_amount = @Subtotal + @ShippingFee - discount_amount
    WHERE id = @OrderId;
    
    SET @i = @i + 1;
END

PRINT N'✅ Đã tạo thành công 20 đơn hàng (trạng thái: COMPLETED) với đa dạng thể loại sách cho tài khoản (' + @TargetEmail + ')';
