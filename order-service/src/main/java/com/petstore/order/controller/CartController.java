package com.petstore.order.controller;

import com.petstore.order.context.UserContext;
import com.petstore.order.dto.AddToCartRequest;
import com.petstore.order.dto.ApiResponse;
import com.petstore.order.dto.CartResponse;
import com.petstore.order.dto.UpdateCartItemRequest;
import com.petstore.order.exception.BadRequestException;
import com.petstore.order.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Customer shopping cart operations and live total calculations")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private Long getRequiredUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BadRequestException("Authenticated user context is required to access cart");
        }
        return userId;
    }

    @GetMapping
    @Operation(summary = "Get user cart", description = "Retrieves active shopping cart with live product prices and stock validation")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        Long userId = getRequiredUserId();
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved", cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds a product or increments quantity if already in cart")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        Long userId = getRequiredUserId();
        CartResponse cart = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Update item quantity", description = "Sets new quantity for a specific cart line item")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request) {

        Long userId = getRequiredUserId();
        CartResponse cart = cartService.updateCartItemQuantity(userId, id, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cart));
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Remove item from cart", description = "Deletes a specific line item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@PathVariable Long id) {
        Long userId = getRequiredUserId();
        CartResponse cart = cartService.removeFromCart(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Removes all items from the current user's cart")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        Long userId = getRequiredUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
