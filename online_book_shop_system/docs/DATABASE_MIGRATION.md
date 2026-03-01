# Database Migration - Bookstore Schema

## Overview

The code has been updated to work with the bookstore database schema (24 tables + carts).

## Setup

### 1. Run the main schema

Execute the full bookstore SQL schema (24 tables) in SQL Server Management Studio or Azure Data Studio.

### 2. Run the carts migration

```sql
-- From backend/src/main/resources/db/schema.sql
-- Creates carts and cart_items tables
```

### 3. Configuration

`application.properties` uses:
- **Database:** `bookstore` (change if different)
- **ddl-auto:** `update` (Hibernate can create missing tables)

## API Changes

| Old | New |
|-----|-----|
| `bookId` in requests | `variantId` (book_variants.id) |
| `GET /api/books` returns Book | Returns BookVariantDTO |
| Order.customerId | Order.user (User entity) |
| Cart.customerId | Cart.user (User entity) |

## Entity Mapping

| Table | Entity |
|-------|--------|
| roles | Role |
| users | User |
| books | BookInfo |
| book_variants | BookVariant |
| orders | Order |
| order_items | OrderItem |
| carts | Cart |
| cart_items | CartItem |

## Example Requests

**Create book (variant):**
```json
POST /api/books
{
  "title": "Clean Code",
  "sku": "SKU-001",
  "salePrice": 39.99,
  "description": "A Handbook of Agile Software Craftsmanship",
  "status": "ACTIVE"
}
```

**Place order:**
```json
POST /api/orders
{
  "items": [{"variantId": 1, "quantity": 2}],
  "email": "customer@example.com",
  "shippingAddress": "123 Main St",
  "recipientName": "John Doe",
  "customerId": 1
}
```

**Add to cart:**
```json
POST /api/cart/{cartId}/items
{
  "variantId": 1,
  "quantity": 2
}
```

## Seed Data

On first run (when DB is empty), the app seeds:
- Roles: ADMIN, CUSTOMER, STAFF, MANAGER
- Test user: customer@example.com (CUSTOMER role)

Use `customerId: 1` for testing with the seeded user.
