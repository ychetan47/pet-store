package com.petstore.catalog.dto;

import jakarta.validation.constraints.Min;

public class StockReservationRequest {

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    public StockReservationRequest() {
    }

    public StockReservationRequest(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
