package com.petstore.inventory.service;

import com.petstore.common.exception.BadRequestException;
import com.petstore.common.exception.InsufficientStockException;
import com.petstore.common.exception.ResourceNotFoundException;
import com.petstore.product.entity.Product;
import com.petstore.product.entity.ProductStatus;
import com.petstore.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Atomically validates and deducts inventory using a pessimistic write lock.
     * Prevents race conditions during concurrent checkouts.
     */
    @Transactional
    public Product lockAndDeductStock(Long productId, int quantityToDeduct) {
        if (quantityToDeduct <= 0) {
            throw new BadRequestException("Quantity to deduct must be greater than zero");
        }

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Product is currently inactive: " + product.getName());
        }

        if (product.getStockQuantity() < quantityToDeduct) {
            logger.warn("Insufficient stock for product {}: requested {}, available {}",
                    productId, quantityToDeduct, product.getStockQuantity());
            throw new InsufficientStockException(productId, quantityToDeduct, product.getStockQuantity());
        }

        int newStock = product.getStockQuantity() - quantityToDeduct;
        product.setStockQuantity(newStock);
        Product updatedProduct = productRepository.save(product);

        logger.info("Stock deducted for product id {}. New stock: {}", productId, newStock);
        return updatedProduct;
    }

    /**
     * Restores inventory when an order is cancelled.
     */
    @Transactional
    public void restoreStock(Long productId, int quantityToRestore) {
        if (quantityToRestore <= 0) return;

        productRepository.findByIdForUpdate(productId).ifPresent(product -> {
            int newStock = product.getStockQuantity() + quantityToRestore;
            product.setStockQuantity(newStock);
            productRepository.save(product);
            logger.info("Stock restored for product id {}. New stock: {}", productId, newStock);
        });
    }

    /**
     * Non-locking check of available stock.
     */
    @Transactional(readOnly = true)
    public void validateStockAvailability(Long productId, int requestedQuantity) {
        Product product = productRepository.findByIdAndStatus(productId, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getStockQuantity() < requestedQuantity) {
            throw new InsufficientStockException(productId, requestedQuantity, product.getStockQuantity());
        }
    }
}
