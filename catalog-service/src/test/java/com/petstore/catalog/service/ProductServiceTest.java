package com.petstore.catalog.service;

import com.petstore.catalog.dto.ProductCreateRequest;
import com.petstore.catalog.dto.ProductDetailResponse;
import com.petstore.catalog.entity.Category;
import com.petstore.catalog.entity.Product;
import com.petstore.catalog.entity.ProductStatus;
import com.petstore.catalog.outbox.OutboxService;
import com.petstore.catalog.repository.CategoryRepository;
import com.petstore.catalog.repository.ProductImageRepository;
import com.petstore.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private CategoryRepository categoryRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private ProductService productService;

    private Category sampleCategory;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category("Dog Food", "dog-food", null, null, true);
        sampleCategory.setId(10L);

        sampleProduct = new Product("Royal Canin Maxi", "royal-canin-maxi", "Premium dog food",
                "Royal Canin", new BigDecimal("2850.00"), 10, sampleCategory, "4kg", ProductStatus.ACTIVE);
        sampleProduct.setId(101L);
    }

    @Test
    @DisplayName("Should retrieve product details by ID")
    void shouldRetrieveProductById() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(sampleProduct));

        ProductDetailResponse response = productService.getProductById(101L);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Royal Canin Maxi", response.getName());
        assertEquals(new BigDecimal("2850.00"), response.getPrice());
    }

    @Test
    @DisplayName("Should retrieve distinct brands")
    void shouldRetrieveDistinctBrands() {
        when(productRepository.findDistinctBrands()).thenReturn(List.of("Pedigree", "Royal Canin", "Whiskas"));

        List<String> brands = productService.getDistinctBrands();

        assertEquals(3, brands.size());
        assertTrue(brands.contains("Royal Canin"));
    }

    @Test
    @DisplayName("Should create product with generated slug")
    void shouldCreateProduct() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Whiskas Ocean Fish");
        request.setBrand("Whiskas");
        request.setPrice(new BigDecimal("450.00"));
        request.setStockQuantity(20);
        request.setCategoryId(10L);

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(sampleCategory));
        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setId(201L);
            return p;
        });

        ProductDetailResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals(201L, response.getId());
        assertEquals("Whiskas Ocean Fish", response.getName());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(outboxService, times(1)).recordEvent(eq("ProductCreated"), eq("201"), eq("PRODUCT"), eq(1), any(), any());
    }
}
