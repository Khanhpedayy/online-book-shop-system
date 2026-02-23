# Unit Test Cases - Online Book Shop

## BookServiceTest

| # | Test Method | Description | Expected Result |
|---|-------------|-------------|-----------------|
| 1 | `createBook_shouldReturnSavedBook` | Create a new book via service | Returns saved book with title "Clean Code" and price 39.99 |
| 2 | `getAllBooks_shouldReturnListOfBooks` | Get all books | Returns list containing one book |
| 3 | `getBookById_whenExists_shouldReturnBook` | Get book by valid ID (1L) | Returns book with matching ID and title |
| 4 | `getBookById_whenNotExists_shouldThrowException` | Get book by non-existent ID (999L) | Throws RuntimeException with "Book not found" |
| 5 | `updateBook_whenExists_shouldReturnUpdatedBook` | Update existing book | Returns updated book with new title and price |
| 6 | `deleteBook_shouldCallRepository` | Delete book by ID | Verifies deleteById is called on repository |

---

## OrderServiceTest

| # | Test Method | Description | Expected Result |
|---|-------------|-------------|-----------------|
| 1 | `placeOrder_guestCheckout_shouldCreateOrder` | Place order as guest with valid request | Creates order with correct total (79.98), status PENDING, deducts stock |
| 2 | `placeOrder_emptyItems_shouldThrowException` | Place order with empty items list | Throws IllegalArgumentException "at least one item" |
| 3 | `placeOrder_guestWithoutEmail_shouldThrowException` | Guest checkout without email | Throws IllegalArgumentException "Email is required" |
| 4 | `placeOrder_missingShippingAddress_shouldThrowException` | Order without shipping address | Throws IllegalArgumentException "Shipping address" |
| 5 | `placeOrder_bookNotFound_shouldThrowException` | Order with non-existent book ID | Throws RuntimeException "Book not found" |
| 6 | `placeOrder_inactiveBook_shouldThrowException` | Order inactive book | Throws IllegalArgumentException "not available" |
| 7 | `placeOrder_insufficientStock_shouldThrowException` | Order quantity exceeds stock | Throws IllegalArgumentException "Insufficient stock" |
| 8 | `getOrderById_whenExists_shouldReturnOrder` | Get order by valid ID | Returns order with matching ID |
| 9 | `getOrderById_whenNotExists_shouldThrowException` | Get order by non-existent ID | Throws RuntimeException "Order not found" |
| 10 | `getOrdersByCustomerId_shouldReturnList` | Get orders for customer | Returns list of orders for customer ID |

---

## BookControllerTest

| # | Test Method | Description | HTTP Method | Endpoint | Expected Status |
|---|-------------|-------------|-------------|----------|-----------------|
| 1 | `getAllBooks_shouldReturn200AndList` | List all books | GET | /api/books | 200 OK |
| 2 | `getBookById_shouldReturn200AndBook` | Get book by ID | GET | /api/books/1 | 200 OK |
| 3 | `createBook_shouldReturn201AndCreatedBook` | Create new book | POST | /api/books | 200 OK |
| 4 | `updateBook_shouldReturn200AndUpdatedBook` | Update book | PUT | /api/books/1 | 200 OK |
| 5 | `deleteBook_shouldReturn200` | Delete book | DELETE | /api/books/1 | 200 OK |

---

## OrderControllerTest

| # | Test Method | Description | HTTP Method | Endpoint | Expected Status |
|---|-------------|-------------|-------------|----------|-----------------|
| 1 | `placeOrder_shouldReturn201AndOrder` | Place order (guest checkout) | POST | /api/orders | 201 Created |
| 2 | `getOrderById_shouldReturn200AndOrder` | Get order by ID | GET | /api/orders/1 | 200 OK |
| 3 | `getOrdersByCustomerId_shouldReturn200AndList` | Get orders by customer | GET | /api/orders/customer/1 | 200 OK |

---

## Summary

| Test Class | Total Tests |
|------------|-------------|
| BookServiceTest | 6 |
| OrderServiceTest | 10 |
| BookControllerTest | 5 |
| OrderControllerTest | 3 |
| **Total** | **24** |
