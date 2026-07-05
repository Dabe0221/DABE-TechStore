package com.ecommerce.demo_ecommerce.repository;

import com.ecommerce.demo_ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByEmailOrderByOrderDateDesc(String email);

}