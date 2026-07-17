package com.ecommerce.demo_ecommerce.controller;

import com.ecommerce.demo_ecommerce.entity.Product;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.ecommerce.demo_ecommerce.service.ProductService;
import com.ecommerce.demo_ecommerce.repository.OrderItemRepository;
import com.ecommerce.demo_ecommerce.service.EmailService;

import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.ecommerce.demo_ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.ecommerce.demo_ecommerce.repository.ReviewRepository;
import com.ecommerce.demo_ecommerce.service.SalesReportPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.Comparator;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.ecommerce.demo_ecommerce.service.ActivityLogService;


@Controller

public class AdminController {
    private final UserRepository userRepository;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;
    private final EmailService emailService;
    private final ProductRepository productRepository;
    private final SalesReportPdfService salesReportPdfService;
    private final ActivityLogService activityLogService;
    
@GetMapping("/admin/orders")
public String adminOrders(
        @RequestParam(name = "keyword", defaultValue = "") String keyword,
        @RequestParam(name = "orderStatus", defaultValue = "") String orderStatus,
        @RequestParam(name = "paymentStatus", defaultValue = "") String paymentStatus,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        Model model) {

    String cleanKeyword = keyword.trim();

    int safePage = Math.max(page, 0);
    int safeSize = Math.max(1, Math.min(size, 100));

    Pageable pageable = PageRequest.of(
            safePage,
            safeSize,
            Sort.by(Sort.Direction.DESC, "orderDate")
    );

    Page<Order> orderPage = orderRepository.filterOrders(
            cleanKeyword,
            orderStatus,
            paymentStatus,
            pageable
    );

    model.addAttribute("orders", orderPage.getContent());
    model.addAttribute("orderPage", orderPage);

    model.addAttribute("keyword", cleanKeyword);
    model.addAttribute("selectedOrderStatus", orderStatus);
    model.addAttribute("selectedPaymentStatus", paymentStatus);
    model.addAttribute("selectedSize", safeSize);

    return "admin-orders";
}
   public AdminController(ProductService productService,
                       ProductRepository productRepository,
                       OrderRepository orderRepository,
                       UserRepository userRepository,
                       OrderItemRepository orderItemRepository,
                       ReviewRepository reviewRepository,
                       EmailService emailService,
                       SalesReportPdfService salesReportPdfService,
                       ActivityLogService activityLogService) {

    this.productService = productService;
    this.productRepository = productRepository;
    this.orderRepository = orderRepository;
    this.userRepository = userRepository;
    this.orderItemRepository = orderItemRepository;
    this.reviewRepository = reviewRepository;
    this.emailService = emailService;
    this.salesReportPdfService = new SalesReportPdfService();
    this.activityLogService = activityLogService;
}

    

   @GetMapping("/admin/products")public String adminProducts(
        @RequestParam(name = "keyword", defaultValue = "") String keyword,
        @RequestParam(name = "category", defaultValue = "") String category,
        @RequestParam(name = "stockFilter", defaultValue = "") String stockFilter,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        HttpServletResponse response,
        Model model) {

                response.setHeader(
        "Cache-Control",
        "no-cache, no-store, must-revalidate"
);
response.setHeader("Pragma", "no-cache");
response.setDateHeader("Expires", 0);

    String cleanKeyword = keyword.trim();

    int safePage = Math.max(page, 0);
    int safeSize = Math.max(1, Math.min(size, 100));

    Pageable pageable = PageRequest.of(
            safePage,
            safeSize,
            Sort.by(Sort.Direction.ASC, "name")
    );

    Page<Product> productPage = productRepository.filterProducts(
            cleanKeyword,
            category,
            stockFilter,
            pageable
    );

    model.addAttribute("products", productPage.getContent());
    model.addAttribute("productPage", productPage);

    model.addAttribute("keyword", cleanKeyword);
    model.addAttribute("selectedCategory", category);
    model.addAttribute("selectedStockFilter", stockFilter);
    model.addAttribute("selectedSize", safeSize);

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
        @RequestParam("imageFile") MultipartFile imageFile,
        RedirectAttributes redirectAttributes,
        Authentication authentication
) throws IOException {

    boolean isNewProduct = (product.getId() == null);

    // Editing an existing product
    if (!isNewProduct) {
        Product existingProduct =
                productService.getProductById(product.getId());

        if (existingProduct != null) {
            // Keep the current image unless a new one is uploaded
            product.setImageUrl(existingProduct.getImageUrl());
        }
    }

    // Replace the existing image only when a new file is selected
   if (imageFile != null && !imageFile.isEmpty()) {

    Path uploadDirectory = Paths.get(
            System.getProperty("user.dir"),
            "uploads"
    );

    Files.createDirectories(uploadDirectory);

    String originalFileName = imageFile.getOriginalFilename();

    String safeFileName =
            originalFileName == null
                    ? "product-image"
                    : originalFileName.replaceAll(
                            "[^a-zA-Z0-9._-]",
                            "_"
                    );

    String fileName =
            System.currentTimeMillis()
                    + "_"
                    + safeFileName;

    Path savedFile = uploadDirectory.resolve(fileName);

    imageFile.transferTo(savedFile.toFile());

    product.setImageUrl("/uploads/" + fileName);
}

    productService.saveProduct(product);

if (isNewProduct) {
    activityLogService.log(
            "PRODUCT_CREATED",
            "Created product: " + product.getName(),
            authentication
    );
} else {
    activityLogService.log(
            "PRODUCT_UPDATED",
            "Updated product: " + product.getName(),
            authentication
    );
}

    if (isNewProduct) {
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product added successfully!"
        );
    } else {
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product updated successfully!"
        );
    }

    redirectAttributes.addAttribute(
        "refresh",
        System.currentTimeMillis()
);

return "redirect:/admin/products";
}

@GetMapping("/admin/products/edit/{id}")
public String showEditProductForm(@PathVariable Long id, Model model) {
    model.addAttribute("product", productService.getProductById(id));
    return "admin-product-form";
}

@GetMapping("/admin/products/delete/{id}")
public String deleteProduct(
        @PathVariable Long id,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    Product product = productService.getProductById(id);

    if (product == null) {
        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Product not found."
        );

        return "redirect:/admin/products";
    }

    String productName = product.getName();

    productService.deleteProduct(id);

    activityLogService.log(
            "PRODUCT_DELETED",
            "Deleted product: " + productName,
            authentication
    );

    redirectAttributes.addFlashAttribute(
            "successMessage",
            "Product deleted successfully!"
    );

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
        @RequestParam String status,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    Order order = orderRepository.findById(id)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Order not found: " + id
                    ));

    order.setStatus(status);
    orderRepository.save(order);

    activityLogService.log(
            "ORDER_STATUS_UPDATED",
            "Updated Order #" + id + " status to " + status,
            authentication
    );

    redirectAttributes.addFlashAttribute(
            "successMessage",
            "Order status updated successfully!"
    );

    return "redirect:/admin/orders";
}

@GetMapping("/admin/dashboard")
public String adminDashboard(Model model) {


        model.addAttribute(
        "recentActivities",
        activityLogService.getRecentActivities()
);

    var products = productService.getAllProducts();
    var orders = orderRepository.findAll();

    long pendingOrders = orders.stream()
            .filter(order -> "Pending".equalsIgnoreCase(order.getStatus()))
            .count();

    long processingOrders = orders.stream()
            .filter(order -> "Processing".equalsIgnoreCase(order.getStatus()))
            .count();

    long shippedOrders = orders.stream()
            .filter(order -> "Shipped".equalsIgnoreCase(order.getStatus()))
            .count();

    long deliveredOrders = orders.stream()
            .filter(order -> "Delivered".equalsIgnoreCase(order.getStatus()))
            .count();

    long cancelledOrders = orders.stream()
            .filter(order -> "Cancelled".equalsIgnoreCase(order.getStatus()))
            .count();

    long paidOrders = orders.stream()
            .filter(order -> "Paid".equalsIgnoreCase(order.getPaymentStatus()))
            .count();

    long unpaidOrders = orders.stream()
            .filter(order ->
                    "Unpaid".equalsIgnoreCase(order.getPaymentStatus())
                    || "Pending Payment".equalsIgnoreCase(order.getPaymentStatus()))
            .count();

    BigDecimal totalRevenue = orders.stream()
            .filter(order -> order.getTotalAmount() != null)
            .filter(order -> !"Cancelled".equalsIgnoreCase(order.getStatus()))
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    long ordersToday = orders.stream()
            .filter(order -> order.getOrderDate() != null)
            .filter(order ->
                    order.getOrderDate()
                            .toLocalDate()
                            .equals(LocalDate.now()))
            .count();

    model.addAttribute("totalProducts", products.size());
    model.addAttribute("totalOrders", orders.size());
    model.addAttribute("totalCustomers", userRepository.count());
    model.addAttribute("totalRevenue", totalRevenue);

    model.addAttribute("pendingOrders", pendingOrders);
    model.addAttribute("processingOrders", processingOrders);
    model.addAttribute("shippedOrders", shippedOrders);
    model.addAttribute("deliveredOrders", deliveredOrders);
    model.addAttribute("cancelledOrders", cancelledOrders);

    model.addAttribute("paidOrders", paidOrders);
    model.addAttribute("unpaidOrders", unpaidOrders);
    model.addAttribute("ordersToday", ordersToday);

    model.addAttribute(
            "lowStockProducts",
            products.stream()
                    .filter(product ->
                            product.getStock() != null
                            && product.getStock() <= 5)
                    .toList()
    );

    model.addAttribute(
            "recentOrders",
            orders.stream()
                    .filter(order -> order.getOrderDate() != null)
                    .sorted((first, second) ->
                            second.getOrderDate()
                                    .compareTo(first.getOrderDate()))
                    .limit(5)
                    .toList()
    );

    LocalDate chartEndDate = orders.stream()
            .filter(order -> order.getOrderDate() != null)
            .map(order -> order.getOrderDate().toLocalDate())
            .max(Comparator.naturalOrder())
            .orElse(LocalDate.now());

    List<String> revenueLabels = new ArrayList<>();
    List<Double> revenueData = new ArrayList<>();

    for (int i = 6; i >= 0; i--) {

        LocalDate date = chartEndDate.minusDays(i);

        BigDecimal dailyRevenue = orders.stream()
                .filter(order -> order.getOrderDate() != null)
                .filter(order -> order.getTotalAmount() != null)
                .filter(order ->
                        !"Cancelled".equalsIgnoreCase(order.getStatus()))
                .filter(order ->
                        order.getOrderDate()
                                .toLocalDate()
                                .equals(date))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        revenueLabels.add(
                date.getMonthValue() + "/" + date.getDayOfMonth()
        );

        revenueData.add(dailyRevenue.doubleValue());
    }

    model.addAttribute("revenueLabels", revenueLabels);
    model.addAttribute("revenueData", revenueData);

    var topSellingProducts = orderItemRepository.findAll()
            .stream()
            .filter(item ->
                    item != null
                    && item.getProductName() != null)
                    .filter(item ->
        item.getOrder() != null
        && !"Cancelled".equalsIgnoreCase(
                item.getOrder().getStatus()
        ))
            .collect(Collectors.groupingBy(
                    OrderItem::getProductName,
                    Collectors.summingInt(OrderItem::getQuantity)
            ))
            .entrySet()
            .stream()
            .sorted((first, second) ->
                    second.getValue()
                            .compareTo(first.getValue()))
            .limit(5)
            .map(entry ->
                    new Object[]{
                            entry.getKey(),
                            entry.getValue()
                    })
            .toList();

    model.addAttribute(
            "topSellingProducts",
            topSellingProducts
    );

    model.addAttribute(
            "highestRatedProducts",
            reviewRepository.findHighestRatedProducts()
                    .stream()
                    .limit(5)
                    .toList()
    );

    List<String> orderStatusLabels = List.of(
            "Pending",
            "Processing",
            "Shipped",
            "Delivered",
            "Cancelled"
    );

    List<Long> orderStatusData = List.of(
            pendingOrders,
            processingOrders,
            shippedOrders,
            deliveredOrders,
            cancelledOrders
    );

    model.addAttribute(
            "orderStatusLabels",
            orderStatusLabels
    );

    model.addAttribute(
            "orderStatusData",
            orderStatusData
    );

    return "admin-dashboard";
}

@PostMapping("/admin/orders/{id}/resend-email")
public String resendOrderEmail(@PathVariable Long id,Authentication authentication, RedirectAttributes redirectAttributes) {

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

        activityLogService.log(
                "EMAIL_RESENT",
                "Resent order confirmation email for Order #" + order.getId(),
                authentication
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Order confirmation email resent successfully!"
        );
}else{
        
        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Order not found. Unable to resend email."
        );
}

    

    return "redirect:/admin/orders";
}
       

@PostMapping("/admin/orders/{id}/mark-paid")
public String markOrderPaid(
        @PathVariable Long id,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    Order order = orderRepository.findById(id)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Order not found: " + id
                    ));

    order.setPaymentStatus("Paid");

    if ("Pending".equalsIgnoreCase(order.getStatus())) {
        order.setStatus("Processing");
    }

    orderRepository.save(order);

    activityLogService.log(
            "PAYMENT_UPDATED",
            "Marked Order #" + order.getId() + " as Paid",
            authentication
    );

    redirectAttributes.addFlashAttribute(
            "successMessage",
            "Order marked as Paid successfully!"
    );

    return "redirect:/admin/orders";
}

@GetMapping("/admin/reports")
public String salesReport(
        @RequestParam(name = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @RequestParam(name = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,

        Model model) {

    LocalDate today = LocalDate.now();

    if (from == null) {
        from = today.withDayOfMonth(1);
    }

    if (to == null) {
        to = today;
    }

    if (from.isAfter(to)) {
        LocalDate temporaryDate = from;
        from = to;
        to = temporaryDate;
    }

    LocalDateTime fromDateTime = from.atStartOfDay();
    LocalDateTime toDateTime = to.plusDays(1).atStartOfDay().minusNanos(1);
               

    List<Order> reportOrders =
            orderRepository.findOrdersBetweenDates(
                    fromDateTime,
                    toDateTime
            );

    BigDecimal totalRevenue = reportOrders.stream()
            .filter(order -> order.getTotalAmount() != null)
            .filter(order -> !"Cancelled".equalsIgnoreCase(order.getStatus()))
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    long totalOrders = reportOrders.stream()
            .filter(order -> !"Cancelled".equalsIgnoreCase(order.getStatus()))
            .count();

    Long productsSoldResult =
            orderItemRepository.countProductsSoldBetweenDates(
                    fromDateTime,
                    toDateTime
            );

    long productsSold =
            productsSoldResult == null ? 0 : productsSoldResult;

    BigDecimal averageOrder = BigDecimal.ZERO;

    if (totalOrders > 0) {
        averageOrder = totalRevenue.divide(
                BigDecimal.valueOf(totalOrders),
                2,
                RoundingMode.HALF_UP
        );
    }

    model.addAttribute("from", from);
    model.addAttribute("to", to);

    model.addAttribute("totalRevenue", totalRevenue);
    model.addAttribute("totalOrders", totalOrders);
    model.addAttribute("productsSold", productsSold);
    model.addAttribute("averageOrder", averageOrder);
    model.addAttribute("reportOrders", reportOrders);

    return "admin-sales-report";
}

@GetMapping("/admin/reports/pdf")
public ResponseEntity<byte[]> exportSalesReportPdf(
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,
        Authentication authentication
) {

    LocalDateTime fromDateTime = from.atStartOfDay();

    LocalDateTime toDateTime = to.plusDays(1)
            .atStartOfDay()
            .minusNanos(1);

    List<Order> reportOrders =
            orderRepository.findOrdersBetweenDates(
                    fromDateTime,
                    toDateTime
            );

    List<Order> validOrders = reportOrders.stream()
            .filter(order ->
                    !"Cancelled".equalsIgnoreCase(
                            order.getStatus()
                    )
            )
            .toList();

    BigDecimal totalRevenue = validOrders.stream()
            .filter(order -> order.getTotalAmount() != null)
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    long totalOrders = validOrders.size();

    Long productsSoldResult =
            orderItemRepository.countProductsSoldBetweenDates(
                    fromDateTime,
                    toDateTime
            );

    long productsSold =
            productsSoldResult == null
                    ? 0
                    : productsSoldResult;

    BigDecimal averageOrder = BigDecimal.ZERO;

    if (totalOrders > 0) {
        averageOrder = totalRevenue.divide(
                BigDecimal.valueOf(totalOrders),
                2,
                RoundingMode.HALF_UP
        );
    }

    byte[] pdf = salesReportPdfService.generateReport(
            from,
            to,
            totalRevenue,
            totalOrders,
            productsSold,
            averageOrder,
            validOrders
    );

     activityLogService.log(
            "Report_EXported",
            "Exported  sales report PDF from " + from + " to " + to,
            null
    );

    String filename =
            "sales-report-"
                    + from
                    + "-to-"
                    + to
                    + ".pdf";

    return ResponseEntity.ok()
            .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + filename + "\""
            )
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
}

       
}