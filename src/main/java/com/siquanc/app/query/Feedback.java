package com.siquanc.app.query;

import java.util.ArrayList;

public class Feedback {

    private String rating;
    private ArrayList<String> components;
    private String question;

    public Feedback(){};

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public ArrayList<String> getComponents() {
        return components;
    }

    public void setComponents(ArrayList<String> components) {
        this.components = components;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
