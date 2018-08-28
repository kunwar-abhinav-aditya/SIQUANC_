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
    public String build(Model model) {
        return "build";
    }

    @GetMapping("/compose")
    public String compose(Model model) {
        return "compose";
    }

    @GetMapping("/query")
    public String query(Model model) { return "query"; }

    @GetMapping("/bulk")
    public String bulk(Model model) { return "bulk"; }

    @GetMapping("/index_new")
    public String index_new(Model model) { return "index_new"; }
}
