package com.petstore.cart;

import com.petstore.auth.entity.User;
import com.petstore.auth.repository.UserRepository;
import com.petstore.cart.dto.AddToCartRequest;
import com.petstore.cart.dto.CartDto;
import com.petstore.cart.dto.UpdateCartItemRequest;
import com.petstore.cart.entity.Cart;
import com.petstore.cart.entity.CartItem;
import com.petstore.cart.repository.CartItemRepository;
import com.petstore.cart.repository.CartRepository;
import com.petstore.cart.service.CartService;
import com.petstore.common.exception.InsufficientStockException;
import com.petstore.product.entity.Product;
import com.petstore.product.entity.ProductStatus;
import com.petstore.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User("John Doe", "john@example.com", "password", "1234567890");
        user.setId(1L);

        cart = new Cart(user);
        cart.setId(10L);

        product = new Product("Royal Canin Maxi Adult", "royal-canin-maxi", "Food", "Royal Canin",
                BigDecimal.valueOf(2500), 10, null, "4kg", ProductStatus.ACTIVE);
        product.setId(100L);
    }

    @Test
    void addToCart_newProduct_success() {
        when(productRepository.findByIdAndStatus(100L, ProductStatus.ACTIVE)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 100L)).thenReturn(Optional.empty());

        CartItem savedItem = new CartItem(cart, product, 2);
        savedItem.setId(50L);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedItem);
        when(cartItemRepository.findByCartId(10L)).thenReturn(Collections.singletonList(savedItem));

        AddToCartRequest request = new AddToCartRequest(100L, 2);
        CartDto result = cartService.addToCart(1L, request);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getTotalItems());
        assertEquals(new BigDecimal("5000"), result.getSubtotal());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addToCart_existingProduct_increasesQuantity() {
        when(productRepository.findByIdAndStatus(100L, ProductStatus.ACTIVE)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CartItem existingItem = new CartItem(cart, product, 2);
        existingItem.setId(50L);
        when(cartItemRepository.findByCartIdAndProductId(10L, 100L)).thenReturn(Optional.of(existingItem));

        CartItem updatedItem = new CartItem(cart, product, 5);
        updatedItem.setId(50L);
        when(cartItemRepository.save(existingItem)).thenReturn(updatedItem);
        when(cartItemRepository.findByCartId(10L)).thenReturn(Collections.singletonList(updatedItem));

        AddToCartRequest request = new AddToCartRequest(100L, 3);
        CartDto result = cartService.addToCart(1L, request);

        assertNotNull(result);
        assertEquals(5, result.getTotalItems());
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void addToCart_exceedsStock_throwsInsufficientStockException() {
        when(productRepository.findByIdAndStatus(100L, ProductStatus.ACTIVE)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 100L)).thenReturn(Optional.empty());

        AddToCartRequest request = new AddToCartRequest(100L, 15); // Product stock is 10

        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(1L, request));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void updateCartItemQuantity_validQuantity_success() {
        CartItem item = new CartItem(cart, product, 2);
        item.setId(50L);
        when(cartItemRepository.findById(50L)).thenReturn(Optional.of(item));

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L)).thenReturn(Collections.singletonList(item));

        UpdateCartItemRequest request = new UpdateCartItemRequest(4);
        CartDto result = cartService.updateCartItem(1L, 50L, request);

        assertEquals(4, item.getQuantity());
        verify(cartItemRepository).save(item);
    }

    @Test
    void removeCartItem_success() {
        CartItem item = new CartItem(cart, product, 2);
        item.setId(50L);
        when(cartItemRepository.findById(50L)).thenReturn(Optional.of(item));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L)).thenReturn(new ArrayList<>());

        CartDto result = cartService.removeCartItem(1L, 50L);

        assertEquals(0, result.getTotalItems());
        verify(cartItemRepository).delete(item);
    }

    @Test
    void clearCart_success() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L)).thenReturn(new ArrayList<>());

        CartDto result = cartService.clearCart(1L);

        assertEquals(0, result.getTotalItems());
        verify(cartItemRepository).deleteByCartId(10L);
    }
}
