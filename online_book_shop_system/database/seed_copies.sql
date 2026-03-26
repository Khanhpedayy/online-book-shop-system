/* =========================================================
   SEED COPIES — Tạo cuốn sách vật lý trong kho
   Chạy SAU seed_shipping_data.sql
   Mỗi lot tạo 10 copies (status = AVAILABLE)
   ========================================================= */

-- Tạo copies cho tất cả lots đã tồn tại
DECLARE @lotId BIGINT, @variantId BIGINT, @lotCode VARCHAR(60);
DECLARE @i INT;

DECLARE lot_cursor CURSOR FOR
    SELECT id, variant_id, lot_code FROM lots WHERE deleted_at IS NULL;

OPEN lot_cursor;
FETCH NEXT FROM lot_cursor INTO @lotId, @variantId, @lotCode;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @i = 1;
    WHILE @i <= 10
    BEGIN
        INSERT INTO copies (copy_code, lot_id, variant_id, location, condition_grade, status)
        VALUES (
            @lotCode + '-C' + RIGHT('00' + CAST(@i AS VARCHAR), 2),
            @lotId,
            @variantId,
            CASE 
                WHEN @i <= 3 THEN 'A1-01'
                WHEN @i <= 6 THEN 'A1-02'
                WHEN @i <= 8 THEN 'B1-01'
                ELSE 'B1-02'
            END,
            'NEW',
            'AVAILABLE'
        );
        SET @i = @i + 1;
    END
    FETCH NEXT FROM lot_cursor INTO @lotId, @variantId, @lotCode;
END

CLOSE lot_cursor;
DEALLOCATE lot_cursor;

GO

-- Kiểm tra kết quả
SELECT 
    l.lot_code,
    bv.sku,
    COUNT(c.id) AS total_copies,
    SUM(CASE WHEN c.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available
FROM copies c
JOIN lots l ON l.id = c.lot_id
JOIN book_variants bv ON bv.id = c.variant_id
GROUP BY l.lot_code, bv.sku
ORDER BY l.lot_code;

PRINT N'✅ Copies created! Mỗi lot có 10 copies AVAILABLE, tổng = ' + CAST((SELECT COUNT(*) FROM copies) AS VARCHAR) + ' copies';
