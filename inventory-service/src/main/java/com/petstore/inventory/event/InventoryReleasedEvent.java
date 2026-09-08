package com.petstore.inventory.event;

import java.util.List;

public class InventoryReleasedEvent {

    private Long orderId;
    private List<ReleasedItemPayload> releasedItems;

    public InventoryReleasedEvent() {
    }

    public InventoryReleasedEvent(Long orderId, List<ReleasedItemPayload> releasedItems) {
        this.orderId = orderId;
        this.releasedItems = releasedItems;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public List<ReleasedItemPayload> getReleasedItems() {
        return releasedItems;
    }

    public void setReleasedItems(List<ReleasedItemPayload> releasedItems) {
        this.releasedItems = releasedItems;
    }

    public static class ReleasedItemPayload {
        private Long productId;
        private Integer quantity;

        public ReleasedItemPayload() {
        }

        public ReleasedItemPayload(Long productId, Integer quantity) {
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
