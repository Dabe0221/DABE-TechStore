package com.ecommerce.demo_ecommerce.repository;

import com.ecommerce.demo_ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query(value = """
            SELECT product_name, SUM(quantity) AS sold
            FROM order_items
            GROUP BY product_name
            ORDER BY sold DESC
            """, nativeQuery = true)
    List<Object[]> findTopSellingProducts();

     @Query("""
    SELECT COALESCE(SUM(i.quantity), 0)
    FROM OrderItem i
    WHERE i.order.orderDate BETWEEN :from AND :to
      AND LOWER(i.order.status) <> 'cancelled'
""")
Long countProductsSoldBetweenDates(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
);


}