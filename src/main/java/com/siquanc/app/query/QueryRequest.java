package com.siquanc.app.query;

import java.util.ArrayList;

public class QueryRequest {

    private String queryRequestString;

    private QueryType queryType;

    private boolean requiresQueryBuilding;

    private ArrayList<String> components;

    private ArrayList<String> tasks;

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

    public boolean getRequiresQueryBuilding() {
        return requiresQueryBuilding;
    }

    public void setRequiresQueryBuilding(boolean requiresQueryBuilding) {
        this.requiresQueryBuilding = requiresQueryBuilding;
    }

    public ArrayList<String> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<String> tasks) {
        this.tasks = tasks;
    }
}
