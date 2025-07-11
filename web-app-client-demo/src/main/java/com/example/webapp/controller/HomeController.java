package com.example.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("message", "Welcome to Spring Boot Web Application!");
        return "home";
    }

    @GetMapping("/")
    public String root(Model model) {
        model.addAttribute("message", "Welcome to Spring Boot Web Application!");
        return "home";
    }

} 