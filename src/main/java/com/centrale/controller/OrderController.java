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
import com.centrale.model.entity.User;
import com.centrale.model.enums.OrderStatus;
import com.centrale.model.enums.UserRole;
import com.centrale.repository.impl.OrderRepositoryImpl;
import com.centrale.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrderController extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);
    private OrderService orderService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        orderService = new OrderService(new OrderRepositoryImpl());
        objectMapper = new ObjectMapper();
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
            page = Integer.parseInt(request.getParameter("page"));
        }

        List<Order> orders = orderService.getOrdersPaginated(page, pageSize, search);
        int totalOrders = orderService.getTotalOrdersCount(search);
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        ServletContext servletContext = getServletContext();
        TemplateEngine engine = ThymeleafConfig.getTemplateEngine(servletContext);

        WebContext context = new WebContext(request, response, servletContext);
        context.setVariable("orders", orders);
        context.setVariable("currentPage", page);
        context.setVariable("totalPages", totalPages);
        context.setVariable("pageTitle", "Manage Orders");

        engine.process("admin/manage-orders", context, response.getWriter());
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
