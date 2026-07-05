package com.ecommerce.demo_ecommerce.service;

import com.ecommerce.demo_ecommerce.entity.Product;
import com.ecommerce.demo_ecommerce.entity.Review;
import com.ecommerce.demo_ecommerce.entity.User;
import com.ecommerce.demo_ecommerce.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<Review> getReviewsByProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }

    public void saveReview(Product product, User user, int rating, String comment) {
        Review review = new Review();

        review.setProduct(product);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);

        reviewRepository.save(review);
    }

    public double getAverageRating(Product product) {
        List<Review> reviews = reviewRepository.findByProduct(product);

        if (reviews.isEmpty()) {
            return 0;
        }

        return reviews.stream()
            .filter(Objects::nonNull)
            .mapToInt(r -> r.getRating())
            .average()
            .orElse(0);
    }

    public long getReviewCount(Product product) {
    return reviewRepository.countByProduct(product);
}


}