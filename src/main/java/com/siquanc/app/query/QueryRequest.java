package com.siquanc.app.query;

public class QueryRequest {

    private String queryRequestString;

    private QueryRequest() {}

    public QueryRequest(String queryRequestString) {
        this.queryRequestString = queryRequestString;
    }

    public String getQueryRequestString() {
        return queryRequestString;
    }
}
