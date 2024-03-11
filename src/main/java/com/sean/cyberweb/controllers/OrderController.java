package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.Order;
import com.sean.cyberweb.domain.User;
import com.sean.cyberweb.dto.OrderDto;
import com.sean.cyberweb.services.OrderService;
import com.sean.cyberweb.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public OrderController(OrderService orderService, UserService userService){
        this.orderService = orderService;
        this.userService = userService;
    }

    // 後臺使用 - 查詢所有訂單
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listAll")
    public ResponseEntity<List<OrderDto>> listAllOrders(Pageable pageable) {
        List<OrderDto> orders = orderService.findAll(pageable).getContent();
        return ResponseEntity.ok(orders);
    }

    // 前台會員中心 - 使用者查詢個人訂單
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/listUserOrders")
    public ResponseEntity<List<OrderDto>> listUserOrders(Pageable pageable) {
        try {
            Page<OrderDto> orders = orderService.findAllByUserId(pageable);
            return ResponseEntity.ok(orders.getContent());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    // 獲取個別訂單資訊
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/getOrderInfo")
    public ResponseEntity<Map<String, Object>> getOrderInfo(@RequestParam Long id) {
        Order order = orderService.findById(id);
        OrderDto orderDto = orderService.convertToOrderDto(order);

        // 訂單狀態
        List<String> statuses = Arrays.stream(Order.OrderStatus.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("order", orderDto);
        response.put("statuses", statuses);

        return ResponseEntity.ok(response);
    }

    // 更新
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update")
    public ResponseEntity<?> updateOrder(@RequestBody OrderDto orderDto) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        Order updatedOrder = orderService.updateOrder(orderDto, currentUser);

        return ResponseEntity.ok().build();
    }
}
