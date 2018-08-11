package com.siquanc.app.build;

import java.util.ArrayList;

public class BuildRequest {

    private ArrayList<String> selectedTasks = new ArrayList<String>();

    public BuildRequest() {}

    public ArrayList<String> getSelectedTasks() {
        return selectedTasks;
    }

    public void setSelectedTasks(ArrayList<String> selectedTasks) {
        this.selectedTasks = selectedTasks;
    }


}
