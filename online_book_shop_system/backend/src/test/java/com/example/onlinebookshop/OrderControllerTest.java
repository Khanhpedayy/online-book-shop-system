package com.example.onlinebookshop;

import com.example.onlinebookshop.Config.JacksonConfig;
import com.example.onlinebookshop.Controller.OrderController;
import com.example.onlinebookshop.Entity.Order;
import com.example.onlinebookshop.Service.OrderService;
import com.example.onlinebookshop.dto.OrderItemRequest;
import com.example.onlinebookshop.dto.OrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(JacksonConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void placeOrder_shouldReturn201AndOrder() throws Exception {
        OrderRequest request = new OrderRequest(
                List.of(new OrderItemRequest(1L, 2)),
                "guest@example.com",
                "123 Main St",
                "John Doe",
                null
        );
        Order order = new Order();
        order.setOrderId(1L);
        order.setEmail("guest@example.com");
        order.setTotalAmount(79.98);
        order.setStatus("PENDING");

        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.email").value("guest@example.com"))
                .andExpect(jsonPath("$.totalAmount").value(79.98));

        verify(orderService).placeOrder(any(OrderRequest.class));
    }

    @Test
    void getOrderById_shouldReturn200AndOrder() throws Exception {
        Order order = new Order();
        order.setOrderId(1L);
        order.setEmail("guest@example.com");
        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.email").value("guest@example.com"));

        verify(orderService).getOrderById(1L);
    }

    @Test
    void getOrdersByCustomerId_shouldReturn200AndList() throws Exception {
        Order order = new Order();
        order.setOrderId(1L);
        order.setCustomerId(1L);
        when(orderService.getOrdersByCustomerId(1L)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].customerId").value(1));

        verify(orderService).getOrdersByCustomerId(1L);
    }
}
