package com.sean.cyberweb.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class CartItem {
    private Long id;
    private String name;
    private BigDecimal price;
    private String productImagePath;
    private Integer quantity;
}
