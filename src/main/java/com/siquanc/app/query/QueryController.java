package com.siquanc.app.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/query")
public class QueryController {

    @Autowired
    QueryService queryService;

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public QueryResponse getQueryResponse(@RequestBody QueryRequest queryRequest) throws IOException, InterruptedException, ParserConfigurationException, SAXException, TransformerException {
        return queryService.getQueryResponse(queryRequest);
    }

    @PostMapping("/feedback")
    public String receiveFeedback(@RequestBody Feedback feedback) {
        return queryService.receiveFeedback(feedback);
    }

    @GetMapping("/result")
    public String getDocument() {
        return queryService.getCreatedDocument();
    }

    @PostMapping(path = "/bulk")
    public String bulkQuery(@RequestParam("file") MultipartFile file) throws IOException, InterruptedException, ParserConfigurationException, SAXException, TransformerException {
        return queryService.bulkQuery(file);
    }
}