package com.sean.cyberweb.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CheckoutRequestDto {
    private String paymentMethod;
    private String userHashId;
    private List<CartItemDto> cartItems;
}
