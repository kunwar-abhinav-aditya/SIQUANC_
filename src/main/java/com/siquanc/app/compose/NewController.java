package com.siquanc.app.compose;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/compose")
public class NewController {

    @Autowired
    NewService newService;


    @GetMapping(path="/inputs", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ArrayList<String> getInputs() {
        return newService.getInputs();
    }

    @GetMapping(path="/outputs", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ArrayList<String> getOutputs() {
        return newService.getOutputs();
    }

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public String addComponent(@RequestBody QAComponent qaComponent) {
        return newService.addComponent(qaComponent);
    }
}
