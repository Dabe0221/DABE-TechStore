package com.ecommerce.demo_ecommerce.controller;

import com.ecommerce.demo_ecommerce.cart.ShoppingCart;
import com.ecommerce.demo_ecommerce.entity.Order;
import com.ecommerce.demo_ecommerce.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.ecommerce.demo_ecommerce.cart.CartItem;
import com.ecommerce.demo_ecommerce.entity.OrderItem;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ecommerce.demo_ecommerce.entity.Product;
import com.ecommerce.demo_ecommerce.repository.ProductRepository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
@Controller
public class CheckoutController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public CheckoutController(OrderRepository orderRepository,
                          ProductRepository productRepository) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
}
    private ShoppingCart getCart(HttpSession session) {
        return (ShoppingCart) session.getAttribute("cart");
    }

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model) {
        ShoppingCart cart = getCart(session);

        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("order", new Order());

        return "checkout";
    }


@PostMapping("/checkout/place-order")


public String placeOrder(@ModelAttribute Order order, HttpSession session) {

    ShoppingCart cart = getCart(session);
    

    if (cart == null || cart.getItems().isEmpty()) {
        return "redirect:/cart";
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    order.setEmail(auth.getName());
    order.setTotalAmount(cart.getTotal());
    order.setOrderDate(LocalDateTime.now());
    order.setStatus("Pending"); 
    String paymentMethod = order.getPaymentMethod();

if (paymentMethod == null || paymentMethod.isBlank()) {
    paymentMethod = "Cash on Delivery";
}

order.setPaymentMethod(paymentMethod);

if (paymentMethod.equalsIgnoreCase("Cash on Delivery")) {
    order.setPaymentStatus("Unpaid");
} else {
    order.setPaymentStatus("Pending Payment");
}

    

    List<OrderItem> orderItems = new ArrayList<>();

    for (CartItem cartItem : cart.getItems()) {
    Product product = productRepository.findById(cartItem.getProduct().getId())
            .orElse(null);

    if (product == null) {
        continue;
    }

    if (product.getStock() < cartItem.getQuantity()) {
        return "redirect:/cart";
    }

    OrderItem item = new OrderItem();

    item.setProductName(product.getName());
    item.setPrice(product.getPrice());
    item.setQuantity(cartItem.getQuantity());
    item.setSubtotal(cartItem.getSubtotal());
    item.setOrder(order);

    product.setStock(product.getStock() - cartItem.getQuantity());
    productRepository.save(product);

    orderItems.add(item);
}
    order.setItems(orderItems);

    orderRepository.save(order);

    session.removeAttribute("cart");

    if ("Cash on Delivery".equalsIgnoreCase(order.getPaymentMethod())) {
    return "redirect:/order-success";
}

return "redirect:/payment/" + order.getId();

    
}

@GetMapping("/order-success")
public String orderSuccess() {
    return "order-success";
}

@GetMapping("/payment/{id}")
public String paymentPage(@PathVariable Long id, Model model) {

    Order order = orderRepository.findById(id).orElse(null);

    if (order == null) {
        return "redirect:/";
    }

    model.addAttribute("order", order);

    return "payment";
}

@PostMapping("/payment/{id}/pay")
public String payOrder(@PathVariable Long id) {

    Order order = orderRepository.findById(id).orElse(null);

    if (order == null) {
        return "redirect:/";
    }

    order.setPaymentStatus("Paid");
    orderRepository.save(order);

    return "redirect:/my-orders/" + id + "/invoice";
}

}


