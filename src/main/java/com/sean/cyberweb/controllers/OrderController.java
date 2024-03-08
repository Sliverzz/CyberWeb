package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.Order;
import com.sean.cyberweb.domain.User;
import com.sean.cyberweb.dto.OrderDto;
import com.sean.cyberweb.services.OrderService;
import com.sean.cyberweb.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

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

    @GetMapping("/listAll")
    public ResponseEntity<List<OrderDto>> listAllOrders(Pageable pageable) {
        List<OrderDto> orders = orderService.findAll(pageable).getContent();
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
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        Order updatedOrder = orderService.updateOrder(orderDto, currentUser);

        return ResponseEntity.ok().build();
    }
}
