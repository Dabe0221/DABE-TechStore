package com.ecommerce.demo_ecommerce.repository;

import com.ecommerce.demo_ecommerce.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findTop10ByOrderByCreatedAtDesc();

    List<Notification>
    findTop10ByReadStatusFalseOrderByCreatedAtDesc();

    List<Notification> findByReadStatusFalse();

    long countByReadStatusFalse();
}