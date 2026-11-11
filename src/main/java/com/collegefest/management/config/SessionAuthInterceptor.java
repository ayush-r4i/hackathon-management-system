package com.collegefest.management.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Role-aware session interceptor.
 * Checks session attributes "role", "userId", and "admin" (for backward compat).
 */
public class SessionAuthInterceptor implements HandlerInterceptor {

    private final String requiredRole;

    public SessionAuthInterceptor(String requiredRole) {
        this.requiredRole = requiredRole;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String role = (String) session.getAttribute("role");

        // Backward compatibility: old sessions that only have "admin" attribute
        if (role == null && session.getAttribute("admin") != null) {
            role = "ADMIN";
        }

        if (role == null || !role.equals(requiredRole)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        return true;
    }
}
