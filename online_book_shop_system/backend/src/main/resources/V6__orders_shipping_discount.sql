-- Customer checkout: persist shipping fee only (idempotent for SQL Server).

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_fee')
    ALTER TABLE orders ADD shipping_fee DECIMAL(18, 2) NOT NULL DEFAULT 0;
