package com.siquanc.app.main;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }

    @GetMapping("/build")
    public String productListUser(Model model) {
        return "build";
    }

    @GetMapping("/compose")
    public String productListAdmin(Model model) {
        return "compose";
    }

    @GetMapping("/query")
    public String productAdmin(Model model) {
        return "query";
    }
}
