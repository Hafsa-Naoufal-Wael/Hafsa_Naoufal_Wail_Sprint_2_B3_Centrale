package com.centrale.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.centrale.config.ThymeleafConfig;
import com.centrale.model.entity.Order;
import com.centrale.model.entity.User;
import com.centrale.model.enums.OrderStatus;
import com.centrale.model.enums.UserRole;
import com.centrale.repository.impl.OrderRepositoryImpl;
import com.centrale.service.OrderService;

public class ClientController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ClientController.class.getName());

    private OrderService orderService;


    @Override
    public void init() throws ServletException {
        super.init();
        LOGGER.info("Initializing ClientController");
        orderService = new OrderService(new OrderRepositoryImpl());
        LOGGER.info("ClientController initialized successfully");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        LOGGER.warning("Handling GET request with pathInfo: " + pathInfo);

        if (pathInfo == null) {
            pathInfo = "/";
        }

        switch (pathInfo) {
            case "/":
            case "/orders":
                LOGGER.info("Showing orders");
                showOrders(request, response);
                break;
            default:
                LOGGER.warning("Invalid path requested: " + pathInfo);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void showOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int page = 1;
        int pageSize = 10;
        String search = request.getParameter("search");

        if (request.getParameter("page") != null) {
            page = Integer.parseInt(request.getParameter("page"));
        }

        List<Order> orders = orderService.searchOrdersPaginated(search, page, pageSize);
        int totalOrders = orderService.getTotalClientOrdersCount(currentUser.getId(), search);
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        ServletContext servletContext = getServletContext();
        TemplateEngine engine = ThymeleafConfig.getTemplateEngine(servletContext);

        WebContext context = new WebContext(request, response, servletContext);
        context.setVariable("orders", orders);
        context.setVariable("currentPage", page);
        context.setVariable("totalPages", totalPages);
        context.setVariable("pageTitle", "Your Orders");

        engine.process("client/view-orders", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        LOGGER.info("Handling POST request with action: " + action);

        if (action == null) {
            action = "/";
        }

        switch (action) {
            case "/cancel":
                cancelOrder(request, response);
                break;
            case "/delete":
                deleteOrder(request, response);
                break;
            default:
                LOGGER.warning("Invalid action requested: " + action);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void cancelOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("Entering cancelOrder method");
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || currentUser.getRole() != UserRole.CLIENT) {
            LOGGER.warning("Unauthorized access attempt to cancelOrder");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String orderIdStr = request.getParameter("orderId");
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            LOGGER.warning("Invalid order ID provided for cancellation");
            response.sendRedirect(request.getContextPath() + "/client/orders?error=Invalid+order+ID");
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdStr);
            Optional<Order> orderOptional = orderService.getOrderById(orderId);

            if (!orderOptional.isPresent()
                    || !orderOptional.get().getClient().getUser().getId().equals(currentUser.getId())) {
                LOGGER.warning("Attempt to cancel non-existent or unauthorized order");
                response.sendRedirect(
                        request.getContextPath() + "/client/orders?error=Order+not+found+or+unauthorized");
                return;
            }

            Order order = orderOptional.get();

            if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED
                    || order.getStatus() == OrderStatus.CANCELLED) {
                LOGGER.warning("Attempt to cancel order with status: " + order.getStatus());
                response.sendRedirect(request.getContextPath() + "/client/orders?error=Cannot+cancel+order+with+status+"
                        + order.getStatus());
                return;
            }

            orderService.cancelOrder(order);
            LOGGER.info("Order " + orderId + " cancelled successfully");
            response.sendRedirect(request.getContextPath() + "/client/orders?success=Order+cancelled+successfully");
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid order ID format: " + orderIdStr);
            response.sendRedirect(request.getContextPath() + "/client/orders?error=Invalid+order+ID+format");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error cancelling order", e);
            response.sendRedirect(
                    request.getContextPath() + "/client/orders?error=Error+cancelling+order:+" + e.getMessage());
        }
    }

    private void deleteOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("Entering deleteOrder method");
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || currentUser.getRole() != UserRole.CLIENT) {
            LOGGER.warning("Unauthorized access attempt to deleteOrder");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String orderIdStr = request.getParameter("orderId");
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            LOGGER.warning("Invalid order ID provided for deletion");
            response.sendRedirect(request.getContextPath() + "/client/orders?error=Invalid+order+ID");
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdStr);
            Optional<Order> orderOptional = orderService.getOrderById(orderId);

            if (!orderOptional.isPresent()
                    || !orderOptional.get().getClient().getUser().getId().equals(currentUser.getId())) {
                LOGGER.warning("Attempt to delete non-existent or unauthorized order");
                response.sendRedirect(
                        request.getContextPath() + "/client/orders?error=Order+not+found+or+unauthorized");
                return;
            }

            Order order = orderOptional.get();

            if (order.getStatus() != OrderStatus.CANCELLED) {
                LOGGER.warning("Attempt to delete non-cancelled order");
                response.sendRedirect(
                        request.getContextPath() + "/client/orders?error=Only+cancelled+orders+can+be+deleted");
                return;
            }

            orderService.deleteOrder(order);
            LOGGER.info("Order " + orderId + " deleted successfully");
            response.sendRedirect(request.getContextPath() + "/client/orders?success=Order+deleted+successfully");
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid order ID format: " + orderIdStr);
            response.sendRedirect(request.getContextPath() + "/client/orders?error=Invalid+order+ID+format");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting order", e);
            response.sendRedirect(
                    request.getContextPath() + "/client/orders?error=Error+deleting+order:+" + e.getMessage());
        }
    }

}
