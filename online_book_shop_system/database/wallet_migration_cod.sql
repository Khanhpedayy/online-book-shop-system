-- ============================================================
-- wallet_migration_cod.sql
-- COD Shipper Wallet — Bảng lịch sử nộp tiền
-- Chạy trên cùng DB, tách file để không xung đột migration cũ
-- ============================================================

-- Bảng theo dõi nộp tiền COD của shipper
IF NOT EXISTS (SELECT 1 FROM sysobjects WHERE name='cod_deposits' AND xtype='U')
BEGIN
    CREATE TABLE dbo.cod_deposits (
        id              BIGINT IDENTITY(1,1) PRIMARY KEY,
        deposit_code    VARCHAR(50)     NOT NULL,
        staff_id        BIGINT          NOT NULL,
        amount          DECIMAL(18,2)   NOT NULL CHECK (amount > 0),
        status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                        CONSTRAINT CHK_cod_dep_status CHECK (status IN ('PENDING','PAID','CANCELLED')),
        payos_order_code INT            NULL,        -- số int random gửi PayOS
        payos_link_id   VARCHAR(200)    NULL,        -- paymentLinkId từ PayOS response
        checkout_url    VARCHAR(1000)   NULL,        -- link QR checkout
        paid_at         DATETIME        NULL,
        created_at      DATETIME        NOT NULL DEFAULT GETDATE(),
        updated_at      DATETIME        NOT NULL DEFAULT GETDATE(),
        deleted_at      DATETIME        NULL,
        CONSTRAINT UQ_cod_deposits_code UNIQUE (deposit_code),
        CONSTRAINT FK_cod_dep_staff FOREIGN KEY (staff_id) REFERENCES dbo.users(id)
    );
    CREATE INDEX IX_cod_deposits_staff   ON dbo.cod_deposits(staff_id);
    CREATE INDEX IX_cod_deposits_payos   ON dbo.cod_deposits(payos_link_id) WHERE payos_link_id IS NOT NULL;
    CREATE INDEX IX_cod_deposits_status  ON dbo.cod_deposits(status);
    PRINT 'Created table cod_deposits';
END
ELSE
    PRINT 'Table cod_deposits already exists — skipped';

-- Đảm bảo users.wallet_balance tồn tại (nếu chưa chạy wallet_migration.sql)
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.users') AND name = 'wallet_balance'
)
BEGIN
    ALTER TABLE dbo.users
        ADD wallet_balance DECIMAL(18,2) NOT NULL DEFAULT 0
            CONSTRAINT CHK_users_wallet_balance CHECK (wallet_balance >= 0);
    PRINT 'Added wallet_balance column to users';
END
ELSE
    PRINT 'Column wallet_balance already exists — skipped';
