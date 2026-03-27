/* Additional orders 008-030 to bring total test orders to 30 */
SET NOCOUNT ON;
/* safety ids from base seed */
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@bookshop.com' AND deleted_at IS NULL)
BEGIN
    PRINT N'❌ Missing base users. Run app/DataInitializer first.';
    RETURN;
END;

DECLARE @adminId BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'admin@bookshop.com' AND deleted_at IS NULL);
DECLARE @staffId BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'staff@bookshop.com' AND deleted_at IS NULL);
DECLARE @managerId BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'manager@bookshop.com' AND deleted_at IS NULL);
DECLARE @customerId BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'customer@example.com' AND deleted_at IS NULL);
DECLARE @customer2Id BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'customer2@example.com' AND deleted_at IS NULL);
DECLARE @customer3Id BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'customer3@example.com' AND deleted_at IS NULL);
DECLARE @customer4Id BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'customer4@example.com' AND deleted_at IS NULL);

IF @customer2Id IS NULL OR @customer3Id IS NULL OR @customer4Id IS NULL
BEGIN
    PRINT N'❌ Missing extra customers. Run test_data_for_bookshop_project.sql first.';
    RETURN;
END;

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-008' AND deleted_at IS NULL)
BEGIN
    PRINT N'Creating extra test orders 008-030...';
END;

DECLARE @var1 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 1 AND deleted_at IS NULL);
DECLARE @var2 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 2 AND deleted_at IS NULL);
DECLARE @var13 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 13 AND deleted_at IS NULL);
DECLARE @var25 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 25 AND deleted_at IS NULL);
DECLARE @var28 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 28 AND deleted_at IS NULL);
DECLARE @var37 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 37 AND deleted_at IS NULL);
DECLARE @var39 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 39 AND deleted_at IS NULL);
DECLARE @var57 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 57 AND deleted_at IS NULL);
DECLARE @var58 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 58 AND deleted_at IS NULL);
DECLARE @var67 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 67 AND deleted_at IS NULL);
DECLARE @var70 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 70 AND deleted_at IS NULL);
DECLARE @var77 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 77 AND deleted_at IS NULL);
DECLARE @var85 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 85 AND deleted_at IS NULL);
DECLARE @var86 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 86 AND deleted_at IS NULL);
DECLARE @var87 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 87 AND deleted_at IS NULL);
DECLARE @var95 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 95 AND deleted_at IS NULL);
DECLARE @var96 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 96 AND deleted_at IS NULL);
DECLARE @var97 BIGINT = (SELECT TOP 1 id FROM book_variants WHERE book_id = 97 AND deleted_at IS NULL);
DECLARE @copy057_02 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-057-02' AND deleted_at IS NULL);
DECLARE @copy057_03 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-057-03' AND deleted_at IS NULL);
DECLARE @copy058_01 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-058-01' AND deleted_at IS NULL);
DECLARE @copy058_02 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-058-02' AND deleted_at IS NULL);
DECLARE @copy067_02 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-067-02' AND deleted_at IS NULL);
DECLARE @copy067_03 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-067-03' AND deleted_at IS NULL);
DECLARE @copy070_01 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-070-01' AND deleted_at IS NULL);
DECLARE @copy070_02 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-070-02' AND deleted_at IS NULL);
DECLARE @copy077_01 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-077-01' AND deleted_at IS NULL);
DECLARE @copy077_02 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-077-02' AND deleted_at IS NULL);
DECLARE @copy085_03 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-085-03' AND deleted_at IS NULL);
DECLARE @copy085_04 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-085-04' AND deleted_at IS NULL);
DECLARE @copy086_02 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-086-02' AND deleted_at IS NULL);
DECLARE @copy086_03 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-086-03' AND deleted_at IS NULL);
DECLARE @copy087_01 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-087-01' AND deleted_at IS NULL);
DECLARE @copy087_02 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-087-02' AND deleted_at IS NULL);
DECLARE @copy095_02 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-095-02' AND deleted_at IS NULL);
DECLARE @copy095_03 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-095-03' AND deleted_at IS NULL);
DECLARE @copy096_01 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-096-01' AND deleted_at IS NULL);
DECLARE @copy097_02 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-097-02' AND deleted_at IS NULL);
DECLARE @copy097_03 BIGINT = (SELECT TOP 1 id FROM copies WHERE copy_code = 'CP-097-03' AND deleted_at IS NULL);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-008' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-008', @customerId, 'NEW', 'PENDING', 'VND',
    256000, 30000, 0, 286000,
    N'Test Customer', '0901000001', N'12 Nguyen Hue', N'Ben Nghe', N'Quan 1', N'Ho Chi Minh', N'Ho Chi Minh', 'STANDARD',
    NULL, NULL, N'Auto generated extra test order', NULL, NULL,
    DATEADD(DAY,-23,SYSUTCDATETIME()), NULL, NULL, NULL, NULL, NULL, NULL,
    NULL, NULL, NULL, NULL, SYSUTCDATETIME(), @customerId
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-009' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-009', @customer2Id, 'NEW', 'PENDING', 'VND',
    171000, 30000, 0, 201000,
    N'Nguyen Thi Lan', '0901000002', N'25 Hai Ba Trung', N'Trang Tien', N'Hoan Kiem', N'Ha Noi', N'Ha Noi', 'STANDARD',
    NULL, NULL, N'Auto generated extra test order', NULL, NULL,
    DATEADD(DAY,-22,SYSUTCDATETIME()), NULL, NULL, NULL, NULL, NULL, NULL,
    NULL, NULL, NULL, NULL, SYSUTCDATETIME(), @customer2Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-010' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-010', @customer3Id, 'NEW', 'PENDING', 'VND',
    129000, 50000, 0, 179000,
    N'Tran Van Minh', '0901000003', N'101 Le Duan', N'Thach Thang', N'Hai Chau', N'Da Nang', N'Da Nang', 'EXPRESS',
    NULL, NULL, N'Auto generated extra test order', NULL, NULL,
    DATEADD(DAY,-21,SYSUTCDATETIME()), NULL, NULL, NULL, NULL, NULL, NULL,
    NULL, NULL, NULL, NULL, SYSUTCDATETIME(), @customer3Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-011' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-011', @customer4Id, 'NEW', 'PENDING', 'VND',
    170000, 30000, 30000, 170000,
    N'Le Thu Ha', '0901000004', N'08 Tran Phu', N'Loc Tho', N'Nha Trang', N'Nha Trang', N'Khanh Hoa', 'STANDARD',
    NULL, NULL, N'Auto generated extra test order', NULL, NULL,
    DATEADD(DAY,-20,SYSUTCDATETIME()), NULL, NULL, NULL, NULL, NULL, NULL,
    NULL, NULL, NULL, NULL, SYSUTCDATETIME(), @customer4Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-012' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-012', @customerId, 'CONFIRMED', 'PAID', 'VND',
    154000, 30000, 0, 184000,
    N'Test Customer', '0901000001', N'12 Nguyen Hue', N'Ben Nghe', N'Quan 1', N'Ho Chi Minh', N'Ho Chi Minh', 'STANDARD',
    NULL, NULL, N'Auto generated extra test order', NULL, NULL,
    DATEADD(DAY,-19,SYSUTCDATETIME()), DATEADD(DAY,-19,SYSUTCDATETIME()), NULL, NULL, NULL, NULL, NULL,
    @staffId, NULL, NULL, NULL, SYSUTCDATETIME(), @customerId
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-013' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-013', @customer2Id, 'CONFIRMED', 'PAID', 'VND',
    168000, 30000, 0, 198000,
    N'Nguyen Thi Lan', '0901000002', N'25 Hai Ba Trung', N'Trang Tien', N'Hoan Kiem', N'Ha Noi', N'Ha Noi', 'STANDARD',
    NULL, NULL, N'Auto generated extra test order', NULL, NULL,
    DATEADD(DAY,-18,SYSUTCDATETIME()), DATEADD(DAY,-18,SYSUTCDATETIME()), NULL, NULL, NULL, NULL, NULL,
    @staffId, NULL, NULL, NULL, SYSUTCDATETIME(), @customer2Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-014' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-014', @customer3Id, 'CONFIRMED', 'PAID', 'VND',
    157000, 30000, 0, 187000,
    N'Tran Van Minh', '0901000003', N'101 Le Duan', N'Thach Thang', N'Hai Chau', N'Da Nang', N'Da Nang', 'STANDARD',
    NULL, NULL, N'Auto generated extra test order', NULL, NULL,
    DATEADD(DAY,-17,SYSUTCDATETIME()), DATEADD(DAY,-17,SYSUTCDATETIME()), NULL, NULL, NULL, NULL, NULL,
    @staffId, NULL, NULL, NULL, SYSUTCDATETIME(), @customer3Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-015' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-015', @customer4Id, 'CONFIRMED', 'PAID', 'VND',
    177000, 30000, 0, 207000,
    N'Le Thu Ha', '0901000004', N'08 Tran Phu', N'Loc Tho', N'Nha Trang', N'Nha Trang', N'Khanh Hoa', 'STANDARD',
    NULL, NULL, N'Auto generated extra test order', NULL, NULL,
    DATEADD(DAY,-16,SYSUTCDATETIME()), DATEADD(DAY,-16,SYSUTCDATETIME()), NULL, NULL, NULL, NULL, NULL,
    @staffId, NULL, NULL, NULL, SYSUTCDATETIME(), @customer4Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-016' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-016', @customerId, 'PACKED', 'PAID', 'VND',
    44000, 30000, 0, 74000,
    N'Test Customer', '0901000001', N'12 Nguyen Hue', N'Ben Nghe', N'Quan 1', N'Ho Chi Minh', N'Ho Chi Minh', 'STANDARD',
    NULL, NULL, N'Auto generated extra test order', N'Packed for testing', NULL,
    DATEADD(DAY,-15,SYSUTCDATETIME()), DATEADD(DAY,-15,SYSUTCDATETIME()), DATEADD(DAY,-14,SYSUTCDATETIME()), NULL, NULL, NULL, NULL,
    @staffId, @staffId, NULL, NULL, SYSUTCDATETIME(), @customerId
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-017' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-017', @customer2Id, 'PACKED', 'PAID', 'VND',
    110000, 30000, 0, 140000,
    N'Nguyen Thi Lan', '0901000002', N'25 Hai Ba Trung', N'Trang Tien', N'Hoan Kiem', N'Ha Noi', N'Ha Noi', 'STANDARD',
    NULL, NULL, N'Auto generated extra test order', N'Packed for testing', NULL,
    DATEADD(DAY,-14,SYSUTCDATETIME()), DATEADD(DAY,-14,SYSUTCDATETIME()), DATEADD(DAY,-13,SYSUTCDATETIME()), NULL, NULL, NULL, NULL,
    @staffId, @staffId, NULL, NULL, SYSUTCDATETIME(), @customer2Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-018' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-018', @customer3Id, 'PACKED', 'PAID', 'VND',
    151000, 30000, 0, 181000,
    N'Tran Van Minh', '0901000003', N'101 Le Duan', N'Thach Thang', N'Hai Chau', N'Da Nang', N'Da Nang', 'STANDARD',
    NULL, NULL, N'Auto generated extra test order', N'Packed for testing', NULL,
    DATEADD(DAY,-13,SYSUTCDATETIME()), DATEADD(DAY,-13,SYSUTCDATETIME()), DATEADD(DAY,-12,SYSUTCDATETIME()), NULL, NULL, NULL, NULL,
    @staffId, @staffId, NULL, NULL, SYSUTCDATETIME(), @customer3Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-019' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-019', @customer4Id, 'PACKED', 'PAID', 'VND',
    144000, 30000, 0, 174000,
    N'Le Thu Ha', '0901000004', N'08 Tran Phu', N'Loc Tho', N'Nha Trang', N'Nha Trang', N'Khanh Hoa', 'STANDARD',
    NULL, NULL, N'Auto generated extra test order', N'Packed for testing', NULL,
    DATEADD(DAY,-12,SYSUTCDATETIME()), DATEADD(DAY,-12,SYSUTCDATETIME()), DATEADD(DAY,-11,SYSUTCDATETIME()), NULL, NULL, NULL, NULL,
    @staffId, @staffId, NULL, NULL, SYSUTCDATETIME(), @customer4Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-020' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-020', @customerId, 'SHIPPED', 'PAID', 'VND',
    143000, 30000, 0, 173000,
    N'Test Customer', '0901000001', N'12 Nguyen Hue', N'Ben Nghe', N'Quan 1', N'Ho Chi Minh', N'Ho Chi Minh', 'STANDARD',
    N'GHN', 'TRK-TEST-020', N'Auto generated extra test order', N'Packed for testing', NULL,
    DATEADD(DAY,-11,SYSUTCDATETIME()), DATEADD(DAY,-11,SYSUTCDATETIME()), DATEADD(DAY,-10,SYSUTCDATETIME()), DATEADD(DAY,-9,SYSUTCDATETIME()), NULL, NULL, NULL,
    @staffId, @staffId, @staffId, NULL, SYSUTCDATETIME(), @customerId
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-021' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-021', @customer2Id, 'SHIPPED', 'PAID', 'VND',
    299000, 50000, 0, 349000,
    N'Nguyen Thi Lan', '0901000002', N'25 Hai Ba Trung', N'Trang Tien', N'Hoan Kiem', N'Ha Noi', N'Ha Noi', 'EXPRESS',
    N'GHN', 'TRK-TEST-021', N'Auto generated extra test order', N'Packed for testing', NULL,
    DATEADD(DAY,-10,SYSUTCDATETIME()), DATEADD(DAY,-10,SYSUTCDATETIME()), DATEADD(DAY,-9,SYSUTCDATETIME()), DATEADD(DAY,-8,SYSUTCDATETIME()), NULL, NULL, NULL,
    @staffId, @staffId, @staffId, NULL, SYSUTCDATETIME(), @customer2Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-022' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-022', @customer3Id, 'SHIPPED', 'PAID', 'VND',
    181000, 30000, 0, 211000,
    N'Tran Van Minh', '0901000003', N'101 Le Duan', N'Thach Thang', N'Hai Chau', N'Da Nang', N'Da Nang', 'STANDARD',
    N'GHN', 'TRK-TEST-022', N'Auto generated extra test order', N'Packed for testing', NULL,
    DATEADD(DAY,-9,SYSUTCDATETIME()), DATEADD(DAY,-9,SYSUTCDATETIME()), DATEADD(DAY,-8,SYSUTCDATETIME()), DATEADD(DAY,-7,SYSUTCDATETIME()), NULL, NULL, NULL,
    @staffId, @staffId, @staffId, NULL, SYSUTCDATETIME(), @customer3Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-023' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-023', @customer4Id, 'SHIPPED', 'PAID', 'VND',
    147000, 30000, 30000, 147000,
    N'Le Thu Ha', '0901000004', N'08 Tran Phu', N'Loc Tho', N'Nha Trang', N'Nha Trang', N'Khanh Hoa', 'STANDARD',
    N'GHN', 'TRK-TEST-023', N'Auto generated extra test order', N'Packed for testing', NULL,
    DATEADD(DAY,-8,SYSUTCDATETIME()), DATEADD(DAY,-8,SYSUTCDATETIME()), DATEADD(DAY,-7,SYSUTCDATETIME()), DATEADD(DAY,-6,SYSUTCDATETIME()), NULL, NULL, NULL,
    @staffId, @staffId, @staffId, NULL, SYSUTCDATETIME(), @customer4Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-024' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-024', @customerId, 'DELIVERED', 'PAID', 'VND',
    151000, 30000, 0, 181000,
    N'Test Customer', '0901000001', N'12 Nguyen Hue', N'Ben Nghe', N'Quan 1', N'Ho Chi Minh', N'Ho Chi Minh', 'STANDARD',
    N'GHN', 'TRK-TEST-024', N'Auto generated extra test order', N'Packed for testing', NULL,
    DATEADD(DAY,-7,SYSUTCDATETIME()), DATEADD(DAY,-7,SYSUTCDATETIME()), DATEADD(DAY,-6,SYSUTCDATETIME()), DATEADD(DAY,-5,SYSUTCDATETIME()), DATEADD(DAY,-4,SYSUTCDATETIME()), NULL, NULL,
    @staffId, @staffId, @staffId, NULL, SYSUTCDATETIME(), @customerId
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-025' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-025', @customer2Id, 'DELIVERED', 'PAID', 'VND',
    69000, 30000, 0, 99000,
    N'Nguyen Thi Lan', '0901000002', N'25 Hai Ba Trung', N'Trang Tien', N'Hoan Kiem', N'Ha Noi', N'Ha Noi', 'STANDARD',
    N'GHN', 'TRK-TEST-025', N'Auto generated extra test order', N'Packed for testing', NULL,
    DATEADD(DAY,-6,SYSUTCDATETIME()), DATEADD(DAY,-6,SYSUTCDATETIME()), DATEADD(DAY,-5,SYSUTCDATETIME()), DATEADD(DAY,-4,SYSUTCDATETIME()), DATEADD(DAY,-3,SYSUTCDATETIME()), NULL, NULL,
    @staffId, @staffId, @staffId, NULL, SYSUTCDATETIME(), @customer2Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-026' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-026', @customer3Id, 'DELIVERED', 'PAID', 'VND',
    191000, 30000, 0, 221000,
    N'Tran Van Minh', '0901000003', N'101 Le Duan', N'Thach Thang', N'Hai Chau', N'Da Nang', N'Da Nang', 'STANDARD',
    N'GHN', 'TRK-TEST-026', N'Auto generated extra test order', N'Packed for testing', NULL,
    DATEADD(DAY,-5,SYSUTCDATETIME()), DATEADD(DAY,-5,SYSUTCDATETIME()), DATEADD(DAY,-4,SYSUTCDATETIME()), DATEADD(DAY,-3,SYSUTCDATETIME()), DATEADD(DAY,-2,SYSUTCDATETIME()), NULL, NULL,
    @staffId, @staffId, @staffId, NULL, SYSUTCDATETIME(), @customer3Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-027' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-027', @customer4Id, 'COMPLETED', 'PAID', 'VND',
    371000, 50000, 0, 421000,
    N'Le Thu Ha', '0901000004', N'08 Tran Phu', N'Loc Tho', N'Nha Trang', N'Nha Trang', N'Khanh Hoa', 'EXPRESS',
    N'GHN', 'TRK-TEST-027', N'Auto generated extra test order', N'Completed successfully', NULL,
    DATEADD(DAY,-4,SYSUTCDATETIME()), DATEADD(DAY,-4,SYSUTCDATETIME()), DATEADD(DAY,-3,SYSUTCDATETIME()), DATEADD(DAY,-2,SYSUTCDATETIME()), DATEADD(DAY,-1,SYSUTCDATETIME()), DATEADD(DAY,-0,SYSUTCDATETIME()), NULL,
    @staffId, @staffId, @staffId, NULL, SYSUTCDATETIME(), @customer4Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-028' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-028', @customerId, 'COMPLETED', 'PAID', 'VND',
    184000, 30000, 0, 214000,
    N'Test Customer', '0901000001', N'12 Nguyen Hue', N'Ben Nghe', N'Quan 1', N'Ho Chi Minh', N'Ho Chi Minh', 'STANDARD',
    N'GHN', 'TRK-TEST-028', N'Auto generated extra test order', N'Completed successfully', NULL,
    DATEADD(DAY,-3,SYSUTCDATETIME()), DATEADD(DAY,-3,SYSUTCDATETIME()), DATEADD(DAY,-2,SYSUTCDATETIME()), DATEADD(DAY,-1,SYSUTCDATETIME()), DATEADD(DAY,-0,SYSUTCDATETIME()), DATEADD(DAY,--1,SYSUTCDATETIME()), NULL,
    @staffId, @staffId, @staffId, NULL, SYSUTCDATETIME(), @customerId
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-029' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-029', @customer2Id, 'CANCELLED', 'FAILED', 'VND',
    161000, 30000, 0, 191000,
    N'Nguyen Thi Lan', '0901000002', N'25 Hai Ba Trung', N'Trang Tien', N'Hoan Kiem', N'Ha Noi', N'Ha Noi', 'STANDARD',
    NULL, NULL, N'Auto generated extra test order', NULL, N'Khach doi y / test cancel flow',
    DATEADD(DAY,-2,SYSUTCDATETIME()), NULL, NULL, NULL, NULL, NULL, DATEADD(DAY,-1,SYSUTCDATETIME()),
    NULL, NULL, NULL, @customerId, SYSUTCDATETIME(), @customer2Id
);

IF NOT EXISTS (SELECT 1 FROM orders WHERE order_code = 'ORD-TEST-030' AND deleted_at IS NULL)
INSERT INTO orders (
    order_code, user_id, status, payment_status, currency,
    subtotal_amount, shipping_fee, discount_amount, total_amount,
    ship_name, ship_phone, ship_line1, ship_ward, ship_district, ship_city, ship_province, ship_method,
    carrier, tracking_code, customer_note, staff_note, cancel_reason,
    placed_at, confirmed_at, packed_at, shipped_at, delivered_at, completed_at, cancelled_at,
    confirmed_by, packed_by, shipped_by, cancelled_by, created_at, created_by
)
VALUES (
    'ORD-TEST-030', @customer3Id, 'CANCELLED', 'FAILED', 'VND',
    262000, 30000, 0, 292000,
    N'Tran Van Minh', '0901000003', N'101 Le Duan', N'Thach Thang', N'Hai Chau', N'Da Nang', N'Da Nang', 'STANDARD',
    NULL, NULL, N'Auto generated extra test order', NULL, N'Khach doi y / test cancel flow',
    DATEADD(DAY,-1,SYSUTCDATETIME()), NULL, NULL, NULL, NULL, NULL, DATEADD(DAY,-0,SYSUTCDATETIME()),
    NULL, NULL, NULL, @customerId, SYSUTCDATETIME(), @customer3Id
);

DECLARE @ord8 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-008' AND deleted_at IS NULL);
DECLARE @ord9 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-009' AND deleted_at IS NULL);
DECLARE @ord10 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-010' AND deleted_at IS NULL);
DECLARE @ord11 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-011' AND deleted_at IS NULL);
DECLARE @ord12 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-012' AND deleted_at IS NULL);
DECLARE @ord13 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-013' AND deleted_at IS NULL);
DECLARE @ord14 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-014' AND deleted_at IS NULL);
DECLARE @ord15 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-015' AND deleted_at IS NULL);
DECLARE @ord16 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-016' AND deleted_at IS NULL);
DECLARE @ord17 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-017' AND deleted_at IS NULL);
DECLARE @ord18 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-018' AND deleted_at IS NULL);
DECLARE @ord19 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-019' AND deleted_at IS NULL);
DECLARE @ord20 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-020' AND deleted_at IS NULL);
DECLARE @ord21 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-021' AND deleted_at IS NULL);
DECLARE @ord22 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-022' AND deleted_at IS NULL);
DECLARE @ord23 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-023' AND deleted_at IS NULL);
DECLARE @ord24 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-024' AND deleted_at IS NULL);
DECLARE @ord25 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-025' AND deleted_at IS NULL);
DECLARE @ord26 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-026' AND deleted_at IS NULL);
DECLARE @ord27 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-027' AND deleted_at IS NULL);
DECLARE @ord28 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-028' AND deleted_at IS NULL);
DECLARE @ord29 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-029' AND deleted_at IS NULL);
DECLARE @ord30 BIGINT = (SELECT TOP 1 id FROM orders WHERE order_code = 'ORD-TEST-030' AND deleted_at IS NULL);

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord8 AND variant_id = @var1 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord8, @var1, NULL, b.title, bv.sku, NULL, 78000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var1;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord8 AND variant_id = @var25 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord8, @var25, NULL, b.title, bv.sku, NULL, 89000, 2, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var25;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord9 AND variant_id = @var37 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord9, @var37, NULL, b.title, bv.sku, NULL, 72000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var37;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord9 AND variant_id = @var13 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord9, @var13, NULL, b.title, bv.sku, NULL, 99000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var13;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord10 AND variant_id = @var28 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord10, @var28, NULL, b.title, bv.sku, NULL, 129000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var28;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord11 AND variant_id = @var39 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord11, @var39, NULL, b.title, bv.sku, NULL, 85000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var39;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord11 AND variant_id = @var2 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord11, @var2, NULL, b.title, bv.sku, NULL, 85000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var2;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord12 AND copy_id = @copy057_02 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord12, @var57, @copy057_02, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 82000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy057_02 WHERE bv.id = @var57;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord12 AND variant_id = @var37 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord12, @var37, NULL, b.title, bv.sku, NULL, 72000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var37;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord13 AND copy_id = @copy058_01 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord13, @var58, @copy058_01, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 79000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy058_01 WHERE bv.id = @var58;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord13 AND variant_id = @var25 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord13, @var25, NULL, b.title, bv.sku, NULL, 89000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var25;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord14 AND copy_id = @copy067_02 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord14, @var67, @copy067_02, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 58000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy067_02 WHERE bv.id = @var67;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord14 AND variant_id = @var13 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord14, @var13, NULL, b.title, bv.sku, NULL, 99000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var13;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord15 AND copy_id = @copy077_01 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord15, @var77, @copy077_01, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 99000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy077_01 WHERE bv.id = @var77;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord15 AND variant_id = @var1 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord15, @var1, NULL, b.title, bv.sku, NULL, 78000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var1;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord16 AND copy_id = @copy085_03 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord16, @var85, @copy085_03, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 22000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy085_03 WHERE bv.id = @var85;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord16 AND copy_id = @copy086_02 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord16, @var86, @copy086_02, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 22000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy086_02 WHERE bv.id = @var86;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord17 AND copy_id = @copy087_01 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord17, @var87, @copy087_01, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 25000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy087_01 WHERE bv.id = @var87;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord17 AND variant_id = @var39 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord17, @var39, NULL, b.title, bv.sku, NULL, 85000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var39;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord18 AND copy_id = @copy070_01 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord18, @var70, @copy070_01, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 62000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy070_01 WHERE bv.id = @var70;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord18 AND variant_id = @var25 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord18, @var25, NULL, b.title, bv.sku, NULL, 89000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var25;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord19 AND copy_id = @copy095_02 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord19, @var95, @copy095_02, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 72000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy095_02 WHERE bv.id = @var95;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord19 AND variant_id = @var37 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord19, @var37, NULL, b.title, bv.sku, NULL, 72000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var37;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord20 AND copy_id = @copy096_01 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord20, @var96, @copy096_01, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 65000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy096_01 WHERE bv.id = @var96;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord20 AND variant_id = @var1 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord20, @var1, NULL, b.title, bv.sku, NULL, 78000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var1;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord21 AND copy_id = @copy097_02 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord21, @var97, @copy097_02, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 299000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy097_02 WHERE bv.id = @var97;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord22 AND copy_id = @copy057_03 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord22, @var57, @copy057_03, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 82000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy057_03 WHERE bv.id = @var57;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord22 AND variant_id = @var13 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord22, @var13, NULL, b.title, bv.sku, NULL, 99000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var13;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord23 AND copy_id = @copy067_03 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord23, @var67, @copy067_03, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 58000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy067_03 WHERE bv.id = @var67;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord23 AND variant_id = @var25 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord23, @var25, NULL, b.title, bv.sku, NULL, 89000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var25;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord24 AND copy_id = @copy058_02 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord24, @var58, @copy058_02, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 79000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy058_02 WHERE bv.id = @var58;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord24 AND variant_id = @var37 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord24, @var37, NULL, b.title, bv.sku, NULL, 72000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var37;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord25 AND copy_id = @copy085_04 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord25, @var85, @copy085_04, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 22000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy085_04 WHERE bv.id = @var85;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord25 AND copy_id = @copy086_03 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord25, @var86, @copy086_03, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 22000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy086_03 WHERE bv.id = @var86;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord25 AND copy_id = @copy087_02 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord25, @var87, @copy087_02, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 25000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy087_02 WHERE bv.id = @var87;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord26 AND copy_id = @copy070_02 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord26, @var70, @copy070_02, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 62000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy070_02 WHERE bv.id = @var70;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord26 AND variant_id = @var28 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord26, @var28, NULL, b.title, bv.sku, NULL, 129000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var28;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord27 AND copy_id = @copy095_03 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord27, @var95, @copy095_03, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 72000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy095_03 WHERE bv.id = @var95;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord27 AND copy_id = @copy097_03 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord27, @var97, @copy097_03, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 299000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy097_03 WHERE bv.id = @var97;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord28 AND copy_id = @copy077_02 AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, pick_method, picked_by, picked_at, created_at)
SELECT @ord28, @var77, @copy077_02, b.title, bv.sku, c.condition_grade, COALESCE(c.sell_price_override, 99000), 1, 'SCAN', @staffId, DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id JOIN copies c ON c.id = @copy077_02 WHERE bv.id = @var77;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord28 AND variant_id = @var39 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord28, @var39, NULL, b.title, bv.sku, NULL, 85000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var39;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord29 AND variant_id = @var25 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord29, @var25, NULL, b.title, bv.sku, NULL, 89000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var25;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord29 AND variant_id = @var37 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord29, @var37, NULL, b.title, bv.sku, NULL, 72000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var37;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord30 AND variant_id = @var1 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord30, @var1, NULL, b.title, bv.sku, NULL, 78000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var1;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord30 AND variant_id = @var13 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord30, @var13, NULL, b.title, bv.sku, NULL, 99000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var13;

IF NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @ord30 AND variant_id = @var39 AND copy_id IS NULL AND deleted_at IS NULL)
INSERT INTO order_items (order_id, variant_id, copy_id, title_snapshot, sku_snapshot, condition_snapshot, unit_price, quantity, created_at)
SELECT @ord30, @var39, NULL, b.title, bv.sku, NULL, 85000, 1, SYSUTCDATETIME()
FROM books b JOIN book_variants bv ON bv.book_id = b.id WHERE bv.id = @var39;

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord8)
INSERT INTO payments (order_id, provider, provider_transaction_id, pay_url, amount, currency, status, expired_at, created_at, created_by)
VALUES (@ord8, 'PAYOS', 'PAY-TEST-008', N'https://pay.test/008', 286000, 'VND', 'CREATED', DATEADD(HOUR,2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customerId);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord9)
INSERT INTO payments (order_id, provider, provider_transaction_id, pay_url, amount, currency, status, expired_at, created_at, created_by)
VALUES (@ord9, 'PAYOS', 'PAY-TEST-009', N'https://pay.test/009', 201000, 'VND', 'CREATED', DATEADD(HOUR,2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer2Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord10)
INSERT INTO payments (order_id, provider, provider_transaction_id, pay_url, amount, currency, status, expired_at, created_at, created_by)
VALUES (@ord10, 'PAYOS', 'PAY-TEST-010', N'https://pay.test/010', 179000, 'VND', 'CREATED', DATEADD(HOUR,2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer3Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord11)
INSERT INTO payments (order_id, provider, provider_transaction_id, pay_url, amount, currency, status, expired_at, created_at, created_by)
VALUES (@ord11, 'PAYOS', 'PAY-TEST-011', N'https://pay.test/011', 170000, 'VND', 'CREATED', DATEADD(HOUR,2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer4Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord12)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord12, 'PAYOS', 'PAY-TEST-012', 184000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customerId);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord13)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord13, 'PAYOS', 'PAY-TEST-013', 198000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer2Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord14)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord14, 'PAYOS', 'PAY-TEST-014', 187000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer3Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord15)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord15, 'PAYOS', 'PAY-TEST-015', 207000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer4Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord16)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord16, 'PAYOS', 'PAY-TEST-016', 74000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customerId);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord17)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord17, 'PAYOS', 'PAY-TEST-017', 140000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer2Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord18)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord18, 'PAYOS', 'PAY-TEST-018', 181000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer3Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord19)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord19, 'PAYOS', 'PAY-TEST-019', 174000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer4Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord20)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord20, 'PAYOS', 'PAY-TEST-020', 173000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customerId);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord21)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord21, 'PAYOS', 'PAY-TEST-021', 349000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer2Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord22)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord22, 'PAYOS', 'PAY-TEST-022', 211000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer3Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord23)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord23, 'PAYOS', 'PAY-TEST-023', 147000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer4Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord24)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord24, 'PAYOS', 'PAY-TEST-024', 181000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customerId);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord25)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord25, 'PAYOS', 'PAY-TEST-025', 99000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer2Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord26)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord26, 'PAYOS', 'PAY-TEST-026', 221000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer3Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord27)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord27, 'PAYOS', 'PAY-TEST-027', 421000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customer4Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord28)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, paid_at, created_at, created_by)
VALUES (@ord28, 'PAYOS', 'PAY-TEST-028', 214000, 'VND', 'SUCCEEDED', DATEADD(DAY,-2,SYSUTCDATETIME()), SYSUTCDATETIME(), @customerId);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord29)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, created_at, created_by)
VALUES (@ord29, 'PAYOS', 'PAY-TEST-029', 191000, 'VND', 'FAILED', SYSUTCDATETIME(), @customer2Id);

IF NOT EXISTS (SELECT 1 FROM payments WHERE order_id = @ord30)
INSERT INTO payments (order_id, provider, provider_transaction_id, amount, currency, status, created_at, created_by)
VALUES (@ord30, 'PAYOS', 'PAY-TEST-030', 292000, 'VND', 'FAILED', SYSUTCDATETIME(), @customer3Id);

UPDATE copies SET status = 'RESERVED', updated_at = SYSUTCDATETIME() WHERE id = @copy057_02 AND status IN ('AVAILABLE');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'RESERVE', c.variant_id, c.id, 1, c.location, 'ORDER', @ord12, 'SALE', N'Reserved for confirmed order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy057_02
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord12 AND it.copy_id = @copy057_02 AND it.movement_type = 'RESERVE');

UPDATE copies SET status = 'RESERVED', updated_at = SYSUTCDATETIME() WHERE id = @copy058_01 AND status IN ('AVAILABLE');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'RESERVE', c.variant_id, c.id, 1, c.location, 'ORDER', @ord13, 'SALE', N'Reserved for confirmed order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy058_01
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord13 AND it.copy_id = @copy058_01 AND it.movement_type = 'RESERVE');

UPDATE copies SET status = 'RESERVED', updated_at = SYSUTCDATETIME() WHERE id = @copy067_02 AND status IN ('AVAILABLE');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'RESERVE', c.variant_id, c.id, 1, c.location, 'ORDER', @ord14, 'SALE', N'Reserved for confirmed order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy067_02
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord14 AND it.copy_id = @copy067_02 AND it.movement_type = 'RESERVE');

UPDATE copies SET status = 'RESERVED', updated_at = SYSUTCDATETIME() WHERE id = @copy077_01 AND status IN ('AVAILABLE');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'RESERVE', c.variant_id, c.id, 1, c.location, 'ORDER', @ord15, 'SALE', N'Reserved for confirmed order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy077_01
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord15 AND it.copy_id = @copy077_01 AND it.movement_type = 'RESERVE');

UPDATE copies SET status = 'PACKED', updated_at = SYSUTCDATETIME() WHERE id = @copy085_03 AND status IN ('AVAILABLE','PICKED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord16, 'SALE', N'Packed extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy085_03
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord16 AND it.copy_id = @copy085_03 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'PACKED', updated_at = SYSUTCDATETIME() WHERE id = @copy086_02 AND status IN ('AVAILABLE','PICKED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord16, 'SALE', N'Packed extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy086_02
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord16 AND it.copy_id = @copy086_02 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'PACKED', updated_at = SYSUTCDATETIME() WHERE id = @copy087_01 AND status IN ('AVAILABLE','PICKED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord17, 'SALE', N'Packed extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy087_01
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord17 AND it.copy_id = @copy087_01 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'PACKED', updated_at = SYSUTCDATETIME() WHERE id = @copy070_01 AND status IN ('AVAILABLE','PICKED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord18, 'SALE', N'Packed extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy070_01
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord18 AND it.copy_id = @copy070_01 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'PACKED', updated_at = SYSUTCDATETIME() WHERE id = @copy095_02 AND status IN ('AVAILABLE','PICKED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord19, 'SALE', N'Packed extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy095_02
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord19 AND it.copy_id = @copy095_02 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'SHIPPED', updated_at = SYSUTCDATETIME() WHERE id = @copy096_01 AND status IN ('AVAILABLE','PICKED','PACKED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord20, 'SALE', N'Shipped extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy096_01
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord20 AND it.copy_id = @copy096_01 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'SHIPPED', updated_at = SYSUTCDATETIME() WHERE id = @copy097_02 AND status IN ('AVAILABLE','PICKED','PACKED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord21, 'SALE', N'Shipped extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy097_02
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord21 AND it.copy_id = @copy097_02 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'SHIPPED', updated_at = SYSUTCDATETIME() WHERE id = @copy057_03 AND status IN ('AVAILABLE','PICKED','PACKED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord22, 'SALE', N'Shipped extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy057_03
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord22 AND it.copy_id = @copy057_03 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'SHIPPED', updated_at = SYSUTCDATETIME() WHERE id = @copy067_03 AND status IN ('AVAILABLE','PICKED','PACKED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord23, 'SALE', N'Shipped extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy067_03
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord23 AND it.copy_id = @copy067_03 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'SOLD', updated_at = SYSUTCDATETIME() WHERE id = @copy058_02 AND status IN ('AVAILABLE','PICKED','PACKED','SHIPPED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord24, 'SALE', N'Sold extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy058_02
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord24 AND it.copy_id = @copy058_02 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'SOLD', updated_at = SYSUTCDATETIME() WHERE id = @copy085_04 AND status IN ('AVAILABLE','PICKED','PACKED','SHIPPED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord25, 'SALE', N'Sold extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy085_04
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord25 AND it.copy_id = @copy085_04 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'SOLD', updated_at = SYSUTCDATETIME() WHERE id = @copy086_03 AND status IN ('AVAILABLE','PICKED','PACKED','SHIPPED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord25, 'SALE', N'Sold extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy086_03
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord25 AND it.copy_id = @copy086_03 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'SOLD', updated_at = SYSUTCDATETIME() WHERE id = @copy087_02 AND status IN ('AVAILABLE','PICKED','PACKED','SHIPPED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord25, 'SALE', N'Sold extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy087_02
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord25 AND it.copy_id = @copy087_02 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'SOLD', updated_at = SYSUTCDATETIME() WHERE id = @copy070_02 AND status IN ('AVAILABLE','PICKED','PACKED','SHIPPED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord26, 'SALE', N'Sold extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy070_02
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord26 AND it.copy_id = @copy070_02 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'SOLD', updated_at = SYSUTCDATETIME() WHERE id = @copy095_03 AND status IN ('AVAILABLE','PICKED','PACKED','SHIPPED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord27, 'SALE', N'Sold extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy095_03
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord27 AND it.copy_id = @copy095_03 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'SOLD', updated_at = SYSUTCDATETIME() WHERE id = @copy097_03 AND status IN ('AVAILABLE','PICKED','PACKED','SHIPPED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord27, 'SALE', N'Sold extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy097_03
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord27 AND it.copy_id = @copy097_03 AND it.movement_type = 'OUT');

UPDATE copies SET status = 'SOLD', updated_at = SYSUTCDATETIME() WHERE id = @copy077_02 AND status IN ('AVAILABLE','PICKED','PACKED','SHIPPED','RESERVED');
INSERT INTO inventory_transactions (movement_type, variant_id, copy_id, quantity, from_location, reference_type, reference_id, reason, note, created_at, created_by)
SELECT 'OUT', c.variant_id, c.id, 1, c.location, 'ORDER', @ord28, 'SALE', N'Sold extra test order', DATEADD(DAY,-2,SYSUTCDATETIME()), @staffId
FROM copies c WHERE c.id = @copy077_02
AND NOT EXISTS (SELECT 1 FROM inventory_transactions it WHERE it.reference_type = 'ORDER' AND it.reference_id = @ord28 AND it.copy_id = @copy077_02 AND it.movement_type = 'OUT');

/* Recompute lot counters safely (idempotent) */
;WITH qty_orders AS (
    SELECT oi.variant_id,
           SUM(CASE WHEN o.status = 'CONFIRMED' AND oi.copy_id IS NULL THEN oi.quantity ELSE 0 END) AS qty_reserved,
           SUM(CASE WHEN o.status IN ('PACKED','SHIPPED','DELIVERED','COMPLETED') AND oi.copy_id IS NULL THEN oi.quantity ELSE 0 END) AS qty_sold
    FROM order_items oi
    JOIN orders o ON o.id = oi.order_id
    WHERE o.deleted_at IS NULL AND oi.deleted_at IS NULL
    GROUP BY oi.variant_id
), copy_state AS (
    SELECT c.variant_id,
           SUM(CASE WHEN c.status = 'RESERVED' THEN 1 ELSE 0 END) AS qty_reserved,
           SUM(CASE WHEN c.status IN ('PACKED','SHIPPED','SOLD','RETURNED') THEN 1 ELSE 0 END) AS qty_sold,
           SUM(CASE WHEN c.status = 'DAMAGED' THEN 1 ELSE 0 END) AS qty_damaged,
           SUM(CASE WHEN c.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS qty_available
    FROM copies c
    WHERE c.deleted_at IS NULL
    GROUP BY c.variant_id
)
UPDATE l
SET l.qty_reserved = CASE WHEN b.sell_mode = 'PER_COPY' THEN ISNULL(cs.qty_reserved, 0) ELSE ISNULL(qo.qty_reserved, 0) END,
    l.qty_sold = CASE WHEN b.sell_mode = 'PER_COPY' THEN ISNULL(cs.qty_sold, 0) ELSE ISNULL(qo.qty_sold, 0) END,
    l.qty_damaged = CASE WHEN b.sell_mode = 'PER_COPY' THEN ISNULL(cs.qty_damaged, 0) ELSE l.qty_damaged END,
    l.qty_available = CASE
        WHEN b.sell_mode = 'PER_COPY' THEN ISNULL(cs.qty_available, 0)
        ELSE CASE
            WHEN l.qty_received - ISNULL(qo.qty_reserved, 0) - ISNULL(qo.qty_sold, 0) - ISNULL(l.qty_damaged, 0) < 0 THEN 0
            ELSE l.qty_received - ISNULL(qo.qty_reserved, 0) - ISNULL(qo.qty_sold, 0) - ISNULL(l.qty_damaged, 0)
        END
    END,
    l.updated_at = SYSUTCDATETIME()
FROM lots l
JOIN book_variants bv ON bv.id = l.variant_id
JOIN books b ON b.id = bv.book_id
LEFT JOIN qty_orders qo ON qo.variant_id = l.variant_id
LEFT JOIN copy_state cs ON cs.variant_id = l.variant_id;

;WITH q AS (
    SELECT l.variant_id, SUM(l.qty_available) AS qty_available
    FROM lots l WHERE l.deleted_at IS NULL GROUP BY l.variant_id
)
UPDATE b
SET stock_quantity = ISNULL(src.total_qty, 0), updated_at = SYSUTCDATETIME(), updated_by = @managerId
FROM books b
OUTER APPLY (SELECT SUM(q.qty_available) AS total_qty FROM book_variants bv LEFT JOIN q ON q.variant_id = bv.id WHERE bv.book_id = b.id AND bv.deleted_at IS NULL) AS src;

PRINT N'✅ Added extra test orders ORD-TEST-008 -> ORD-TEST-030';
