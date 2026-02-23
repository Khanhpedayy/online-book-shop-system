package com.example.onlinebookshop;

import com.example.onlinebookshop.dto.OrderItemRequest;
import com.example.onlinebookshop.dto.OrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private BookRepository bookRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Book book;
    private OrderRequest validRequest;

    @BeforeEach
    void setUp() {
        book = new Book(1L, "Clean Code", "978-0132350884", 39.99,
                "Description", 10, "active");

        validRequest = new OrderRequest(
                List.of(new OrderItemRequest(1L, 2)),
                "guest@example.com",
                "123 Main St",
                "John Doe",
                null
        );
    }

    @Test
    void placeOrder_guestCheckout_shouldCreateOrder() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setOrderId(1L);
            return o;
        });

        Order result = orderService.placeOrder(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("guest@example.com");
        assertThat(result.getTotalAmount()).isEqualTo(79.98);  // 39.99 * 2
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(book);  // stock updated
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void placeOrder_emptyItems_shouldThrowException() {
        OrderRequest request = new OrderRequest(Collections.emptyList(), "guest@test.com", "123 St", "John", null);

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    void placeOrder_guestWithoutEmail_shouldThrowException() {
        OrderRequest request = new OrderRequest(
                List.of(new OrderItemRequest(1L, 1)),
                "",
                "123 Main St",
                "John",
                null
        );

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is required");
    }

    @Test
    void placeOrder_missingShippingAddress_shouldThrowException() {
        OrderRequest request = new OrderRequest(
                List.of(new OrderItemRequest(1L, 1)),
                "guest@test.com",
                "",
                "John",
                null
        );

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Shipping address");
    }

    @Test
    void placeOrder_bookNotFound_shouldThrowException() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());
        OrderRequest request = new OrderRequest(
                List.of(new OrderItemRequest(999L, 1)),
                "guest@test.com",
                "123 St",
                "John",
                null
        );

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    void placeOrder_inactiveBook_shouldThrowException() {
        book.setStatus("inactive");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> orderService.placeOrder(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void placeOrder_insufficientStock_shouldThrowException() {
        book.setStockQuantity(1);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> orderService.placeOrder(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void getOrderById_whenExists_shouldReturnOrder() {
        Order order = new Order();
        order.setOrderId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_whenNotExists_shouldThrowException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void getOrdersByCustomerId_shouldReturnList() {
        Order order = new Order();
        order.setCustomerId(1L);
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(order));

        List<Order> result = orderService.getOrdersByCustomerId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
        verify(orderRepository).findByCustomerId(1L);
    }
}
