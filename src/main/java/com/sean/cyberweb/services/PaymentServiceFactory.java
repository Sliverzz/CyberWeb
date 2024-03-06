package com.sean.cyberweb.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentServiceFactory {

    private final LinePayService linePayService;
    private final PayPalService payPalService;

    @Autowired
    public PaymentServiceFactory(LinePayService linePayService, PayPalService payPalService) {
        this.linePayService = linePayService;
        this.payPalService = payPalService;
    }

    public PaymentService getPaymentService(String paymentMethod) {
        return switch (paymentMethod) {
            case "linePay" -> linePayService;
            case "paypal" -> payPalService;
            default -> throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        };
    }
}
