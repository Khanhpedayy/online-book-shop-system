/* =========================================================
   TEST DATA FOR ONLINE BOOK SHOP PROJECT
   Run AFTER:
   1) databasebansach.sql
   2) application startup/DataInitializer (roles + 4 base users)
   3) seed_50_books_half_each_category_with_image_slots.sql

   Purpose:
   - create stock for both QUANTITY and PER_COPY books
   - create realistic orders in many statuses
   - create return/return_items records for return intake flow
   - create cart, wishlist, reviews, support tickets
   - keep enough data for staff / manager / packing / shipping testing
   ========================================================= */

SET NOCOUNT ON;

/* ===================== SAFETY CHECKS ===================== */
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@bookshop.com')
BEGIN
    PRINT N'❌ Missing base users. Run app/DataInitializer first.';
    RETURN;
END;

IF NOT EXISTS (SELECT 1 FROM books)
BEGIN
    PRINT N'❌ Missing seeded books. Run seed_50_books_half_each_category_with_image_slots.sql first.';
    RETURN;
END;

DECLARE @adminId BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'admin@bookshop.com' AND deleted_at IS NULL);
DECLARE @staffId BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'staff@bookshop.com' AND deleted_at IS NULL);
DECLARE @managerId BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'manager@bookshop.com' AND deleted_at IS NULL);
DECLARE @customerId BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'customer@example.com' AND deleted_at IS NULL);
DECLARE @customerRoleId INT = (SELECT TOP 1 id FROM roles WHERE code = 'CUSTOMER');

/* ===================== EXTRA CUSTOMERS ===================== */
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'customer2@example.com' AND deleted_at IS NULL)
INSERT INTO users (role_id, email, password_hash, full_name, phone, status, created_at, created_by)
VALUES (@customerRoleId, 'customer2@example.com', 'noop', N'Nguyen Thi Lan', '0901000002', 'ACTIVE', SYSUTCDATETIME(), @adminId);

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'customer3@example.com' AND deleted_at IS NULL)
INSERT INTO users (role_id, email, password_hash, full_name, phone, status, created_at, created_by)
VALUES (@customerRoleId, 'customer3@example.com', 'noop', N'Tran Van Minh', '0901000003', 'ACTIVE', SYSUTCDATETIME(), @adminId);

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'customer4@example.com' AND deleted_at IS NULL)
INSERT INTO users (role_id, email, password_hash, full_name, phone, status, created_at, created_by)
VALUES (@customerRoleId, 'customer4@example.com', 'noop', N'Le Thu Ha', '0901000004', 'ACTIVE', SYSUTCDATETIME(), @adminId);

DECLARE @customer2Id BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'customer2@example.com' AND deleted_at IS NULL);
DECLARE @customer3Id BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'customer3@example.com' AND deleted_at IS NULL);
DECLARE @customer4Id BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'customer4@example.com' AND deleted_at IS NULL);

/* ===================== ADDRESSES ===================== */
IF NOT EXISTS (SELECT 1 FROM user_addresses WHERE user_id = @customerId AND is_default = 1 AND deleted_at IS NULL)
INSERT INTO user_addresses (user_id, recipient_name, phone, line1, ward, district, city, province, postal_code, country, is_default, created_at)
VALUES (@customerId, N'Test Customer', '0901000001', N'12 Nguyen Hue', N'Ben Nghe', N'Quan 1', N'Ho Chi Minh', N'Ho Chi Minh', '700000', N'Vietnam', 1, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM user_addresses WHERE user_id = @customer2Id AND is_default = 1 AND deleted_at IS NULL)
INSERT INTO user_addresses (user_id, recipient_name, phone, line1, ward, district, city, province, postal_code, country, is_default, created_at)
VALUES (@customer2Id, N'Nguyen Thi Lan', '0901000002', N'25 Hai Ba Trung', N'Trang Tien', N'Hoan Kiem', N'Ha Noi', N'Ha Noi', '100000', N'Vietnam', 1, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM user_addresses WHERE user_id = @customer3Id AND is_default = 1 AND deleted_at IS NULL)
INSERT INTO user_addresses (user_id, recipient_name, phone, line1, ward, district, city, province, postal_code, country, is_default, created_at)
VALUES (@customer3Id, N'Tran Van Minh', '0901000003', N'101 Le Duan', N'Thach Thang', N'Hai Chau', N'Da Nang', N'Da Nang', '550000', N'Vietnam', 1, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM user_addresses WHERE user_id = @customer4Id AND is_default = 1 AND deleted_at IS NULL)
INSERT INTO user_addresses (user_id, recipient_name, phone, line1, ward, district, city, province, postal_code, country, is_default, created_at)
VALUES (@customer4Id, N'Le Thu Ha', '0901000004', N'08 Tran Phu', N'Loc Tho', N'Nha Trang', N'Khanh Hoa', N'Khanh Hoa', '650000', N'Vietnam', 1, SYSUTCDATETIME());

/* ===================== VOUCHERS ===================== */
IF NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'WELCOME10' AND deleted_at IS NULL)
INSERT INTO vouchers (code, name, discount_type, discount_value, min_order_value, max_discount, usage_limit, used_count, per_user_limit, starts_at, expires_at, is_active, created_at, created_by)
VALUES ('WELCOME10', N'Giảm 10% đơn đầu', 'PERCENT', 10, 100000, 50000, 1000, 0, 1, DATEADD(DAY,-30,SYSUTCDATETIME()), DATEADD(DAY,180,SYSUTCDATETIME()), 1, SYSUTCDATETIME(), @adminId);

IF NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'SHIPFREE' AND deleted_at IS NULL)
INSERT INTO vouchers (code, name, discount_type, discount_value, min_order_value, max_discount, usage_limit, used_count, per_user_limit, starts_at, expires_at, is_active, created_at, created_by)
VALUES ('SHIPFREE', N'Giảm 30K phí ship', 'FIXED', 30000, 150000, 30000, 500, 0, 2, DATEADD(DAY,-30,SYSUTCDATETIME()), DATEADD(DAY,120,SYSUTCDATETIME()), 1, SYSUTCDATETIME(), @adminId);

DECLARE @voucherWelcome BIGINT = (SELECT TOP 1 id FROM vouchers WHERE code = 'WELCOME10' AND deleted_at IS NULL);
DECLARE @voucherShip BIGINT = (SELECT TOP 1 id FROM vouchers WHERE code = 'SHIPFREE' AND deleted_at IS NULL);

/* ===================== SUPPLIERS ===================== */
IF NOT EXISTS (SELECT 1 FROM suppliers WHERE name = N'Alpha Books Distribution' AND deleted_at IS NULL)
INSERT INTO suppliers (name, code, email, phone, address, contact_person, is_active, created_at)
VALUES (N'Alpha Books Distribution', 'SUP-ALPHA', 'alpha@supplier.test', '0287100001', N'KCN Tan Binh, HCM', N'Pham Anh', 1, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM suppliers WHERE name = N'Beta Education Supply' AND deleted_at IS NULL)
INSERT INTO suppliers (name, code, email, phone, address, contact_person, is_active, created_at)
VALUES (N'Beta Education Supply', 'SUP-BETA', 'beta@supplier.test', '0287100002', N'Cau Giay, Ha Noi', N'Nguyen Binh', 1, SYSUTCDATETIME());

DECLARE @supplierAlpha BIGINT = (SELECT TOP 1 id FROM suppliers WHERE name = N'Alpha Books Distribution' AND deleted_at IS NULL);
DECLARE @supplierBeta BIGINT = (SELECT TOP 1 id FROM suppliers WHERE name = N'Beta Education Supply' AND deleted_at IS NULL);

/* ===================== SELL MODE / VARIANT CONFIG ===================== */
UPDATE books
SET sell_mode = CASE WHEN id IN (57,58,59,60,61,67,68,69,70,71,77,78,79,80,85,86,87,88,89,95,96,97) THEN 'PER_COPY' ELSE 'QUANTITY' END,
    updated_at = SYSUTCDATETIME(),
    updated_by = @adminId
WHERE deleted_at IS NULL
  AND id IN (1,2,3,4,5,6,13,14,15,16,17,18,25,26,27,28,29,30,37,38,39,40,41,47,48,49,50,51,57,58,59,60,61,67,68,69,70,71,77,78,79,80,85,86,87,88,89,95,96,97);

UPDATE book_variants
SET condition_prices_json = '{"LIKE_NEW":{"pct":10},"GOOD":{"pct":20},"FAIR":{"pct":35}}',
    updated_at = SYSUTCDATETIME(),
    updated_by = @adminId
WHERE book_id IN (57,58,59,60,61,67,68,69,70,71,77,78,79,80,85,86,87,88,89,95,96,97)
  AND deleted_at IS NULL;

/* ===================== LOTS ===================== */
DECLARE @variantId BIGINT;
DECLARE @bookId BIGINT;
DECLARE @supplierId BIGINT;
DECLARE @isPerCopy BIT;
DECLARE @lotCode VARCHAR(60);
DECLARE @receiptCode VARCHAR(60);
DECLARE @invoiceNo VARCHAR(80);

DECLARE variant_cursor CURSOR FAST_FORWARD FOR
SELECT bv.id,
       bv.book_id,
       CASE WHEN b.sell_mode = 'PER_COPY' THEN 1 ELSE 0 END AS is_per_copy,
       CASE WHEN b.category_id IN (1,2,3,4,5) THEN @supplierAlpha ELSE @supplierBeta END AS supplier_id
FROM book_variants bv
JOIN books b ON b.id = bv.book_id
WHERE bv.deleted_at IS NULL
  AND b.deleted_at IS NULL
  AND bv.book_id IN (1,2,3,4,5,6,13,14,15,16,17,18,25,26,27,28,29,30,37,38,39,40,41,47,48,49,50,51,57,58,59,60,61,67,68,69,70,71,77,78,79,80,85,86,87,88,89,95,96,97);

OPEN variant_cursor;
FETCH NEXT FROM variant_cursor INTO @variantId, @bookId, @isPerCopy, @supplierId;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @lotCode = CONCAT('LOT-', RIGHT('000' + CAST(@bookId AS VARCHAR(10)), 3), '-A');
    SET @receiptCode = CONCAT('RC-', RIGHT('000' + CAST(@bookId AS VARCHAR(10)), 3));
    SET @invoiceNo = CONCAT('INV-', RIGHT('000' + CAST(@bookId AS VARCHAR(10)), 3));

    IF NOT EXISTS (SELECT 1 FROM lots WHERE lot_code = @lotCode AND deleted_at IS NULL)
    BEGIN
        INSERT INTO lots (
            lot_code, supplier_id, variant_id, receipt_code, invoice_no, warehouse, received_at,
            unit_cost, qty_received, qty_available, qty_reserved, qty_sold, qty_damaged, qty_returned,
            condition_default, status, note, created_at, created_by
        )
        VALUES (
            @lotCode, @supplierId, @variantId, @receiptCode, @invoiceNo, N'MAIN', DATEADD(DAY, -45, SYSUTCDATETIME()),
            CASE WHEN @isPerCopy = 1 THEN 0.55 * (SELECT TOP 1 sale_price FROM book_variants WHERE id = @variantId) ELSE 0.50 * (SELECT TOP 1 sale_price FROM book_variants WHERE id = @variantId) END,
            CASE WHEN @isPerCopy = 1 THEN 6 ELSE 40 END,
            CASE WHEN @isPerCopy = 1 THEN 6 ELSE 40 END,
            0, 0, 0, 0,
            'NEW', 'RELEASED', N'Test lot auto-generated', SYSUTCDATETIME(), @managerId
        );
    END;

    FETCH NEXT FROM variant_cursor INTO @variantId, @bookId, @isPerCopy, @supplierId;
END;
CLOSE variant_cursor;
DEALLOCATE variant_cursor;

/* ===================== COPIES FOR PER_COPY BOOKS ===================== */
DECLARE @copyLotId BIGINT;
DECLARE @copyVariantId BIGINT;
DECLARE @copyBookId BIGINT;
DECLARE @i INT;
DECLARE @copyCode VARCHAR(80);
DECLARE copy_cursor CURSOR FAST_FORWARD FOR
SELECT l.id, l.variant_id, bv.book_id
FROM lots l
JOIN book_variants bv ON bv.id = l.variant_id
JOIN books b ON b.id = bv.book_id
WHERE b.sell_mode = 'PER_COPY'
  AND l.deleted_at IS NULL
  AND bv.deleted_at IS NULL
  AND b.deleted_at IS NULL;

OPEN copy_cursor;
FETCH NEXT FROM copy_cursor INTO @copyLotId, @copyVariantId, @copyBookId;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @i = 1;
    WHILE @i <= 6
    BEGIN
        SET @copyCode = CONCAT('CP-', RIGHT('000' + CAST(@copyBookId AS VARCHAR(10)), 3), '-', RIGHT('00' + CAST(@i AS VARCHAR(10)), 2));
        IF NOT EXISTS (SELECT 1 FROM copies WHERE copy_code = @copyCode AND deleted_at IS NULL)
        BEGIN
            INSERT INTO copies (
                copy_code, lot_id, variant_id, location, condition_grade, condition_note,
                has_signature, is_first_edition, attributes_json, images_json, sell_price_override,
                status, created_at, created_by
            )
            VALUES (
                @copyCode, @copyLotId, @copyVariantId,
                CONCAT('A', ((@copyBookId % 5) + 1), '-', RIGHT('0' + CAST(@i AS VARCHAR(10)), 2)),
                CASE WHEN @i = 1 THEN 'NEW' WHEN @i IN (2,3,4) THEN 'LIKE_NEW' WHEN @i = 5 THEN 'GOOD' ELSE 'FAIR' END,
                CASE WHEN @i = 6 THEN N'Bìa xước nhẹ để test grading' ELSE NULL END,
                CASE WHEN @copyBookId IN (77,78,85,86) AND @i = 1 THEN 1 ELSE 0 END,
                CASE WHEN @copyBookId IN (77,78,85,86) AND @i <= 2 THEN 1 ELSE 0 END,
                CASE WHEN @copyBookId IN (85,86,87,88,89) THEN '{"sealed":true}' ELSE NULL END,
                NULL,
                CASE WHEN @i = 1 AND @copyBookId IN (77,78,85,86) THEN (SELECT TOP 1 sale_price * 1.25 FROM book_variants WHERE id = @copyVariantId)
                     WHEN @i = 6 THEN (SELECT TOP 1 sale_price * 0.70 FROM book_variants WHERE id = @copyVariantId)
                     ELSE NULL END,
                'AVAILABLE', SYSUTCDATETIME(), @staffId
            );
        END;
        SET @i = @i + 1;
    END;

    FETCH NEXT FROM copy_cursor INTO @copyLotId, @copyVariantId, @copyBookId;
END;
CLOSE copy_cursor;
DEALLOCATE copy_cursor;

/* ===================== INVENTORY TRANSACTIONS: RECEIPT ===================== */
INSERT INTO inventory_transactions (movement_type, variant_id, lot_id, quantity, to_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'IN', l.variant_id, l.id, l.qty_received, l.warehouse, 'RECEIPT', l.id, 'FOUND', N'Initial test receipt', DATEADD(DAY,-45,SYSUTCDATETIME()), @managerId
FROM lots l
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'RECEIPT' AND it.reference_id = l.id
);

/* ===================== STOCK QUANTITY ===================== */
;WITH q AS (
    SELECT l.variant_id, SUM(l.qty_available) AS qty_available
    FROM lots l
    WHERE l.deleted_at IS NULL
    GROUP BY l.variant_id
)
UPDATE b
SET stock_quantity = ISNULL(src.total_qty, 0),
    updated_at = SYSUTCDATETIME(),
    updated_by = @managerId
FROM books b
OUTER APPLY (
    SELECT SUM(q.qty_available) AS total_qty
    FROM book_variants bv
    LEFT JOIN q ON q.variant_id = bv.id
    WHERE bv.book_id = b.id AND bv.deleted_at IS NULL
) src;

/* ===================== ORDERS ===================== */
IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-001' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, voucher_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    customer_note, staff_note,
    placed_at, confirmed_at, confirmed_by, created_at, created_by
)
VALUES (
    'ORD-TEST-001', @customerId, @voucherWelcome, 'NEW', 'PENDING', 'VND',
    173000, 30000, 17000, 186000,
    N'Test Customer', '0901000001', N'12 Nguyen Hue', N'Ben Nghe', N'Quan 1', N'Ho Chi Minh', N'Ho Chi Minh', 'STANDARD',
    N'Đơn mới chưa xử lý', NULL,
    DATEADD(DAY,-1,SYSUTCDATETIME()), NULL, NULL, SYSUTCDATETIME(), @customerId
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-002' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    customer_note, staff_note,
    placed_at, confirmed_at, confirmed_by, created_at, created_by
)
VALUES (
    'ORD-TEST-002', @customer2Id, 'CONFIRMED', 'PAID', 'VND',
    268000, 30000, 0, 298000,
    N'Nguyen Thi Lan', '0901000002', N'25 Hai Ba Trung', N'Trang Tien', N'Hoan Kiem', N'Ha Noi', N'Ha Noi', 'STANDARD',
    N'Đã confirm, chờ picking', N'Allocate tự động xong',
    DATEADD(DAY,-3,SYSUTCDATETIME()), DATEADD(DAY,-3,SYSUTCDATETIME()), @staffId, SYSUTCDATETIME(), @customer2Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-003' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    customer_note, staff_note,
    placed_at, confirmed_at, packed_at, confirmed_by, packed_by, created_at, created_by
)
VALUES (
    'ORD-TEST-003', @customer3Id, 'PACKED', 'PAID', 'VND',
    171000, 30000, 0, 201000,
    N'Tran Van Minh', '0901000003', N'101 Le Duan', N'Thach Thang', N'Hai Chau', N'Da Nang', N'Da Nang', 'EXPRESS',
    N'Đơn test packing slip', N'Đã pick và đóng gói xong',
    DATEADD(DAY,-4,SYSUTCDATETIME()), DATEADD(DAY,-4,SYSUTCDATETIME()), DATEADD(DAY,-3,SYSUTCDATETIME()), @staffId, @staffId, SYSUTCDATETIME(), @customer3Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-004' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, voucher_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note,
    placed_at, confirmed_at, packed_at, shipped_at, confirmed_by, packed_by, shipped_by, created_at, created_by
)
VALUES (
    'ORD-TEST-004', @customer4Id, @voucherShip, 'SHIPPED', 'PAID', 'VND',
    220000, 30000, 30000, 220000,
    N'Le Thu Ha', '0901000004', N'08 Tran Phu', N'Loc Tho', N'Nha Trang', N'Khanh Hoa', N'Khanh Hoa', 'STANDARD',
    N'GHN', 'GHNTEST004', N'Giao giờ hành chính', N'Đã bàn giao cho hãng vận chuyển',
    DATEADD(DAY,-6,SYSUTCDATETIME()), DATEADD(DAY,-6,SYSUTCDATETIME()), DATEADD(DAY,-5,SYSUTCDATETIME()), DATEADD(DAY,-5,SYSUTCDATETIME()), @staffId, @staffId, @staffId, SYSUTCDATETIME(), @customer4Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-005' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, confirmed_by, packed_by, shipped_by, created_at, created_by
)
VALUES (
    'ORD-TEST-005', @customerId, 'DELIVERED', 'PAID', 'VND',
    87000, 30000, 0, 117000,
    N'Test Customer', '0901000001', N'12 Nguyen Hue', N'Ben Nghe', N'Quan 1', N'Ho Chi Minh', N'Ho Chi Minh', 'STANDARD',
    N'VNPOST', 'VNPOST005', N'Đơn đã giao thành công', N'Có thể dùng để test return request',
    DATEADD(DAY,-10,SYSUTCDATETIME()), DATEADD(DAY,-10,SYSUTCDATETIME()), DATEADD(DAY,-9,SYSUTCDATETIME()), DATEADD(DAY,-9,SYSUTCDATETIME()), DATEADD(DAY,-7,SYSUTCDATETIME()), @staffId, @staffId, @staffId, SYSUTCDATETIME(), @customerId
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-006' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at,
    confirmed_by, packed_by, shipped_by, created_at, created_by
)
VALUES (
    'ORD-TEST-006', @customer2Id, 'COMPLETED', 'PAID', 'VND',
    199000, 30000, 0, 229000,
    N'Nguyen Thi Lan', '0901000002', N'25 Hai Ba Trung', N'Trang Tien', N'Hoan Kiem', N'Ha Noi', N'Ha Noi', 'EXPRESS',
    N'GHN', 'GHNTEST006', N'Đơn hoàn tất dùng để test review/return', N'Đã hoàn tất',
    DATEADD(DAY,-20,SYSUTCDATETIME()), DATEADD(DAY,-20,SYSUTCDATETIME()), DATEADD(DAY,-19,SYSUTCDATETIME()), DATEADD(DAY,-19,SYSUTCDATETIME()), DATEADD(DAY,-17,SYSUTCDATETIME()), DATEADD(DAY,-15,SYSUTCDATETIME()),
    @staffId, @staffId, @staffId, SYSUTCDATETIME(), @customer2Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-007' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    customer_note, cancel_reason, staff_note,
    placed_at, cancelled_at, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-007', @customer3Id, 'CANCELLED', 'FAILED', 'VND',
    72000, 30000, 0, 102000,
    N'Tran Van Minh', '0901000003', N'101 Le Duan', N'Thach Thang', N'Hai Chau', N'Da Nang', N'Da Nang', 'STANDARD',
    N'Khách đổi ý', N'Khách yêu cầu hủy trước khi confirm', N'Hủy trước khi xử lý kho',
    DATEADD(DAY,-2,SYSUTCDATETIME()), DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId, SYSUTCDATETIME(), @customer3Id
);

DECLARE @ord1 BIGINT = (SELECT id FROM orders WHERE order_code = 'ORD-TEST-001' AND deleted_at IS NULL);
DECLARE @ord2 BIGINT = (SELECT id FROM orders WHERE order_code = 'ORD-TEST-002' AND deleted_at IS NULL);
DECLARE @ord3 BIGINT = (SELECT id FROM orders WHERE order_code = 'ORD-TEST-003' AND deleted_at IS NULL);
DECLARE @ord4 BIGINT = (SELECT id FROM orders WHERE order_code = 'ORD-TEST-004' AND deleted_at IS NULL);
DECLARE @ord5 BIGINT = (SELECT id FROM orders WHERE order_code = 'ORD-TEST-005' AND deleted_at IS NULL);
DECLARE @ord6 BIGINT = (SELECT id FROM orders WHERE order_code = 'ORD-TEST-006' AND deleted_at IS NULL);
DECLARE @ord7 BIGINT = (SELECT id FROM orders WHERE order_code = 'ORD-TEST-007' AND deleted_at IS NULL);

/* ===================== ORDER ITEMS ===================== */
DECLARE @var1 BIGINT = (SELECT id FROM book_variants WHERE book_id = 1 AND deleted_at IS NULL);
DECLARE @var25 BIGINT = (SELECT id FROM book_variants WHERE book_id = 25 AND deleted_at IS NULL);
DECLARE @var28 BIGINT = (SELECT id FROM book_variants WHERE book_id = 28 AND deleted_at IS NULL);
DECLARE @var37 BIGINT = (SELECT id FROM book_variants WHERE book_id = 37 AND deleted_at IS NULL);
DECLARE @var39 BIGINT = (SELECT id FROM book_variants WHERE book_id = 39 AND deleted_at IS NULL);
DECLARE @var57 BIGINT = (SELECT id FROM book_variants WHERE book_id = 57 AND deleted_at IS NULL);
DECLARE @var58 BIGINT = (SELECT id FROM book_variants WHERE book_id = 58 AND deleted_at IS NULL);
DECLARE @var67 BIGINT = (SELECT id FROM book_variants WHERE book_id = 67 AND deleted_at IS NULL);
DECLARE @var70 BIGINT = (SELECT id FROM book_variants WHERE book_id = 70 AND deleted_at IS NULL);
DECLARE @var77 BIGINT = (SELECT id FROM book_variants WHERE book_id = 77 AND deleted_at IS NULL);
DECLARE @var85 BIGINT = (SELECT id FROM book_variants WHERE book_id = 85 AND deleted_at IS NULL);
DECLARE @var86 BIGINT = (SELECT id FROM book_variants WHERE book_id = 86 AND deleted_at IS NULL);
DECLARE @var87 BIGINT = (SELECT id FROM book_variants WHERE book_id = 87 AND deleted_at IS NULL);
DECLARE @var95 BIGINT = (SELECT id FROM book_variants WHERE book_id = 95 AND deleted_at IS NULL);
DECLARE @var97 BIGINT = (SELECT id FROM book_variants WHERE book_id = 97 AND deleted_at IS NULL);

DECLARE @copy57_01 BIGINT = (SELECT id FROM copies WHERE copy_code = 'CP-057-01' AND deleted_at IS NULL);
DECLARE @copy57_02 BIGINT = (SELECT id FROM copies WHERE copy_code = 'CP-057-02' AND deleted_at IS NULL);
DECLARE @copy58_01 BIGINT = (SELECT id FROM copies WHERE copy_code = 'CP-058-01' AND deleted_at IS NULL);
DECLARE @copy67_01 BIGINT = (SELECT id FROM copies WHERE copy_code = 'CP-067-01' AND deleted_at IS NULL);
DECLARE @copy70_01 BIGINT = (SELECT id FROM copies WHERE copy_code = 'CP-070-01' AND deleted_at IS NULL);
DECLARE @copy77_01 BIGINT = (SELECT id FROM copies WHERE copy_code = 'CP-077-01' AND deleted_at IS NULL);
DECLARE @copy85_01 BIGINT = (SELECT id FROM copies WHERE copy_code = 'CP-085-01' AND deleted_at IS NULL);
DECLARE @copy85_02 BIGINT = (SELECT id FROM copies WHERE copy_code = 'CP-085-02' AND deleted_at IS NULL);
DECLARE @copy86_01 BIGINT = (SELECT id FROM copies WHERE copy_code = 'CP-086-01' AND deleted_at IS NULL);
DECLARE @copy87_01 BIGINT = (SELECT id FROM copies WHERE copy_code = 'CP-087-01' AND deleted_at IS NULL);
DECLARE @copy95_01 BIGINT = (SELECT id FROM copies WHERE copy_code = 'CP-095-01' AND deleted_at IS NULL);
DECLARE @copy97_01 BIGINT = (SELECT id FROM copies WHERE copy_code = 'CP-097-01' AND deleted_at IS NULL);

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord1 AND variant_id = @var1 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord1, @var1, NULL, b.title, bv.sku, NULL, 78000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var1;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord1 AND variant_id = @var37 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord1, @var37, NULL, b.title, bv.sku, NULL, 72000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var37;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord2 AND copy_id = @copy57_01 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord2, @var57, @copy57_01, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 82000), 1, 'SCAN', @staffId, DATEADD(DAY,-3,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy57_01 WHERE bv.id = @var57;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord2 AND variant_id = @var25 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord2, @var25, NULL, b.title, bv.sku, NULL, 89000, 2, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var25;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord3 AND copy_id = @copy85_01 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord3, @var85, @copy85_01, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 22000), 1, 'SCAN', @staffId, DATEADD(DAY,-3,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy85_01 WHERE bv.id = @var85;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord3 AND copy_id = @copy86_01 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord3, @var86, @copy86_01, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 22000), 1, 'SCAN', @staffId, DATEADD(DAY,-3,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy86_01 WHERE bv.id = @var86;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord3 AND variant_id = @var39 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord3, @var39, NULL, b.title, bv.sku, NULL, 85000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var39;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord4 AND copy_id = @copy67_01 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord4, @var67, @copy67_01, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 58000), 1, 'MANUAL', @staffId, DATEADD(DAY,-5,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy67_01 WHERE bv.id = @var67;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord4 AND variant_id = @var28 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord4, @var28, NULL, b.title, bv.sku, NULL, 129000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var28;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord4 AND variant_id = @var37 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord4, @var37, NULL, b.title, bv.sku, NULL, 72000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var37;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord5 AND copy_id = @copy95_01 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord5, @var95, @copy95_01, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 72000), 1, 'SCAN', @staffId, DATEADD(DAY,-9,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy95_01 WHERE bv.id = @var95;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord6 AND copy_id = @copy85_02 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord6, @var85, @copy85_02, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 22000), 1, 'SCAN', @staffId, DATEADD(DAY,-19,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy85_02 WHERE bv.id = @var85;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord6 AND copy_id = @copy97_01 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord6, @var97, @copy97_01, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 299000), 1, 'SCAN', @staffId, DATEADD(DAY,-19,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy97_01 WHERE bv.id = @var97;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord7 AND variant_id = @var37 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord7, @var37, NULL, b.title, bv.sku, NULL, 72000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var37;

/* ===================== PAYMENTS ===================== */
IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord1)
INSERT INTO payments (order_id, provider, provider_transaction_id, pay_url, amount, currency, status, expired_at, created_at, created_by)
VALUES (@ord1, 'PAYOS', 'PAY-TEST-001', N'https://pay.test/001', 186000, 'VND', 'CREATED', DATEADD(HOUR,2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customerId);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord2)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord2, 'PAYOS', 'PAY-TEST-002', 298000, 'VND', 'SUCCEEDED', DATEADD(DAY,-3,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer2Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord3)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord3, 'PAYOS', 'PAY-TEST-003', 201000, 'VND', 'SUCCEEDED', DATEADD(DAY,-4,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer3Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord4)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord4, 'PAYOS', 'PAY-TEST-004', 220000, 'VND', 'SUCCEEDED', DATEADD(DAY,-6,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer4Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord5)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord5, 'PAYOS', 'PAY-TEST-005', 117000, 'VND', 'SUCCEEDED', DATEADD(DAY,-10,SYSUTCDATETIME()), SYSUTCDATETIME(), @customerId);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord6)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord6, 'PAYOS', 'PAY-TEST-006', 229000, 'VND', 'SUCCEEDED', DATEADD(DAY,-20,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer2Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord7)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, created_at, created_by)
VALUES (@ord7, 'PAYOS', 'PAY-TEST-007', 102000, 'VND', 'FAILED', SYSUTCDATETIME(), @customer3Id);

/* ===================== UPDATE COPY STATUS FOR ORDER FLOW ===================== */
UPDATE copies SET status = 'RESERVED', reserved_at = DATEADD(DAY,-3,SYSUTCDATETIME()), reserve_expires_at = DATEADD(DAY,1,SYSUTCDATETIME()), updated_at = SYSUTCDATETIME()
WHERE id = @copy57_01 AND status = 'AVAILABLE';

UPDATE copies SET status = 'PACKED', updated_at = SYSUTCDATETIME()
WHERE id IN (@copy85_01, @copy86_01) AND status IN ('AVAILABLE','PICKED','RESERVED');

UPDATE copies SET status = 'SHIPPED', updated_at = SYSUTCDATETIME()
WHERE id = @copy67_01 AND status IN ('AVAILABLE','PICKED','PACKED','RESERVED');

UPDATE copies SET status = 'SOLD', updated_at = SYSUTCDATETIME()
WHERE id IN (@copy95_01, @copy85_02, @copy97_01) AND status IN ('AVAILABLE','PICKED','PACKED','SHIPPED','RESERVED');

/* ===================== INVENTORY MOVEMENTS FOR SALES / RESERVE ===================== */
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'RESERVE', c.variant_id, c.id, 1, c.location, 'ORDER', @ord2, 'SALE', N'Reserved for confirmed order', DATEADD(DAY,-3,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy57_01
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord2 AND it.copy_id = @copy57_01 AND it.movement_type = 'RESERVE');

INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord3, 'SALE', N'Packed manga copies', DATEADD(DAY,-3,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id IN (@copy85_01, @copy86_01)
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord3 AND it.copy_id = c.id AND it.movement_type = 'OUT');

INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord4, 'SALE', N'Shipped per-copy book', DATEADD(DAY,-5,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy67_01
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord4 AND it.copy_id = @copy67_01 AND it.movement_type = 'OUT');

INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord5, 'SALE', N'Delivered textbook', DATEADD(DAY,-9,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy95_01
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord5 AND it.copy_id = @copy95_01 AND it.movement_type = 'OUT');

INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord6, 'SALE', N'Completed collectible order', DATEADD(DAY,-19,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id IN (@copy85_02, @copy97_01)
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord6 AND it.copy_id = c.id AND it.movement_type = 'OUT');

/* ===================== RETURNS ===================== */
IF NOT EXISTS (SELECT 1 FROM returns WHERE return_code = 'RET-TEST-001' AND deleted_at IS NULL)
INSERT INTO returns (return_code, order_id, status, reason, note, refund_amount, requested_by, approved_by, approved_at, received_at, created_at, created_by)
VALUES ('RET-TEST-001', @ord5, 'RECEIVED', N'Sách bị cong góc khi giao', N'Test return intake cho staff', 72000, @customerId, @managerId, DATEADD(DAY,-5,SYSUTCDATETIME()), DATEADD(DAY,-4,SYSUTCDATETIME()), SYSUTCDATETIME(), @customerId);

IF NOT EXISTS (SELECT 1 FROM returns WHERE return_code = 'RET-TEST-002' AND deleted_at IS NULL)
INSERT INTO returns (return_code, order_id, status, reason, note, refund_amount, requested_by, approved_by, approved_at, received_at, created_at, created_by)
VALUES ('RET-TEST-002', @ord6, 'APPROVED', N'Khách đổi sang sản phẩm khác', N'Chờ staff nhận hàng hoàn', 22000, @customer2Id, @managerId, DATEADD(DAY,-2,SYSUTCDATETIME()), NULL, SYSUTCDATETIME(), @customer2Id);

DECLARE @ret1 BIGINT = (SELECT id FROM returns WHERE return_code = 'RET-TEST-001' AND deleted_at IS NULL);
DECLARE @ret2 BIGINT = (SELECT id FROM returns WHERE return_code = 'RET-TEST-002' AND deleted_at IS NULL);
DECLARE @oiOrd5 BIGINT = (SELECT TOP 1 id FROM order_items WHERE order_id = @ord5 AND copy_id = @copy95_01 AND deleted_at IS NULL);
DECLARE @oiOrd6 BIGINT = (SELECT TOP 1 id FROM order_items WHERE order_id = @ord6 AND copy_id = @copy85_02 AND deleted_at IS NULL);

IF NOT EXISTS (SELECT 1 FROM return_items WHERE return_id = @ret1 AND order_item_id = @oiOrd5)
INSERT INTO return_items (return_id, order_item_id, copy_id, quantity, received_condition_grade, received_condition_note, action, processed_by, processed_at, created_at)
VALUES (@ret1, @oiOrd5, @copy95_01, 1, 'GOOD', N'Góc bìa cong nhẹ, có thể restock reprice', 'RESTOCK_REPRICE', @staffId, DATEADD(DAY,-4,SYSUTCDATETIME()), SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM return_items WHERE return_id = @ret2 AND order_item_id = @oiOrd6)
INSERT INTO return_items (return_id, order_item_id, copy_id, quantity, received_condition_grade, received_condition_note, action, processed_by, processed_at, created_at)
VALUES (@ret2, @oiOrd6, @copy85_02, 1, NULL, N'Chưa nhận hàng, chỉ mới approve', NULL, NULL, NULL, SYSUTCDATETIME());

UPDATE copies
SET status = 'RETURNED', condition_grade = 'GOOD', condition_note = N'Khách trả lại - test return intake', updated_at = SYSUTCDATETIME()
WHERE id = @copy95_01;

INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, to_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'RETURN', c.variant_id, c.id, 1, c.location, 'RETURN', @ret1, 'FOUND', N'Customer return received', DATEADD(DAY,-4,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy95_01
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'RETURN' AND it.reference_id = @ret1 AND it.copy_id = @copy95_01);

/* ===================== CART / WISHLIST ===================== */
IF NOT EXISTS (SELECT 1 FROM cart_items WHERE user_id = @customerId AND variant_id = @var25 AND copy_id IS NULL)
INSERT INTO cart_items (user_id, variant_id, copy_id, quantity, added_at)
VALUES (@customerId, @var25, NULL, 1, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM cart_items WHERE user_id = @customerId AND variant_id = @var58 AND copy_id = @copy58_01)
INSERT INTO cart_items (user_id, variant_id, copy_id, quantity, added_at)
VALUES (@customerId, @var58, @copy58_01, 1, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM wishlist_items WHERE user_id = @customer2Id AND variant_id = @var70)
INSERT INTO wishlist_items (user_id, variant_id, created_at) VALUES (@customer2Id, @var70, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM wishlist_items WHERE user_id = @customer3Id AND variant_id = @var77)
INSERT INTO wishlist_items (user_id, variant_id, created_at) VALUES (@customer3Id, @var77, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM wishlist_items WHERE user_id = @customer4Id AND variant_id = @var87)
INSERT INTO wishlist_items (user_id, variant_id, created_at) VALUES (@customer4Id, @var87, SYSUTCDATETIME());

/* ===================== REVIEWS ===================== */
IF NOT EXISTS (SELECT 1 FROM reviews WHERE user_id = @customer2Id AND book_id = 85 AND deleted_at IS NULL)
INSERT INTO reviews (user_id, book_id, order_id, rating, title, content, status, created_at)
VALUES (@customer2Id, 85, @ord6, 5, N'Bản đẹp', N'Đóng gói tốt, đúng bản manga mình cần.', 'PUBLISHED', SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM reviews WHERE user_id = @customerId AND book_id = 95 AND deleted_at IS NULL)
INSERT INTO reviews (user_id, book_id, order_id, rating, title, content, status, created_at)
VALUES (@customerId, 95, @ord5, 3, N'Sách ổn nhưng méo góc', N'Nội dung ổn nhưng sách bị cong nhẹ khi nhận hàng.', 'PUBLISHED', SYSUTCDATETIME());

/* ===================== SUPPORT TICKETS ===================== */
IF NOT EXISTS (SELECT 1 FROM support_tickets WHERE ticket_code = 'TIC-TEST-001' AND deleted_at IS NULL)
INSERT INTO support_tickets (ticket_code, user_id, order_id, assigned_to, category, priority, status, subject, messages_json, created_at, updated_at)
VALUES (
    'TIC-TEST-001', @customerId, @ord5, @staffId, 'RETURN', 'HIGH', 'IN_PROGRESS',
    N'Yêu cầu hỗ trợ đổi trả đơn ORD-TEST-005',
    '[{"from":"customer","message":"Sách bị cong góc khi giao tới","at":"2026-03-20T09:00:00","isInternal":false},{"from":"staff","message":"Shop đã tạo phiếu return để kiểm tra","at":"2026-03-20T10:00:00","isInternal":true}]',
    SYSUTCDATETIME(), SYSUTCDATETIME()
);

IF NOT EXISTS (SELECT 1 FROM support_tickets WHERE ticket_code = 'TIC-TEST-002' AND deleted_at IS NULL)
INSERT INTO support_tickets (ticket_code, user_id, order_id, assigned_to, category, priority, status, subject, messages_json, created_at, updated_at)
VALUES (
    'TIC-TEST-002', @customer4Id, @ord4, @staffId, 'SHIPPING', 'NORMAL', 'OPEN',
    N'Hỏi mã vận đơn đơn ORD-TEST-004',
    '[{"from":"customer","message":"Cho mình xin tình trạng giao hàng hiện tại","at":"2026-03-23T14:20:00","isInternal":false}]',
    SYSUTCDATETIME(), SYSUTCDATETIME()
);

/* ===================== LOT COUNTS AFTER ORDERS ===================== */
UPDATE l
SET qty_available = CASE WHEN base.new_qty < 0 THEN 0 ELSE base.new_qty END,
    qty_sold = base.sold_qty,
    qty_reserved = base.reserved_qty,
    qty_returned = base.returned_qty,
    updated_at = SYSUTCDATETIME()
FROM lots l
CROSS APPLY (
    SELECT
        sold_qty = CASE WHEN b.sell_mode = 'PER_COPY' THEN (SELECT COUNT(*) FROM copies c WHERE c.lot_id = l.id AND c.status IN ('PICKED','PACKED','SHIPPED','SOLD','RETURNED')) ELSE
                         (SELECT ISNULL(SUM(oi.quantity),0)
                          FROM order_items oi
                          JOIN orders o ON o.id = oi.order_id
                          WHERE oi.variant_id = l.variant_id
                            AND oi.copy_id IS NULL
                            AND oi.deleted_at IS NULL
                            AND o.deleted_at IS NULL
                            AND o.status IN ('CONFIRMED','PACKED','SHIPPED','DELIVERED','COMPLETED')) END,
        reserved_qty = CASE WHEN b.sell_mode = 'PER_COPY' THEN (SELECT COUNT(*) FROM copies c WHERE c.lot_id = l.id AND c.status = 'RESERVED') ELSE
                            (SELECT ISNULL(SUM(oi.quantity),0)
                             FROM order_items oi
                             JOIN orders o ON o.id = oi.order_id
                             WHERE oi.variant_id = l.variant_id
                               AND oi.copy_id IS NULL
                               AND oi.deleted_at IS NULL
                               AND o.deleted_at IS NULL
                               AND o.status = 'NEW') END,
        returned_qty = CASE WHEN b.sell_mode = 'PER_COPY' THEN (SELECT COUNT(*) FROM copies c WHERE c.lot_id = l.id AND c.status = 'RETURNED') ELSE 0 END,
        new_qty = l.qty_received
                  - CASE WHEN b.sell_mode = 'PER_COPY' THEN (SELECT COUNT(*) FROM copies c WHERE c.lot_id = l.id AND c.status IN ('PICKED','PACKED','SHIPPED','SOLD','RETURNED'))
                         ELSE (SELECT ISNULL(SUM(oi.quantity),0)
                               FROM order_items oi
                               JOIN orders o ON o.id = oi.order_id
                               WHERE oi.variant_id = l.variant_id
                                 AND oi.copy_id IS NULL
                                 AND oi.deleted_at IS NULL
                                 AND o.deleted_at IS NULL
                                 AND o.status IN ('CONFIRMED','PACKED','SHIPPED','DELIVERED','COMPLETED')) END
                  - CASE WHEN b.sell_mode = 'PER_COPY' THEN (SELECT COUNT(*) FROM copies c WHERE c.lot_id = l.id AND c.status = 'RESERVED')
                         ELSE 0 END
    FROM book_variants bv
    JOIN books b ON b.id = bv.book_id
    WHERE bv.id = l.variant_id
) base;

/* ===================== RECALC STOCK QUANTITY AGAIN ===================== */
;WITH q AS (
    SELECT bv.book_id, SUM(l.qty_available) AS qty_available
    FROM book_variants bv
    LEFT JOIN lots l ON l.variant_id = bv.id AND l.deleted_at IS NULL
    WHERE bv.deleted_at IS NULL
    GROUP BY bv.book_id
)
UPDATE b
SET stock_quantity = ISNULL(q.qty_available, 0),
    updated_at = SYSUTCDATETIME(),
    updated_by = @managerId
FROM books b
LEFT JOIN q ON q.book_id = b.id;

/* ===================== SUMMARY ===================== */
PRINT N'✅ Test data inserted/updated successfully';
PRINT N'   - extra customers + addresses';
PRINT N'   - suppliers + lots + copies';
PRINT N'   - quantity books + per-copy books';
PRINT N'   - 7 orders across many statuses';
PRINT N'   - payments, returns, return_items';
PRINT N'   - cart, wishlist, reviews, support tickets';
PRINT N'   - stock_quantity recalculated';
