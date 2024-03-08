package com.sean.cyberweb.exception;

// 庫存警報
public class StockShortageException extends RuntimeException {
    public StockShortageException(String productName, int stock) {
        super(productName + " is out of stock.\nCurrent stock: " + stock);
    }
}