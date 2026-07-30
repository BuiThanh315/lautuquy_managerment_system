package com.lautuquy.management.controller.customer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer/menu")
public class MenuController {

    @GetMapping
    public String viewMenu() {
        return "redirect:/customer/landing";
    }
}
