package com.ecommerce.demo_ecommerce.service;

import com.ecommerce.demo_ecommerce.entity.Product;
import com.ecommerce.demo_ecommerce.entity.User;
import com.ecommerce.demo_ecommerce.entity.Wishlist;
import com.ecommerce.demo_ecommerce.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    public List<Wishlist> getWishlistByUser(User user) {
        return wishlistRepository.findByUser(user);
    }

    public void addToWishlist(User user, Product product) {
        if (wishlistRepository.findByUserAndProduct(user, product).isEmpty()) {
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setProduct(product);
            wishlist.setCreatedAt(LocalDateTime.now());
            wishlistRepository.save(wishlist);
        }
    }

   @Transactional
public void removeFromWishlist(User user, Product product) {
    wishlistRepository.deleteByUserAndProduct(user, product);
}
}