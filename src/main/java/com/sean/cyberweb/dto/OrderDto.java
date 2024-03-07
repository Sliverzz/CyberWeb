package com.sean.cyberweb.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Setter
@Getter
public class OrderDto {
    private Long id;
    private String orderNumber;
    private BigDecimal totalPrice;
    private String status;
    private List<OrderItemDto> orderItems = new ArrayList<>();
}
