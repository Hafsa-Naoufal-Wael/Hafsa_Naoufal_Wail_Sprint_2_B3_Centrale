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
import com.centrale.model.enums.OrderStatus;
import com.centrale.model.enums.UserRole;
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
        } else if (pathInfo.equals("/manage")) {
            manageOrders(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        if ("/process".equals(action)) {
            processOrder(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void processOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        UserRole userRole = (UserRole) session.getAttribute("userRole");

        if (user == null || userRole != UserRole.CLIENT) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        Client client = clientService.findByUser(user);
        if (client == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        List<OrderItem> cartItems = (List<OrderItem>) session.getAttribute("cart");
        if (cartItems == null || cartItems.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart?error=Cart+is+empty");
            return;
        }

        String shippingAddress = request.getParameter("shippingAddress");
        String paymentMethod = request.getParameter("paymentMethod");

        if (shippingAddress == null || shippingAddress.trim().isEmpty()
                || paymentMethod == null || paymentMethod.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/orders/checkout?error=Invalid+input");
            return;
        }

        try {
            Order order = orderService.createOrder(client, cartItems, shippingAddress.trim(), paymentMethod.trim());
            session.removeAttribute("cart");
            session.setAttribute("cartCount", 0);
            response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + order.getId()
                    + "&success=Order+placed+successfully");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/orders/checkout?error=Order+processing+failed:+" + e.getMessage());
        }
    }

    private double calculateTotal(List<OrderItem> cartItems) {
        return cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
                .sum();
    }

    private void viewOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        String userRole = (String) session.getAttribute("userRole");

        if (user == null || !"CLIENT".equalsIgnoreCase(userRole)) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        Client client = clientService.findByUser(user);
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
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        UserRole userRole = (UserRole) session.getAttribute("userRole");

        if (user == null || userRole != UserRole.CLIENT) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        Client client = clientService.findByUser(user);
        if (client == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

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

// Ensure the order belongs to the current user
        if (!order.getClient().getId().equals(client.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        TemplateEngine templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
        WebContext context = new WebContext(request, response, getServletContext());
        context.setVariable("pageTitle", "Order Details");
        context.setVariable("order", order);
        context.setVariable("content", "client/order-detail");
        templateEngine.process("index", context, response.getWriter());
    }

    private void manageOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        String userRole = (String) session.getAttribute("userRole");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        int page = 1;
        int pageSize = 10;
        String searchTerm = request.getParameter("search");
        String pageParam = request.getParameter("page");

        if (pageParam != null && !pageParam.isEmpty()) {
            page = Integer.parseInt(pageParam);
        }

        List<Order> orders;
        int totalOrders;

        if ("ADMIN".equalsIgnoreCase(userRole)) {
            orders = orderService.getAllOrdersPaginated(page, pageSize, searchTerm);
            totalOrders = orderService.getTotalOrderCount(searchTerm);
        } else {
            Client client = clientService.findByUser(user);
            if (client == null) {
                response.sendRedirect(request.getContextPath() + "/auth/login");
                return;
            }
            orders = orderService.getOrdersByClientPaginated(client, page, pageSize, searchTerm);
            totalOrders = orderService.getTotalOrderCountByClient(client, searchTerm);
        }

        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        TemplateEngine templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
        WebContext context = new WebContext(request, response, getServletContext());
        context.setVariable("pageTitle", "Manage Orders");
        context.setVariable("orders", orders);
        context.setVariable("currentPage", page);
        context.setVariable("totalPages", totalPages);
        context.setVariable("searchTerm", searchTerm);
        context.setVariable("userRole", userRole);
        context.setVariable("content", "order/manage-orders");
        templateEngine.process("index", context, response.getWriter());
    }

    private void modifyOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        UserRole userRole = (UserRole) session.getAttribute("userRole");

        if (user == null || userRole != UserRole.CLIENT) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        Client client = clientService.findByUser(user);
        if (client == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        Long orderId = Long.parseLong(request.getParameter("orderId"));
        String newStatus = request.getParameter("status");

        try {
            Order order = orderService.getOrderById(orderId).orElseThrow(() -> new Exception("Order not found"));

            if (!order.getClient().getId().equals(client.getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PROCESSING) {
                order.setStatus(OrderStatus.valueOf(newStatus));
                orderService.saveOrder(order);
                response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId
                        + "&success=Order+modified+successfully");
            } else {
                response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId
                        + "&error=Cannot+modify+order+in+current+status");
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/orders?error=Order+modification+failed");
        }
    }

    private void deleteOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        UserRole userRole = (UserRole) session.getAttribute("userRole");

        if (user == null || userRole != UserRole.CLIENT) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        Client client = clientService.findByUser(user);
        if (client == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String orderIdParam = request.getParameter("orderId");
        if (orderIdParam == null || orderIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/orders?error=Invalid+order+ID");
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdParam);
            Optional<Order> orderOptional = orderService.getOrderById(orderId);

            if (!orderOptional.isPresent()) {
                response.sendRedirect(request.getContextPath() + "/orders?error=Order+not+found");
                return;
            }

            Order order = orderOptional.get();

            if (!order.getClient().getId().equals(client.getId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PROCESSING) {
                orderService.deleteOrder(order);
                response.sendRedirect(request.getContextPath() + "/orders?success=Order+deleted+successfully");
            } else {
                response.sendRedirect(request.getContextPath() + "/orders?error=Cannot+delete+order+in+current+status");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/orders?error=Invalid+order+ID");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/orders?error=Order+deletion+failed");
        }
    }
}
