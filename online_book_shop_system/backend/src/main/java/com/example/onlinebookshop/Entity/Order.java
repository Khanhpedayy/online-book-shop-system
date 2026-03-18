package com.example.onlinebookshop.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true, length = 60)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";        // thay NEW

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus = "UNPAID";  // thay PENDING

    @Column(name = "payment_method", length = 20)
    private String paymentMethod; // COD, PAYOS

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "subtotal_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "shipping_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "VND";

    @Column(name = "ship_name", nullable = false, length = 150)
    private String shipName;

    @Column(name = "ship_phone", nullable = false, length = 30)
    private String shipPhone;

    @Column(name = "ship_line1", nullable = false, length = 255)
    private String shipLine1;

    @Column(name = "ship_line2", length = 255)
    private String shipLine2;

    @Column(name = "placed_at", nullable = false)
    private LocalDateTime placedAt = LocalDateTime.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}
