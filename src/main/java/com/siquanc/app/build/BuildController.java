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

@RestController
@RequestMapping("/buildservice")
public class BuildController {

    @Autowired
    BuildService buildService;


    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ArrayList<String> getAllTasks() {
        return buildService.getAllTasks();
    }

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public Map<String, ArrayList<String>> getTasks(@RequestBody BuildRequest buildRequest) throws IOException, InterruptedException, ParserConfigurationException, SAXException, TransformerException {
        return buildService.getComponents(buildRequest);
    }

    @PostMapping(path="/pipeline", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void buildPipelineRunQuery(@RequestBody BuildPipeline buildPipeline) {
        buildService.buildPipelineRunQuery(buildPipeline);
    }
}
