package com.sean.cyberweb.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String name; // 產品名稱

    @Column(nullable = false, length = 500)
    private String description; // 產品描述

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // 價格

    @Column(nullable = false)
    private Integer stock; // 庫存數量

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated; // 創建時間

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated; // 最後更新時間
}
