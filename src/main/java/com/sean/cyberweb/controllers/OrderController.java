package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.Order;
import com.sean.cyberweb.dto.OrderDto;
import com.sean.cyberweb.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @GetMapping("/listAll")
    public ResponseEntity<List<Order>> listAllOrders() {
        List<Order> orders = orderService.findAll(Pageable.unpaged()).getContent();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/getOrderInfo")
    public ResponseEntity<OrderDto> getOrderInfo(@RequestParam Long id) {
        Order order = orderService.findById(id);
        OrderDto orderDto = orderService.convertToOrderDto(order);
        return ResponseEntity.ok(orderDto);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateOrder(@RequestBody OrderDto orderDto) {

        Order updatedOrder = orderService.updateOrder(orderDto);

        // 返回更新后的订单
        return ResponseEntity.ok().build();
    }
}
