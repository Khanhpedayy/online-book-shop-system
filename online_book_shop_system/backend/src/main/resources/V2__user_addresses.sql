-- Customer saved shipping addresses (run manually if not using Flyway)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'user_addresses')
CREATE TABLE user_addresses (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id         BIGINT         NOT NULL REFERENCES users(id),
    label           NVARCHAR(80)   NULL,
    recipient_name  NVARCHAR(150)  NOT NULL,
    phone           NVARCHAR(30)   NULL,
    line1           NVARCHAR(255)  NOT NULL,
    line2           NVARCHAR(255)  NULL,
    city            NVARCHAR(100)  NULL,
    is_default      BIT            NOT NULL DEFAULT 0,
    created_at      DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME(),
    deleted_at      DATETIME2      NULL
);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_user_addresses_user_id' AND object_id = OBJECT_ID('user_addresses'))
    CREATE INDEX IX_user_addresses_user_id ON user_addresses(user_id) WHERE deleted_at IS NULL;
