package com.ecommerce.demo_ecommerce.repository;

import com.ecommerce.demo_ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String nameKeyword,
            String descriptionKeyword
    );

    List<Product> findByCategoryIgnoreCase(String category);
    
    List<Product> findTop4ByCategoryAndIdNot(String category, Long id);

    

    
}