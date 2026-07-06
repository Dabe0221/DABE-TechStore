package com.ecommerce.demo_ecommerce.repository;

import com.ecommerce.demo_ecommerce.entity.Product;
import com.ecommerce.demo_ecommerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProduct(Product product);

    long countByProduct(Product product);

    @Query("""
           SELECT r.product.name, AVG(r.rating), COUNT(r)
           FROM Review r
           GROUP BY r.product.name
           ORDER BY AVG(r.rating) DESC
           """)
    List<Object[]> findHighestRatedProducts();
}