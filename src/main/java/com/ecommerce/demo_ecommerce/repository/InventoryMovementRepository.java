package com.ecommerce.demo_ecommerce.repository;

import com.ecommerce.demo_ecommerce.entity.InventoryMovement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovement, Long> {

    /*
     * Original methods used by the existing service
     */

    List<InventoryMovement> findTop20ByOrderByCreatedAtDesc();

    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(
            Long productId
    );

    List<InventoryMovement> findByMovementTypeOrderByCreatedAtDesc(
            String movementType
    );


    /*
     * Paginated methods used by the admin inventory page
     */

    Page<InventoryMovement> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );

    Page<InventoryMovement>
    findByProductNameContainingIgnoreCaseOrderByCreatedAtDesc(
            String keyword,
            Pageable pageable
    );

    Page<InventoryMovement>
    findByMovementTypeOrderByCreatedAtDesc(
            String movementType,
            Pageable pageable
    );

    Page<InventoryMovement>
    findByProductNameContainingIgnoreCaseAndMovementTypeOrderByCreatedAtDesc(
            String keyword,
            String movementType,
            Pageable pageable
    );
}