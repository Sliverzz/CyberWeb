package com.sean.cyberweb.repository;

import com.sean.cyberweb.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findAllByUserId(Long userId, Pageable pageable);
    Optional<Order> findByOrderNumber(String orderNumber);
}

