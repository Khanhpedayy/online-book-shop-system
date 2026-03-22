package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.*;
import com.example.onlinebookshop.Repository.*;
import com.example.onlinebookshop.dto.CheckoutRequest;
import com.example.onlinebookshop.dto.OrderItemRequest;
import com.example.onlinebookshop.dto.OrderRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final BookVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    public OrderServiceImpl(OrderRepository orderRepository, BookVariantRepository variantRepository,
                            UserRepository userRepository, CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    @Transactional
    public Order placeOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("You must log in to place an order. Guests cannot buy without an account.");
        }
        if (request.getShippingAddress() == null || request.getShippingAddress().isBlank()) {
            throw new IllegalArgumentException("Shipping address is required");
        }

        User user = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getCustomerId()));

        Order order = new Order();
        order.setOrderCode("ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        order.setUser(user);
        order.setStatus("NEW");
        order.setPaymentStatus("PENDING");
        order.setShipName(request.getRecipientName() != null ? request.getRecipientName() : user.getFullName());
        String phone = request.getPhone() != null && !request.getPhone().isBlank()
                ? request.getPhone().trim()
                : (user.getPhone() != null ? user.getPhone() : "");
        order.setShipPhone(phone);
        order.setShipLine1(request.getShippingAddress());

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            BookVariant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Book not found: " + itemReq.getVariantId()));
            BookInfo book = variant.getBook();
            if (book == null) {
                throw new IllegalArgumentException("Book is not available: variant has no book (" + variant.getSku() + ")");
            }
            if (!variant.getIsActive() || !"ACTIVE".equalsIgnoreCase(book.getStatus())) {
                throw new IllegalArgumentException("Book is not available: " + book.getTitle());
            }

            int qty = itemReq.getQuantity() != null && itemReq.getQuantity() > 0 ? itemReq.getQuantity() : 1;
            BigDecimal unitPrice = variant.getSalePrice() != null ? variant.getSalePrice() : BigDecimal.ZERO;
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            total = total.add(subtotal);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setVariant(variant);
            item.setBook(book);
            item.setTitleSnapshot(book.getTitle() != null ? book.getTitle() : variant.getSku());
            item.setSkuSnapshot(variant.getSku());
            item.setUnitPrice(unitPrice);
            item.setQuantity(qty);
            items.add(item);
        }

        order.setSubtotalAmount(total);
        order.setTotalAmount(total);
        order.setItems(items);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order placeOrderFromCart(Long userId, CheckoutRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByUser_IdWithVariantAndBook(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        if (request.getCustomerId() == null || !request.getCustomerId().equals(userId)) {
            throw new IllegalArgumentException("You must log in to checkout. Guests cannot buy without an account.");
        }
        if (request.getShippingAddress() == null || request.getShippingAddress().isBlank()) {
            throw new IllegalArgumentException("Shipping address is required");
        }

        List<OrderItemRequest> items = cartItems.stream()
                .map(ci -> new OrderItemRequest(ci.getVariant().getId(), ci.getQuantity()))
                .toList();
        OrderRequest orderRequest = new OrderRequest(items, request.getEmail(), request.getShippingAddress(),
                request.getRecipientName(), request.getPhone(), request.getPaymentMethod(), request.getCustomerId());
        Order order = placeOrder(orderRequest);
        cartItemRepository.deleteAll(cartItems);
        return order;
    }

    @Override
    @Transactional
    public Order placeOrderFromCartByEmail(String email, CheckoutRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        if (request.getCustomerId() != null && !request.getCustomerId().equals(user.getId())) {
            throw new IllegalArgumentException("Customer id does not match authenticated user");
        }
        request.setCustomerId(user.getId());
        return placeOrderFromCart(user.getId(), request);
    }

    @Override
    public String createPayment(Order order) {
        throw new UnsupportedOperationException(
                "PayOS (or other) payment URL creation is not wired in OrderServiceImpl yet.");
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderDetailByEmail(Long orderId, String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        Order order = getOrderById(orderId);
        if (order.getDeletedAt() != null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Forbidden");
        }
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByUserIdAndDeletedAtIsNull(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByEmail(String email, String status, String keyword, String fromDate, String toDate) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);
        String kw = keyword != null && !keyword.isBlank() ? keyword.trim().toLowerCase(Locale.ROOT) : null;
        String st = status != null && !status.isBlank() ? status.trim().toUpperCase(Locale.ROOT) : null;

        return orderRepository.findByUserIdAndDeletedAtIsNull(user.getId()).stream()
                .filter(o -> st == null || st.equalsIgnoreCase(o.getStatus()))
                .filter(o -> kw == null || (o.getOrderCode() != null && o.getOrderCode().toLowerCase(Locale.ROOT).contains(kw))
                        || (o.getShipName() != null && o.getShipName().toLowerCase(Locale.ROOT).contains(kw)))
                .filter(o -> from == null || !o.getPlacedAt().toLocalDate().isBefore(from))
                .filter(o -> to == null || !o.getPlacedAt().toLocalDate().isAfter(to))
                .sorted(Comparator.comparing(Order::getPlacedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        if (order.getDeletedAt() != null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Forbidden");
        }
        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            return;
        }
        if (!"NEW".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Only new orders can be cancelled");
        }
        order.setStatus("CANCELLED");
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}
