package com.petstore.catalog.dto;

import java.math.BigDecimal;

public class StockReservationResponse {

    private Long productId;
    private String productName;
    private BigDecimal price;
    private int reservedQuantity;
    private int remainingStock;
    private boolean success;

    public StockReservationResponse() {
    }

    public StockReservationResponse(Long productId, String productName, BigDecimal price, int reservedQuantity, int remainingStock, boolean success) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.reservedQuantity = reservedQuantity;
        this.remainingStock = remainingStock;
        this.success = success;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public int getRemainingStock() {
        return remainingStock;
    }

    public void setRemainingStock(int remainingStock) {
        this.remainingStock = remainingStock;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
