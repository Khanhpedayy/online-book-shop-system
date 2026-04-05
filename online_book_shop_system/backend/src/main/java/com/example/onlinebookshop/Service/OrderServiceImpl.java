package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.*;
import com.example.onlinebookshop.Repository.*;
import com.example.onlinebookshop.paymentlog.PaymentLogRepository;
import com.example.onlinebookshop.payos.PayOSClient;
import com.example.onlinebookshop.shipping.ShippingFeeService;
import com.example.onlinebookshop.stock.StockRepository;
import com.example.onlinebookshop.util.VietnamPhoneUtils;
import com.example.onlinebookshop.dto.CheckoutRequest;
import com.example.onlinebookshop.dto.OrderItemRequest;
import com.example.onlinebookshop.dto.OrderRequest;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final BookVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final PayOSClient payOSClient;
    private final PaymentLogRepository paymentLogRepository;
    private final ShippingFeeService shippingFeeService;
    private final StockRepository stockRepository;

    public OrderServiceImpl(OrderRepository orderRepository, BookVariantRepository variantRepository,
                            UserRepository userRepository, CartItemRepository cartItemRepository,
                            PayOSClient payOSClient, PaymentLogRepository paymentLogRepository,
                            ShippingFeeService shippingFeeService,
                            StockRepository stockRepository) {
        this.orderRepository = orderRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.payOSClient = payOSClient;
        this.paymentLogRepository = paymentLogRepository;
        this.shippingFeeService = shippingFeeService;
        this.stockRepository = stockRepository;
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

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        Map<Long, Integer> orderQtyByBookId = new HashMap<>();
        Map<Long, BookInfo> bookByIdForStock = new HashMap<>();

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
            Long bookId = book.getId();
            orderQtyByBookId.merge(bookId, qty, Integer::sum);
            bookByIdForStock.put(bookId, book);

            BigDecimal unitPrice = variant.getSalePrice() != null ? variant.getSalePrice() : BigDecimal.ZERO;
            BigDecimal line = unitPrice.multiply(BigDecimal.valueOf(qty));
            subtotal = subtotal.add(line);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setVariant(variant);
            item.setTitleSnapshot(book.getTitle() != null ? book.getTitle() : variant.getSku());
            item.setSkuSnapshot(variant.getSku());
            item.setUnitPrice(unitPrice);
            item.setQuantity(qty);
            items.add(item);
        }

        for (Map.Entry<Long, Integer> e : orderQtyByBookId.entrySet()) {
            BookInfo b = bookByIdForStock.get(e.getKey());
            // Same source as cart: aggregate lots.qty_available (books.stock_quantity is often stale).
            int available = stockRepository.getStockQuantity(e.getKey());
            int requested = e.getValue();
            if (requested > available) {
                String title = b.getTitle() != null ? b.getTitle() : "sản phẩm này";
                throw new IllegalArgumentException(String.format(
                        "Không đủ tồn kho cho \"%s\": hiện còn %d, đơn của bạn cần %d.",
                        title, available, requested));
            }
        }

        BigDecimal shippingFee = shippingFeeService.computeShippingFee(subtotal);
        order.setSubtotalAmount(subtotal);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(subtotal.add(shippingFee));
        String pm = request.getPaymentMethod();
        order.setPaymentMethod(pm != null && !pm.isBlank() ? pm.trim().toUpperCase(Locale.ROOT) : "COD");
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
        if (request.getRecipientName() == null || request.getRecipientName().isBlank()) {
            throw new IllegalArgumentException("Recipient name is required");
        }
        String rawPhone = request.getPhone();
        if (rawPhone == null || rawPhone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        String phoneNorm = VietnamPhoneUtils.normalizeVnPhone(rawPhone.trim());
        if (!VietnamPhoneUtils.isValidVnPhone(phoneNorm)) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (VD: 09xxxxxxxx).");
        }

        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        String checkoutEmail = request.getEmail();
        if (checkoutEmail == null || checkoutEmail.isBlank()) {
            checkoutEmail = buyer.getEmail();
        }

        List<OrderItemRequest> items = cartItems.stream()
                .map(ci -> new OrderItemRequest(ci.getVariant().getId(), ci.getQuantity()))
                .toList();
        OrderRequest orderRequest = new OrderRequest(items, checkoutEmail, request.getShippingAddress(),
                request.getRecipientName().trim(), phoneNorm, request.getPaymentMethod(), request.getCustomerId());
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
    @Transactional
    public String createPayment(Order order) {
        if (order.getPaymentMethod() == null || !"PAYOS".equalsIgnoreCase(order.getPaymentMethod())) {
            throw new IllegalArgumentException("Payment link is only available for PayOS orders.");
        }
        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Cannot create payment for a cancelled order.");
        }
        String ps = order.getPaymentStatus();
        if (ps != null
                && !"PENDING".equalsIgnoreCase(ps)
                && !"UNPAID".equalsIgnoreCase(ps)
                && !"CANCELLED".equalsIgnoreCase(ps)) {
            throw new IllegalStateException("Order is not eligible for payment.");
        }
        if (order.getId() == null) {
            throw new IllegalStateException("Order must be saved before creating a payment link.");
        }
        PayOSClient.PayOSCheckoutResult result = payOSClient.createCheckout(
                order.getId(), order.getTotalAmount(), order.getOrderCode());
        paymentLogRepository.insertPayOsLink(
                order.getId(), result.paymentLinkId(), order.getTotalAmount(), result.payosOrderCode());
        return result.checkoutUrl();
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
        Order order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        if (order.getDeletedAt() != null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Forbidden");
        }
        // Force-load items while Hibernate session is still open
        Hibernate.initialize(order.getItems());
        order.getItems().forEach(item -> {
            try { Hibernate.initialize(item.getVariant()); } catch (Exception ignored) {}
        });
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
