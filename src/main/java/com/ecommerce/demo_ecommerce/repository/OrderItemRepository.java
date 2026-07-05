package com.ecommerce.demo_ecommerce.repository;

import com.ecommerce.demo_ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT i.productName, SUM(i.quantity) " +
           "FROM OrderItem i " +
           "GROUP BY i.productName " +
           "ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findTopSellingProducts();
}