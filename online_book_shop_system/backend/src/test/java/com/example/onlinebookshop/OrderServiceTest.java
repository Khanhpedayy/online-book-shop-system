package com.example.onlinebookshop;

import com.example.onlinebookshop.Entity.*;
import com.example.onlinebookshop.Repository.*;
import com.example.onlinebookshop.Service.OrderServiceImpl;
import com.example.onlinebookshop.paymentlog.PaymentLogRepository;
import com.example.onlinebookshop.payos.PayOSClient;
import com.example.onlinebookshop.dto.OrderItemRequest;
import com.example.onlinebookshop.dto.OrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BookVariantRepository variantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private PayOSClient payOSClient;

    @Mock
    private PaymentLogRepository paymentLogRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private BookInfo bookInfo;
    private BookVariant variant;
    private OrderRequest validRequest;

    @BeforeEach
    void setUp() {
        Role role = new Role(2, "CUSTOMER", "Customer", null, null, null, null);
        user = new User();
        user.setId(1L);
        user.setRole(role);
        user.setEmail("customer@example.com");
        user.setFullName("Test Customer");
        user.setStatus("ACTIVE");

        bookInfo = new BookInfo(1L, null, null, null, "Clean Code", null, "slug", null, null, "ACTIVE",null, null, null, null, null);
        variant = new BookVariant(1L, bookInfo, "SKU-001", BigDecimal.valueOf(39.99), BigDecimal.valueOf(39.99),null, true, null, null);

        validRequest = new OrderRequest(
                List.of(new OrderItemRequest(1L, 2)),
                "customer@example.com",
                "123 Main St",
                "John Doe",
                "0900000000",
                "COD",
                1L
        );
    }

    @Test
    void placeOrder_customerCheckout_shouldCreateOrder() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        Order result = orderService.placeOrder(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(79.98));
        assertThat(result.getStatus()).isEqualTo("NEW");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void placeOrder_emptyItems_shouldThrowException() {
        OrderRequest request = new OrderRequest(Collections.emptyList(), "customer@test.com", "123 St", "John", 1L);

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    void placeOrder_guestWithoutCustomerId_shouldThrowException() {
        OrderRequest request = new OrderRequest(
                List.of(new OrderItemRequest(1L, 1)),
                "guest@test.com",
                "123 Main St",
                "John",
                null,
                "COD",
                null
        );

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("log in");
    }

    @Test
    void getOrderById_whenExists_shouldReturnOrder() {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrdersByCustomerId_shouldReturnList() {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(List.of(order));

        List<Order> result = orderService.getOrdersByCustomerId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(orderRepository).findByUserIdAndDeletedAtIsNull(1L);
    }
}
