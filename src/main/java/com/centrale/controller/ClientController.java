package com.centrale.controller;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.centrale.config.ThymeleafConfig;
import com.centrale.model.entity.Client;
import com.centrale.model.entity.User;
import com.centrale.model.entity.Order;
import com.centrale.model.enums.UserRole;
import com.centrale.repository.impl.OrderRepositoryImpl;
import com.centrale.repository.impl.ClientRepositoryImpl;
import com.centrale.service.OrderService;
import com.centrale.service.ClientService;

public class ClientController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ClientController.class.getName());

    private OrderService orderService;
    private ClientService clientService;

    @Override
    public void init() throws ServletException {
        super.init();
        LOGGER.info("Initializing ClientController");
        orderService = new OrderService(new OrderRepositoryImpl());
        clientService = new ClientService(new ClientRepositoryImpl());
        LOGGER.info("ClientController initialized successfully");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        LOGGER.info("Handling GET request with pathInfo: " + pathInfo);
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/dashboard")) {
            showDashboard(request, response);
        } else if (pathInfo.equals("/orders")) {
            showOrders(request, response);
        } else {
            LOGGER.warning("Invalid path requested: " + pathInfo);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("Showing dashboard");
        HttpSession session = request.getSession();
        LOGGER.info("Session ID: " + session.getId());

        User user = (User) session.getAttribute("user");
        UserRole userRole = (UserRole) session.getAttribute("userRole");

        if (user == null || userRole != UserRole.CLIENT) {
            LOGGER.warning("Invalid user or role. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        Client client = clientService.findByUser(user);
        if (client == null) {
            LOGGER.warning("Unable to find Client for user with email: " + user.getEmail());
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        List<Order> recentOrders = orderService.getRecentOrdersByClient(client, 5);
        int totalOrders = orderService.getTotalOrdersCountByClient(client);

        TemplateEngine templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
        WebContext context = new WebContext(request, response, getServletContext());
        context.setVariable("pageTitle", "Client Dashboard");
        context.setVariable("user", client);
        context.setVariable("recentOrders", recentOrders);
        context.setVariable("totalOrders", totalOrders);
        LOGGER.info("Processing template: client/dashboard");
        templateEngine.process("client/dashboard", context, response.getWriter());
        LOGGER.info("Dashboard template processed successfully");
    }

    private void showOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("Showing orders");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        UserRole userRole = (UserRole) session.getAttribute("userRole");

        if (user == null || userRole != UserRole.CLIENT) {
            LOGGER.warning("Invalid user or role. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        Client client = clientService.findByUser(user);
        if (client == null) {
            LOGGER.warning("Unable to find Client for user with email: " + user.getEmail());
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        List<Order> orders = orderService.getOrdersByClient(client);

        TemplateEngine templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
        WebContext context = new WebContext(request, response, getServletContext());
        context.setVariable("pageTitle", "Your Orders");
        context.setVariable("user", client);
        context.setVariable("orders", orders);
        LOGGER.info("Processing template: client/view-orders");
        templateEngine.process("client/view-orders", context, response.getWriter());
        LOGGER.info("View orders template processed successfully");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        LOGGER.info("Handling POST request with action: " + action);
        if ("/update-profile".equals(action)) {
            updateProfile(request, response);
        } else {
            LOGGER.warning("Invalid action requested: " + action);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("Updating profile");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        UserRole userRole = (UserRole) session.getAttribute("userRole");

        LOGGER.info("User from session: " + (user != null ? user.getEmail() : "null"));
        LOGGER.info("User role from session: " + userRole);

        if (user == null || userRole != UserRole.CLIENT) {
            LOGGER.warning("Invalid user or role. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        Client client = clientService.findByUser(user);
        if (client == null) {
            LOGGER.warning("Unable to find Client for user with email: " + user.getEmail());
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        LOGGER.info("Updating profile for client: " + client.getUser().getEmail());

        String phoneNumber = request.getParameter("phoneNumber");
        String address = request.getParameter("address");
        String deliveryAddress = request.getParameter("deliveryAddress");
        String paymentMethod = request.getParameter("paymentMethod");

        client.setPhoneNumber(phoneNumber);
        client.setAddress(address);
        client.setDeliveryAddress(deliveryAddress);
        client.setPaymentMethod(paymentMethod);

        try {
            clientService.saveClient(client);
            LOGGER.info("Profile updated successfully for client: " + client.getUser().getEmail());
            response.sendRedirect(request.getContextPath() + "/client/dashboard?success=Profile+updated+successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating profile for client: " + client.getUser().getEmail(), e);
            response.sendRedirect(request.getContextPath() + "/client/dashboard?error=Profile+update+failed");
        }
    }
}
