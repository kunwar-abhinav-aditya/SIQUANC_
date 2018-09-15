package com.siquanc.app.main;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@CrossOrigin(origins = "http://localhost:10000/*")
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

    @GetMapping("/stars")
    public String stars(Model model) { return "stars"; }

    @GetMapping("/resource")
    public String resource(Model model) { return "resource"; }
}
