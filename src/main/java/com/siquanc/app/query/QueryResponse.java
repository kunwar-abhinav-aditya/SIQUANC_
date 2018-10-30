package com.siquanc.app.query;

import java.util.ArrayList;
import java.util.UUID;

public class QueryResponse {

    public QueryResponse(){}

    public QueryResponse(ArrayList<String> queryResponseStrings) {
        this.queryResponseStrings = queryResponseStrings;
    }

    private ArrayList<String> queryResponseStrings;

    private String queryIdentifier;

    private String fullResponse;

    public ArrayList<String> getQueryResponseStrings() {
        return queryResponseStrings;
    }

    public void setQueryResponseStrings(ArrayList<String> queryResponseStrings) {
        this.queryResponseStrings = queryResponseStrings;
    }

    public String getQueryIdentifier() {
        return queryIdentifier;
    }

    public void setQueryIdentifier(String queryIdentifier) {
        this.queryIdentifier = queryIdentifier;
    }

    public String getFullResponse() {
        return fullResponse;
    }

    public void setFullResponse(String fullResponse) {
        this.fullResponse = fullResponse;
    }
}
