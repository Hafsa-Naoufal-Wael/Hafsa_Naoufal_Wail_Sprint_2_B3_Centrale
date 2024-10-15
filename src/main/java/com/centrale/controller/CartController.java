package com.centrale.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.centrale.config.ThymeleafConfig;
import com.centrale.model.entity.Order;
import com.centrale.model.entity.OrderItem;
import com.centrale.model.entity.Product;
import com.centrale.model.entity.User;
import com.centrale.repository.impl.OrderRepositoryImpl;
import com.centrale.repository.impl.ProductRepositoryImpl;
import com.centrale.service.OrderService;
import com.centrale.service.ProductService;

public class CartController extends HttpServlet {
    private ProductService productService;
    private OrderService orderService;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        super.init();
        productService = new ProductService(new ProductRepositoryImpl());
        orderService = new OrderService(new OrderRepositoryImpl());
        templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        if (action == null || action.equals("/")) {
            viewCart(request, response);
        } else if (action.equals("/checkout")) {
            showCheckoutPage(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        if (action == null || action.equals("/")) {
            addToCart(request, response);
        } else if (action.equals("/remove")) {
            removeFromCart(request, response);
        } else if (action.equals("/checkout")) {
            processCheckout(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void viewCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        List<OrderItem> cartItems = (List<OrderItem>) session.getAttribute("cart");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        double total = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
                .sum();

        WebContext context = new WebContext(request, response, getServletContext());
        context.setVariable("cartItems", cartItems);
        context.setVariable("total", total);
        context.setVariable("pageTitle", "Shopping Cart");

        templateEngine.process("client/shopping-cart", context, response.getWriter());
    }

    private void showCheckoutPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        List<OrderItem> cartItems = (List<OrderItem>) session.getAttribute("cart");
        if (cartItems == null || cartItems.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        double total = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
                .sum();

        WebContext context = new WebContext(request, response, getServletContext());
        context.setVariable("cartItems", cartItems);
        context.setVariable("total", total);
        context.setVariable("pageTitle", "Checkout");

        templateEngine.process("client/checkout", context, response.getWriter());
    }

    private void addToCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productIdParam = request.getParameter("productId");
        String quantityParam = request.getParameter("quantity");

        if (productIdParam == null || quantityParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing productId or quantity");
            return;
        }

        try {
            Long productId = Long.parseLong(productIdParam);
            int quantity = Integer.parseInt(quantityParam);

            Product product = productService.getProductById(productId).orElse(null);
            if (product != null) {
                OrderItem orderItem = new OrderItem(product, quantity);

                HttpSession session = request.getSession();
                List<OrderItem> cart = (List<OrderItem>) session.getAttribute("cart");
                if (cart == null) {
                    cart = new ArrayList<>();
                }
                cart.add(orderItem);
                session.setAttribute("cart", cart);

                response.sendRedirect(request.getContextPath() + "/cart");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid productId or quantity");
        }
    }

    private void removeFromCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long productId = Long.parseLong(request.getParameter("productId"));

        HttpSession session = request.getSession();
        List<OrderItem> cart = (List<OrderItem>) session.getAttribute("cart");
        if (cart != null) {
            cart.removeIf(item -> item.getProduct().getId().equals(productId));
            session.setAttribute("cart", cart);
        }

        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private void processCheckout(HttpServletRequest request, HttpServletResponse response)
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

        // Create order using OrderService
        Order order = orderService.createOrder(user, cartItems, shippingAddress);

        // Clear the cart
        session.removeAttribute("cart");

        // Redirect to order confirmation page
        response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + order.getId());
    }
}