package com.ecommerce.demo_ecommerce.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(
            String to,
            Long orderId,
            String customerName,
            String paymentMethod,
            String paymentStatus,
            String orderStatus) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("DABE TechStore - Order Confirmation");

        message.setText(
                "Hello " + customerName + ",\n\n"
                        + "Thank you for shopping with DABE TechStore!\n\n"
                        + "Order Number: #" + orderId + "\n"
                        + "Payment Method: " + paymentMethod + "\n"
                        + "Payment Status: " + paymentStatus + "\n"
                        + "Order Status: " + orderStatus + "\n\n"
                        + "You can view your order anytime by logging into your account.\n\n"
                        + "Thank you for choosing DABE TechStore!"
        );

        try {
    mailSender.send(message);
    System.out.println("Email sent successfully to: " + to);
} catch (Exception e) {
    System.out.println("Email failed to send to: " + to);
    System.out.println("Reason: " + e.getMessage());
}
    }
}