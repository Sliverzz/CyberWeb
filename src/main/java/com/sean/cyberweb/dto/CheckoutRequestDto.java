package com.sean.cyberweb.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class CheckoutRequestDto {
    private String paymentMethod;
    private String userHashId;
    private List<CartItemDto> cartItems;
}
