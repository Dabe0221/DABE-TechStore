package com.ecommerce.demo_ecommerce.repository;

import com.ecommerce.demo_ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query(value = """
            SELECT product_name, SUM(quantity) AS sold
            FROM order_items
            GROUP BY product_name
            ORDER BY sold DESC
            """, nativeQuery = true)
    List<Object[]> findTopSellingProducts();
}