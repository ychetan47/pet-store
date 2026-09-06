package com.petstore.product;

import com.petstore.category.service.CategoryService;
import com.petstore.common.exception.ResourceNotFoundException;
import com.petstore.common.response.PageResponse;
import com.petstore.product.dto.ProductDetailDto;
import com.petstore.product.dto.ProductSummaryDto;
import com.petstore.product.entity.Product;
import com.petstore.product.entity.ProductStatus;
import com.petstore.product.repository.ProductRepository;
import com.petstore.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("Whiskas Ocean Fish Adult", "whiskas-ocean-fish", "Cat dry food", "Whiskas",
                BigDecimal.valueOf(820), 40, null, "3kg", ProductStatus.ACTIVE);
        product.setId(101L);
    }

    @Test
    void getProductById_found_returnsProductDetailDto() {
        when(productRepository.findByIdAndStatus(101L, ProductStatus.ACTIVE)).thenReturn(Optional.of(product));

        ProductDetailDto dto = productService.getProductById(101L);

        assertNotNull(dto);
        assertEquals(101L, dto.getId());
        assertEquals("Whiskas Ocean Fish Adult", dto.getName());
        assertEquals("Whiskas", dto.getBrand());
    }

    @Test
    void getProductById_notFound_throwsResourceNotFoundException() {
        when(productRepository.findByIdAndStatus(999L, ProductStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    void getProducts_returnsPageResponse() {
        Page<Product> page = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<ProductSummaryDto> response = productService.getProducts(
                null, null, null, null, null, null, 0, 10, "createdAt", "desc"
        );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Whiskas Ocean Fish Adult", response.getContent().get(0).getName());
    }

    @Test
    void getAvailableBrands_returnsBrandsList() {
        when(productRepository.findDistinctBrands()).thenReturn(List.of("Farmina", "Pedigree", "Royal Canin", "Whiskas"));

        List<String> brands = productService.getAvailableBrands();

        assertNotNull(brands);
        assertEquals(4, brands.size());
        assertTrue(brands.contains("Royal Canin"));
    }
}
