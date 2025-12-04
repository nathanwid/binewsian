package com.binewsian.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorCode", "404");
                model.addAttribute("errorMessage", "Halaman Tidak Ditemukan");
                model.addAttribute("errorDescription", "Halaman yang Anda cari tidak ditemukan di server.");
                return "error/404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorCode", "500");
                model.addAttribute("errorMessage", "Internal Server Error");
                model.addAttribute("errorDescription", "Terjadi kesalahan pada server. Silakan coba lagi nanti.");
                return "error/error";
            }
        }

        model.addAttribute("errorCode", "Error");
        model.addAttribute("errorMessage", "Terjadi Kesalahan");
        model.addAttribute("errorDescription", "Mohon maaf, terjadi kesalahan yang tidak terduga.");
        return "error/error";
    }
}