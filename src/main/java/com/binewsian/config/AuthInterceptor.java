package com.binewsian.config;

import com.binewsian.annotation.RequireRole;
import com.binewsian.constant.AppConstant;
import com.binewsian.enums.Role;
import com.binewsian.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        if (requireRole != null) {
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute("user") == null) {
                response.sendRedirect("/login?error=unauthorized");
                return false;
            }

            User user = (User) session.getAttribute("user");
            Role[] requiredRoles = requireRole.value();

            boolean hasRole = false;
            for (Role role : requiredRoles) {
                if (user.getRole() == role) {
                    hasRole = true;
                    break;
                }
            }

            if (!hasRole) {
                response.sendRedirect("/access-denied");
                return false;
            }
        }

        return true;
    }
}
