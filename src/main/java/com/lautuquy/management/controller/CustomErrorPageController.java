package com.lautuquy.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller điều hướng các trang hiển thị lỗi tùy chỉnh (403, 404, 500, 400).
 */
@Controller
@RequestMapping("/error")
public class CustomErrorPageController {

    @GetMapping("/403")
    public String error403() {
        return "error/403";
    }

    @GetMapping("/404")
    public String error404() {
        return "error/404";
    }

    @GetMapping("/500")
    public String error500() {
        return "error/500";
    }

    @GetMapping("/400")
    public String error400() {
        return "error/400";
    }
}
