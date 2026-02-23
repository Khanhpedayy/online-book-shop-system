package com.example.onlinebookshop;

import com.example.onlinebookshop.dto.OrderItemRequest;
import com.example.onlinebookshop.dto.OrderRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;

    public OrderServiceImpl(OrderRepository orderRepository, BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional
    public Order placeOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Guest must provide email
        if (request.getCustomerId() == null) {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new IllegalArgumentException("Email is required for guest checkout");
            }
        }

        if (request.getShippingAddress() == null || request.getShippingAddress().isBlank()) {
            throw new IllegalArgumentException("Shipping address is required");
        }

        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setEmail(request.getEmail());
        order.setShippingAddress(request.getShippingAddress());
        order.setRecipientName(request.getRecipientName());
        order.setStatus("PENDING");

        double total = 0;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            Book book = bookRepository.findById(itemReq.getBookId())
                    .orElseThrow(() -> new RuntimeException("Book not found: " + itemReq.getBookId()));

            if (!"active".equalsIgnoreCase(book.getStatus())) {
                throw new IllegalArgumentException("Book is not available: " + book.getTitle());
            }

            int qty = itemReq.getQuantity() != null ? itemReq.getQuantity() : 1;
            if (qty <= 0) continue;

            if (book.getStockQuantity() == null || book.getStockQuantity() < qty) {
                throw new IllegalArgumentException("Insufficient stock for: " + book.getTitle() + " (available: " + (book.getStockQuantity() != null ? book.getStockQuantity() : 0) + ")");
            }

            double subtotal = book.getPrice() * qty;
            total += subtotal;

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setBook(book);
            item.setQuantity(qty);
            item.setPrice(book.getPrice());
            item.setSubtotal(subtotal);
            items.add(item);

            book.setStockQuantity(book.getStockQuantity() - qty);
            bookRepository.save(book);
        }

        order.setTotalAmount(total);
        order.setItems(items);
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
}
