package com.petstore.inventory.event;

import java.util.List;

public class InventoryReservedEvent {

    private Long orderId;
    private List<ReservedItemPayload> reservedItems;

    public InventoryReservedEvent() {
    }

    public InventoryReservedEvent(Long orderId, List<ReservedItemPayload> reservedItems) {
        this.orderId = orderId;
        this.reservedItems = reservedItems;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public List<ReservedItemPayload> getReservedItems() {
        return reservedItems;
    }

    public void setReservedItems(List<ReservedItemPayload> reservedItems) {
        this.reservedItems = reservedItems;
    }

    public static class ReservedItemPayload {
        private Long productId;
        private Integer quantity;

        public ReservedItemPayload() {
        }

        public ReservedItemPayload(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
