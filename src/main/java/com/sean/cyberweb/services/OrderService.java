package com.sean.cyberweb.services;

import com.sean.cyberweb.domain.Order;
import com.sean.cyberweb.domain.OrderItem;
import com.sean.cyberweb.domain.Product;
import com.sean.cyberweb.dto.CartItem;
import com.sean.cyberweb.repository.OrderRepository;
import com.sean.cyberweb.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, UserService userService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    @Transactional
    public Order createOrder(List<CartItem> cartItems, String userHashId) {

        // 解密後儲存id
        Long userId = userService.decode(userHashId);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user hash ID.");
        }

        Order newOrder = new Order();
        newOrder.setUserId(userId);
        newOrder.setStatus(Order.OrderStatus.PROCESSING);
        newOrder.setOrderItems(new HashSet<>());

        // 生成唯一訂單編號
        String uniqueOrderNumber = generateUniqueOrderNumber();
        newOrder.setOrderNumber(uniqueOrderNumber); // 設置訂單編號

        BigDecimal totalPrice = BigDecimal.ZERO;

        // 檢查前端傳來的cartItem
        for (CartItem cartItem : cartItems) {
            if (cartItem.getId() == null) {
                throw new IllegalArgumentException("Product ID must not be null");
            }
            Product product = productRepository.findById(cartItem.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product with ID " + cartItem.getId() + " not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(newOrder); // 關聯到訂單
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());

            totalPrice = totalPrice.add(cartItem.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));

            newOrder.getOrderItems().add(orderItem); // 將訂單項目加到訂單中
        }

        // 設置total price
        newOrder.setTotalPrice(totalPrice);

        // save
        return orderRepository.save(newOrder);
    }

    // 產生唯一訂單編號
    private String generateUniqueOrderNumber() {
        return "ORD-" + System.currentTimeMillis(); // 使用當前時間戳生成唯一訂單編號
    }
}
