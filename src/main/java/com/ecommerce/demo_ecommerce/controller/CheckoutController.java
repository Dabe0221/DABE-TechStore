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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
@Controller
public class CheckoutController {

    private final OrderRepository orderRepository;

    public CheckoutController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
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

    List<OrderItem> orderItems = new ArrayList<>();

    for (CartItem cartItem : cart.getItems()) {
        OrderItem item = new OrderItem();

        item.setProductName(cartItem.getProduct().getName());
        item.setPrice(cartItem.getProduct().getPrice());
        item.setQuantity(cartItem.getQuantity());
        item.setSubtotal(cartItem.getSubtotal());
        item.setOrder(order);

        orderItems.add(item);
    }

    order.setItems(orderItems);

    orderRepository.save(order);

    session.removeAttribute("cart");

    return "redirect:/order-success";

    
}

@GetMapping("/order-success")
public String orderSuccess() {
    return "order-success";
}

}


