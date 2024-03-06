package com.sean.cyberweb.services;

import org.springframework.stereotype.Service;

import com.sean.cyberweb.domain.Order;
import com.sean.cyberweb.services.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PayPalService implements PaymentService {

//    @Value("${paypal.clientId}")
//    private String clientId;
//
//    @Value("${paypal.clientSecret}")
//    private String clientSecret;

    @Override
    public String initiatePayment(Order order) {
        // 尚未動工

        // 返回付款url
        String paymentUrl = "https://www.paypal.com/cgi-bin/webscr";
        return paymentUrl;
    }
}

