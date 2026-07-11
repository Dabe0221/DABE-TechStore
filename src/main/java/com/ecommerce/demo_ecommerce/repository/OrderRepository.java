package com.ecommerce.demo_ecommerce.repository;

import com.ecommerce.demo_ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByEmailOrderByOrderDateDesc(String email);

    @Query("""
        SELECT o
        FROM Order o
        WHERE LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(o.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY o.orderDate DESC
    """)
    List<Order> searchOrders(@Param("keyword") String keyword);

    @Query("""
        SELECT o
        FROM Order o
        WHERE (:keyword = '' OR
               LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(o.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:orderStatus = '' OR o.status = :orderStatus)
          AND (:paymentStatus = '' OR o.paymentStatus = :paymentStatus)
        ORDER BY o.orderDate DESC
    """)
    List<Order> filterOrders(
            @Param("keyword") String keyword,
            @Param("orderStatus") String orderStatus,
            @Param("paymentStatus") String paymentStatus
    );
}