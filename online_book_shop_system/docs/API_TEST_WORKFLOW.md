# API Test Workflow - Online Book Shop

## Prerequisites

1. Start the application: `mvn spring-boot:run` (from backend folder)
2. Base URL: `http://localhost:8080`
3. Use Postman or any REST client

---

## Workflow 1: Browse & Direct Order (Customer Only)

**Note:** Guests must log in to become customers before they can buy. Only logged-in customers can place orders.

| Step | Action | Method | Endpoint | Body / Notes |
|-----|--------|--------|----------|--------------|
| 1 | List all books | GET | `/api/books` | — |
| 2 | Get book detail | GET | `/api/books/1` | Replace `1` with actual bookId |
| 3 | Create a book (if needed) | POST | `/api/books` | `{ "title": "Clean Code", "isbn": "978-0132350884", "price": 39.99, "description": "...", "stockQuantity": 10, "status": "active" }` |
| 4 | Place order (customer) | POST | `/api/orders` | `{ "items": [{"bookId": 1, "quantity": 2}], "email": "customer@example.com", "shippingAddress": "123 Main St", "recipientName": "John Doe", "customerId": 1 }` — **customerId required** |
| 5 | Get order | GET | `/api/orders/{orderId}` | Use orderId from step 4 response |

---

## Workflow 2: Add to Cart → Checkout (Customer Only)

**Note:** Only logged-in customers can add to cart. Guests must use direct order (Workflow 1).

| Step | Action | Method | Endpoint | Body / Notes |
|-----|--------|--------|----------|--------------|
| 1 | Get or create cart | GET | `/api/cart/customer/{customerId}` | Replace `{customerId}` — Save `cartId` from response |
| 2 | Add item to cart | POST | `/api/cart/{cartId}/items` | `{ "bookId": 1, "quantity": 2 }` |
| 3 | Add another item | POST | `/api/cart/{cartId}/items` | `{ "bookId": 2, "quantity": 1 }` |
| 4 | Get cart | GET | `/api/cart/{cartId}` | Verify items |
| 5 | Update quantity | PUT | `/api/cart/{cartId}/items/1` | `{ "quantity": 3 }` |
| 6 | Checkout from cart | POST | `/api/orders/from-cart/{cartId}` | `{ "email": "customer@example.com", "shippingAddress": "123 Main St", "recipientName": "Jane Doe", "customerId": 1 }` |
| 7 | Verify cart empty | GET | `/api/cart/{cartId}` | Should have empty items |
| 8 | Get order | GET | `/api/orders/{orderId}` | Use orderId from step 6 |

---

## Workflow 3: Customer Cart (Summary)

| Step | Action | Method | Endpoint |
|-----|--------|--------|----------|
| 1 | Get or create cart | GET | `/api/cart/customer/{customerId}` |
| 2 | Add to cart | POST | `/api/cart/{cartId}/items` |
| 3 | Checkout | POST | `/api/orders/from-cart/{cartId}` |

---

## Expected Responses

| Endpoint | Success Status | Error Examples |
|----------|----------------|----------------|
| GET /api/books | 200 | — |
| GET /api/books/{id} | 200 | 404 Book not found |
| POST /api/books | 200 | — |
| POST /api/orders | 201 | 400 Email required, Insufficient stock |
| POST /api/cart | 201 | — |
| GET /api/cart/{cartId} | 200 | 404 Cart not found |
| POST /api/cart/{cartId}/items | 200 | 400 Book not available |
| POST /api/orders/from-cart/{cartId} | 201 | 400 Cart is empty |

---

## Test Checklist

- [ ] Create book with `status: "active"`
- [ ] Place direct order (customer) — guest without customerId rejected
- [ ] Get customer cart, add items, checkout
- [ ] Guest cannot add to cart (use GET /api/cart/customer/1 only for customers)
- [ ] Update cart item quantity
- [ ] Remove item from cart
- [ ] Checkout empties cart
- [ ] Inactive book rejected when adding to cart
- [ ] Insufficient stock rejected when ordering
