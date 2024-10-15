package com.centrale.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.centrale.model.entity.Client;
import com.centrale.model.entity.Order;
import com.centrale.model.entity.OrderItem;
import com.centrale.model.entity.User;
import com.centrale.model.enums.OrderStatus;
import com.centrale.repository.OrderRepository;

public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void deleteOrder(Order order) {
        orderRepository.delete(order);
    }

    public List<Order> getOrdersByClient(Client client) {
        return orderRepository.findByClient(client);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getAllOrdersPaginated(int page, int size) {
        return orderRepository.findAllPaginated(page, size);
    }

    public Order createOrder(User user, List<OrderItem> cartItems, String shippingAddress) {
        Order order = new Order();
        order.setClient((Client) user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(shippingAddress);
    
        // Set order items and calculate total
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : cartItems) {
            item.setOrder(order);
            total = total.add(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setOrderItems(cartItems);
        order.setTotal(total);
    
        return saveOrder(order);
    }


}
