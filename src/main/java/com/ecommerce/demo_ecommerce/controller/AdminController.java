package com.ecommerce.demo_ecommerce.controller;

import com.ecommerce.demo_ecommerce.entity.Product;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.ecommerce.demo_ecommerce.service.ProductService;
import com.ecommerce.demo_ecommerce.repository.OrderItemRepository;
import com.ecommerce.demo_ecommerce.service.EmailService;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.ecommerce.demo_ecommerce.repository.OrderRepository;
import com.ecommerce.demo_ecommerce.repository.UserRepository;
import com.ecommerce.demo_ecommerce.entity.Order;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import com.ecommerce.demo_ecommerce.entity.OrderItem;
import java.util.stream.Collectors;
import com.ecommerce.demo_ecommerce.repository.ReviewRepository;









@Controller

public class AdminController {
    private final UserRepository userRepository;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;
    private final EmailService emailService;

    
@GetMapping("/admin/orders")
public String adminOrders(
        @RequestParam(name = "keyword", defaultValue = "") String keyword,
        @RequestParam(name = "orderStatus", defaultValue = "") String orderStatus,
        @RequestParam(name = "paymentStatus", defaultValue = "") String paymentStatus,
        Model model) {

    String cleanKeyword = keyword.trim();

    var orders = orderRepository.filterOrders(
            cleanKeyword,
            orderStatus,
            paymentStatus
    );

    model.addAttribute("orders", orders);
    model.addAttribute("keyword", cleanKeyword);
    model.addAttribute("selectedOrderStatus", orderStatus);
    model.addAttribute("selectedPaymentStatus", paymentStatus);

    return "admin-orders";
}
   public AdminController(ProductService productService,
                       OrderRepository orderRepository,
                       UserRepository userRepository,
                       OrderItemRepository orderItemRepository,
                       ReviewRepository reviewRepository,
                       EmailService emailService) {
    this.productService = productService;
    this.orderRepository = orderRepository;
    this.userRepository = userRepository;
    this.orderItemRepository = orderItemRepository;
    this.reviewRepository = reviewRepository;
    this.emailService = emailService;
}

    

    @GetMapping("/admin/products")
    public String adminProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin-products";
    }

    @GetMapping("/admin/add-product")
public String showAddProductForm(Model model) {
    model.addAttribute("product", new Product());
    return "admin-product-form";
}

@PostMapping("/admin/products/save")
public String saveProduct(
        @ModelAttribute @NonNull Product product,
        @RequestParam("imageFile") MultipartFile imageFile
) throws IOException {

    // Editing an existing product
    if (product.getId() != null) {
        Product existingProduct = productService.getProductById(product.getId());

        if (existingProduct != null) {
            // Keep the current image unless a new one is uploaded
            product.setImageUrl(existingProduct.getImageUrl());
        }
    }

    // Replace the existing image only when a new file is selected
    if (imageFile != null && !imageFile.isEmpty()) {

        String uploadDir =
                System.getProperty("user.dir")
                + "/src/main/resources/static/uploads/";

        String originalFileName = imageFile.getOriginalFilename();

        String safeFileName =
                originalFileName == null
                ? "product-image"
                : originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        String fileName =
                System.currentTimeMillis() + "_" + safeFileName;

        File uploadFolder = new File(uploadDir);

        if (!uploadFolder.exists() && !uploadFolder.mkdirs()) {
            throw new IOException(
                    "Could not create upload directory: " + uploadDir
            );
        }

        File savedFile = new File(uploadFolder, fileName);
        imageFile.transferTo(savedFile);

        product.setImageUrl("/uploads/" + fileName);
    }

    productService.saveProduct(product);

    return "redirect:/admin/products";
}

@GetMapping("/admin/products/edit/{id}")
public String showEditProductForm(@PathVariable Long id, Model model) {
    model.addAttribute("product", productService.getProductById(id));
    return "admin-product-form";
}

@GetMapping("/admin/products/delete/{id}")
public String deleteProduct(@PathVariable("id") long id) {
    productService.deleteProduct(id);
    return "redirect:/admin/products";
}
 @GetMapping("/admin/order-details")
public String viewOrderDetails(@RequestParam @NonNull Long id, Model model) {
    Order order = orderRepository.findById(id).orElse(null);

    if (order == null) {
        return "redirect:/admin/orders";
    }

    model.addAttribute("order", order);
    return "admin-order-details";
}

@PostMapping("/admin/update-order-status")
public String updateOrderStatus(
        @RequestParam @NonNull Long id,
        @RequestParam String status) {

    Order order = orderRepository.findById(id).orElse(null);

    if (order != null) {
        order.setStatus(status);
        orderRepository.save(order);
    }

    return "redirect:/admin/orders";
}

@GetMapping("/admin/dashboard")
public String adminDashboard(Model model) {

    var products = productService.getAllProducts();
    var orders = orderRepository.findAll();

    model.addAttribute("totalProducts", products.size());
    model.addAttribute("totalOrders", orders.size());

    model.addAttribute("totalRevenue",
            orders.stream()
                    .filter(order -> order.getTotalAmount() != null)
                    .filter(order -> !"Cancelled".equals(order.getStatus()))
                    .map(order -> order.getTotalAmount())
                    .reduce(java.math.BigDecimal.ZERO, (subtotal, amount) -> subtotal.add(amount)));


                    model.addAttribute("processingOrders",
        orders.stream()
                .filter(order -> "Processing".equalsIgnoreCase(order.getStatus()))
                .count());

model.addAttribute("shippedOrders",
        orders.stream()
                .filter(order -> "Shipped".equalsIgnoreCase(order.getStatus()))
                .count());

model.addAttribute("cancelledOrders",
        orders.stream()
                .filter(order -> "Cancelled".equalsIgnoreCase(order.getStatus()))
                .count());

model.addAttribute("paidOrders",
        orders.stream()
                .filter(order -> "Paid".equalsIgnoreCase(order.getPaymentStatus()))
                .count());

model.addAttribute("unpaidOrders",
        orders.stream()
                .filter(order ->
                        "Unpaid".equalsIgnoreCase(order.getPaymentStatus())
                        || "Pending Payment".equalsIgnoreCase(order.getPaymentStatus()))
                .count());

    model.addAttribute("pendingOrders",
            orders.stream()
                    .filter(order -> "Pending".equals(order.getStatus()))
                    .count());

    model.addAttribute("deliveredOrders",
            orders.stream()
                    .filter(order -> "Delivered".equals(order.getStatus()))
                    .count());

    model.addAttribute("lowStockProducts",
            products.stream()
                    .filter(product -> product.getStock() != null && product.getStock() <= 5)
                    .toList());

    model.addAttribute("recentOrders",
            orders.stream()
                    .filter(order -> order.getOrderDate() != null)
                    .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                    .limit(5)
                    .toList());

                model.addAttribute("totalCustomers", userRepository.count());

                long ordersToday = orders.stream()
        .filter(order -> order.getOrderDate() != null)
        .filter(order -> order.getOrderDate()
                .toLocalDate()
                .equals(java.time.LocalDate.now()))
        .count();

model.addAttribute("ordersToday", ordersToday);

java.time.LocalDate chartEndDate = orders.stream()
        .filter(order -> order.getOrderDate() != null)
        .map(order -> order.getOrderDate().toLocalDate())
        .max(java.util.Comparator.naturalOrder())
        .orElse(java.time.LocalDate.now());

java.util.List<String> revenueLabels = new java.util.ArrayList<>();
java.util.List<Double> revenueData = new java.util.ArrayList<>();

for (int i = 6; i >= 0; i--) {
    java.time.LocalDate date = chartEndDate.minusDays(i);

    java.math.BigDecimal dailyRevenue = orders.stream()
            .filter(order -> order.getOrderDate() != null)
            .filter(order -> order.getTotalAmount() != null)
            .filter(order -> !"Cancelled".equals(order.getStatus()))
            .filter(order -> order.getOrderDate().toLocalDate().equals(date))
            .map(order -> order.getTotalAmount())
            .reduce(java.math.BigDecimal.ZERO, (a, b) -> a.add(b));

    revenueLabels.add(date.getMonthValue() + "/" + date.getDayOfMonth());
    revenueData.add(dailyRevenue.doubleValue());
}

model.addAttribute("revenueLabels", revenueLabels);
model.addAttribute("revenueData", revenueData);
var topSellingProducts = orderItemRepository.findAll()
        .stream()
        .filter(item -> item != null && item.getProductName() != null)
        .collect(Collectors.groupingBy(
                item -> item.getProductName(),
                Collectors.summingInt(OrderItem::getQuantity)
        ))
        .entrySet()
        .stream()
        .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
        .limit(5)
        .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
        .toList();

System.out.println("===== TOP SELLING PRODUCTS =====");
topSellingProducts.forEach(item ->
        System.out.println(item[0] + " : " + item[1]));

model.addAttribute("topSellingProducts", topSellingProducts);

model.addAttribute("highestRatedProducts",
        reviewRepository.findHighestRatedProducts()
                .stream()
                .limit(5)
                .toList());
    return "admin-dashboard";
}

@PostMapping("/admin/orders/{id}/resend-email")
public String resendOrderEmail(@PathVariable Long id) {

    Order order = orderRepository.findById(id).orElse(null);

    if (order != null) {
        emailService.sendOrderConfirmation(
                order.getEmail(),
                order.getId(),
                order.getCustomerName(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getStatus()
        );
    }

    return "redirect:/admin/orders";
}
        @PostMapping("/admin/orders/{id}/mark-paid")
public String markOrderPaid(@PathVariable Long id) {

    Order order = orderRepository.findById(id).orElse(null);

    if (order != null) {
        order.setPaymentStatus("Paid");

        if ("Pending".equals(order.getStatus())) {
            order.setStatus("Processing");
        }

        orderRepository.save(order);
    }

    return "redirect:/admin/orders";
}

       
}