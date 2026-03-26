package com.example.onlinebookshop.customer;

import com.example.onlinebookshop.Config.JwtUtils;
import com.example.onlinebookshop.Entity.BookInfo;
import com.example.onlinebookshop.Entity.BookVariant;
import com.example.onlinebookshop.Entity.Role;
import com.example.onlinebookshop.Entity.User;
import com.example.onlinebookshop.dto.AddToCartRequest;
import com.example.onlinebookshop.dto.AddressRequest;
import com.example.onlinebookshop.dto.CheckoutRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.onlinebookshop.Repository.BookInfoRepository;
import com.example.onlinebookshop.Repository.BookVariantRepository;
import com.example.onlinebookshop.Repository.RoleRepository;
import com.example.onlinebookshop.Repository.UserRepository;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CustomerFlowIntegrationTest {

    private static final String CUSTOMER_EMAIL = "testcustomer@bookshop.com";

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookInfoRepository bookInfoRepository;

    @Autowired
    private BookVariantRepository bookVariantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long seededVariantId;

    @BeforeEach
    void seedMinimalData() {
        Role customerRole = roleRepository.findByCode("CUSTOMER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setCode("CUSTOMER");
                    r.setName("Customer");
                    r.setDescription("Customer role for tests");
                    return roleRepository.save(r);
                });

        User customer = userRepository.findByEmailAndDeletedAtIsNull(CUSTOMER_EMAIL)
                .orElseGet(() -> {
                    User u = new User();
                    u.setRole(customerRole);
                    u.setEmail(CUSTOMER_EMAIL);
                    u.setPasswordHash(passwordEncoder.encode("123456"));
                    u.setFullName("Test Customer");
                    u.setPhone("0901000002");
                    u.setStatus("ACTIVE");
                    return userRepository.save(u);
                });

        BookInfo book = new BookInfo();
        book.setTitle("Test Customer Flow Book");
        book.setSlug("test-customer-flow-book");
        book.setStatus("ACTIVE");
        book = bookInfoRepository.save(book);

        BookVariant variant = new BookVariant();
        variant.setBook(book);
        variant.setSku("TEST-CUSTOMER-FLOW-VARIANT");
        variant.setListPrice(new java.math.BigDecimal("100.00"));
        variant.setSalePrice(new java.math.BigDecimal("90.00"));
        variant.setIsActive(true);
        seededVariantId = bookVariantRepository.save(variant).getId();

        if (customer.getId() == null) {
            throw new IllegalStateException("Seed customer user id not generated");
        }
    }

    private String authHeader(String token) {
        return "Bearer " + token;
    }

    @Test
    @DisplayName("Customer flow: address -> cart -> checkout COD -> verify order")
    void customer_checkoutCOD_fullFlow() throws Exception {
        String token = jwtUtils.generateToken(CUSTOMER_EMAIL, "CUSTOMER");

        // 1) Create address
        AddressRequest addr = new AddressRequest();
        addr.setLabel("Home");
        addr.setRecipientName("John Doe");
        addr.setPhone("0901234567");
        addr.setLine1("123 Test St");
        addr.setLine2(null);
        addr.setCity("HCM");
        addr.setDefaultAddress(true);

        mockMvc.perform(post("/api/me/addresses")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addr)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.recipientName").value("John Doe"))
                .andExpect(jsonPath("$.defaultAddress").value(true));

        // 2) Add item to cart
        AddToCartRequest add = new AddToCartRequest();
        add.setVariantId(seededVariantId);
        add.setQuantity(1);
        add.setCopyId(null);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(add)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variantId").value(seededVariantId));

        // 3) Checkout from cart using COD
        CheckoutRequest checkout = new CheckoutRequest();
        checkout.setEmail(CUSTOMER_EMAIL);
        checkout.setShippingAddress("123 Test St");
        checkout.setRecipientName("John Doe");
        checkout.setPhone("0901234567");
        checkout.setPaymentMethod("COD");
        checkout.setCustomerId(null); // backend sets it based on JWT email

        MvcResult orderRes = mockMvc.perform(post("/api/orders/from-cart")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkout)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").isNumber())
                .andReturn();

        com.fasterxml.jackson.databind.JsonNode orderJson = objectMapper.readTree(orderRes.getResponse().getContentAsString());
        long orderId = orderJson.get("orderId").asLong();
        java.math.BigDecimal totalAmount = orderJson.get("totalAmount").decimalValue();
        Assertions.assertTrue(totalAmount.compareTo(java.math.BigDecimal.ZERO) > 0, "totalAmount must be > 0");

        // 4) Verify created order detail belongs to the customer
        mockMvc.perform(get("/api/orders/{id}/me", orderId)
                        .header("Authorization", authHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.paymentMethod").value("COD"))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$.shipLine1").value("123 Test St"));
    }
}

