package com.siquanc.app.main;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
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

    @GetMapping("/new")
    public String compose(Model model) {
        return "new";
    }

    @GetMapping("/query")
    public String query(Model model) { return "query"; }

    @GetMapping("/bulk")
    public String bulk(Model model) { return "bulk"; }

    @GetMapping("/stars")
    public String stars(Model model) { return "stars"; }

    @GetMapping("/team")
    public String team(Model model) { return "team"; }

    @GetMapping("/resource")
    public String resource(Model model) { return "resource"; }
}
