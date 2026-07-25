package com.ecommerce.demo_ecommerce.service;

import com.ecommerce.demo_ecommerce.model.Notification;
import com.ecommerce.demo_ecommerce.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository
    ) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(
            String type,
            String message,
            String link
    ) {
        Notification notification = new Notification(
                type,
                message,
                link
        );

        notificationRepository.save(notification);
    }

    public List<Notification> getRecentNotifications() {
        return notificationRepository
                .findTop10ByOrderByCreatedAtDesc();
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository
                .findTop10ByReadStatusFalseOrderByCreatedAtDesc();
    }

    public long getUnreadCount() {
        return notificationRepository.countByReadStatusFalse();
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification not found: " + id
                                )
                        );

        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
       List<Notification> unreadNotifications =
        notificationRepository.findByReadStatusFalse();

        for (Notification notification : unreadNotifications) {
            notification.setReadStatus(true);
        }

        notificationRepository.saveAll(unreadNotifications);
    }
}