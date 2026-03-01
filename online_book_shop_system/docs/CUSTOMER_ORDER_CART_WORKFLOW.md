# Customer Order & Add to Cart Workflow

## Overview

This document describes the workflow for customers to browse books, add items to cart, and place orders. **Guests must log in to become customers before they can buy.**

---

## High-Level Flow

```mermaid
flowchart TD
    A[Start] --> B[Browse Books]
    B --> C[View Book Details]
    C --> D[Add to Cart]
    D --> E{Continue Shopping?}
    E -->|Yes| B
    E -->|No| F[View Cart]
    F --> G{Update Cart?}
    G -->|Yes| H[Update Qty / Remove Item]
    H --> F
    G -->|No| I[Proceed to Checkout]
    I --> J{Guest or Customer?}
    J -->|Guest| K[Enter Email, Address]
    J -->|Customer| L[Use Saved Address]
    K --> M[Place Order]
    L --> M
    M --> N[Order Confirmation]
    N --> O[End]
```

---

## Detailed Workflow

### 1. Browse Books

| Step | Action | API / Component |
|------|--------|-----------------|
| 1.1 | Customer opens book catalog | `GET /api/books` |
| 1.2 | Customer filters/searches (optional) | Future: `GET /api/books?search=&status=active` |
| 1.3 | Customer clicks on a book | `GET /api/books/{id}` |

### 2. Add to Cart

| Step | Action | API / Component |
|------|--------|-----------------|
| 2.1 | Customer selects quantity | User input |
| 2.2 | Customer clicks "Add to Cart" | `POST /api/cart/items` *(to be implemented)* |
| 2.3 | System validates book (active, in stock) | Backend validation |
| 2.4 | Cart updated (session or DB) | Store cart item |
| 2.5 | Show success message | UI feedback |

```mermaid
sequenceDiagram
    participant C as Customer
    participant UI as Frontend
    participant API as Backend API

    C->>UI: Add to Cart (bookId, qty)
    UI->>API: POST /api/cart/items
    API->>API: Validate book exists & active
    API->>API: Check stock
    alt Success
        API-->>UI: 201 Cart updated
        UI-->>C: "Added to cart"
    else Out of stock
        API-->>UI: 400 Insufficient stock
        UI-->>C: Error message
    end
```

### 3. View & Manage Cart

| Step | Action | API / Component |
|------|--------|-----------------|
| 3.1 | Customer views cart | `GET /api/cart` |
| 3.2 | Update quantity | `PUT /api/cart/items/{bookId}` |
| 3.3 | Remove item | `DELETE /api/cart/items/{bookId}` |
| 3.4 | Cart shows subtotal | Calculated from items |

### 4. Checkout & Place Order

| Step | Action | API / Component |
|------|--------|-----------------|
| 4.1 | Customer clicks "Checkout" | Navigate to checkout |
| 4.2 | Guest: enter email, address, name | Form input |
| 4.3 | Customer: use saved profile (optional) | Pre-filled form |
| 4.4 | Submit order | `POST /api/orders` |
| 4.5 | System validates cart, stock, address | Backend validation |
| 4.6 | Create order, deduct stock | Order & OrderItem saved |
| 4.7 | Clear cart | Session/DB update |
| 4.8 | Show order confirmation | Order ID, total, status |

```mermaid
sequenceDiagram
    participant C as Customer
    participant UI as Frontend
    participant API as Backend API
    participant DB as Database

    C->>UI: Click Checkout
    UI->>API: POST /api/orders (items, email, address)
    API->>DB: Validate books & stock
    alt Valid
        API->>DB: Create Order + OrderItems
        API->>DB: Deduct stock
        API->>DB: Clear cart (if cart exists)
        API-->>UI: 201 Order created
        UI-->>C: Order confirmation
    else Invalid
        API-->>UI: 400 Error
        UI-->>C: Show error
    end
```

---

## Current vs Planned Implementation

| Feature | Status | API |
|---------|--------|-----|
| Browse books | ✅ Implemented | `GET /api/books`, `GET /api/books/{id}` |
| Place order (direct) | ✅ Implemented | `POST /api/orders` |
| Create cart | ✅ Implemented | `POST /api/cart` |
| Get cart | ✅ Implemented | `GET /api/cart/{cartId}` |
| Add to cart | ✅ Implemented | `POST /api/cart/{cartId}/items` |
| Update cart item | ✅ Implemented | `PUT /api/cart/{cartId}/items/{bookId}` |
| Remove from cart | ✅ Implemented | `DELETE /api/cart/{cartId}/items/{bookId}` |
| Checkout from cart | ✅ Implemented | `POST /api/orders/from-cart/{cartId}` |

---

## Cart Data Model (Proposed)

```
Cart
├── cartId
├── customerId (null for guest)
├── sessionId (for guest)
└── CartItem[]
    ├── bookId
    ├── quantity
    └── priceAtAdd (snapshot)
```

---

## State Diagram: Cart → Order

```mermaid
stateDiagram-v2
    [*] --> Browsing
    Browsing --> Cart: Add to cart
    Cart --> Browsing: Continue shopping
    Cart --> Cart: Update / Remove items
    Cart --> Checkout: Proceed to checkout
    Checkout --> OrderPlaced: Place order
    OrderPlaced --> [*]
    Checkout --> Cart: Cancel
```
