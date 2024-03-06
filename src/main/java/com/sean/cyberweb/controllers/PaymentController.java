package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.Order;
import com.sean.cyberweb.dto.CartItem;
import com.sean.cyberweb.services.OrderService;
import com.sean.cyberweb.services.PaymentService;
import com.sean.cyberweb.services.PaymentServiceFactory;
import com.sean.cyberweb.dto.CheckoutRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController // 返回JSON
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentServiceFactory paymentServiceFactory;
    private final OrderService orderService;

    @Autowired
    public PaymentController(PaymentServiceFactory paymentServiceFactory, OrderService orderService) {
        this.paymentServiceFactory = paymentServiceFactory;
        this.orderService = orderService;
    }

    // 結單 -> 付款
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest checkoutRequest) {
        try {
            // 1. 從請求中提取支付方法和購物車項目
            String paymentMethod = checkoutRequest.getPaymentMethod();
            List<CartItem> cartItems = checkoutRequest.getCartItems();

            // 2. 創建訂單
            Order order = orderService.createOrder(cartItems, checkoutRequest.getUserHashId());

            // 3. 根據支付方法，獲取對應的支付服務
            PaymentService paymentService = paymentServiceFactory.getPaymentService(paymentMethod);

            // 4. 啟動支付流程
            String paymentUrl = paymentService.initiatePayment(order);

            // 5. 返回支付URL
            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error processing checkout request.");
        }
    }
}