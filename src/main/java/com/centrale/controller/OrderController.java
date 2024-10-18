package com.centrale.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.centrale.config.ThymeleafConfig;
import com.centrale.model.entity.Order;
import com.centrale.model.entity.OrderItem;
import com.centrale.model.entity.User;
import com.centrale.model.entity.Client;
import com.centrale.model.enums.OrderStatus;
import com.centrale.model.enums.UserRole;
import com.centrale.repository.ClientRepository;
import com.centrale.repository.impl.ClientRepositoryImpl;
import com.centrale.repository.impl.OrderRepositoryImpl;
import com.centrale.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.centrale.service.ClientService;

public class OrderController extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);
    private OrderService orderService;
    private ObjectMapper objectMapper;
    private ClientService clientService;

    @Override
    public void init() throws ServletException {
        super.init();
        orderService = new OrderService(new OrderRepositoryImpl());
        objectMapper = new ObjectMapper();
        clientService = new ClientService(new ClientRepositoryImpl());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();

        if ("/admin/orders".equals(servletPath)) {
            handleAdminGet(request, response, pathInfo);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();

        if ("/admin/orders".equals(servletPath)) {
            handleAdminPost(request, response, pathInfo);
        } else if ("/process".equals(pathInfo)) {
            processOrder(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleAdminGet(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        if ("/get".equals(pathInfo)) {
            getOrder(request, response);
        } else {
            showOrders(request, response);
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

        String shipping_address = request.getParameter("shipping_address");
        String paymentMethod = request.getParameter("paymentMethod");

        if (shipping_address == null || shipping_address.trim().isEmpty()
                || paymentMethod == null || paymentMethod.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/orders/checkout?error=Invalid+input");
            return;
        }

        try {
            // Trim and validate input
            shipping_address = shipping_address.trim();
            paymentMethod = paymentMethod.trim();

            // Update client information
            client.setDeliveryAddress(shipping_address);
            client.setPaymentMethod(paymentMethod);
            clientService.updateClient(client);

            double total = calculateTotal(cartItems);
            Order order = orderService.createOrder(client, cartItems, shipping_address, paymentMethod);
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
    private void showOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("Admin orders accessed");
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int page = 1;
        int pageSize = 10;
        String search = request.getParameter("search");

        if (request.getParameter("page") != null) {
            try {
                page = Integer.parseInt(request.getParameter("page"));
                if (page < 1) {
                    page = 1;
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid page number, defaulting to page 1");
            }
        }

        LOGGER.info("Search term: {}", search);
        LOGGER.info("Fetching orders");
        List<Order> orders = orderService.searchOrdersPaginated(search, page, pageSize);
        LOGGER.info("Orders fetched: {}", orders.size());

        LOGGER.info("Getting total order count");
        int totalOrders = orderService.getTotalOrdersCount(search);
        LOGGER.info("Total orders: {}", totalOrders);

        LOGGER.info("Total pages: {}", (int) Math.ceil((double) totalOrders / pageSize));

        ServletContext servletContext = getServletContext();
        TemplateEngine engine = ThymeleafConfig.getTemplateEngine(servletContext);

        LOGGER.info("Setting up WebContext");
        WebContext context = new WebContext(request, response, servletContext);
        context.setVariable("orders", orders);
        context.setVariable("currentPage", page);
        context.setVariable("totalPages", (int) Math.ceil((double) totalOrders / pageSize));
        context.setVariable("pageTitle", "Manage Orders");
        context.setVariable("search", search);

        LOGGER.info("Processing template");
        engine.process("admin/manage-orders", context, response.getWriter());
        LOGGER.info("Template processed");
    }

    private void handleAdminPost(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        if ("/updateStatus".equals(pathInfo)) {
            updateOrderStatus(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void getOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Order ID is required");
            return;
        }

        Long id = Long.parseLong(idParam);
        Optional<Order> orderOptional = orderService.getOrderById(id);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(order));
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
        }
    }

    private void updateOrderStatus(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        String idParam = request.getParameter("id");
        String newStatus = request.getParameter("status");

        if (idParam == null || idParam.isEmpty() || newStatus == null || newStatus.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Order ID and new status are required");
            return;
        }

        try {
            Long id = Long.parseLong(idParam);
            OrderStatus status = OrderStatus.valueOf(newStatus);

            Optional<Order> orderOptional = orderService.getOrderById(id);

            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                order.setStatus(status);
                orderService.updateOrder(order);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Order status updated successfully");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
            }
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order status");
        }
    }
}
