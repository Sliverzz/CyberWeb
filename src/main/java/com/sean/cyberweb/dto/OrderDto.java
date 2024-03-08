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
    private String dateCreated; // 創建時間(供前端時間格式顯示)
    private String lastUpdated; // 最後更新時間(供前端時間格式顯示)
    private String lastUpdateUser; // 最後更新人(供前端顯示使用)
    private List<OrderItemDto> orderItems = new ArrayList<>();
}
