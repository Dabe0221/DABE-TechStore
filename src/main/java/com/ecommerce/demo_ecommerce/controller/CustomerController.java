package com.ecommerce.demo_ecommerce.controller;
import com.ecommerce.demo_ecommerce.entity.User;
import com.ecommerce.demo_ecommerce.repository.OrderRepository;
import com.ecommerce.demo_ecommerce.repository.UserRepository;
import com.ecommerce.demo_ecommerce.entity.Order;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import com.ecommerce.demo_ecommerce.entity.Product;
import com.ecommerce.demo_ecommerce.service.ProductService;
import com.ecommerce.demo_ecommerce.service.WishlistService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;





@Controller
public class CustomerController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final WishlistService wishlistService;
    public CustomerController(OrderRepository orderRepository,
                          UserRepository userRepository,
                          WishlistService wishlistService,
                          ProductService productService) {

    this.orderRepository = orderRepository;
    this.userRepository = userRepository;
    this.wishlistService = wishlistService;
    this.productService = productService;
}

    @GetMapping("/my-orders")
    public String myOrders(Authentication authentication, Model model) {
        String email = authentication.getName();

        model.addAttribute("orders",
                orderRepository.findByEmailOrderByOrderDateDesc(email));

        return "my-orders";
    }

    @GetMapping("/my-profile")
    public String myProfile(Authentication authentication, Model model) {
        String email = authentication.getName();

        model.addAttribute("user",
                userRepository.findByEmail(email).orElse(null));

        return "my-profile";
    }

    @PostMapping("/my-profile/update")
public String updateProfile(@ModelAttribute User formUser,
                            Authentication authentication) {

    String email = authentication.getName();

    User user = userRepository.findByEmail(email).orElse(null);

    if (user != null) {
        user.setName(formUser.getName());
        user.setPhone(formUser.getPhone());
        user.setAddress(formUser.getAddress());

        userRepository.save(user);
    }

    return "redirect:/my-profile";
}
     @GetMapping("/my-orders/{id}")
public String myOrderDetails(@PathVariable @NonNull Long id,
                           Authentication authentication,
                             Model model) {

    String email = authentication.getName();

    Order order = orderRepository.findById(id).orElse(null);

    if (order == null || !order.getEmail().equals(email)) {
        return "redirect:/my-orders";
    }

    model.addAttribute("order", order);

    return "my-order-details";

}
           @PostMapping("/wishlist/add/{id}")
public String addToWishlist(@PathVariable Long id,
                            Authentication authentication) {

    String email = authentication.getName();

    User user = userRepository.findByEmail(email).orElse(null);

    Product product = productService.getProductById(id);

    if (user != null && product != null) {
        wishlistService.addToWishlist(user, product);
    }

    return "redirect:/product/" + id;
}          
    @PostMapping("/wishlist/remove/{id}")
public String removeFromWishlist(@PathVariable Long id,
                                 Authentication authentication) {

    String email = authentication.getName();

    User user = userRepository.findByEmail(email).orElse(null);

    Product product = productService.getProductById(id);

    if (user != null && product != null) {
        wishlistService.removeFromWishlist(user, product);
    }

    return "redirect:/wishlist";
}
   @GetMapping("/wishlist")
public String myWishlist(Authentication authentication, Model model) {

    String email = authentication.getName();

    User user = userRepository.findByEmail(email).orElse(null);

    if (user == null) {
        return "redirect:/";
    }

    model.addAttribute("wishlistItems", wishlistService.getWishlistByUser(user));

    return "wishlist";
}
 @GetMapping("/my-orders/{id}/invoice")
public String myOrderInvoice(@PathVariable Long id,
                             Authentication authentication,
                             Model model) {

    String email = authentication.getName();

    Order order = orderRepository.findById(id).orElse(null);

    if (order == null || !order.getEmail().equals(email)) {
        return "redirect:/my-orders";
    }

    model.addAttribute("order", order);

    return "invoice";
}

}
