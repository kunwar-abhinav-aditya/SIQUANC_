package com.siquanc.app.query;

public class QanaryIntermediateResponse {

    private String endpoint;
    private String inGraph;
    private String outGraph;
    private String question;

    public QanaryIntermediateResponse(){}

    public QanaryIntermediateResponse(String endpoint, String inGraph, String outGraph, String question) {
        this.endpoint = endpoint;
        this.inGraph = inGraph;
        this.outGraph = outGraph;
        this.question = question;
    }
    public String getEndpoint() {
        return endpoint;
    }

    public String getInGraph() {
        return inGraph;
    }

    public String getOutGraph() {
        return outGraph;
    }

    public String getQuestion() {
        return question;
    }
}
