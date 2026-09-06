package com.petstore.cart.service;

import com.petstore.auth.entity.User;
import com.petstore.auth.repository.UserRepository;
import com.petstore.cart.dto.*;
import com.petstore.cart.entity.Cart;
import com.petstore.cart.entity.CartItem;
import com.petstore.cart.repository.CartItemRepository;
import com.petstore.cart.repository.CartRepository;
import com.petstore.common.exception.BadRequestException;
import com.petstore.common.exception.InsufficientStockException;
import com.petstore.common.exception.ResourceNotFoundException;
import com.petstore.common.exception.UnauthorizedException;
import com.petstore.product.entity.Product;
import com.petstore.product.entity.ProductStatus;
import com.petstore.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            Cart cart = new Cart(user);
            return cartRepository.save(cart);
        });
    }

    @Transactional
    public CartDto getCustomerCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemDto> itemDtos = items.stream()
                .map(CartItemDto::fromEntity)
                .collect(Collectors.toList());
        return CartDto.create(cart.getId(), itemDtos);
    }

    @Transactional
    public CartDto addToCart(Long userId, AddToCartRequest request) {
        if (request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }

        Product product = productRepository.findByIdAndStatus(request.getProductId(), ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        Cart cart = getOrCreateCart(userId);

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        int totalRequestedQuantity = request.getQuantity();
        if (existingItemOpt.isPresent()) {
            totalRequestedQuantity += existingItemOpt.get().getQuantity();
        }

        if (totalRequestedQuantity > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getId(), totalRequestedQuantity, product.getStockQuantity());
        }

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(totalRequestedQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem(cart, product, request.getQuantity());
            cartItemRepository.save(newItem);
        }

        return getCustomerCart(userId);
    }

    @Transactional
    public CartDto updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        if (request.getQuantity() <= 0) {
            return removeCartItem(userId, cartItemId);
        }

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to modify this cart item");
        }

        Product product = item.getProduct();
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Product is no longer active");
        }

        if (request.getQuantity() > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getId(), request.getQuantity(), product.getStockQuantity());
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return getCustomerCart(userId);
    }

    @Transactional
    public CartDto removeCartItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to remove this cart item");
        }

        cartItemRepository.delete(item);
        return getCustomerCart(userId);
    }

    @Transactional
    public CartDto clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
        return getCustomerCart(userId);
    }
}
