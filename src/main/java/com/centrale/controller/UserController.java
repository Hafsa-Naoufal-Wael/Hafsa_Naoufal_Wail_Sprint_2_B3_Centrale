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
import com.centrale.model.entity.Admin;
import com.centrale.model.entity.Client;
import com.centrale.model.entity.User;
import com.centrale.model.enums.UserRole;
import com.centrale.repository.impl.AdminRepositoryImpl;
import com.centrale.repository.impl.ClientRepositoryImpl;
import com.centrale.repository.impl.UserRepositoryImpl;
import com.centrale.service.AdminService;
import com.centrale.service.ClientService;
import com.centrale.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserController extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private UserService userService;
    private AdminService adminService;
    private ClientService clientService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        userService = new UserService(new UserRepositoryImpl());
        adminService = new AdminService(new AdminRepositoryImpl());
        clientService = new ClientService(new ClientRepositoryImpl());
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();

        if ("/admin/users".equals(servletPath)) {
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

        if ("/admin/users".equals(servletPath)) {
            handleAdminPost(request, response, pathInfo);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleAdminGet(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        if ("/get".equals(pathInfo)) {
            getUser(request, response);
        } else {
            showUsers(request, response);
        }
    }

    private void handleAdminPost(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        if ("/add".equals(pathInfo)) {
            addUser(request, response);
        } else if ("/edit".equals(pathInfo)) {
            editUser(request, response);
        } else if ("/delete".equals(pathInfo)) {
            deleteUser(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void showUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Admin admin = adminService.findByUser(currentUser);
        if (admin == null || admin.getAccessLevel() < 2) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        int page = 1;
        int pageSize = 10;
        String search = request.getParameter("search");

        if (request.getParameter("page") != null) {
            page = Integer.parseInt(request.getParameter("page"));
        }

        List<User> users = userService.getUsersPaginated(page, pageSize, search);
        int totalUsers = userService.getTotalUsersCount(search);
        int totalPages = (int) Math.ceil((double) totalUsers / pageSize);

        ServletContext servletContext = getServletContext();
        TemplateEngine engine = ThymeleafConfig.getTemplateEngine(servletContext);

        WebContext context = new WebContext(request, response, servletContext);
        context.setVariable("users", users);
        context.setVariable("currentPage", page);
        context.setVariable("totalPages", totalPages);
        context.setVariable("pageTitle", "Manage Users");
        context.setVariable("admin", admin);

        engine.process("admin/manage-users", context, response.getWriter());
    }

    private void getUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdminAuthorized(request, response, 1)) {
            return;
        }

        Long id = Long.parseLong(request.getParameter("id"));
        Optional<User> userOptional = userService.getUserById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(user));
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void addUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdminAuthorized(request, response, 2)) {
            return;
        }

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String roleStr = request.getParameter("role");

        try {
            UserRole role = UserRole.valueOf(roleStr);
            User newUser = new User();
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRole(role);
            userService.saveUser(newUser);

            if (role == UserRole.ADMIN) {
                String accessLevelStr = request.getParameter("accessLevel");
                int accessLevel = Integer.parseInt(accessLevelStr);
                Admin newAdmin = new Admin();
                newAdmin.setUser(newUser);
                newAdmin.setAccessLevel(accessLevel);
                adminService.saveAdmin(newAdmin);
            } else if (role == UserRole.CLIENT) {
                String address = request.getParameter("address");
                String phoneNumber = request.getParameter("phoneNumber");
                Client newClient = new Client();
                newClient.setUser(newUser);
                newClient.setAddress(address != null ? address : "");
                newClient.setPhoneNumber(phoneNumber != null ? phoneNumber : "");
                clientService.saveClient(newClient);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("User added successfully");
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error adding user: ", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid input");
        }
    }

    private void editUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdminAuthorized(request, response, 2)) {
            return;
        }

        Long id = Long.parseLong(request.getParameter("id"));
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String roleStr = request.getParameter("role");

        Optional<User> userOptional = userService.getUserById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            UserRole newRole = UserRole.valueOf(roleStr);
            user.setRole(newRole);

            userService.updateUser(user);

            if (newRole == UserRole.CLIENT) {
                String address = request.getParameter("address");
                String phoneNumber = request.getParameter("phoneNumber");
                Client client = clientService.findByUser(user);
                if (client == null) {
                    client = new Client();
                    client.setUser(user);
                }
                client.setAddress(address != null ? address : "");
                client.setPhoneNumber(phoneNumber != null ? phoneNumber : "");
                clientService.saveClient(client);
            } else if (newRole == UserRole.ADMIN) {
                String accessLevelStr = request.getParameter("accessLevel");
                int accessLevel = Integer.parseInt(accessLevelStr);
                Admin admin = adminService.findByUser(user);
                if (admin == null) {
                    admin = new Admin();
                    admin.setUser(user);
                }
                admin.setAccessLevel(accessLevel);
                adminService.saveAdmin(admin);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("User updated successfully");
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
        }
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("Deleting user with ID: " + request.getParameter("id"));
        if (!isAdminAuthorized(request, response, 2)) {
            return;
        }
        LOGGER.info("User authorized to delete");
        Long id = Long.parseLong(request.getParameter("id"));
        Optional<User> userOptional = userService.getUserById(id);
        LOGGER.info("User found: " + userOptional.isPresent());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            LOGGER.info("User: " + user);
            if (user.getRole() == UserRole.ADMIN) {
                Admin admin = adminService.findByUser(user);
                if (admin != null && admin.getAccessLevel() == 2) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot delete admin with access level 2");
                    return;
                }
                adminService.deleteAdmin(admin);
            } else if (user.getRole() == UserRole.CLIENT) {
                Client client = clientService.findByUser(user);
                if (client != null) {
                    clientService.deleteClient(client);
                }
            }
            userService.deleteUser(user);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("User deleted successfully");
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
        }
    }

    private boolean isAdminAuthorized(HttpServletRequest request, HttpServletResponse response, int requiredLevel)
            throws IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return false;
        }

        Admin admin = adminService.findByUser(currentUser);
        if (admin == null || admin.getAccessLevel() < requiredLevel) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient access level");
            return false;
        }

        return true;
    }
}
