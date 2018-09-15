package com.siquanc.app.compose;

import com.siquanc.app.build.BuildPipeline;
import com.siquanc.app.build.BuildRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:10000")
@RestController
@RequestMapping("/compose")
public class ComposeController {

    @Autowired
    ComposeService composeService;


    @GetMapping(path="/inputs", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ArrayList<String> getInputs() {
        return composeService.getInputs();
    }

    @GetMapping(path="/outputs", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ArrayList<String> getOutputs() {
        return composeService.getOutputs();
    }

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public String addComponent(@RequestBody QAComponent qaComponent) {
        return composeService.addComponent(qaComponent);
    }
}
