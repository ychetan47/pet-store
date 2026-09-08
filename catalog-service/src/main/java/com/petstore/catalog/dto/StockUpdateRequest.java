package com.petstore.catalog.dto;

import jakarta.validation.constraints.Min;

public class StockUpdateRequest {

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private int stock;

    public StockUpdateRequest() {
    }

    public StockUpdateRequest(int stock) {
        this.stock = stock;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
