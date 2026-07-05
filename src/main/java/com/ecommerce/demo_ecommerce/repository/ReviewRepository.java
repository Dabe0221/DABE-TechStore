package com.ecommerce.demo_ecommerce.repository;

import com.ecommerce.demo_ecommerce.entity.Product;
import com.ecommerce.demo_ecommerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProduct(Product product);
    
    long countByProduct(Product product);
   
}




