package com.petstore.cart.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDto {

    private Long id;
    private List<CartItemDto> items = new ArrayList<>();
    private int totalItems;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public CartDto() {
    }

    public static CartDto create(Long cartId, List<CartItemDto> items) {
        CartDto dto = new CartDto();
        dto.setId(cartId);
        dto.setItems(items);

        int totalCount = 0;
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItemDto item : items) {
            totalCount += item.getQuantity();
            subtotal = subtotal.add(item.getSubtotal());
        }

        dto.setTotalItems(totalCount);
        dto.setSubtotal(subtotal);
        dto.setDeliveryFee(BigDecimal.ZERO); // V1: free delivery / 0
        dto.setTotalAmount(subtotal.add(dto.getDeliveryFee()));

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
