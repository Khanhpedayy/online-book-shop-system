-- ============================================================
-- V1: Staff Operations Tables
-- ============================================================

-- ── order_notes ──
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'order_notes')
CREATE TABLE order_notes (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id    BIGINT       NOT NULL REFERENCES orders(id),
    staff_id    BIGINT       NOT NULL REFERENCES users(id),
    content     NVARCHAR(2000) NOT NULL,
    created_at  DATETIME2    DEFAULT SYSUTCDATETIME()
);

-- ── shipments ──
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'shipments')
CREATE TABLE shipments (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id      BIGINT        NOT NULL REFERENCES orders(id),
    carrier       NVARCHAR(100),
    tracking_code NVARCHAR(200),
    box_count     INT           DEFAULT 1,
    status        NVARCHAR(30)  DEFAULT 'CREATED',
    shipped_at    DATETIME2,
    delivered_at  DATETIME2,
    note          NVARCHAR(500),
    created_at    DATETIME2     DEFAULT SYSUTCDATETIME()
);

-- ── payment_logs ──
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'payment_logs')
CREATE TABLE payment_logs (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id        BIGINT         NOT NULL REFERENCES orders(id),
    provider        NVARCHAR(50)   DEFAULT 'PAYOS',
    transaction_id  NVARCHAR(200),
    amount          DECIMAL(15,2),
    status          NVARCHAR(30),
    flagged         BIT            DEFAULT 0,
    flag_reason     NVARCHAR(500),
    raw_data        NVARCHAR(MAX),
    created_at      DATETIME2      DEFAULT SYSUTCDATETIME()
);

-- ── incidents ──
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'incidents')
CREATE TABLE incidents (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    type          NVARCHAR(50)   NOT NULL,
    order_id      BIGINT         REFERENCES orders(id),
    copy_id       BIGINT         REFERENCES copies(id),
    reported_by   BIGINT         REFERENCES users(id),
    description   NVARCHAR(2000),
    status        NVARCHAR(30)   DEFAULT 'OPEN',
    created_at    DATETIME2      DEFAULT SYSUTCDATETIME()
);

-- ── pick_list_items ──
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'pick_list_items')
CREATE TABLE pick_list_items (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id       BIGINT        NOT NULL REFERENCES orders(id),
    order_item_id  BIGINT        NOT NULL REFERENCES order_items(id),
    copy_id        BIGINT        REFERENCES copies(id),
    location       NVARCHAR(50),
    status         NVARCHAR(30)  DEFAULT 'PENDING',
    picked_at      DATETIME2,
    picked_by      BIGINT        REFERENCES users(id),
    created_at     DATETIME2     DEFAULT SYSUTCDATETIME()
);

-- ── Add columns to orders (idempotent) ──
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='orders' AND COLUMN_NAME='confirmed_at')
    ALTER TABLE orders ADD confirmed_at DATETIME2;

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='orders' AND COLUMN_NAME='packed_at')
    ALTER TABLE orders ADD packed_at DATETIME2;

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='orders' AND COLUMN_NAME='delivery_status')
    ALTER TABLE orders ADD delivery_status NVARCHAR(30) DEFAULT 'NOT_SHIPPED';

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='orders' AND COLUMN_NAME='priority')
    ALTER TABLE orders ADD priority INT DEFAULT 0;

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='orders' AND COLUMN_NAME='sla_deadline')
    ALTER TABLE orders ADD sla_deadline DATETIME2;
