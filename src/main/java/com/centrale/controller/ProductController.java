package com.centrale.controller;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.centrale.config.ThymeleafConfig;
import com.centrale.model.entity.Product;
import com.centrale.repository.ProductRepository;
import com.centrale.repository.impl.ProductRepositoryImpl;
import com.centrale.service.ProductService;

public class ProductController extends HttpServlet {
    private ProductService productService;

    @Override
    public void init() throws ServletException {
        super.init();
        ProductRepository productRepository = new ProductRepositoryImpl();
        productService = new ProductService(productRepository);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                try {
                    Long productId = Long.parseLong(pathParts[1]);
                    Optional<Product> productOptional = productService.getProductById(productId);
                    if (productOptional.isPresent()) {
                        Product product = productOptional.get();
                        TemplateEngine templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
                        WebContext context = new WebContext(request, response, getServletContext());
                        context.setVariable("pageTitle", product.getName());
                        context.setVariable("product", product);
                        context.setVariable("content", "client/product-detail");
                        templateEngine.process("layouts/main-layout", context, response.getWriter());
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}