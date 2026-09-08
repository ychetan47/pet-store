package com.petstore.catalog.service;

import com.petstore.catalog.entity.Category;
import com.petstore.catalog.entity.Product;
import com.petstore.catalog.entity.ProductStatus;
import com.petstore.catalog.exception.BadRequestException;
import com.petstore.catalog.exception.InsufficientStockException;
import com.petstore.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        Category category = new Category("Dog Food", "dog-food", null, null, true);
        category.setId(10L);

        sampleProduct = new Product("Royal Canin Maxi", "royal-canin-maxi", "Premium dog food",
                "Royal Canin", new BigDecimal("2850.00"), 10, category, "4kg", ProductStatus.ACTIVE);
        sampleProduct.setId(101L);
    }

    @Test
    @DisplayName("Should lock and deduct stock successfully")
    void shouldLockAndDeductStockSuccessfully() {
        when(productRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product updated = inventoryService.lockAndDeductStock(101L, 3);

        assertNotNull(updated);
        assertEquals(7, updated.getStockQuantity());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when requested quantity exceeds stock")
    void shouldThrowWhenStockInsufficient() {
        when(productRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(sampleProduct));

        assertThrows(InsufficientStockException.class, () -> inventoryService.lockAndDeductStock(101L, 15));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when product is inactive")
    void shouldThrowWhenProductInactive() {
        sampleProduct.setStatus(ProductStatus.INACTIVE);
        when(productRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(sampleProduct));

        assertThrows(BadRequestException.class, () -> inventoryService.lockAndDeductStock(101L, 2));
    }

    @Test
    @DisplayName("Should restore stock successfully")
    void shouldRestoreStockSuccessfully() {
        when(productRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.restoreStock(101L, 5);

        assertEquals(15, sampleProduct.getStockQuantity());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("Should update stock directly for Admin")
    void shouldUpdateStockDirectly() {
        when(productRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product updated = inventoryService.updateStock(101L, 50);

        assertEquals(50, updated.getStockQuantity());
        verify(productRepository, times(1)).save(sampleProduct);
    }
}
