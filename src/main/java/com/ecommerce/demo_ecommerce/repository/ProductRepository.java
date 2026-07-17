package com.ecommerce.demo_ecommerce.repository;

import com.ecommerce.demo_ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name,
            String description
    );

    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findTop4ByCategoryAndIdNot(
            String category,
            Long id
    );

    @Query("""
        SELECT p
        FROM Product p
        WHERE (:keyword = '' OR
               LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:category = '' OR LOWER(p.category) = LOWER(:category))
          AND (
                :stockFilter = ''
                OR (:stockFilter = 'IN_STOCK' AND p.stock > 5)
                OR (:stockFilter = 'LOW_STOCK' AND p.stock BETWEEN 1 AND 5)
                OR (:stockFilter = 'OUT_OF_STOCK' AND p.stock = 0)
              )
        """)
    Page<Product> filterProducts(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("stockFilter") String stockFilter,
            Pageable pageable
    );
}