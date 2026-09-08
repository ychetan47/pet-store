package com.petstore.catalog.controller;

import com.petstore.catalog.dto.ApiResponse;
import com.petstore.catalog.dto.PageResponse;
import com.petstore.catalog.dto.ProductDetailResponse;
import com.petstore.catalog.dto.ProductResponse;
import com.petstore.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Customer product catalog browsing and discovery")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Search & filter products", description = "Browses catalog with pet, category, brand, and price filters")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false) String pet,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        PageResponse<ProductResponse> response = productService.getProducts(
                pet, categoryId, brand, minPrice, maxPrice, search, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves full product details including image gallery")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(@PathVariable Long id) {
        ProductDetailResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product details retrieved", product));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug", description = "Retrieves product details by URL slug")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductBySlug(@PathVariable String slug) {
        ProductDetailResponse product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Product details retrieved", product));
    }

    @GetMapping("/brands")
    @Operation(summary = "Get available brands", description = "Returns distinct brands with active products")
    public ResponseEntity<ApiResponse<List<String>>> getBrands() {
        List<String> brands = productService.getDistinctBrands();
        return ResponseEntity.ok(ApiResponse.success("Brands retrieved", brands));
    }
}
