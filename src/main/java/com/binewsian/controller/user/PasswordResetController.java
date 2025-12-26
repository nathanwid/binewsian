package com.binewsian.controller.user;

import com.binewsian.constant.AppConstant;
import com.binewsian.exception.BiNewsianException;
import com.binewsian.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "user/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, HttpServletRequest request, Model model) {
        try {
            String appUrl = request.getScheme() + "://" + request.getServerName() +
                    ":" + request.getServerPort() + request.getContextPath();

            passwordResetService.createPasswordResetTokenForUser(email, appUrl);

            model.addAttribute("message", "Password reset link has been sent to your email.");
        } catch (BiNewsianException ex) {
            model.addAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            model.addAttribute("error", "Error processing request. Please try again.");
        }
        return "user/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, RedirectAttributes redirectAttributes, Model model) {
        boolean valid = passwordResetService.validatePasswordResetToken(token);

        if (!valid) {
            redirectAttributes.addFlashAttribute("error", AppConstant.INVALID_ERROR_PASSWORD_RESET_LINK);
            return "redirect:/login";
        }

        model.addAttribute("token", token);
        return "user/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes, Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);
            return "user/reset-password";
        }

        try {
            passwordResetService.updatePassword(token, password);
            redirectAttributes.addFlashAttribute("message", "Password successfully reset.");
            return "redirect:/login";
        } catch (BiNewsianException ex) {
            model.addAttribute("error", ex.getMessage());
            return "redirect:/login";
        } catch (Exception ex) {
            model.addAttribute("error", "Error resetting password. Please try again.");
            model.addAttribute("token", token);
            return "user/reset-password";
        }
    }

}
