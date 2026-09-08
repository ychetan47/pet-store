package com.petstore.inventory.dto;

import com.petstore.inventory.entity.InventoryReservation;
import com.petstore.inventory.entity.ReservationStatus;
import java.time.Instant;

public class InventoryReservationDto {

    private Long id;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private ReservationStatus status;
    private Instant createdAt;

    public InventoryReservationDto() {
    }

    public static InventoryReservationDto fromEntity(InventoryReservation reservation) {
        InventoryReservationDto dto = new InventoryReservationDto();
        dto.setId(reservation.getId());
        dto.setOrderId(reservation.getOrderId());
        dto.setProductId(reservation.getProductId());
        dto.setQuantity(reservation.getQuantity());
        dto.setStatus(reservation.getStatus());
        dto.setCreatedAt(reservation.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
