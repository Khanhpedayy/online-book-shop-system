-- Customer order: persist checkout payment method (COD / PAYOS / …)
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'payment_method')
    ALTER TABLE orders ADD payment_method NVARCHAR(20) NULL;
