package com.ecommerce.demo_ecommerce.service;

import com.ecommerce.demo_ecommerce.entity.Product;
import com.ecommerce.demo_ecommerce.repository.ProductRepository;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

   public Product getProductById(Long id) {
    if (id == null) {
        return null;
    }
    return productRepository.findById(id).orElse(null);
}

public List<Product> searchProducts(String keyword) {
    return productRepository
            .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    keyword,
                    keyword
            );
}
          public List<Product> getProductsByCategory(String category) {
    return productRepository.findByCategoryIgnoreCase(category);
}
   public void saveProduct(@NonNull    Product product) {
    productRepository.save(product);
}
public void deleteProduct(@NonNull Long id) {
    productRepository.deleteById(id);
}
 public List<Product> getRelatedProducts(Product product) {
    return productRepository.findTop4ByCategoryAndIdNot(
            product.getCategory(),
            product.getId()
    );
}

}