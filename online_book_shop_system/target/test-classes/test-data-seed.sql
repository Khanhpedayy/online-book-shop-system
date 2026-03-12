-- ============================================================
-- TEST DATA SEED for Integration Tests
-- Uses explicit IDs (90000+) to avoid collision with real data.
-- Matches actual DB schema (updated databasebansach.sql).
-- ============================================================

-- ── USERS (role_id: 1=ADMIN, 2=MANAGER, 3=STAFF, 4=CUSTOMER) ──
SET IDENTITY_INSERT users ON;
IF NOT EXISTS (SELECT 1 FROM users WHERE id = 90001)
    INSERT INTO users (id, full_name, email, phone, password_hash, role_id, status, created_at)
    VALUES (90001, N'Test Admin', 'testadmin@bookshop.com', '0901000001', 'hash123', 1, 'ACTIVE', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM users WHERE id = 90002)
    INSERT INTO users (id, full_name, email, phone, password_hash, role_id, status, created_at)
    VALUES (90002, N'Test Customer', 'testcustomer@bookshop.com', '0901000002', 'hash456', 4, 'ACTIVE', SYSUTCDATETIME());
SET IDENTITY_INSERT users OFF;

-- ── CATEGORIES ──
SET IDENTITY_INSERT categories ON;
IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 90001)
    INSERT INTO categories (id, name, slug, description, sort_order, is_active, created_at)
    VALUES (90001, N'Test Fiction', 'test-fiction', N'Fiction books for testing', 1, 1, SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 90002)
    INSERT INTO categories (id, name, slug, description, sort_order, is_active, created_at)
    VALUES (90002, N'Test Science', 'test-science', N'Science books for testing', 2, 1, SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 90003)
    INSERT INTO categories (id, name, slug, description, sort_order, is_active, created_at)
    VALUES (90003, N'Test History', 'test-history', N'History books for testing', 3, 1, SYSUTCDATETIME());
SET IDENTITY_INSERT categories OFF;

-- ── AUTHORS ──
SET IDENTITY_INSERT authors ON;
IF NOT EXISTS (SELECT 1 FROM authors WHERE id = 90001)
    INSERT INTO authors (id, name, slug, created_at)
    VALUES (90001, N'Author Test One', 'author-test-one', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM authors WHERE id = 90002)
    INSERT INTO authors (id, name, slug, created_at)
    VALUES (90002, N'Author Test Two', 'author-test-two', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM authors WHERE id = 90003)
    INSERT INTO authors (id, name, slug, created_at)
    VALUES (90003, N'Author Test Three', 'author-test-three', SYSUTCDATETIME());
SET IDENTITY_INSERT authors OFF;

-- ── SUPPLIERS ──
SET IDENTITY_INSERT suppliers ON;
IF NOT EXISTS (SELECT 1 FROM suppliers WHERE id = 90001)
    INSERT INTO suppliers (id, name, code, contact_person, email, phone, address, is_active, created_at)
    VALUES (90001, N'Test Supplier Alpha', 'SUP-ALPHA', N'Mr Alpha', 'alpha@supplier.com', '0281000001', N'123 Alpha St', 1, SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM suppliers WHERE id = 90002)
    INSERT INTO suppliers (id, name, code, contact_person, email, phone, address, is_active, created_at)
    VALUES (90002, N'Test Supplier Beta', 'SUP-BETA', N'Ms Beta', 'beta@supplier.com', '0281000002', N'456 Beta Ave', 1, SYSUTCDATETIME());
SET IDENTITY_INSERT suppliers OFF;

-- ── BOOKS ──
SET IDENTITY_INSERT books ON;
IF NOT EXISTS (SELECT 1 FROM books WHERE id = 90001)
    INSERT INTO books (id, category_id, isbn13, isbn10, title, subtitle, slug, publisher_name, publication_year, language, short_description, description_html, tags_json, sell_mode, status, created_at)
    VALUES (90001, 90001, '9780000000001', '0000000001', N'Test Clean Code', N'A Handbook', 'test-clean-code', N'Test Publisher', 2023, 'vi', N'Short desc 1', N'<p>Full description</p>', '["testing","code"]', 'QUANTITY', 'ACTIVE', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM books WHERE id = 90002)
    INSERT INTO books (id, category_id, isbn13, isbn10, title, subtitle, slug, publisher_name, publication_year, language, short_description, description_html, tags_json, sell_mode, status, created_at)
    VALUES (90002, 90001, '9780000000002', '0000000002', N'Test Design Patterns', N'Elements of Reusable', 'test-design-patterns', N'Test Publisher', 2022, 'en', N'Short desc 2', N'<p>Design patterns</p>', '["patterns"]', 'QUANTITY', 'ACTIVE', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM books WHERE id = 90003)
    INSERT INTO books (id, category_id, isbn13, title, slug, publisher_name, publication_year, language, short_description, sell_mode, status, created_at)
    VALUES (90003, 90002, '9780000000003', N'Test Algorithms', 'test-algorithms', N'Science Press', 2021, 'vi', N'Short desc 3', 'QUANTITY', 'DRAFT', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM books WHERE id = 90004)
    INSERT INTO books (id, category_id, isbn13, title, slug, publisher_name, publication_year, language, short_description, sell_mode, status, created_at)
    VALUES (90004, 90003, '9780000000004', N'Test World History', 'test-world-history', N'History House', 2020, 'vi', N'Short desc 4', 'QUANTITY', 'HIDDEN', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM books WHERE id = 90005)
    INSERT INTO books (id, category_id, isbn13, title, slug, publisher_name, publication_year, language, short_description, sell_mode, status, created_at, deleted_at)
    VALUES (90005, 90001, '9780000000005', N'Test Deleted Book', 'test-deleted-book', N'Old Press', 2019, 'vi', N'Deleted book', 'QUANTITY', 'ACTIVE', SYSUTCDATETIME(), SYSUTCDATETIME());
SET IDENTITY_INSERT books OFF;

-- ── BOOK_AUTHORS ──
INSERT INTO book_authors (book_id, author_id, role, sort_order)
SELECT 90001, 90001, 'AUTHOR', 1 WHERE NOT EXISTS (SELECT 1 FROM book_authors WHERE book_id = 90001 AND author_id = 90001);
INSERT INTO book_authors (book_id, author_id, role, sort_order)
SELECT 90001, 90002, 'TRANSLATOR', 2 WHERE NOT EXISTS (SELECT 1 FROM book_authors WHERE book_id = 90001 AND author_id = 90002);
INSERT INTO book_authors (book_id, author_id, role, sort_order)
SELECT 90002, 90002, 'AUTHOR', 1 WHERE NOT EXISTS (SELECT 1 FROM book_authors WHERE book_id = 90002 AND author_id = 90002);

-- ── BOOK_IMAGES ──
SET IDENTITY_INSERT book_images ON;
IF NOT EXISTS (SELECT 1 FROM book_images WHERE id = 90001)
    INSERT INTO book_images (id, book_id, url, alt_text, is_cover, sort_order, created_at)
    VALUES (90001, 90001, 'https://example.com/img/clean-code-cover.jpg', 'Clean Code Cover', 1, 1, SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM book_images WHERE id = 90002)
    INSERT INTO book_images (id, book_id, url, alt_text, is_cover, sort_order, created_at)
    VALUES (90002, 90001, 'https://example.com/img/clean-code-back.jpg', 'Clean Code Back', 0, 2, SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM book_images WHERE id = 90003)
    INSERT INTO book_images (id, book_id, url, alt_text, is_cover, sort_order, created_at)
    VALUES (90003, 90002, 'https://example.com/img/design-patterns-cover.jpg', 'Design Patterns Cover', 1, 1, SYSUTCDATETIME());
SET IDENTITY_INSERT book_images OFF;

-- ── BOOK_VARIANTS ──
SET IDENTITY_INSERT book_variants ON;
IF NOT EXISTS (SELECT 1 FROM book_variants WHERE id = 90001)
    INSERT INTO book_variants (id, book_id, sku, format, edition, language, list_price, sale_price, page_count, weight_grams, is_active, created_at)
    VALUES (90001, 90001, 'TEST-CC-HC', 'HARDCOVER', '1st', 'vi', 250000, 220000, 464, 650, 1, SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM book_variants WHERE id = 90002)
    INSERT INTO book_variants (id, book_id, sku, format, edition, language, list_price, sale_price, page_count, weight_grams, is_active, created_at)
    VALUES (90002, 90001, 'TEST-CC-PB', 'PAPERBACK', '1st', 'vi', 180000, 160000, 464, 400, 1, SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM book_variants WHERE id = 90003)
    INSERT INTO book_variants (id, book_id, sku, format, edition, language, list_price, sale_price, page_count, weight_grams, is_active, created_at)
    VALUES (90003, 90002, 'TEST-DP-HC', 'HARDCOVER', '1st', 'en', 350000, 300000, 395, 700, 1, SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM book_variants WHERE id = 90004)
    INSERT INTO book_variants (id, book_id, sku, format, edition, language, list_price, sale_price, is_active, created_at)
    VALUES (90004, 90003, 'TEST-ALG-PB', 'PAPERBACK', '2nd', 'vi', 100000, 80000, 1, SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM book_variants WHERE id = 90005)
    INSERT INTO book_variants (id, book_id, sku, format, edition, language, list_price, sale_price, is_active, created_at)
    VALUES (90005, 90004, 'TEST-HIS-PB', 'PAPERBACK', '1st', 'vi', 200000, 170000, 1, SYSUTCDATETIME());
SET IDENTITY_INSERT book_variants OFF;

-- ── LOTS ──
SET IDENTITY_INSERT lots ON;
IF NOT EXISTS (SELECT 1 FROM lots WHERE id = 90001)
    INSERT INTO lots (id, lot_code, variant_id, supplier_id, qty_received, qty_available, qty_damaged, unit_cost, condition_default, status, invoice_no, note, received_at, created_at)
    VALUES (90001, 'TEST-LOT-001', 90001, 90001, 50, 40, 2, 150000, 'NEW', 'RELEASED', 'INV-T-001', 'Test lot 1', DATEADD(day, -10, SYSUTCDATETIME()), SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM lots WHERE id = 90002)
    INSERT INTO lots (id, lot_code, variant_id, supplier_id, qty_received, qty_available, qty_damaged, unit_cost, condition_default, status, invoice_no, note, received_at, created_at)
    VALUES (90002, 'TEST-LOT-002', 90002, 90001, 30, 28, 0, 100000, 'NEW', 'RELEASED', 'INV-T-002', 'Test lot 2', DATEADD(day, -5, SYSUTCDATETIME()), SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM lots WHERE id = 90003)
    INSERT INTO lots (id, lot_code, variant_id, supplier_id, qty_received, qty_available, qty_damaged, unit_cost, condition_default, status, invoice_no, note, received_at, created_at)
    VALUES (90003, 'TEST-LOT-003', 90003, 90002, 20, 18, 1, 200000, 'NEW', 'LOCKED', 'INV-T-003', 'Test lot 3 (locked)', DATEADD(day, -100, SYSUTCDATETIME()), SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM lots WHERE id = 90004)
    INSERT INTO lots (id, lot_code, variant_id, supplier_id, qty_received, qty_available, qty_damaged, unit_cost, condition_default, status, received_at, created_at)
    VALUES (90004, 'TEST-LOT-004', 90001, 90002, 10, 10, 0, 155000, 'LIKE_NEW', 'RELEASED', DATEADD(day, -200, SYSUTCDATETIME()), SYSUTCDATETIME());
SET IDENTITY_INSERT lots OFF;

-- ── COPIES ──
SET IDENTITY_INSERT copies ON;
IF NOT EXISTS (SELECT 1 FROM copies WHERE id = 90001)
    INSERT INTO copies (id, copy_code, variant_id, lot_id, condition_grade, condition_note, status, location, created_at)
    VALUES (90001, 'TEST-CPY-001', 90001, 90001, 'NEW', NULL, 'AVAILABLE', 'A-01-01', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM copies WHERE id = 90002)
    INSERT INTO copies (id, copy_code, variant_id, lot_id, condition_grade, condition_note, status, location, created_at)
    VALUES (90002, 'TEST-CPY-002', 90001, 90001, 'NEW', NULL, 'AVAILABLE', 'A-01-02', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM copies WHERE id = 90003)
    INSERT INTO copies (id, copy_code, variant_id, lot_id, condition_grade, condition_note, status, location, created_at)
    VALUES (90003, 'TEST-CPY-003', 90001, 90001, 'LIKE_NEW', 'Minor scratch', 'AVAILABLE', 'A-01-03', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM copies WHERE id = 90004)
    INSERT INTO copies (id, copy_code, variant_id, lot_id, condition_grade, status, location, created_at)
    VALUES (90004, 'TEST-CPY-004', 90001, 90001, 'GOOD', 'DAMAGED', 'A-02-01', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM copies WHERE id = 90005)
    INSERT INTO copies (id, copy_code, variant_id, lot_id, condition_grade, status, location, created_at)
    VALUES (90005, 'TEST-CPY-005', 90002, 90002, 'NEW', 'AVAILABLE', 'B-01-01', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM copies WHERE id = 90006)
    INSERT INTO copies (id, copy_code, variant_id, lot_id, condition_grade, status, location, created_at)
    VALUES (90006, 'TEST-CPY-006', 90002, 90002, 'NEW', 'SOLD', 'B-01-02', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM copies WHERE id = 90007)
    INSERT INTO copies (id, copy_code, variant_id, lot_id, condition_grade, status, location, created_at)
    VALUES (90007, 'TEST-CPY-007', 90003, 90003, 'NEW', 'AVAILABLE', 'C-01-01', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM copies WHERE id = 90008)
    INSERT INTO copies (id, copy_code, variant_id, lot_id, condition_grade, status, location, created_at)
    VALUES (90008, 'TEST-CPY-008', 90003, 90003, 'FAIR', 'AVAILABLE', 'C-01-02', SYSUTCDATETIME());
SET IDENTITY_INSERT copies OFF;

-- ── ORDERS ──
SET IDENTITY_INSERT orders ON;
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 90001)
    INSERT INTO orders (id, order_code, user_id, status, payment_status, currency, subtotal_amount, total_amount, ship_name, ship_phone, ship_line1, ship_city, placed_at, created_at)
    VALUES (90001, 'TEST-ORD-001', 90002, 'COMPLETED', 'PAID', 'VND', 440000, 440000, N'Test Customer', '0901000002', N'123 Test St', N'HCM', DATEADD(day, -5, SYSUTCDATETIME()), SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 90002)
    INSERT INTO orders (id, order_code, user_id, status, payment_status, currency, subtotal_amount, total_amount, ship_name, ship_phone, ship_line1, ship_city, placed_at, created_at)
    VALUES (90002, 'TEST-ORD-002', 90002, 'CONFIRMED', 'PENDING', 'VND', 300000, 300000, N'Test Customer', '0901000002', N'456 Test Ave', N'HCM', DATEADD(day, -2, SYSUTCDATETIME()), SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 90003)
    INSERT INTO orders (id, order_code, user_id, status, payment_status, currency, subtotal_amount, total_amount, ship_name, ship_phone, ship_line1, ship_city, placed_at, created_at)
    VALUES (90003, 'TEST-ORD-003', 90002, 'CANCELLED', 'REFUNDED', 'VND', 170000, 170000, N'Test Customer', '0901000002', N'789 Test Blvd', N'HCM', DATEADD(day, -1, SYSUTCDATETIME()), SYSUTCDATETIME());
SET IDENTITY_INSERT orders OFF;

-- ── ORDER_ITEMS ──
SET IDENTITY_INSERT order_items ON;
IF NOT EXISTS (SELECT 1 FROM order_items WHERE id = 90001)
    INSERT INTO order_items (id, order_id, variant_id, copy_id, quantity, unit_price, line_total, title_snapshot, sku_snapshot, created_at)
    VALUES (90001, 90001, 90001, 90006, 1, 220000, 220000, N'Test Clean Code', 'TEST-CC-HC', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM order_items WHERE id = 90002)
    INSERT INTO order_items (id, order_id, variant_id, copy_id, quantity, unit_price, line_total, title_snapshot, sku_snapshot, created_at)
    VALUES (90002, 90001, 90002, NULL, 1, 160000, 160000, N'Test Clean Code PB', 'TEST-CC-PB', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM order_items WHERE id = 90003)
    INSERT INTO order_items (id, order_id, variant_id, copy_id, quantity, unit_price, line_total, title_snapshot, sku_snapshot, created_at)
    VALUES (90003, 90002, 90003, 90007, 1, 300000, 300000, N'Test Design Patterns', 'TEST-DP-HC', SYSUTCDATETIME());
SET IDENTITY_INSERT order_items OFF;

-- ── RETURNS ──
SET IDENTITY_INSERT returns ON;
IF NOT EXISTS (SELECT 1 FROM returns WHERE id = 90001)
    INSERT INTO returns (id, return_code, order_id, status, reason, note, refund_amount, requested_by, created_at)
    VALUES (90001, 'TEST-RET-001', 90001, 'REQUESTED', N'Defective product', N'Book has torn pages', 220000, 90002, SYSUTCDATETIME());
SET IDENTITY_INSERT returns OFF;

-- ── RETURN_ITEMS ──
SET IDENTITY_INSERT return_items ON;
IF NOT EXISTS (SELECT 1 FROM return_items WHERE id = 90001)
    INSERT INTO return_items (id, return_id, order_item_id, copy_id, quantity)
    VALUES (90001, 90001, 90001, 90006, 1);
IF NOT EXISTS (SELECT 1 FROM return_items WHERE id = 90002)
    INSERT INTO return_items (id, return_id, order_item_id, copy_id, quantity)
    VALUES (90002, 90001, 90002, NULL, 1);
SET IDENTITY_INSERT return_items OFF;

-- ── INVENTORY_TRANSACTIONS ──
SET IDENTITY_INSERT inventory_transactions ON;
IF NOT EXISTS (SELECT 1 FROM inventory_transactions WHERE id = 90001)
    INSERT INTO inventory_transactions (id, movement_type, variant_id, lot_id, copy_id, quantity, reference_type, reason, note, created_at)
    VALUES (90001, 'IN', 90001, 90001, NULL, 50, 'LOT', 'RECEIVE', 'Initial receipt LOT-001', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM inventory_transactions WHERE id = 90002)
    INSERT INTO inventory_transactions (id, movement_type, variant_id, lot_id, copy_id, quantity, reference_type, reason, note, created_at)
    VALUES (90002, 'OUT', 90001, 90001, 90006, 1, 'ORDER', 'SALE', 'Sold via order TEST-ORD-001', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM inventory_transactions WHERE id = 90003)
    INSERT INTO inventory_transactions (id, movement_type, variant_id, lot_id, copy_id, quantity, reference_type, reason, note, created_at)
    VALUES (90003, 'ADJUST', 90001, 90001, 90004, 1, 'MANUAL', 'DAMAGED', 'Found damaged during inspection', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM inventory_transactions WHERE id = 90004)
    INSERT INTO inventory_transactions (id, movement_type, variant_id, lot_id, copy_id, quantity, reference_type, reason, note, created_at)
    VALUES (90004, 'ADJUST', 90003, 90003, NULL, 2, 'MANUAL', 'COUNT_DIFF', 'Stocktaking adjustment', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM inventory_transactions WHERE id = 90005)
    INSERT INTO inventory_transactions (id, movement_type, variant_id, lot_id, copy_id, quantity, reference_type, reason, note, created_at)
    VALUES (90005, 'ADJUST', 90002, 90002, NULL, 1, 'MANUAL', 'LOST', 'Missing from shelf', SYSUTCDATETIME());
SET IDENTITY_INSERT inventory_transactions OFF;
