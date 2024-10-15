package com.centrale.controller;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.centrale.config.ThymeleafConfig;
import com.centrale.model.entity.User;
import com.centrale.model.enums.UserRole;
import com.centrale.repository.impl.UserRepositoryImpl;
import com.centrale.service.AuthService;

public class AuthController extends HttpServlet {

    private AuthService authService;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        super.init();
        authService = new AuthService(new UserRepositoryImpl());
        templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getPathInfo();
        switch (action) {
            case "/login":
                showLoginForm(request, response);
                break;
            case "/register":
                showRegisterForm(request, response);
                break;
            case "/logout":
                logout(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getPathInfo();
        switch (action) {
            case "/login":
                processLogin(request, response);
                break;
            case "/register":
                processRegister(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showLoginForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebContext context = new WebContext(request, response, getServletContext());
        context.setVariable("pageTitle", "Login");
        templateEngine.process("auth/login", context, response.getWriter());
    }

    private void showRegisterForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebContext context = new WebContext(request, response, getServletContext());
        context.setVariable("pageTitle", "Register");
        templateEngine.process("auth/register", context, response.getWriter());
    }

    private void processLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        Optional<User> userOptional = authService.login(email, password);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            response.sendRedirect(request.getContextPath() + "/");
        } else {
            response.sendRedirect(request.getContextPath() + "/auth/login?error=true");
        }
    }

    private void processRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        User user = authService.register(firstName, lastName, email, password, UserRole.CLIENT);
        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            response.sendRedirect(request.getContextPath() + "/");
        } else {
            response.sendRedirect(request.getContextPath() + "/auth/register?error=true");
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/");
    }
}