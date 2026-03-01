package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.*;
import com.example.onlinebookshop.Repository.*;
import com.example.onlinebookshop.dto.CheckoutRequest;
import com.example.onlinebookshop.dto.OrderItemRequest;
import com.example.onlinebookshop.dto.OrderRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        order.setShipPhone(user.getPhone() != null ? user.getPhone() : "");
        order.setShipLine1(request.getShippingAddress());

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            BookVariant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Book not found: " + itemReq.getVariantId()));
            if (!variant.getIsActive() || (variant.getBook() != null && !"ACTIVE".equalsIgnoreCase(variant.getBook().getStatus()))) {
                throw new IllegalArgumentException("Book is not available: " + (variant.getBook() != null ? variant.getBook().getTitle() : variant.getSku()));
            }

            int qty = itemReq.getQuantity() != null && itemReq.getQuantity() > 0 ? itemReq.getQuantity() : 1;
            BigDecimal unitPrice = variant.getSalePrice() != null ? variant.getSalePrice() : BigDecimal.ZERO;
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            total = total.add(subtotal);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setVariant(variant);
            item.setTitleSnapshot(variant.getBook() != null ? variant.getBook().getTitle() : variant.getSku());
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
        List<CartItem> cartItems = cartItemRepository.findByUser_IdOrderByAddedAtDesc(userId);
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
                request.getRecipientName(), request.getCustomerId());
        Order order = placeOrder(orderRequest);
        cartItemRepository.deleteAll(cartItems);
        return order;
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
        return orderRepository.findByUserIdAndDeletedAtIsNull(customerId);
    }
}
