package com.example.demo.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        int statusCode = Integer.parseInt(status.toString());
        response.setStatus(statusCode);
        String msg = "";
        String errorcode ="";
        if(statusCode == 404) {
            msg = "페이지를 찾을 수 없습니다.";
            errorcode = "404";
        }
        if(statusCode == 403) {
           msg = "접근이 거부되었습니다.";
           errorcode = "403";
        }
        if(statusCode == 500) {
            msg = "올바르지 않은 접근입니다.";
            errorcode = "500";
        }
        model.addAttribute("code", errorcode);
        model.addAttribute("msg", msg);
        return "error";
    }


    public String getErrorPath() {
        return "/error";
    }
}

