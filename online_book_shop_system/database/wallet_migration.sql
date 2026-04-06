/* =========================================================
   WALLET MIGRATION — Virtual Wallet Feature
   SQL Server | Chạy SAU databasebansach.sql
   Ngày tạo: 2026-04-06
   =========================================================

   Thay đổi:
   1. ALTER TABLE users   → thêm wallet_balance
   2. ALTER TABLE orders  → mở rộng CHECK status thêm DELIVERY_FAILED
   3. CREATE TABLE wallet_transactions
   4. CREATE TABLE withdrawal_requests
   ========================================================= */

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;


/* ===================================================
   1. Thêm wallet_balance vào bảng users
   =================================================== */

ALTER TABLE dbo.users
ADD wallet_balance DECIMAL(18,2) NOT NULL DEFAULT 0;

-- Đảm bảo số dư không âm
ALTER TABLE dbo.users
ADD CONSTRAINT CK_users_wallet_balance CHECK (wallet_balance >= 0);


/* ===================================================
   2. Mở rộng trạng thái orders thêm DELIVERY_FAILED
   (SQL Server không ALTER CHECK constraint trực tiếp → drop rồi add lại)
   =================================================== */

ALTER TABLE dbo.orders
DROP CONSTRAINT CK_orders_status;

ALTER TABLE dbo.orders
ADD CONSTRAINT CK_orders_status CHECK (
    status IN (
        'NEW',
        'CONFIRMED',
        'PACKED',
        'SHIPPED',
        'DELIVERED',
        'COMPLETED',
        'CANCELLED',
        'DELIVERY_FAILED'   -- trạng thái mới: giao hàng thất bại
    )
);


/* ===================================================
   3. Bảng wallet_transactions — Lịch sử giao dịch ví
   =================================================== */

CREATE TABLE dbo.wallet_transactions (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id         BIGINT          NOT NULL,

    -- CREDIT = tiền vào ví, DEBIT = tiền ra (khi tạo withdrawal)
    type            VARCHAR(10)     NOT NULL,
    amount          DECIMAL(18,2)   NOT NULL,
    balance_after   DECIMAL(18,2)   NOT NULL,   -- snapshot số dư sau giao dịch

    -- nguồn gốc
    ref_type        VARCHAR(30)     NULL,        -- ORDER_REFUND | WITHDRAWAL
    ref_id          BIGINT          NULL,        -- order_id hoặc withdrawal_request_id

    note            NVARCHAR(300)   NULL,

    created_at      DATETIME2(0)    NOT NULL DEFAULT SYSUTCDATETIME(),
    created_by      BIGINT          NULL,        -- NULL = system auto
    row_version     ROWVERSION,

    CONSTRAINT FK_wt_user       FOREIGN KEY (user_id) REFERENCES dbo.users(id),
    CONSTRAINT CK_wt_type       CHECK (type IN ('CREDIT', 'DEBIT')),
    CONSTRAINT CK_wt_amount     CHECK (amount > 0),
    CONSTRAINT CK_wt_balance    CHECK (balance_after >= 0)
);

CREATE INDEX IX_wt_user   ON dbo.wallet_transactions(user_id, created_at DESC);
CREATE INDEX IX_wt_ref    ON dbo.wallet_transactions(ref_type, ref_id);


/* ===================================================
   4. Bảng withdrawal_requests — Yêu cầu rút tiền
   =================================================== */

CREATE TABLE dbo.withdrawal_requests (
    id                   BIGINT IDENTITY(1,1) PRIMARY KEY,
    request_code         VARCHAR(60)     NOT NULL,  -- WD-YYYYMMDD-XXXXXX

    user_id              BIGINT          NOT NULL,
    amount               DECIMAL(18,2)   NOT NULL,

    -- thông tin ngân hàng (snapshot lúc tạo yêu cầu)
    bank_name            NVARCHAR(100)   NOT NULL,
    bank_account_number  VARCHAR(50)     NOT NULL,
    bank_account_name    NVARCHAR(150)   NOT NULL,

    -- trạng thái
    status               VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    -- PENDING  → chờ admin duyệt
    -- APPROVED → admin đã duyệt + chuyển khoản
    -- REJECTED → admin từ chối

    admin_note           NVARCHAR(500)   NULL,   -- lý do từ chối hoặc ghi chú khi duyệt
    processed_by         BIGINT          NULL,   -- admin user_id
    processed_at         DATETIME2(0)    NULL,

    -- link tới wallet_transaction DEBIT (trừ tiền ví khi tạo request)
    wallet_tx_id         BIGINT          NULL,

    created_at           DATETIME2(0)    NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at           DATETIME2(0)    NULL,
    deleted_at           DATETIME2(0)    NULL,
    row_version          ROWVERSION,

    CONSTRAINT FK_wr_user       FOREIGN KEY (user_id)       REFERENCES dbo.users(id),
    CONSTRAINT FK_wr_admin      FOREIGN KEY (processed_by)  REFERENCES dbo.users(id),
    CONSTRAINT FK_wr_wallet_tx  FOREIGN KEY (wallet_tx_id)  REFERENCES dbo.wallet_transactions(id),
    CONSTRAINT CK_wr_status     CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT CK_wr_amount     CHECK (amount > 0)
);

CREATE UNIQUE INDEX UX_wr_code     ON dbo.withdrawal_requests(request_code) WHERE deleted_at IS NULL;
CREATE INDEX IX_wr_user            ON dbo.withdrawal_requests(user_id, created_at DESC);
CREATE INDEX IX_wr_status          ON dbo.withdrawal_requests(status, created_at);

-- Chặn tạo nhiều hơn 1 PENDING withdrawal cùng lúc cho 1 user
CREATE UNIQUE INDEX UX_wr_user_pending
    ON dbo.withdrawal_requests(user_id)
    WHERE status = 'PENDING' AND deleted_at IS NULL;


/* ===================================================
   END WALLET MIGRATION
   =================================================== */
