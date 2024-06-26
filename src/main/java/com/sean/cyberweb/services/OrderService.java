package com.sean.cyberweb.services;

import com.sean.cyberweb.domain.Order;
import com.sean.cyberweb.domain.OrderItem;
import com.sean.cyberweb.domain.Product;
import com.sean.cyberweb.domain.User;
import com.sean.cyberweb.dto.CartItemDto;
import com.sean.cyberweb.dto.OrderDto;
import com.sean.cyberweb.dto.OrderItemDto;
import com.sean.cyberweb.exception.StockShortageException;
import com.sean.cyberweb.repository.OrderItemRepository;
import com.sean.cyberweb.repository.OrderRepository;
import com.sean.cyberweb.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, UserService userService, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userService = userService;
        this.orderItemRepository = orderItemRepository;
    }

    // 創建訂單
    @Transactional
    public Order createOrder(List<CartItemDto> cartItems, String userHashId) {
        // 解密後儲存id
        Long userId = userService.decode(userHashId);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user hash ID.");
        }

        Order newOrder = new Order();
        newOrder.setUserId(userId);
        newOrder.setStatus(Order.OrderStatus.UNPAID);
        newOrder.setOrderItems(new HashSet<>());

        // 生成唯一訂單編號
        String uniqueOrderNumber = generateUniqueOrderNumber();
        newOrder.setOrderNumber(uniqueOrderNumber); // 設置訂單編號

        BigDecimal totalPrice = BigDecimal.ZERO;

        // 檢查前端傳來的cartItem
        for (CartItemDto cartItem : cartItems) {
            if (cartItem.getId() == null) {
                throw new IllegalArgumentException("Product ID must not be null");
            }
            Product product = productRepository.findById(cartItem.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product with ID " + cartItem.getId() + " not found"));

            // 檢查庫存量
            if (cartItem.getQuantity() > product.getStock()) {
                throw new StockShortageException(product.getName(), product.getStock());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(newOrder); // 關聯到訂單
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());

            totalPrice = totalPrice.add(cartItem.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));

            newOrder.getOrderItems().add(orderItem); // 將訂單項目加到訂單中

            // 減去庫存量
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // 設置total price
        newOrder.setTotalPrice(totalPrice);

        // save
        return orderRepository.save(newOrder);
    }

    // 查詢所有訂單
    public Page<OrderDto> findAll(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        // CyberWeb 統一時間格式 "yyyy-MM-dd HH:mm:ss"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Page<OrderDto> dtoPage = orderPage.map(order -> {
            OrderDto dto = new OrderDto();
            dto.setId(order.getId());
            dto.setOrderNumber(order.getOrderNumber());
            dto.setStatus(order.getStatus().toString());
            dto.setTotalPrice(order.getTotalPrice());
            dto.setDateCreated(order.getDateCreated().format(formatter));
            dto.setLastUpdated(order.getLastUpdated().format(formatter));
            dto.setLastUpdateUser(order.getLastUpdateUserName());
            return dto;
        });

        return dtoPage;
    }

    public Page<OrderDto> findAllByUserId(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated"); // Handle this exception appropriately.
        }

        Page<Order> orderPage = orderRepository.findAllByUserId(currentUser.getId(), pageable);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return orderPage.map(order -> {
            OrderDto dto = new OrderDto();
            dto.setId(order.getId());
            dto.setOrderNumber(order.getOrderNumber());
            dto.setStatus(order.getStatus().toString());
            dto.setTotalPrice(order.getTotalPrice());
            dto.setDateCreated(order.getDateCreated().format(formatter));
            dto.setLastUpdated(order.getLastUpdated().format(formatter));
            dto.setLastUpdateUser(order.getLastUpdateUserName());
            return dto;
        });
    }

    // id查找個別訂單
    public Order findById(UUID id) {
        // 使用Optional防止null值
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            throw new IllegalStateException("Order with id " + id + " does not exist.");
        }
        return orderOptional.get();
    }

    // 訂單頁付款 - 獲取訂單資訊並裝進dto
    public OrderDto getOrderDetails(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return null;
        }

        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setOrderNumber(order.getOrderNumber());
        orderDto.setTotalPrice(order.getTotalPrice());
        orderDto.setStatus(order.getStatus().name());
        orderDto.setDateCreated(order.getDateCreated().toString()); // 格式化日期
        orderDto.setLastUpdated(order.getLastUpdated().toString()); // 格式化日期
        // 填充OrderItems
        order.getOrderItems().forEach(item -> {
            OrderItemDto orderItemDto = new OrderItemDto();
            orderItemDto.setId(item.getId());
            orderItemDto.setProductId(item.getProduct().getId());
            orderItemDto.setProductName(item.getProduct().getName());
            orderItemDto.setPrice(item.getPrice());
            orderItemDto.setQuantity(item.getQuantity());
            orderItemDto.setProductImagePath(item.getProduct().getProductImagePath());
            orderDto.getOrderItems().add(orderItemDto);
        });

        return orderDto;
    }

    // 更新訂單
    @Transactional
    public Order updateOrder(OrderDto orderDto, User currentUser) {
        Order order = orderRepository.findById(orderDto.getId())
                .orElseThrow(() -> new IllegalStateException("productId " + orderDto.getId() + " not exist."));

        order.setOrderNumber(orderDto.getOrderNumber());
        order.setTotalPrice(orderDto.getTotalPrice());
        order.setStatus(Order.OrderStatus.valueOf(orderDto.getStatus()));
        order.setLastUpdateUser(currentUser);

        Set<Long> incomingItemIds = orderDto.getOrderItems().stream()
                .map(OrderItemDto::getId)
                .collect(Collectors.toSet());

        // 移除不存在於前端傳來列表中的訂單項目，並補回相應的庫存
        List<OrderItem> toRemove = order.getOrderItems().stream()
                .filter(item -> !incomingItemIds.contains(item.getId()))
                .toList();
        toRemove.forEach(item -> {
            Product product = item.getProduct();
            int removedQuantity = item.getQuantity();

            // 將刪除的訂單項數量加回產品庫存
            product.setStock(product.getStock() + removedQuantity);
            productRepository.save(product);

            order.getOrderItems().remove(item);
            orderItemRepository.delete(item);
        });

        // 處理更新或新增的訂單項目
        orderDto.getOrderItems().forEach(itemDto -> {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalStateException("productId " + itemDto.getProductId() + " not exist."));

            OrderItem orderItem = order.getOrderItems().stream()
                    .filter(item -> item.getId() != null && item.getId().equals(itemDto.getId()))
                    .findFirst()
                    .orElseGet(OrderItem::new);

            int originalQuantity = orderItem.getId() != null ? orderItem.getQuantity() : 0;
            int newQuantity = itemDto.getQuantity();
            int quantityDifference = newQuantity - originalQuantity;

            if (quantityDifference > 0 && quantityDifference > product.getStock()) {
                throw new StockShortageException(product.getName(), product.getStock());
            } else {
                // 更新產品的庫存量
                product.setStock(product.getStock() - quantityDifference);
                productRepository.save(product);
            }

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(newQuantity);
            orderItem.setPrice(itemDto.getPrice());

            if (orderItem.getId() == null) {
                order.getOrderItems().add(orderItem);
            }
        });

        return orderRepository.save(order);
    }

    // 單獨更新訂單狀態
    @Transactional
    public void updateOrderStatus(UUID orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order with ID " + orderId + " does not exist."));
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    // 單獨更新訂單的transactionId(linePay)
    @Transactional
    public void updateOrderTransactionId(UUID orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order with ID " + orderId + " does not exist."));
        order.setTransactionId(transactionId); // 更新transactionId
        orderRepository.save(order);
    }

    /**
     * 將訂單實體轉換為訂單數據傳輸對象 (DTO)，用於前後端數據交互。
     */
    public OrderDto convertToOrderDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setOrderNumber(order.getOrderNumber());
        orderDto.setTotalPrice(order.getTotalPrice());
        orderDto.setStatus(order.getStatus().toString());

        // 設置最後更新人
        orderDto.setLastUpdateUser(order.getLastUpdateUserName()); // 使用 getLastUpdateUserName() 取得更新人名字

        List<OrderItemDto> itemDtos = order.getOrderItems().stream().map(item -> {
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getName());
            itemDto.setPrice(item.getPrice());
            itemDto.setQuantity(item.getQuantity());
            return itemDto;
        }).collect(Collectors.toList());

        orderDto.setOrderItems(itemDtos);

        return orderDto;
    }

    // 產生唯一訂單編號
    private String generateUniqueOrderNumber() {
        return "ORD-" + System.currentTimeMillis(); // 使用當前時間戳生成唯一訂單編號
    }
}
