package com.petstore.order.service;

import com.petstore.order.client.ProductCatalogClient;
import com.petstore.order.dto.AddToCartRequest;
import com.petstore.order.dto.CartResponse;
import com.petstore.order.dto.ProductDto;
import com.petstore.order.entity.Cart;
import com.petstore.order.entity.CartItem;
import com.petstore.order.exception.InsufficientStockException;
import com.petstore.order.repository.CartItemRepository;
import com.petstore.order.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductCatalogClient productCatalogClient;

    @InjectMocks
    private CartService cartService;

    private Cart sampleCart;
    private ProductDto sampleProduct;

    @BeforeEach
    void setUp() {
        sampleCart = new Cart(1L);
        sampleCart.setId(10L);
        sampleCart.setItems(new ArrayList<>());

        sampleProduct = new ProductDto();
        sampleProduct.setId(101L);
        sampleProduct.setName("Royal Canin Maxi");
        sampleProduct.setPrice(new BigDecimal("2850.00"));
        sampleProduct.setStockQuantity(10);
        sampleProduct.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("Should add product to cart and calculate correct total")
    void shouldAddProductToCart() {
        AddToCartRequest request = new AddToCartRequest(101L, 2);

        when(productCatalogClient.getProduct(101L)).thenReturn(sampleProduct);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(sampleCart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 101L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));

        CartResponse response = cartService.addToCart(1L, request);

        assertNotNull(response);
        assertEquals(2, response.getTotalItems());
        assertEquals(new BigDecimal("5700.00"), response.getTotalAmount());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should reject adding item exceeding available stock")
    void shouldRejectAddingWhenStockInsufficient() {
        AddToCartRequest request = new AddToCartRequest(101L, 15);

        when(productCatalogClient.getProduct(101L)).thenReturn(sampleProduct);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(sampleCart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 101L)).thenReturn(Optional.empty());

        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(1L, request));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should remove item from cart")
    void shouldRemoveItemFromCart() {
        CartItem item = new CartItem(sampleCart, 101L, 1);
        item.setId(5L);
        sampleCart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(sampleCart));
        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(item));

        CartResponse response = cartService.removeFromCart(1L, 5L);

        assertNotNull(response);
        assertEquals(0, response.getTotalItems());
        verify(cartItemRepository, times(1)).delete(item);
    }
}
