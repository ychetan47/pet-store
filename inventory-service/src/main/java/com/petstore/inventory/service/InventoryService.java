package com.petstore.inventory.service;

import com.petstore.inventory.dto.InventoryItemDto;
import com.petstore.inventory.entity.InventoryItem;
import com.petstore.inventory.entity.InventoryReservation;
import com.petstore.inventory.entity.ReservationStatus;
import com.petstore.inventory.event.*;
import com.petstore.inventory.exception.BadRequestException;
import com.petstore.inventory.exception.ResourceNotFoundException;
import com.petstore.inventory.outbox.OutboxService;
import com.petstore.inventory.repository.InventoryItemRepository;
import com.petstore.inventory.repository.InventoryReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryReservationRepository reservationRepository;
    private final OutboxService outboxService;
    private final com.petstore.inventory.metrics.InventoryMetrics inventoryMetrics;

    public InventoryService(InventoryItemRepository inventoryItemRepository,
                            InventoryReservationRepository reservationRepository,
                            OutboxService outboxService,
                            com.petstore.inventory.metrics.InventoryMetrics inventoryMetrics) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.reservationRepository = reservationRepository;
        this.outboxService = outboxService;
        this.inventoryMetrics = inventoryMetrics;
    }

    /**
     * Atomically validates and reserves inventory for an order using pessimistic write locks.
     * Sorts products by ID to strictly eliminate database deadlocks under high concurrency.
     */
    @Transactional
    public boolean reserveStock(Long orderId, List<OrderCreatedEvent.OrderItemPayload> items, String correlationId) {
        if (items == null || items.isEmpty()) {
            logger.warn("No items to reserve for order {}", orderId);
            return false;
        }

        // Sort items by product ID to avoid deadlocks across concurrent threads
        List<OrderCreatedEvent.OrderItemPayload> sortedItems = new ArrayList<>(items);
        sortedItems.sort(Comparator.comparing(OrderCreatedEvent.OrderItemPayload::getProductId));

        List<InventoryReservationFailedEvent.FailedItemPayload> failedItems = new ArrayList<>();
        Map<Long, InventoryItem> lockedItems = new HashMap<>();

        // First pass: lock and check availability
        for (OrderCreatedEvent.OrderItemPayload item : sortedItems) {
            Long productId = item.getProductId();
            int requested = item.getQuantity();

            Optional<InventoryItem> optItem = inventoryItemRepository.findByProductIdForUpdate(productId);
            if (optItem.isEmpty()) {
                logger.warn("Product {} not found in inventory for order {}", productId, orderId);
                failedItems.add(new InventoryReservationFailedEvent.FailedItemPayload(productId, requested, 0));
            } else {
                InventoryItem inv = optItem.get();
                lockedItems.put(productId, inv);
                if (inv.getAvailableQuantity() < requested) {
                    logger.warn("Insufficient stock for product {}: requested {}, available {}",
                            productId, requested, inv.getAvailableQuantity());
                    failedItems.add(new InventoryReservationFailedEvent.FailedItemPayload(
                            productId, requested, inv.getAvailableQuantity()
                    ));
                }
            }
        }

        // If any item cannot be fulfilled, fail the reservation atomically
        if (!failedItems.isEmpty()) {
            logger.info("Stock reservation failed for order {}. Recording failed reservation and event.", orderId);
            for (OrderCreatedEvent.OrderItemPayload item : sortedItems) {
                reservationRepository.save(new InventoryReservation(
                        orderId, item.getProductId(), item.getQuantity(), ReservationStatus.FAILED
                ));
            }

            InventoryReservationFailedEvent failureEvent = new InventoryReservationFailedEvent(
                    orderId, "Insufficient stock for requested items", failedItems
            );
            outboxService.recordEvent(
                    "InventoryReservationFailed",
                    orderId.toString(),
                    "INVENTORY",
                    1,
                    correlationId,
                    failureEvent
            );
            inventoryMetrics.incrementReservationFailures();
            return false;
        }

        // Second pass: All items available, execute deduction and reservation
        List<InventoryReservedEvent.ReservedItemPayload> reservedList = new ArrayList<>();
        for (OrderCreatedEvent.OrderItemPayload item : sortedItems) {
            Long productId = item.getProductId();
            int qty = item.getQuantity();

            InventoryItem inv = lockedItems.get(productId);
            inv.setAvailableQuantity(inv.getAvailableQuantity() - qty);
            inv.setReservedQuantity(inv.getReservedQuantity() + qty);
            inventoryItemRepository.save(inv);

            reservationRepository.save(new InventoryReservation(
                    orderId, productId, qty, ReservationStatus.RESERVED
            ));
            reservedList.add(new InventoryReservedEvent.ReservedItemPayload(productId, qty));
        }

        logger.info("Stock successfully reserved for order {}. Enqueuing InventoryReserved event.", orderId);
        InventoryReservedEvent successEvent = new InventoryReservedEvent(orderId, reservedList);
        outboxService.recordEvent(
                "InventoryReserved",
                orderId.toString(),
                "INVENTORY",
                1,
                correlationId,
                successEvent
        );
        inventoryMetrics.incrementReservations();
        return true;
    }

    /**
     * Releases reserved stock when an order is cancelled.
     */
    @Transactional
    public void releaseStock(Long orderId, String correlationId) {
        List<InventoryReservation> activeReservations = reservationRepository.findByOrderIdAndStatus(
                orderId, ReservationStatus.RESERVED
        );

        if (activeReservations.isEmpty()) {
            logger.info("No active reservations to release for order {}", orderId);
            return;
        }

        List<InventoryReservation> sortedReservations = new ArrayList<>(activeReservations);
        sortedReservations.sort(Comparator.comparing(InventoryReservation::getProductId));
        List<InventoryReleasedEvent.ReleasedItemPayload> releasedList = new ArrayList<>();

        for (InventoryReservation res : sortedReservations) {
            inventoryItemRepository.findByProductIdForUpdate(res.getProductId()).ifPresent(inv -> {
                inv.setAvailableQuantity(inv.getAvailableQuantity() + res.getQuantity());
                inv.setReservedQuantity(Math.max(0, inv.getReservedQuantity() - res.getQuantity()));
                inventoryItemRepository.save(inv);
            });

            res.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(res);
            releasedList.add(new InventoryReleasedEvent.ReleasedItemPayload(res.getProductId(), res.getQuantity()));
        }

        logger.info("Released {} reserved items for cancelled order {}", releasedList.size(), orderId);
        InventoryReleasedEvent releaseEvent = new InventoryReleasedEvent(orderId, releasedList);
        outboxService.recordEvent(
                "InventoryReleased",
                orderId.toString(),
                "INVENTORY",
                1,
                correlationId,
                releaseEvent
        );
        inventoryMetrics.incrementReleases();
    }

    /**
     * Initializes inventory for a newly created product.
     */
    @Transactional
    public void createItem(Long productId, Integer initialStock) {
        if (inventoryItemRepository.findByProductId(productId).isPresent()) {
            logger.info("Inventory item for product {} already exists", productId);
            return;
        }

        InventoryItem item = new InventoryItem(productId, initialStock != null ? initialStock : 0);
        inventoryItemRepository.save(item);
        logger.info("Created inventory item for product {} with stock {}", productId, initialStock);
    }

    /**
     * Directly updates available stock quantity (Admin operation).
     */
    @Transactional
    public InventoryItemDto updateStock(Long productId, Integer newStock) {
        if (newStock < 0) {
            throw new BadRequestException("Stock quantity cannot be negative");
        }

        InventoryItem item = inventoryItemRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "productId", productId));

        item.setAvailableQuantity(newStock);
        InventoryItem saved = inventoryItemRepository.save(item);
        logger.info("Admin updated stock for product {} to {}", productId, newStock);
        return InventoryItemDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public InventoryItemDto getInventory(Long productId) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "productId", productId));
        return InventoryItemDto.fromEntity(item);
    }

    @Transactional(readOnly = true)
    public Page<InventoryItemDto> getAllInventory(Pageable pageable) {
        return inventoryItemRepository.findAllByOrderByProductIdAsc(pageable)
                .map(InventoryItemDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<InventoryItemDto> getInventoryBatch(List<Long> productIds) {
        return inventoryItemRepository.findAllByProductIdIn(productIds).stream()
                .map(InventoryItemDto::fromEntity)
                .collect(Collectors.toList());
    }
}
