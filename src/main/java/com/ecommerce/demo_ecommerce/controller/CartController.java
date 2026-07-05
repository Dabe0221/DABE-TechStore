package com.ecommerce.demo_ecommerce.controller;

import com.ecommerce.demo_ecommerce.cart.CartItem;
import com.ecommerce.demo_ecommerce.cart.ShoppingCart;
import com.ecommerce.demo_ecommerce.entity.Product;
import com.ecommerce.demo_ecommerce.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

    private final ProductService productService;

    public CartController(ProductService productService) {
        this.productService = productService;
    }

    private ShoppingCart getCart(HttpSession session) {
        ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");

        if (cart == null) {
            cart = new ShoppingCart();
            session.setAttribute("cart", cart);
        }

        return cart;
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        ShoppingCart cart = getCart(session);
        model.addAttribute("cart", cart);
        return "cart";
    }

    @GetMapping("/cart/add/{id}")
public String addToCart(@PathVariable Long id,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

    Product product = productService.getProductById(id);

    if (product == null || product.getStock() <= 0) {
        redirectAttributes.addFlashAttribute("error", "Product is out of stock.");
        return "redirect:/";
    }

    ShoppingCart cart = getCart(session);

    int quantityInCart = cart.getQuantityByProductId(id);

    if (quantityInCart >= product.getStock()) {
        redirectAttributes.addFlashAttribute(
                "error",
                "Only " + product.getStock() + " item(s) available in stock.");
        return "redirect:/cart";
    }

    cart.addProduct(product);

    return "redirect:/cart";
}

    @GetMapping("/cart/increase/{id}")
public String increaseQuantity(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

    Product product = productService.getProductById(id);
    ShoppingCart cart = getCart(session);

    int quantityInCart = cart.getQuantityByProductId(id);

    if (product == null) {
        redirectAttributes.addFlashAttribute("error", "Product not found.");
        return "redirect:/cart";
    }

    if (quantityInCart >= product.getStock()) {
        redirectAttributes.addFlashAttribute(
                "error",
                "Only " + product.getStock() + " item(s) available in stock.");
        return "redirect:/cart";
    }

    cart.increaseProduct(id);

    return "redirect:/cart";
}

    @GetMapping("/cart/decrease/{id}")
    public String decreaseQuantity(@PathVariable Long id, HttpSession session) {
        getCart(session).decreaseProduct(id);
        return "redirect:/cart";
    }

    @GetMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id, HttpSession session) {
        getCart(session).removeProduct(id);
        return "redirect:/cart";
    }

    @PostMapping("/cart/add/{id}")
public String addToCartWithQuantity(@PathVariable Long id,
                                    @RequestParam int quantity,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

    Product product = productService.getProductById(id);

    if (product == null || product.getStock() <= 0) {
        redirectAttributes.addFlashAttribute("error", "Product is out of stock.");
        return "redirect:/";
    }

    ShoppingCart cart = getCart(session);

    int quantityInCart = cart.getQuantityByProductId(id);

    if (quantityInCart + quantity > product.getStock()) {
        redirectAttributes.addFlashAttribute(
                "error",
                "Only " + product.getStock() + " item(s) available in stock.");
        return "redirect:/product/" + id;
    }

    for (int i = 0; i < quantity; i++) {
        cart.addProduct(product);
    }

    return "redirect:/cart";
}

}