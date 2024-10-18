package com.centrale.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
import com.centrale.model.entity.Admin;
import com.centrale.model.entity.Order;
import com.centrale.model.entity.Product;
import com.centrale.model.entity.User;
import com.centrale.model.enums.UserRole;
import com.centrale.repository.impl.AdminRepositoryImpl;
import com.centrale.repository.impl.OrderRepositoryImpl;
import com.centrale.repository.impl.ProductRepositoryImpl;
import com.centrale.repository.impl.UserRepositoryImpl;
import com.centrale.service.AdminService;
import com.centrale.service.OrderService;
import com.centrale.service.ProductService;
import com.centrale.service.UserService;

public class AdminController extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
    private OrderService orderService;
    private UserService userService;
    private ProductService productService;
    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        super.init();
        orderService = new OrderService(new OrderRepositoryImpl());
        userService = new UserService(new UserRepositoryImpl());
        productService = new ProductService(new ProductRepositoryImpl());
        adminService = new AdminService(new AdminRepositoryImpl());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            pathInfo = "/";
        }

        switch (pathInfo) {
            case "/dashboard":
                showDashboard(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("Admin dashboard accessed");
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        LOGGER.info("Admin dashboard accessed");
        ServletContext servletContext = getServletContext();
        TemplateEngine engine = ThymeleafConfig.getTemplateEngine(servletContext);
        Admin admin = adminService.findByUser(currentUser);

        WebContext context = new WebContext(request, response, servletContext);
        context.setVariable("pageTitle", "Admin Dashboard");
        context.setVariable("admin", admin);
        engine.process("admin/dashboard", context, response.getWriter());
    }

    

}
