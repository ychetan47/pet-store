package com.petstore.catalog.event;

public class ProductDeactivatedEvent {

    private Long productId;

    public ProductDeactivatedEvent() {
    }

    public ProductDeactivatedEvent(Long productId) {
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
