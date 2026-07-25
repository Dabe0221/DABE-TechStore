package com.ecommerce.demo_ecommerce.controller;

import com.ecommerce.demo_ecommerce.service.NotificationService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AdminNotificationAdvice {

    private final NotificationService notificationService;

    public AdminNotificationAdvice(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void addNotificationAttributes(Model model) {

        model.addAttribute(
                "recentNotifications",
                notificationService.getRecentNotifications()
        );

        model.addAttribute(
                "unreadNotificationCount",
                notificationService.getUnreadCount()
        );

        
    }
}