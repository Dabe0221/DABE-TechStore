package com.ecommerce.demo_ecommerce.service;

import com.ecommerce.demo_ecommerce.entity.InventoryMovement;
import com.ecommerce.demo_ecommerce.entity.Product;
import com.ecommerce.demo_ecommerce.repository.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class InventoryMovementService {

    private final InventoryMovementRepository inventoryMovementRepository;

    public InventoryMovementService(
            InventoryMovementRepository inventoryMovementRepository) {

        this.inventoryMovementRepository =
                inventoryMovementRepository;
    }

    public InventoryMovement recordMovement(
            Product product,
            String movementType,
            int quantityChanged,
            int stockBefore,
            int stockAfter,
            Long orderId,
            String performedBy,
            String notes) {

        InventoryMovement movement =
                new InventoryMovement();

        movement.setProductId(product.getId());
        movement.setProductName(product.getName());
        movement.setMovementType(movementType);
        movement.setQuantityChanged(quantityChanged);
        movement.setStockBefore(stockBefore);
        movement.setStockAfter(stockAfter);
        movement.setOrderId(orderId);
        movement.setPerformedBy(performedBy);
        movement.setNotes(notes);

        return inventoryMovementRepository.save(movement);
    }

    public List<InventoryMovement> getRecentMovements() {
        return inventoryMovementRepository
                .findTop20ByOrderByCreatedAtDesc();
    }

    public List<InventoryMovement> getMovementsByProduct(
            Long productId) {

        return inventoryMovementRepository
                .findByProductIdOrderByCreatedAtDesc(productId);
    }

    public List<InventoryMovement> getMovementsByType(
            String movementType) {

        return inventoryMovementRepository
                .findByMovementTypeOrderByCreatedAtDesc(
                        movementType
                );
    }

public Page<InventoryMovement> getInventoryMovements(
        String keyword,
        String movementType,
        Pageable pageable) {

    boolean hasKeyword =
            keyword != null && !keyword.isBlank();

    boolean hasType =
            movementType != null && !movementType.isBlank();

    if (hasKeyword && hasType) {
        return inventoryMovementRepository
                .findByProductNameContainingIgnoreCaseAndMovementTypeOrderByCreatedAtDesc(
                        keyword,
                        movementType,
                        pageable
                );
    }

    if (hasKeyword) {
        return inventoryMovementRepository
                .findByProductNameContainingIgnoreCaseOrderByCreatedAtDesc(
                        keyword,
                        pageable
                );
    }

    if (hasType) {
        return inventoryMovementRepository
                .findByMovementTypeOrderByCreatedAtDesc(
                        movementType,
                        pageable
                );
    }

    return inventoryMovementRepository
            .findAllByOrderByCreatedAtDesc(pageable);
}

}