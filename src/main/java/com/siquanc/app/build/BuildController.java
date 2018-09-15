package com.siquanc.app.build;

import com.siquanc.app.query.QueryRequest;
import com.siquanc.app.query.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@CrossOrigin(origins = "http://frankenstein.sda.tech/build")
@RestController
@RequestMapping("/build")
public class BuildController {

    @Autowired
    BuildService buildService;


    @GetMapping(path="/tasks", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ArrayList<String> getAllTasks() {
        return buildService.getAllTasks();
    }

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public Map<String, ArrayList<String>> getComponents(@RequestBody BuildRequest buildRequest) throws IOException, InterruptedException, ParserConfigurationException, SAXException, TransformerException {
        return buildService.getComponents(buildRequest);
    }

    @PostMapping(path="/bestpipelines", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ArrayList<ArrayList<String>> getBestPipelines(@RequestBody Map<String, ArrayList<String>> componentsPerTask) throws IOException, InterruptedException, ParserConfigurationException, SAXException, TransformerException {
        return buildService.getBestPipelines(componentsPerTask);
    }

    @PostMapping(path="/pipeline", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void buildPipelineRunQuery(@RequestBody BuildPipeline buildPipeline) {
        buildService.buildPipelineRunQuery(buildPipeline);
    }
}
