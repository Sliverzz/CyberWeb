package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.Order;
import com.sean.cyberweb.domain.User;
import com.sean.cyberweb.dto.OrderDto;
import com.sean.cyberweb.services.LinePayService;
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

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final LinePayService linePayService;

    @Autowired
    public OrderController(OrderService orderService, UserService userService, LinePayService linePayService){
        this.orderService = orderService;
        this.userService = userService;
        this.linePayService = linePayService;
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
    public ResponseEntity<Map<String, Object>> getOrderInfo(@RequestParam UUID id) {
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

    // 前台申請取消訂單 & 後台確認取消訂單
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID orderId) {
        try {
            // 查詢當前訂單狀態
            Order order = orderService.findById(orderId);
            Order.OrderStatus currentStatus = order.getStatus();

            // 根據當前訂單狀態更新
            if (currentStatus == Order.OrderStatus.UNPAID) {
                orderService.updateOrderStatus(orderId, Order.OrderStatus.CANCEL_REQUESTED);
                return ResponseEntity.ok().body("Order cancellation requested successfully.");

            } else if (currentStatus == Order.OrderStatus.CANCEL_REQUESTED) {
                orderService.updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
                return ResponseEntity.ok().body("Order has been cancelled successfully.");

            } else {
                // 如果為其他狀態就不給更改
                return ResponseEntity.badRequest().body("Order cannot be cancelled in its current state.");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while processing the order cancellation.");
        }
    }

    // 前台申請訂單退款 & 後台確認將訂單退款(linePay)
    @PostMapping("/linePayRefund/{orderId}")
    public ResponseEntity<?> linePayRefund(@PathVariable UUID orderId) {
        try {
            // 查詢當前訂單狀態
            Order order = orderService.findById(orderId);
            Order.OrderStatus currentStatus = order.getStatus();

            // 根據當前訂單狀態更新
            if (currentStatus == Order.OrderStatus.PAID) {
                orderService.updateOrderStatus(orderId, Order.OrderStatus.REFUND_REQUESTED);
                return ResponseEntity.ok().body("Refund request submitted successfully.");

            } else if (currentStatus == Order.OrderStatus.REFUND_REQUESTED) {
                linePayService.refundPayment(order.getTransactionId(), orderId, order.getTotalPrice());
                return ResponseEntity.ok().body("Order has been refunded successfully.");

            } else {
                // 如果為其他狀態就不給更改
                return ResponseEntity.badRequest().body("Order cannot be refunded in its current state.");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while processing the refund request.");
        }
    }

    // 構建付款用dto以便啟動付款
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/details/{orderId}")
    public ResponseEntity<?> orderDetails(@PathVariable UUID orderId) {
        try {
            OrderDto orderDto = orderService.getOrderDetails(orderId);

            if (orderDto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
            }

            return ResponseEntity.ok(orderDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching order details");
        }
    }
}
