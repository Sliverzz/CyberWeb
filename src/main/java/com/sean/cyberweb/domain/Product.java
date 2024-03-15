package com.sean.cyberweb.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 產品名稱

    @Column(nullable = false, length = 500)
    private String description; // 產品描述

    @Column(nullable = false, precision = 10)
    private BigDecimal price; // 價格

    @Column(nullable = false)
    private Integer stock; // 庫存數量

    @Column(name = "product_image_path")
    private String productImagePath; // 產品圖片位址

    @Column(nullable = false)
    private boolean status; // 上下架狀態

    public boolean hasProductImgUrl() {
        return productImagePath != null && !productImagePath.isEmpty();
    }

    @CreationTimestamp
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated; // 創建時間

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated; // 最後更新時間
}
