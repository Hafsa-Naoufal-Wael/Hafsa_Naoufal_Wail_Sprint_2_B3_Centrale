package com.centrale.service;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.centrale.model.entity.Client;
import com.centrale.model.entity.Order;

import com.centrale.model.enums.OrderStatus;
import com.centrale.repository.OrderRepository;


public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        orderService = new OrderService(orderRepository);
    }

    @Test
    public void testCancelOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        orderService.cancelOrder(order);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).update(order);
    }

    @Test
    public void testGetOrdersByClientPaginated() {
        Client client = new Client();
        int page = 1;
        int pageSize = 10;
        String searchTerm = "test";
        List<Order> orders = Arrays.asList(new Order(), new Order());

        when(orderRepository.findByClientPaginated(client, page, pageSize, searchTerm)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByClientPaginated(client, page, pageSize, searchTerm);

        assertEquals(orders, result);
        verify(orderRepository).findByClientPaginated(client, page, pageSize, searchTerm);
    }
}