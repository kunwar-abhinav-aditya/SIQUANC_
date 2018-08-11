package com.siquanc.app.build;

import com.siquanc.app.query.QueryRequest;

import java.util.ArrayList;

public class BuildPipeline {

    private QueryRequest queryRequest;

    private ArrayList<String> pipeline;

    public BuildPipeline(){};

    public ArrayList<String> getPipeline() {
        return pipeline;
    }

    public void setPipeline(ArrayList<String> pipeline) {
        this.pipeline = pipeline;
    }

    public QueryRequest getQueryRequest() {
        return queryRequest;
    }

    public void setQueryRequest(QueryRequest queryRequest) {
        this.queryRequest = queryRequest;
    }
}
