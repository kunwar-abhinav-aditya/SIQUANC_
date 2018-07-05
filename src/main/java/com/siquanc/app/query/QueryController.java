package com.siquanc.app.query;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/query")
public class QueryController {

    private final QueryService queryService;

    public QueryController(QueryService queryService) { this.queryService = queryService; }

    @PostMapping
    public QueryResponse getQueryResponse(@RequestBody QueryRequest queryRequest) throws IOException {
        return queryService.getQueryResponse(queryRequest);
    }

}