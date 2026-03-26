/* =========================================================
   BOOKSTORE DATABASE — COMPACT SCHEMA (24 tables)
   SQL Server | Soft-delete + Audit columns
   Sections: 1)AUTH  2)CATALOG  3)INVENTORY
             4)SALES 5)ENGAGEMENT 6)SYSTEM
   ========================================================= */

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;


/* ===================== 1) AUTH ===================== */

CREATE TABLE roles (
  id                INT IDENTITY(1,1) PRIMARY KEY,
  code              VARCHAR(20)    NOT NULL,
  name              NVARCHAR(50)   NOT NULL,
  description       NVARCHAR(200)  NULL,
  permissions_json  NVARCHAR(MAX)  NULL,  -- ["catalog.view","catalog.create","order.update",...]

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  updated_at  DATETIME2(0) NULL,
  row_version ROWVERSION,

  CONSTRAINT UQ_roles_code UNIQUE (code),
  CONSTRAINT CK_roles_code CHECK (code IN ('ADMIN','MANAGER','STAFF','CUSTOMER'))
);

CREATE TABLE users (
  id            BIGINT IDENTITY(1,1) PRIMARY KEY,
  role_id       INT           NOT NULL,
  email         VARCHAR(255)  NOT NULL,
  password_hash VARCHAR(255)  NULL,
  full_name     NVARCHAR(150) NOT NULL,
  phone         VARCHAR(30)   NULL,
  avatar_url    NVARCHAR(500) NULL,
  status        VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
  last_login_at DATETIME2(0)  NULL,

  created_at    DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  created_by    BIGINT NULL,
  updated_at    DATETIME2(0) NULL,
  updated_by    BIGINT NULL,
  deleted_at    DATETIME2(0) NULL,
  deleted_by    BIGINT NULL,
  row_version   ROWVERSION,

  CONSTRAINT FK_users_role   FOREIGN KEY (role_id) REFERENCES roles(id),
  CONSTRAINT CK_users_status CHECK (status IN ('ACTIVE','DISABLED'))
);
CREATE UNIQUE INDEX UX_users_email ON users(email) WHERE deleted_at IS NULL;
CREATE INDEX IX_users_role        ON users(role_id);

CREATE TABLE user_addresses (
  id             BIGINT IDENTITY(1,1) PRIMARY KEY,
  user_id        BIGINT NOT NULL,
  recipient_name NVARCHAR(150) NOT NULL,
  phone          VARCHAR(30)   NOT NULL,
  line1          NVARCHAR(255) NOT NULL,
  line2          NVARCHAR(255) NULL,
  ward           NVARCHAR(120) NULL,
  district       NVARCHAR(120) NULL,
  city           NVARCHAR(120) NULL,
  province       NVARCHAR(120) NULL,
  postal_code    VARCHAR(20)   NULL,
  country        NVARCHAR(80)  NOT NULL DEFAULT N'Vietnam',
  is_default     BIT NOT NULL DEFAULT 0,

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  updated_at  DATETIME2(0) NULL,
  deleted_at  DATETIME2(0) NULL,
  row_version ROWVERSION,

  CONSTRAINT FK_addr_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX IX_addr_user ON user_addresses(user_id);


/* ===================== 2) CATALOG ===================== */

CREATE TABLE categories (
  id          BIGINT IDENTITY(1,1) PRIMARY KEY,
  parent_id   BIGINT NULL,
  name        NVARCHAR(120) NOT NULL,
  slug        VARCHAR(160)  NOT NULL,
  description NVARCHAR(500) NULL,
  icon_url    NVARCHAR(500) NULL,
  sort_order  INT NOT NULL DEFAULT 0,
  is_active   BIT NOT NULL DEFAULT 1,

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  updated_at  DATETIME2(0) NULL,
  deleted_at  DATETIME2(0) NULL,
  row_version ROWVERSION,

  CONSTRAINT FK_cat_parent FOREIGN KEY (parent_id) REFERENCES categories(id)
);
CREATE UNIQUE INDEX UX_cat_slug ON categories(slug) WHERE deleted_at IS NULL;
CREATE INDEX IX_cat_parent      ON categories(parent_id);

CREATE TABLE authors (
  id         BIGINT IDENTITY(1,1) PRIMARY KEY,
  name       NVARCHAR(200)  NOT NULL,
  slug       VARCHAR(220)   NOT NULL,
  bio        NVARCHAR(2000) NULL,
  avatar_url NVARCHAR(500)  NULL,

  created_at DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  updated_at DATETIME2(0) NULL,
  deleted_at DATETIME2(0) NULL,
  row_version ROWVERSION
);
CREATE UNIQUE INDEX UX_authors_slug ON authors(slug) WHERE deleted_at IS NULL;

CREATE TABLE books (
  id                BIGINT IDENTITY(1,1) PRIMARY KEY,
  category_id       BIGINT NULL,

  isbn13            VARCHAR(13)    NULL,
  isbn10            VARCHAR(10)    NULL,
  title             NVARCHAR(255)  NOT NULL,
  subtitle          NVARCHAR(255)  NULL,
  slug              VARCHAR(260)   NOT NULL,

  publisher_name    NVARCHAR(200)  NULL,
  publication_year  INT NULL,
  language          VARCHAR(30) NULL,

  short_description NVARCHAR(500)  NULL,
  description_html  NVARCHAR(MAX)  NULL,
  tags_json         NVARCHAR(MAX)  NULL,  -- ["fiction","bestseller","2024"]

  sell_mode         VARCHAR(20) NOT NULL DEFAULT 'QUANTITY',  -- QUANTITY | PER_COPY
  status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  created_by  BIGINT NULL,
  updated_at  DATETIME2(0) NULL,
  updated_by  BIGINT NULL,
  deleted_at  DATETIME2(0) NULL,
  deleted_by  BIGINT NULL,
  row_version ROWVERSION,

  CONSTRAINT FK_books_cat     FOREIGN KEY (category_id) REFERENCES categories(id),
  CONSTRAINT CK_books_mode    CHECK (sell_mode IN ('QUANTITY','PER_COPY')),
  CONSTRAINT CK_books_status  CHECK (status IN ('ACTIVE','HIDDEN','DRAFT'))
);
CREATE UNIQUE INDEX UX_books_slug   ON books(slug)   WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX UX_books_isbn13 ON books(isbn13) WHERE isbn13 IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX IX_books_cat           ON books(category_id);
CREATE INDEX IX_books_status        ON books(status) WHERE deleted_at IS NULL;

CREATE TABLE book_authors (
  book_id    BIGINT NOT NULL,
  author_id  BIGINT NOT NULL,
  role       VARCHAR(20) NOT NULL DEFAULT 'AUTHOR',
  sort_order INT NOT NULL DEFAULT 0,
  PRIMARY KEY (book_id, author_id, role),
  CONSTRAINT FK_ba_book   FOREIGN KEY (book_id) REFERENCES books(id),
  CONSTRAINT FK_ba_author FOREIGN KEY (author_id) REFERENCES authors(id),
  CONSTRAINT CK_ba_role   CHECK (role IN ('AUTHOR','TRANSLATOR','ILLUSTRATOR','EDITOR'))
);

CREATE TABLE book_variants (
  id           BIGINT IDENTITY(1,1) PRIMARY KEY,
  book_id      BIGINT NOT NULL,
  sku          VARCHAR(80)   NOT NULL,
  format       VARCHAR(20)   NULL,           -- HARDCOVER, PAPERBACK, BOXSET
  edition      NVARCHAR(80)  NULL,
  language     VARCHAR(30)   NULL,

  list_price   DECIMAL(18,2) NOT NULL DEFAULT 0,
  sale_price   DECIMAL(18,2) NOT NULL DEFAULT 0,
  condition_prices_json NVARCHAR(MAX) NULL,  -- {"LIKE_NEW":{"pct":10},"GOOD":{"pct":20},"FAIR":{"pct":35}}

  page_count   INT NULL,
  weight_grams INT NULL,
  width_mm     INT NULL,
  height_mm    INT NULL,
  thickness_mm INT NULL,

  is_active    BIT NOT NULL DEFAULT 1,

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  created_by  BIGINT NULL,
  updated_at  DATETIME2(0) NULL,
  updated_by  BIGINT NULL,
  deleted_at  DATETIME2(0) NULL,
  deleted_by  BIGINT NULL,
  row_version ROWVERSION,

  CONSTRAINT FK_bv_book   FOREIGN KEY (book_id) REFERENCES books(id),
  CONSTRAINT CK_bv_format CHECK (format IS NULL OR format IN ('HARDCOVER','PAPERBACK','BOXSET'))
);
CREATE UNIQUE INDEX UX_bv_sku ON book_variants(sku) WHERE deleted_at IS NULL;
CREATE INDEX IX_bv_book       ON book_variants(book_id);

CREATE TABLE book_images (
  id         BIGINT IDENTITY(1,1) PRIMARY KEY,
  book_id    BIGINT NOT NULL,
  variant_id BIGINT NULL,
  url        NVARCHAR(500) NOT NULL,
  alt_text   NVARCHAR(200) NULL,
  is_cover   BIT NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,

  created_at DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  deleted_at DATETIME2(0) NULL,
  row_version ROWVERSION,

  CONSTRAINT FK_bi_book    FOREIGN KEY (book_id) REFERENCES books(id),
  CONSTRAINT FK_bi_variant FOREIGN KEY (variant_id) REFERENCES book_variants(id)
);
CREATE INDEX IX_bi_book ON book_images(book_id);


/* ===================== 3) INVENTORY ===================== */

CREATE TABLE suppliers (
  id             BIGINT IDENTITY(1,1) PRIMARY KEY,
  name           NVARCHAR(200) NOT NULL,
  code           VARCHAR(50)   NULL,
  email          VARCHAR(255)  NULL,
  phone          VARCHAR(30)   NULL,
  address        NVARCHAR(300) NULL,
  contact_person NVARCHAR(150) NULL,
  is_active      BIT NOT NULL DEFAULT 1,

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  updated_at  DATETIME2(0) NULL,
  deleted_at  DATETIME2(0) NULL,
  row_version ROWVERSION
);
CREATE UNIQUE INDEX UX_sup_name ON suppliers(name) WHERE deleted_at IS NULL;

CREATE TABLE lots (
  id                BIGINT IDENTITY(1,1) PRIMARY KEY,
  lot_code          VARCHAR(60)  NOT NULL,

  supplier_id       BIGINT NOT NULL,
  variant_id        BIGINT NOT NULL,

  -- goods receipt info (gộp)
  receipt_code      VARCHAR(60)   NULL,
  invoice_no        VARCHAR(80)   NULL,
  warehouse         NVARCHAR(100) NOT NULL DEFAULT N'MAIN',
  received_at       DATETIME2(0)  NOT NULL,

  unit_cost         DECIMAL(18,2) NOT NULL,
  qty_received      INT NOT NULL,
  qty_available     INT NOT NULL DEFAULT 0,
  qty_reserved      INT NOT NULL DEFAULT 0,
  qty_sold          INT NOT NULL DEFAULT 0,
  qty_damaged       INT NOT NULL DEFAULT 0,
  qty_returned      INT NOT NULL DEFAULT 0,

  condition_default VARCHAR(20) NOT NULL DEFAULT 'NEW',
  status            VARCHAR(20) NOT NULL DEFAULT 'RELEASED',
  note              NVARCHAR(500) NULL,

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  created_by  BIGINT NULL,
  updated_at  DATETIME2(0) NULL,
  deleted_at  DATETIME2(0) NULL,
  row_version ROWVERSION,

  CONSTRAINT FK_lots_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
  CONSTRAINT FK_lots_variant  FOREIGN KEY (variant_id) REFERENCES book_variants(id),
  CONSTRAINT CK_lots_qty      CHECK (qty_received > 0),
  CONSTRAINT CK_lots_cond     CHECK (condition_default IN ('NEW','LIKE_NEW','GOOD','FAIR')),
  CONSTRAINT CK_lots_status   CHECK (status IN ('RELEASED','QUARANTINED','LOCKED'))
);
CREATE UNIQUE INDEX UX_lots_code ON lots(lot_code) WHERE deleted_at IS NULL;
CREATE INDEX IX_lots_variant    ON lots(variant_id);

CREATE TABLE copies (
  id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
  copy_code           VARCHAR(80) NOT NULL,
  lot_id              BIGINT NOT NULL,
  variant_id          BIGINT NOT NULL,

  location            VARCHAR(80) NOT NULL DEFAULT 'A1-01',  -- vị trí kệ (text thay vì FK)
  condition_grade     VARCHAR(20) NOT NULL DEFAULT 'NEW',
  condition_note      NVARCHAR(300) NULL,

  has_signature       BIT NOT NULL DEFAULT 0,
  is_first_edition    BIT NOT NULL DEFAULT 0,
  attributes_json     NVARCHAR(MAX) NULL,
  images_json         NVARCHAR(MAX) NULL,

  sell_price_override DECIMAL(18,2) NULL,

  status              VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
  reserved_at         DATETIME2(0) NULL,
  reserve_expires_at  DATETIME2(0) NULL,

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  created_by  BIGINT NULL,
  updated_at  DATETIME2(0) NULL,
  deleted_at  DATETIME2(0) NULL,
  row_version ROWVERSION,

  CONSTRAINT FK_copies_lot FOREIGN KEY (lot_id) REFERENCES lots(id),
  CONSTRAINT FK_copies_var FOREIGN KEY (variant_id) REFERENCES book_variants(id),
  CONSTRAINT CK_copies_cond   CHECK (condition_grade IN ('NEW','LIKE_NEW','GOOD','FAIR')),
  CONSTRAINT CK_copies_status CHECK (status IN ('AVAILABLE','RESERVED','PICKED','PACKED','SHIPPED','SOLD','RETURNED','DAMAGED','LOST'))
);
CREATE UNIQUE INDEX UX_copies_code    ON copies(copy_code) WHERE deleted_at IS NULL;
CREATE INDEX IX_copies_lot            ON copies(lot_id);
CREATE INDEX IX_copies_var_status     ON copies(variant_id, status);
CREATE INDEX IX_copies_reserve_exp    ON copies(reserve_expires_at) WHERE status = 'RESERVED';

CREATE TABLE inventory_transactions (
  id             BIGINT IDENTITY(1,1) PRIMARY KEY,
  movement_type  VARCHAR(20) NOT NULL,  -- IN, OUT, ADJUST, RETURN, RESERVE, RELEASE, TRANSFER
  variant_id     BIGINT NULL,
  lot_id         BIGINT NULL,
  copy_id        BIGINT NULL,
  quantity       INT NOT NULL DEFAULT 1,

  from_location  VARCHAR(80) NULL,
  to_location    VARCHAR(80) NULL,

  reference_type VARCHAR(30) NULL,  -- ORDER, RECEIPT, RETURN, ADJUSTMENT
  reference_id   BIGINT NULL,
  reason         VARCHAR(30) NULL,  -- SALE, DAMAGED, LOST, FOUND, COUNT_DIFF, TRANSFER
  note           NVARCHAR(300) NULL,

  created_at     DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  created_by     BIGINT NULL,
  row_version    ROWVERSION,

  CONSTRAINT CK_it_type CHECK (movement_type IN ('IN','OUT','ADJUST','RETURN','RESERVE','RELEASE','TRANSFER')),
  CONSTRAINT CK_it_qty  CHECK (quantity > 0),
  CONSTRAINT FK_it_var   FOREIGN KEY (variant_id) REFERENCES book_variants(id),
  CONSTRAINT FK_it_lot   FOREIGN KEY (lot_id) REFERENCES lots(id),
  CONSTRAINT FK_it_copy  FOREIGN KEY (copy_id) REFERENCES copies(id)
);
CREATE INDEX IX_it_created ON inventory_transactions(created_at);
CREATE INDEX IX_it_ref     ON inventory_transactions(reference_type, reference_id);


/* ===================== 4) SALES ===================== */

CREATE TABLE vouchers (
  id              BIGINT IDENTITY(1,1) PRIMARY KEY,
  code            VARCHAR(50)   NOT NULL,
  name            NVARCHAR(150) NOT NULL,
  discount_type   VARCHAR(10)   NOT NULL,     -- PERCENT, FIXED
  discount_value  DECIMAL(18,2) NOT NULL,
  min_order_value DECIMAL(18,2) NULL,
  max_discount    DECIMAL(18,2) NULL,
  usage_limit     INT NULL,
  used_count      INT NOT NULL DEFAULT 0,
  per_user_limit  INT NOT NULL DEFAULT 1,
  starts_at       DATETIME2(0) NOT NULL,
  expires_at      DATETIME2(0) NULL,
  is_active       BIT NOT NULL DEFAULT 1,

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  created_by  BIGINT NULL,
  updated_at  DATETIME2(0) NULL,
  deleted_at  DATETIME2(0) NULL,
  row_version ROWVERSION,

  CONSTRAINT CK_voucher_type CHECK (discount_type IN ('PERCENT','FIXED'))
);
CREATE UNIQUE INDEX UX_voucher_code ON vouchers(code) WHERE deleted_at IS NULL;

CREATE TABLE orders (
  id              BIGINT IDENTITY(1,1) PRIMARY KEY,
  order_code      VARCHAR(60) NOT NULL,
  user_id         BIGINT NOT NULL,
  voucher_id      BIGINT NULL,

  status          VARCHAR(20) NOT NULL DEFAULT 'NEW',
  payment_status  VARCHAR(20) NOT NULL DEFAULT 'PENDING',

  currency        VARCHAR(10)   NOT NULL DEFAULT 'VND',
  subtotal_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
  shipping_fee    DECIMAL(18,2) NOT NULL DEFAULT 0,
  discount_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
  total_amount    DECIMAL(18,2) NOT NULL DEFAULT 0,

  -- shipping address (inline snapshot)
  ship_name       NVARCHAR(150) NOT NULL,
  ship_phone      VARCHAR(30)   NOT NULL,
  ship_line1      NVARCHAR(255) NOT NULL,
  ship_line2      NVARCHAR(255) NULL,
  ship_ward       NVARCHAR(120) NULL,
  ship_district   NVARCHAR(120) NULL,
  ship_city       NVARCHAR(120) NULL,
  ship_province   NVARCHAR(120) NULL,
  ship_method     VARCHAR(20)   NULL,   -- STANDARD, EXPRESS

  -- shipment info (inline)
  carrier         NVARCHAR(100) NULL,
  tracking_code   VARCHAR(120)  NULL,

  customer_note   NVARCHAR(300) NULL,
  staff_note      NVARCHAR(500) NULL,
  cancel_reason   NVARCHAR(300) NULL,

  -- timeline timestamps
  placed_at       DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  confirmed_at    DATETIME2(0) NULL,
  packed_at       DATETIME2(0) NULL,
  shipped_at      DATETIME2(0) NULL,
  delivered_at    DATETIME2(0) NULL,
  completed_at    DATETIME2(0) NULL,
  cancelled_at    DATETIME2(0) NULL,

  -- who did what
  confirmed_by    BIGINT NULL,
  packed_by       BIGINT NULL,
  shipped_by      BIGINT NULL,
  cancelled_by    BIGINT NULL,

  created_at      DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  created_by      BIGINT NULL,
  updated_at      DATETIME2(0) NULL,
  updated_by      BIGINT NULL,
  deleted_at      DATETIME2(0) NULL,
  deleted_by      BIGINT NULL,
  row_version     ROWVERSION,

  CONSTRAINT FK_orders_user    FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT FK_orders_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id),
  CONSTRAINT CK_orders_status  CHECK (status IN ('NEW','CONFIRMED','PACKED','SHIPPED','DELIVERED','COMPLETED','CANCELLED','DELIVERY_FAILED')),
  CONSTRAINT CK_orders_pay    CHECK (payment_status IN ('PENDING','PAID','FAILED','REFUNDED'))
);
CREATE UNIQUE INDEX UX_orders_code ON orders(order_code) WHERE deleted_at IS NULL;
CREATE INDEX IX_orders_user        ON orders(user_id);
CREATE INDEX IX_orders_status      ON orders(status, payment_status, placed_at);

CREATE TABLE order_items (
  id                 BIGINT IDENTITY(1,1) PRIMARY KEY,
  order_id           BIGINT NOT NULL,
  variant_id         BIGINT NOT NULL,
  copy_id            BIGINT NULL,           -- gán khi allocate (NULL = chưa allocate)

  title_snapshot     NVARCHAR(255) NOT NULL,
  sku_snapshot       VARCHAR(80)   NOT NULL,
  condition_snapshot VARCHAR(20)   NULL,

  unit_price         DECIMAL(18,2) NOT NULL,
  quantity           INT NOT NULL DEFAULT 1,
  line_total         AS (unit_price * quantity) PERSISTED,

  pick_method        VARCHAR(10) NULL,      -- AUTO, MANUAL, SCAN
  picked_by          BIGINT NULL,
  picked_at          DATETIME2(0) NULL,

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  updated_at  DATETIME2(0) NULL,
  deleted_at  DATETIME2(0) NULL,
  row_version ROWVERSION,

  CONSTRAINT FK_oi_order   FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT FK_oi_variant FOREIGN KEY (variant_id) REFERENCES book_variants(id),
  CONSTRAINT FK_oi_copy    FOREIGN KEY (copy_id) REFERENCES copies(id),
  CONSTRAINT CK_oi_qty     CHECK (quantity >= 1)
);
CREATE INDEX IX_oi_order ON order_items(order_id);
CREATE UNIQUE INDEX UX_oi_copy ON order_items(copy_id) WHERE copy_id IS NOT NULL AND deleted_at IS NULL;

CREATE TABLE payments (
  id                      BIGINT IDENTITY(1,1) PRIMARY KEY,
  order_id                BIGINT NOT NULL,
  provider                VARCHAR(20)   NOT NULL DEFAULT 'PAYOS',
  provider_transaction_id VARCHAR(120)  NULL,
  pay_url                 NVARCHAR(500) NULL,
  amount                  DECIMAL(18,2) NOT NULL,
  currency                VARCHAR(10)   NOT NULL DEFAULT 'VND',
  status                  VARCHAR(20)   NOT NULL DEFAULT 'CREATED',
  paid_at                 DATETIME2(0)  NULL,
  expired_at              DATETIME2(0)  NULL,
  events_json             NVARCHAR(MAX) NULL,  -- [{type,payload,at,idempotencyKey}]

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  created_by  BIGINT NULL,
  updated_at  DATETIME2(0) NULL,
  row_version ROWVERSION,

  CONSTRAINT FK_pay_order  FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT CK_pay_status CHECK (status IN ('CREATED','PENDING','SUCCEEDED','FAILED','CANCELLED','REFUNDED','EXPIRED'))
);
CREATE INDEX IX_pay_order ON payments(order_id);

CREATE TABLE returns (
  id           BIGINT IDENTITY(1,1) PRIMARY KEY,
  return_code  VARCHAR(60) NOT NULL,
  order_id     BIGINT NOT NULL,
  status       VARCHAR(20)   NOT NULL DEFAULT 'REQUESTED',
  reason       NVARCHAR(300) NULL,
  note         NVARCHAR(500) NULL,
  refund_amount DECIMAL(18,2) NULL,

  requested_by BIGINT NULL,
  approved_by  BIGINT NULL,
  approved_at  DATETIME2(0) NULL,
  received_at  DATETIME2(0) NULL,

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  created_by  BIGINT NULL,
  updated_at  DATETIME2(0) NULL,
  deleted_at  DATETIME2(0) NULL,
  row_version ROWVERSION,

  CONSTRAINT FK_ret_order  FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT CK_ret_status CHECK (status IN ('REQUESTED','APPROVED','REJECTED','RECEIVED','REFUNDED','CLOSED'))
);
CREATE UNIQUE INDEX UX_ret_code ON returns(return_code) WHERE deleted_at IS NULL;
CREATE INDEX IX_ret_order       ON returns(order_id);

CREATE TABLE return_items (
  id                       BIGINT IDENTITY(1,1) PRIMARY KEY,
  return_id                BIGINT NOT NULL,
  order_item_id            BIGINT NOT NULL,
  copy_id                  BIGINT NULL,
  quantity                 INT NOT NULL DEFAULT 1,
  received_condition_grade VARCHAR(20)   NULL,
  received_condition_note  NVARCHAR(300) NULL,
  action                   VARCHAR(20)   NULL,
  processed_by             BIGINT NULL,
  processed_at             DATETIME2(0)  NULL,

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  row_version ROWVERSION,

  CONSTRAINT FK_ri_return FOREIGN KEY (return_id) REFERENCES returns(id),
  CONSTRAINT FK_ri_oi     FOREIGN KEY (order_item_id) REFERENCES order_items(id),
  CONSTRAINT FK_ri_copy   FOREIGN KEY (copy_id) REFERENCES copies(id),
  CONSTRAINT CK_ri_action CHECK (action IS NULL OR action IN ('RESTOCK','RESTOCK_REPRICE','DAMAGED','SUPPLIER_RETURN'))
);
CREATE INDEX IX_ri_return ON return_items(return_id);


/* ===================== 5) ENGAGEMENT ===================== */

CREATE TABLE wishlist_items (
  id         BIGINT IDENTITY(1,1) PRIMARY KEY,
  user_id    BIGINT NOT NULL,
  variant_id BIGINT NOT NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),

  CONSTRAINT FK_wl_user    FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT FK_wl_variant FOREIGN KEY (variant_id) REFERENCES book_variants(id),
  CONSTRAINT UQ_wl_unique  UNIQUE (user_id, variant_id)
);

CREATE TABLE reviews (
  id         BIGINT IDENTITY(1,1) PRIMARY KEY,
  user_id    BIGINT NOT NULL,
  book_id    BIGINT NOT NULL,
  order_id   BIGINT NULL,
  rating     INT NOT NULL,
  title      NVARCHAR(120)  NULL,
  content    NVARCHAR(2000) NULL,
  status     VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',

  created_at DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  updated_at DATETIME2(0) NULL,
  deleted_at DATETIME2(0) NULL,
  row_version ROWVERSION,

  CONSTRAINT FK_rev_user  FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT FK_rev_book  FOREIGN KEY (book_id) REFERENCES books(id),
  CONSTRAINT FK_rev_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT CK_rev_rating CHECK (rating BETWEEN 1 AND 5),
  CONSTRAINT CK_rev_status CHECK (status IN ('PUBLISHED','HIDDEN','REPORTED'))
);
CREATE INDEX IX_rev_book ON reviews(book_id);
CREATE UNIQUE INDEX UX_rev_user_book ON reviews(user_id, book_id) WHERE deleted_at IS NULL;

CREATE TABLE support_tickets (
  id            BIGINT IDENTITY(1,1) PRIMARY KEY,
  ticket_code   VARCHAR(60)    NOT NULL,
  user_id       BIGINT NOT NULL,
  order_id      BIGINT NULL,
  assigned_to   BIGINT NULL,
  category      VARCHAR(30)    NOT NULL,
  priority      VARCHAR(10)    NOT NULL DEFAULT 'NORMAL',
  status        VARCHAR(20)    NOT NULL DEFAULT 'OPEN',
  subject       NVARCHAR(200)  NOT NULL,
  messages_json NVARCHAR(MAX)  NOT NULL DEFAULT '[]', -- [{from,message,at,isInternal,attachments}]

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  updated_at  DATETIME2(0) NULL,
  closed_at   DATETIME2(0) NULL,
  deleted_at  DATETIME2(0) NULL,
  row_version ROWVERSION,

  CONSTRAINT FK_tk_user     FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT FK_tk_order    FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT FK_tk_assigned FOREIGN KEY (assigned_to) REFERENCES users(id),
  CONSTRAINT CK_tk_cat      CHECK (category IN ('SHIPPING','PAYMENT','RETURN','PRODUCT','OTHER')),
  CONSTRAINT CK_tk_priority CHECK (priority IN ('LOW','NORMAL','HIGH','URGENT')),
  CONSTRAINT CK_tk_status   CHECK (status IN ('OPEN','IN_PROGRESS','WAITING','RESOLVED','CLOSED'))
);
CREATE UNIQUE INDEX UX_tk_code ON support_tickets(ticket_code) WHERE deleted_at IS NULL;
CREATE INDEX IX_tk_user        ON support_tickets(user_id);


/* ===================== 6) SYSTEM & AUDIT ===================== */

CREATE TABLE settings (
  id          BIGINT IDENTITY(1,1) PRIMARY KEY,
  [group]     VARCHAR(50)    NOT NULL DEFAULT 'GENERAL',
  [key]       VARCHAR(120)   NOT NULL,
  value_json  NVARCHAR(MAX)  NOT NULL,
  description NVARCHAR(300)  NULL,

  created_at  DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  updated_at  DATETIME2(0) NULL,
  updated_by  BIGINT NULL,
  row_version ROWVERSION,

  CONSTRAINT UQ_settings UNIQUE ([group], [key])
);

CREATE TABLE audit_logs (
  id            BIGINT IDENTITY(1,1) PRIMARY KEY,
  actor_user_id BIGINT NULL,
  action        VARCHAR(50)   NOT NULL,
  entity_table  VARCHAR(80)   NOT NULL,
  entity_id     BIGINT NULL,
  changes_json  NVARCHAR(MAX) NULL,
  note          NVARCHAR(300) NULL,
  ip_address    VARCHAR(60)   NULL,
  user_agent    NVARCHAR(300) NULL,
  request_id    VARCHAR(80)   NULL,

  created_at    DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  row_version   ROWVERSION,

  CONSTRAINT FK_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users(id)
);
CREATE INDEX IX_audit_created ON audit_logs(created_at);
CREATE INDEX IX_audit_entity  ON audit_logs(entity_table, entity_id);


/* ===================== ENGAGEMENT (tiếp) ===================== */

CREATE TABLE review_reports (
  id           BIGINT IDENTITY(1,1) PRIMARY KEY,
  review_id    BIGINT NOT NULL,
  reporter_id  BIGINT NOT NULL,
  reason       NVARCHAR(500) NOT NULL,
  status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',   -- PENDING | APPROVED | REJECTED
  admin_note   NVARCHAR(500) NULL,
  reviewed_by  BIGINT NULL,
  reviewed_at  DATETIME2(0) NULL,
  created_at   DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  updated_at   DATETIME2(0) NULL,
  deleted_at   DATETIME2(0) NULL,
  row_version  ROWVERSION,

  CONSTRAINT FK_rr_review   FOREIGN KEY (review_id)   REFERENCES reviews(id),
  CONSTRAINT FK_rr_reporter FOREIGN KEY (reporter_id)  REFERENCES users(id),
  CONSTRAINT FK_rr_admin    FOREIGN KEY (reviewed_by)  REFERENCES users(id),
  CONSTRAINT CK_rr_status   CHECK (status IN ('PENDING','APPROVED','REJECTED'))
);
CREATE INDEX IX_rr_review   ON review_reports(review_id);
CREATE INDEX IX_rr_reporter ON review_reports(reporter_id);
CREATE INDEX IX_rr_status   ON review_reports(status) WHERE deleted_at IS NULL;

CREATE TABLE notifications (
  id         BIGINT IDENTITY(1,1) PRIMARY KEY,
  user_id    BIGINT NOT NULL,
  title      NVARCHAR(200)  NOT NULL,
  body       NVARCHAR(1000) NOT NULL,
  type       VARCHAR(40)    NOT NULL DEFAULT 'GENERAL',
  is_read    BIT NOT NULL DEFAULT 0,
  ref_id     BIGINT NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
  deleted_at DATETIME2(0) NULL,
  row_version ROWVERSION,

  CONSTRAINT FK_notif_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX IX_notif_user    ON notifications(user_id);
CREATE INDEX IX_notif_unread  ON notifications(user_id, is_read) WHERE deleted_at IS NULL;

/* ===================== SEED DATA ===================== */

INSERT INTO roles (code, name, description, permissions_json) VALUES
  ('ADMIN',    N'Administrator',  N'Quản trị hệ thống',
   '["catalog.*","inventory.*","order.*","payment.*","report.*","user.*","system.*"]'),
  ('MANAGER',  N'Manager',        N'Quản lý kho & catalog',
   '["catalog.*","inventory.*","order.view","order.update","payment.view","report.*"]'),
  ('STAFF',    N'Staff',          N'Xử lý đơn hàng',
   '["catalog.view","inventory.view","order.view","order.update","order.pick","payment.view"]'),
  ('CUSTOMER', N'Customer',       N'Khách mua sách',
   '["catalog.view","order.view"]');

INSERT INTO settings ([group], [key], value_json, description) VALUES
  ('PAYMENT',    'PAYOS_CONFIG',    '{"clientId":"","apiKey":"","checksumKey":""}',       N'Cấu hình PayOS'),
  ('SHIPPING',   'SHIPPING_RULES',  '{"freeAbove":500000,"standardFee":30000,"expressFee":50000}', N'Phí vận chuyển'),
  ('HOMEPAGE',   'BANNERS',         '[]',                                                N'Danh sách banner trang chủ'),
  ('HOMEPAGE',   'COLLECTIONS',     '{"new_arrivals":true,"bestsellers":true,"on_sale":true}', N'Cấu hình collections'),
  ('INVENTORY',  'ALLOCATION',      '{"fifoBy":"LOT","reservationTtlMin":30,"conditionPriority":"NEWEST_FIRST","allowStaffOverride":false}', N'Rule xuất kho'),
  ('SYSTEM',     'RESERVATION_TTL', '{"minutes":30}',                                    N'Thời gian giữ hàng');

  ALTER TABLE orders DROP CONSTRAINT CK_orders_status;
ALTER TABLE orders ADD CONSTRAINT CK_orders_status CHECK (status IN ('NEW','CONFIRMED','PACKED','SHIPPED','DELIVERED','COMPLETED','CANCELLED','DELIVERY_FAILED'));
/* ===================== END — 24 TABLES ===================== */