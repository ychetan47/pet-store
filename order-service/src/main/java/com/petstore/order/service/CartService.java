package com.petstore.order.service;

import com.petstore.order.client.ProductCatalogClient;
import com.petstore.order.dto.*;
import com.petstore.order.entity.Cart;
import com.petstore.order.entity.CartItem;
import com.petstore.order.exception.BadRequestException;
import com.petstore.order.exception.InsufficientStockException;
import com.petstore.order.exception.ResourceNotFoundException;
import com.petstore.order.repository.CartItemRepository;
import com.petstore.order.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductCatalogClient productCatalogClient;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductCatalogClient productCatalogClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productCatalogClient = productCatalogClient;
    }

    @Transactional
    public Cart getOrCreateCartEntity(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart(userId);
                    return cartRepository.save(newCart);
                });
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            CartResponse empty = new CartResponse();
            empty.setUserId(userId);
            return empty;
        }

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        if (request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        ProductDto product = productCatalogClient.getProduct(request.getProductId());
        if (!product.isActive()) {
            throw new BadRequestException("Product is currently unavailable: " + product.getName());
        }

        Cart cart = getOrCreateCartEntity(userId);
        Optional<CartItem> existingOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId());

        int targetQuantity = request.getQuantity();
        CartItem itemToSave;

        if (existingOpt.isPresent()) {
            CartItem existing = existingOpt.get();
            targetQuantity = existing.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < targetQuantity) {
                throw new InsufficientStockException(product.getId(), targetQuantity, product.getStockQuantity());
            }
            existing.setQuantity(targetQuantity);
            itemToSave = existing;
        } else {
            if (product.getStockQuantity() < targetQuantity) {
                throw new InsufficientStockException(product.getId(), targetQuantity, product.getStockQuantity());
            }
            itemToSave = new CartItem(cart, request.getProductId(), targetQuantity);
            cart.getItems().add(itemToSave);
        }

        cartItemRepository.save(itemToSave);
        logger.info("Added product {} (qty: {}) to cart of user {}", request.getProductId(), targetQuantity, userId);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItemQuantity(Long userId, Long cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }

        Cart cart = getOrCreateCartEntity(userId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to user's cart");
        }

        ProductDto product = productCatalogClient.getProduct(item.getProductId());
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(product.getId(), quantity, product.getStockQuantity());
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        logger.info("Updated cart item {} to quantity {} for user {}", cartItemId, quantity, userId);

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeFromCart(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCartEntity(userId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to user's cart");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        logger.info("Removed cart item {} from cart of user {}", cartItemId, userId);

        return buildCartResponse(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartItemRepository.deleteByCartId(cart.getId());
            logger.info("Cleared cart for user {}", userId);
        });
    }

    private CartResponse buildCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUserId());

        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartItem item : cart.getItems()) {
            try {
                ProductDto product = productCatalogClient.getProduct(item.getProductId());
                BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                subtotal = subtotal.add(lineTotal);
                totalItems += item.getQuantity();

                boolean inStock = product.isActive() && product.getStockQuantity() >= item.getQuantity();
                itemResponses.add(new CartItemResponse(
                        item.getId(),
                        product.getId(),
                        product.getName(),
                        product.getSlug(),
                        product.getPrice(),
                        item.getQuantity(),
                        lineTotal,
                        product.getPrimaryImageUrl(),
                        inStock,
                        product.getStockQuantity()
                ));
            } catch (Exception ex) {
                logger.warn("Could not retrieve live product details for item in cart: {}", item.getProductId(), ex);
                itemResponses.add(new CartItemResponse(
                        item.getId(),
                        item.getProductId(),
                        "Unavailable Product",
                        "",
                        BigDecimal.ZERO,
                        item.getQuantity(),
                        BigDecimal.ZERO,
                        null,
                        false,
                        0
                ));
            }
        }

        response.setItems(itemResponses);
        response.setTotalItems(totalItems);
        response.setSubtotal(subtotal);
        response.setDeliveryFee(BigDecimal.ZERO); // Free delivery in V1/V2
        response.setTotalAmount(subtotal);
        return response;
    }
}
