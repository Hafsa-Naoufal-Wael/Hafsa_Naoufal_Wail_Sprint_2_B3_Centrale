package com.centrale.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.centrale.config.ThymeleafConfig;
import com.centrale.model.entity.Client;
import com.centrale.model.entity.Order;
import com.centrale.model.entity.OrderItem;
import com.centrale.model.entity.User;
import com.centrale.repository.impl.ClientRepositoryImpl;
import com.centrale.repository.impl.OrderRepositoryImpl;
import com.centrale.service.ClientService;
import com.centrale.service.OrderService;

public class OrderController extends HttpServlet {

    private OrderService orderService;
    private ClientService clientService;

    @Override
    public void init() throws ServletException {
        super.init();
        orderService = new OrderService(new OrderRepositoryImpl());
        clientService = new ClientService(new ClientRepositoryImpl());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            viewOrders(request, response);
        } else if (pathInfo.equals("/detail")) {
            viewOrderDetail(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            createOrder(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void viewOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Client client = (Client) session.getAttribute("client");

        if (client == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        List<Order> orders = orderService.getOrdersByClient(client);

        TemplateEngine templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
        WebContext context = new WebContext(request, response, getServletContext());
        context.setVariable("pageTitle", "Your Orders");
        context.setVariable("orders", orders);
        context.setVariable("content", "client/view-orders");
        templateEngine.process("index", context, response.getWriter());
    }

    private void viewOrderDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String orderId = request.getParameter("id");
        if (orderId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Optional<Order> orderOptional = orderService.getOrderById(Long.parseLong(orderId));
        if (!orderOptional.isPresent()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Order order = orderOptional.get();

        TemplateEngine templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
        WebContext context = new WebContext(request, response, getServletContext());
        context.setVariable("pageTitle", "Order Details");
        context.setVariable("order", order);
        context.setVariable("content", "client/order-detail");
        templateEngine.process("index", context, response.getWriter());
    }

    private void createOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        List<OrderItem> cartItems = (List<OrderItem>) session.getAttribute("cart");
        if (cartItems == null || cartItems.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        String shippingAddress = request.getParameter("address");
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Order order = orderService.createOrder(user, cartItems, shippingAddress);
        session.removeAttribute("cart");

        response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + order.getId());
    }
}