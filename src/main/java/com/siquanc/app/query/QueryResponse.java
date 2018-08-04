package com.siquanc.app.query;

import org.w3c.dom.Document;

public class QueryResponse {

    private String queryResponseString;

    public QueryResponse(){}

    public QueryResponse(String queryResponseString) {
        this.queryResponseString = queryResponseString;
    }

    public String getQueryResponseString() {
        return queryResponseString;
    }
}
