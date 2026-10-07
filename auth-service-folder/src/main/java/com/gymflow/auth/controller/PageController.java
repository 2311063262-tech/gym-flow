package com.gymflow.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping({"/admin/dashboard", "/admin/users", "/admin/profile"})
    public String adminPages() {
        return "forward:/admin/index.html";
    }

    @GetMapping({"/staff", "/trainer", "/member"})
    public String rolePortal() {
        return "forward:/portal.html";
    }

    @GetMapping("/forbidden")
    public String forbidden() {
        return "forward:/forbidden.html";
    }
}
