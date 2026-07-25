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
import com.ecommerce.demo_ecommerce.service.EmailService;
import com.ecommerce.demo_ecommerce.service.NotificationService;
import com.ecommerce.demo_ecommerce.service.InventoryMovementService;


import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
@Controller
public class CheckoutController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final InventoryMovementService inventoryMovementService;

   public CheckoutController(OrderRepository orderRepository,
                          ProductRepository productRepository,
                          EmailService emailService,
                          NotificationService notificationService,
                          InventoryMovementService inventoryMovementService) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.emailService = emailService;
    this.notificationService = notificationService;
    this.inventoryMovementService = inventoryMovementService;
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

List<Product> purchasedProducts = new ArrayList<>();
List<Integer> stockBeforeList = new ArrayList<>();
List<Integer> stockAfterList = new ArrayList<>();
List<Integer> quantityList = new ArrayList<>();





    for (CartItem cartItem : cart.getItems()) {
    
    Long productId = null;
    if (cartItem.getProduct() != null) {
        productId = cartItem.getProduct().getId();
    }

    if (productId == null) {
    
        continue;
    }

    Product product = productRepository.findById(productId)
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

  int stockBefore = product.getStock();
int stockAfter = stockBefore - cartItem.getQuantity();

product.setStock(stockAfter);
productRepository.save(product);


purchasedProducts.add(product);
stockBeforeList.add(stockBefore);
stockAfterList.add(stockAfter);
quantityList.add(cartItem.getQuantity());


if (stockBefore > 0 && stockAfter == 0) {

    notificationService.createNotification(
            "OUT_OF_STOCK",
            product.getName() + " is now out of stock.",
            "/admin/products"
    );

// Notify only when stock first crosses from above 5 to 5 or below
} else if (stockBefore > 5 && stockAfter <= 5) {

    notificationService.createNotification(
            "LOW_STOCK",
            "Low stock warning: "
                    + product.getName()
                    + " has only "
                    + stockAfter
                    + " left.",
            "/admin/products"
    );
}

    orderItems.add(item);
}
    order.setItems(orderItems);


    Order savedOrder = orderRepository.save(order);


    for (int i = 0; i < purchasedProducts.size(); i++) {

    Product purchasedProduct = purchasedProducts.get(i);

    inventoryMovementService.recordMovement(
            purchasedProduct,
            "SALE",
            -quantityList.get(i),
            stockBeforeList.get(i),
            stockAfterList.get(i),
            savedOrder.getId(),
            order.getEmail(),
            "Sold through customer checkout"
    );
}

notificationService.createNotification(
        "NEW_ORDER",
        "New order received: Order #" + savedOrder.getId(),
        "/admin/orders"
);

    
if ("Cash on Delivery".equalsIgnoreCase(order.getPaymentMethod())) {

    emailService.sendOrderConfirmation(
            order.getEmail(),
            order.getId(),
            order.getCustomerName(),
            order.getPaymentMethod(),
            order.getPaymentStatus(),
            order.getStatus()
    );

    session.removeAttribute("cart");

    return "redirect:/order-success";
}

session.removeAttribute("cart");


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

    if ("Pending".equals(order.getStatus())) {
        order.setStatus("Processing");
    }



    orderRepository.save(order);

    emailService.sendOrderConfirmation(
        order.getEmail(),
        order.getId(),
        order.getCustomerName(),
        order.getPaymentMethod(),
        order.getPaymentStatus(),
        order.getStatus()
);

    return "redirect:/payment/" + id + "/success";
}

@GetMapping("/payment/{id}/success")
public String paymentSuccess(@PathVariable Long id, Model model) {

    Order order = orderRepository.findById(id).orElse(null);

    if (order == null) {
        return "redirect:/";
    }

    model.addAttribute("order", order);

    return "payment-success";
}

}


