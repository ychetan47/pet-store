package com.petstore.category.dto;

import com.petstore.category.entity.Category;

public class CategoryResponse {

    private Long id;
    private String name;
    private String slug;
    private Long parentId;
    private String parentName;
    private String imageUrl;
    private boolean active;

    public CategoryResponse() {
    }

    public CategoryResponse(Long id, String name, String slug, Long parentId, String parentName, String imageUrl, boolean active) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.parentId = parentId;
        this.parentName = parentName;
        this.imageUrl = imageUrl;
        this.active = active;
    }

    public static CategoryResponse fromEntity(Category category) {
        Long pId = category.getParentCategory() != null ? category.getParentCategory().getId() : null;
        String pName = category.getParentCategory() != null ? category.getParentCategory().getName() : null;
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                pId,
                pName,
                category.getImageUrl(),
                category.isActive()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
