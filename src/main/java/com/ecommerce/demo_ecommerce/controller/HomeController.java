package com.ecommerce.demo_ecommerce.controller;

import com.ecommerce.demo_ecommerce.entity.Product;
import com.ecommerce.demo_ecommerce.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.ecommerce.demo_ecommerce.entity.User;
import com.ecommerce.demo_ecommerce.repository.UserRepository;
import com.ecommerce.demo_ecommerce.service.ReviewService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;

//netstat -ano | findstr :8080 to find port used
//taskkill /PID <PID#> /F to kill the process using the port
//.\mvnw.cmd clean spring-boot:run
//.\mvnw.cmd clean compile



@Controller
public class HomeController {

    private final ProductService productService;
    private final ReviewService reviewService;
     private final UserRepository userRepository;

    public HomeController(ProductService productService,
                      ReviewService reviewService,
                      UserRepository userRepository) {
    this.productService = productService;
    this.reviewService = reviewService;
    this.userRepository = userRepository;
}

  @GetMapping("/")
public String home(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        Model model) {

    List<Product> products;

    if (keyword != null && !keyword.isBlank()) {
        products = productService.searchProducts(keyword);
        model.addAttribute("keyword", keyword);
    } else if (category != null && !category.isBlank()) {
        products = productService.getProductsByCategory(category);
        model.addAttribute("selectedCategory", category);
    } else {
        products = productService.getAllProducts();
    }

    model.addAttribute("products", products);

    Map<Long, Double> ratingMap = new HashMap<>();
    Map<Long, Long> reviewCountMap = new HashMap<>();

    for (Product product : products) {
        ratingMap.put(product.getId(), reviewService.getAverageRating(product));
        reviewCountMap.put(product.getId(), reviewService.getReviewCount(product));
    }

    model.addAttribute("ratingMap", ratingMap);
    model.addAttribute("reviewCountMap", reviewCountMap);

    return "index";
}
    

    @GetMapping("/product/{id}")
public String productDetails(@PathVariable Long id, Model model) {

    Product product = productService.getProductById(id);

    model.addAttribute("product", product);
    model.addAttribute("relatedProducts", productService.getRelatedProducts(product));
    model.addAttribute("reviews", reviewService.getReviewsByProduct(product));
    model.addAttribute("averageRating", reviewService.getAverageRating(product));

    return "product-details";
}


           @PostMapping("/product/{id}/review")
public String submitReview(@PathVariable Long id,
                           int rating,
                           String comment,
                           Authentication authentication) {

    Product product = productService.getProductById(id);

    User user = userRepository.findByEmail(authentication.getName()).orElse(null);

    if (product != null && user != null) {
        reviewService.saveReview(product, user, rating, comment);
    }

    return "redirect:/product/" + id;
}
          
           
}