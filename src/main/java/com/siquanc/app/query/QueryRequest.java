package com.siquanc.app.query;

import java.util.ArrayList;

public class QueryRequest {

    private String queryRequestString;

    private QueryType queryType;

    private ArrayList<String> components;

    public QueryRequest() {}

    public QueryRequest(String queryRequestString) {

        this.queryRequestString = queryRequestString;
    }

    public String getQueryRequestString() {
        return queryRequestString;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public ArrayList<String> getComponents() {
        return components;
    }

    public void setComponents(ArrayList<String> components) {
        this.components = components;
    }
}
