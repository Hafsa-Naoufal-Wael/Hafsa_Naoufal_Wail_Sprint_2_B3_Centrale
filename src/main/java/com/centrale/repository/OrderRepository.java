package com.centrale.repository;

import com.centrale.model.entity.Order;
import com.centrale.model.entity.Client;
import com.centrale.model.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findAll();
    void delete(Order order);
    List<Order> findByClient(Client client);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findAllPaginated(int page, int size);
}
