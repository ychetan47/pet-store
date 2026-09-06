package com.petstore.category.dto;

import com.petstore.category.entity.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryTreeResponse {

    private Long id;
    private String name;
    private String slug;
    private String imageUrl;
    private List<CategoryTreeResponse> subcategories = new ArrayList<>();

    public CategoryTreeResponse() {
    }

    public CategoryTreeResponse(Long id, String name, String slug, String imageUrl) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.imageUrl = imageUrl;
    }

    public static CategoryTreeResponse fromEntity(Category category) {
        CategoryTreeResponse tree = new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getImageUrl()
        );
        if (category.getSubcategories() != null) {
            for (Category sub : category.getSubcategories()) {
                if (sub.isActive()) {
                    tree.getSubcategories().add(fromEntity(sub));
                }
            }
        }
        return tree;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<CategoryTreeResponse> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<CategoryTreeResponse> subcategories) {
        this.subcategories = subcategories;
    }
}
