package com.sean.cyberweb.services;

import com.sean.cyberweb.domain.Order;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {
    /**
     * 啟動付款流程
     * @param order 需要支付的訂單
     * @return 返回支付页面的URL
     */
    String initiatePayment(Order order);
}
