package com.sean.cyberweb.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders") // 避開SQL保留字
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // 用戶id

    @Column(nullable = false, unique = true)
    private String orderNumber; // 訂單編號

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // 訂單狀態

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalPrice; // 訂單總價

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated; // 創建時間

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated; // 最後更新時間

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("order")
    private Set<OrderItem> orderItems = new HashSet<>();

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public enum OrderStatus {
        UNPAID,        // 未付款
        PAID,          // 已付款
        REFUNDED,      // 已退款
        CANCELLED,     // 已取消
        PROCESSING,    // 正在處理
        SHIPPED,       // 已發貨
        DELIVERED      // 已送達
    }
}
