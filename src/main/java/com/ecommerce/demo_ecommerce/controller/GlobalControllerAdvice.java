package com.ecommerce.demo_ecommerce.controller;

import com.ecommerce.demo_ecommerce.cart.ShoppingCart;
import com.ecommerce.demo_ecommerce.entity.User;
import com.ecommerce.demo_ecommerce.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    public GlobalControllerAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void addGlobalData(
            Authentication authentication,
            HttpSession session,
            Model model) {

        // Logged-in user
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            User user = userRepository
                    .findByEmail(authentication.getName())
                    .orElse(null);

            if (user != null) {
                model.addAttribute("loggedUser", user);
            }
        }

        // Cart quantity
        ShoppingCart cart =
                (ShoppingCart) session.getAttribute("cart");

        int cartCount = 0;

        if (cart != null) {
            cartCount = cart.getTotalQuantity();
        }

        model.addAttribute("cartCount", cartCount);
    }
}