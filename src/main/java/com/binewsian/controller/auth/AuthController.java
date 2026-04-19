package com.binewsian.controller.auth;

import com.binewsian.constant.AppConstant;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.model.User;
import com.binewsian.service.AuthService;
import com.binewsian.service.RememberMeSvc;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RememberMeSvc rememberMeSvc;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, @RequestParam(required = false) String logout,
                            HttpServletRequest request, HttpSession session, Model model) {

        // Check if already logged in via session
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }

        // Check remember me token
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("remember_token".equals(cookie.getName())) {
                    User user = rememberMeSvc.validateTokenAndGetUser(cookie.getValue());
                    if (user != null) {
                        // Auto login
                        session.setAttribute("user", user);
                        return "redirect:/dashboard";
                    }
                }
            }
        }

        if (error != null) {
            if ("unauthorized".equals(error)) {
                model.addAttribute("error", "Please log in first!");
            } else {
                model.addAttribute("error", AppConstant.INCORRECT_EMAIL_PASSWORD);
            }
        }
        if (logout != null) {
            model.addAttribute("message", "You have successfully logged out.");
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        @RequestParam(required = false) String rememberMe, HttpSession session,
                        HttpServletResponse response, Model model) {
        User user;

        try {
            user = authService.authenticate(email, password);
        } catch (BiNewsianException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return "login";
        }

        // Set session
        session.setAttribute("user", user);
        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        // Handle Remember Me
        if ("on".equals(rememberMe)) {
            String token = rememberMeSvc.createToken(email.toLowerCase());
            Cookie cookie = new Cookie("remember_token", token);
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        } else {
            // Remove remember me cookie if not checked
            Cookie cookie = new Cookie("remember_token", "");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        if ("Contributor".equals(user.getRole().getDisplayName())) {
            return "redirect:/contributor/content";
        } else if ("Admin".equals(user.getRole().getDisplayName())) {
            return "redirect:/admin/panel";
        } else {
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password,
                           @RequestParam String confirmPassword, @RequestParam String email,
                           RedirectAttributes redirectAttributes, Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Password does not match!");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        }

        try {
            authService.register(username, password, email);
            redirectAttributes.addFlashAttribute("success", "Registration successful!");
            return "redirect:/login";
        } catch (BiNewsianException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "register";
        } catch (Exception e) {
            log.error("Error register", e);
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        // Get user before invalidating session
        User user = (User) session.getAttribute("user");

        // Delete remember me token
        if (user != null) {
            rememberMeSvc.deleteTokenByEmail(user.getEmail());
        }

        // Delete cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("remember_token".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }

        session.invalidate();
        return "redirect:/login?logout";
    }

}
