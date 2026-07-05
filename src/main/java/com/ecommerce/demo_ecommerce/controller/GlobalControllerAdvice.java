package com.ecommerce.demo_ecommerce.controller;

import com.ecommerce.demo_ecommerce.entity.User;
import com.ecommerce.demo_ecommerce.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    public GlobalControllerAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void addLoggedInUser(Authentication authentication, Model model) {

        if (authentication != null && authentication.isAuthenticated()) {

            User user = userRepository
                    .findByEmail(authentication.getName())
                    .orElse(null);

            if (user != null) {
                model.addAttribute("loggedUser", user);
            }
        }
    }
}