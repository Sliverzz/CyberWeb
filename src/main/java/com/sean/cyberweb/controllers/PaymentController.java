package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.Order;
import com.sean.cyberweb.dto.CartItemDto;
import com.sean.cyberweb.exception.StockShortageException;
import com.sean.cyberweb.services.OrderService;
import com.sean.cyberweb.services.PaymentService;
import com.sean.cyberweb.services.PaymentServiceFactory;
import com.sean.cyberweb.dto.CheckoutRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequestDto checkoutRequestDTO) {
        try {
            // 1. 從請求中提取支付方法和購物車項目
            String paymentMethod = checkoutRequestDTO.getPaymentMethod();
            List<CartItemDto> cartItems = checkoutRequestDTO.getCartItems();

            // 2. 創建訂單
            Order order = orderService.createOrder(cartItems, checkoutRequestDTO.getUserHashId());

            // 3. 根據支付方法，獲取對應的支付服務
            PaymentService paymentService = paymentServiceFactory.getPaymentService(paymentMethod);

            // 4. 啟動支付流程
            String paymentUrl = paymentService.initiatePayment(order);

            // 5. 返回支付URL
            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
        } catch (StockShortageException e) {
            // 庫存不足exception
            return ResponseEntity.badRequest().body(Map.of("error", "stockShortage", "message", e.getMessage()));
        } catch (Exception e) {
            // 其他異常
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing checkout request.");
        }
    }
}