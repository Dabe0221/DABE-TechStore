package com.ecommerce.demo_ecommerce.controller;

import com.ecommerce.demo_ecommerce.entity.User;
import com.ecommerce.demo_ecommerce.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

 @PostMapping("/register")
public String register(
        User user,
        @RequestParam String confirmPassword,
        RedirectAttributes redirectAttributes) {

    String rawPassword = user.getPassword();

    if (rawPassword == null ||
        rawPassword.isBlank() ||
        rawPassword.length() < 8) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Password must be at least 8 characters long."
        );

        return "redirect:/register";
    }

    if (!rawPassword.equals(confirmPassword)) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "The passwords do not match."
        );

        return "redirect:/register";
    }

    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setRole("CUSTOMER");

    userRepository.save(user);

    redirectAttributes.addFlashAttribute(
            "successMessage",
            "Your account was created successfully. You can now sign in."
    );

    return "redirect:/login";
}
    
           @GetMapping("/login")
            public String loginPage() {
           return "login";
}

}