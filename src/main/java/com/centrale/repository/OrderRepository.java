package com.centrale.repository;

import java.util.List;
import java.util.Optional;

import com.centrale.model.entity.Client;
import com.centrale.model.entity.Order;
import com.centrale.model.enums.OrderStatus;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findAll();
    void delete(Order order);
    List<Order> findByClient(Client client);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findRecentByClient(Client client, int limit);
    List<Order> findAllPaginated(int page, int pageSize, String searchTerm);
    int getTotalOrderCount(String searchTerm);
    List<Order> findByClientPaginated(Client client, int page, int pageSize, String searchTerm);
    int getTotalOrderCountByClient(Client client, String searchTerm);
    void update(Order order);
    List<Order> getClientOrdersPaginated(Long clientId, int offset, int pageSize, String search);
    int getTotalClientOrdersCount(Long clientId, String search);
}
