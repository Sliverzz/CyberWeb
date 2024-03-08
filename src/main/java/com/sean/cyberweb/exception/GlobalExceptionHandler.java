package com.sean.cyberweb.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StockShortageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleStockShortage(StockShortageException ex) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", "stockShortage");
        errorDetails.put("message", ex.getMessage());
        return errorDetails;
    }
}