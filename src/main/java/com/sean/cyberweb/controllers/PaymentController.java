package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.Order;
import com.sean.cyberweb.dto.CartItemDto;
import com.sean.cyberweb.exception.StockShortageException;
import com.sean.cyberweb.services.LinePayService;
import com.sean.cyberweb.services.OrderService;
import com.sean.cyberweb.services.PaymentService;
import com.sean.cyberweb.services.PaymentServiceFactory;
import com.sean.cyberweb.dto.CheckoutRequestDto;
import com.sean.cyberweb.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentServiceFactory paymentServiceFactory;
    private final OrderService orderService;
    private final LinePayService linePayService;

    @Autowired
    public PaymentController(PaymentServiceFactory paymentServiceFactory, OrderService orderService, LinePayService linePayService) {
        this.paymentServiceFactory = paymentServiceFactory;
        this.orderService = orderService;
        this.linePayService = linePayService;
    }

    // 購物車付款路線
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

    @GetMapping("/confirm")
    public String confirmPayment(@RequestParam("transactionId") String transactionId,
                                 @RequestParam("orderId") UUID orderId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.findById(orderId);
            if (order == null) {
                throw new Exception("Order does not exist.");
            }

            // 呼叫confirmApi
            linePayService.confirmPayment(transactionId, orderId, order.getTotalPrice(), "TWD");

            // 添加前端顯示訊息
            WebUtils.addFlashMessage(redirectAttributes, "success", "Payment successful, please check the order page for details.");

            return "redirect:/site/order";
        } catch (Exception e) {
            WebUtils.addFlashMessage(redirectAttributes, "error", "An error occurred during payment confirmation, please contact customer service.");

            return "redirect:/site/order";
        }
    }
}