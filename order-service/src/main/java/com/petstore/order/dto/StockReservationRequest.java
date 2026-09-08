package com.petstore.order.dto;

public class StockReservationRequest {

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
