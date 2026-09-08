package com.petstore.catalog.dto;

import com.petstore.catalog.entity.ProductImage;

public class ProductImageDto {

    private Long id;
    private String imageUrl;
    private boolean isPrimary;
    private int displayOrder;

    public ProductImageDto() {
    }

    public ProductImageDto(Long id, String imageUrl, boolean isPrimary, int displayOrder) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.isPrimary = isPrimary;
        this.displayOrder = displayOrder;
    }

    public static ProductImageDto fromEntity(ProductImage image) {
        return new ProductImageDto(
                image.getId(),
                image.getImageUrl(),
                image.isPrimary(),
                image.getDisplayOrder()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
