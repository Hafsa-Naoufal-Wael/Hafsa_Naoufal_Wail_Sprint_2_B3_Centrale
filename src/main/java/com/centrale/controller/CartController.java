package com.centrale.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.centrale.model.entity.OrderItem;
import com.centrale.model.entity.Product;
import com.centrale.repository.impl.ProductRepositoryImpl;
import com.centrale.service.ProductService;

public class CartController extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() throws ServletException {
        super.init();
        productService = new ProductService(new ProductRepositoryImpl());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = getAction(request);
        switch (action) {
            case "view":
                viewCart(request, response);
                break;
            case "checkout":
                showCheckout(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = getAction(request);
        switch (action) {
            case "add":
                addToCart(request, response);
                break;
            case "remove":
                removeFromCart(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String getAction(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return "view";
        }
        return pathInfo.substring(1);
    }

    private void addToCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Long productId = Long.parseLong(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String returnUrl = request.getParameter("returnUrl");

            Optional<Product> product = productService.getProductById(productId);
            if (product.isPresent()) {
                HttpSession session = request.getSession();
                List<OrderItem> cart = getOrCreateCart(session);
                updateCartItem(cart, product.get(), quantity);
                session.setAttribute("cart", cart);
                updateCartCount(session);
                response.sendRedirect(request.getContextPath() + (returnUrl != null ? returnUrl : "/cart")
                        + "?success=Product+added+to+cart");
            } else {
                response.sendRedirect(request.getContextPath() + (returnUrl != null ? returnUrl : "/cart")
                        + "?error=Product+not+found");
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/cart?error=An+error+occurred");

        }
    }

    private void removeFromCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Long productId = Long.parseLong(request.getParameter("id"));
            HttpSession session = request.getSession();
            List<OrderItem> cart = getOrCreateCart(session);
            cart.removeIf(item -> item.getProduct().getId().equals(productId));
            session.setAttribute("cart", cart);
            updateCartCount(session);
            response.sendRedirect(request.getContextPath() + "/cart");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/cart?error=An+error+occurred");
        }
    }

    private void viewCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        List<OrderItem> cartItems = getOrCreateCart(session);
        BigDecimal total = calculateTotal(cartItems);
        request.setAttribute("cartItems", cartItems);
        request.setAttribute("total", total);
        request.setAttribute("pageTitle", "Shopping Cart");
        renderTemplate(request, response, "client/shopping-cart");
    }


    private List<OrderItem> getOrCreateCart(HttpSession session) {
        List<OrderItem> cart = (List<OrderItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    private void updateCartItem(List<OrderItem> cart, Product product, int quantity) {
        Optional<OrderItem> existingItem = cart.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();
        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            cart.add(new OrderItem(product, quantity));
        }
    }

    private int updateCartCount(HttpSession session) {
        List<OrderItem> cart = getOrCreateCart(session);
        int cartCount = cart.stream().mapToInt(OrderItem::getQuantity).sum();
        session.setAttribute("cartCount", cartCount);
        return cartCount;
    }

    private BigDecimal calculateTotal(List<OrderItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void renderTemplate(HttpServletRequest request, HttpServletResponse response, String templateName)
            throws ServletException, IOException {
        TemplateEngine engine = ThymeleafConfig.getTemplateEngine(getServletContext());
        WebContext context = new WebContext(request, response, getServletContext());
        engine.process(templateName, context, response.getWriter());
    }

    private void showCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        List<OrderItem> cartItems = getOrCreateCart(session);
        BigDecimal total = calculateTotal(cartItems);
        request.setAttribute("cartItems", cartItems);
        request.setAttribute("total", total);
        request.setAttribute("pageTitle", "Checkout");
        renderTemplate(request, response, "client/checkout");
    }

}
