package com.siquanc.app.compose;

import com.siquanc.app.build.BuildPipeline;
import com.siquanc.app.build.BuildRequest;
import com.siquanc.app.common.CommonService;
import com.siquanc.app.query.QueryRequest;
import com.siquanc.app.query.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@Service
public class NewService {

    @Autowired
    CommonService commonService;

    /**
     *
     * @return
     */
    public ArrayList<String> getInputs() {
        ArrayList<String> listOfTasks = new ArrayList<>();
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream("src/main/resources/scripts/inputs.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                listOfTasks.add(strLine);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listOfTasks;
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getOutputs() {
        ArrayList<String> listOfTasks = new ArrayList<>();
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream("src/main/resources/scripts/outputs.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                listOfTasks.add(strLine);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listOfTasks;
    }

    /**
     *
     * @param component
     */
    public String addComponent(QAComponent component) {
        Map<String, ArrayList<String>> mapping = new HashMap<>();
        String fileName = null;
        try {
            fileName = "src/main/resources/scripts/components.txt";
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.write("\n");
            String view = buildView(component);
            writer.write(view);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Component created";
    }

    /**
     *
     * @param component
     * @return
     */
    private String buildView(QAComponent component) {

        FileInputStream fstream = null;
        String params = null;
        Map<String,String> paramTagsIn = new LinkedHashMap<>();
        Map<String,String> paramTagsOut = new LinkedHashMap<>();
        StringBuilder ruleHeadParams = new StringBuilder();
        StringBuilder ruleBodyParams = new StringBuilder();
        try {
            fstream = new FileInputStream("src/main/resources/scripts/task_parameters.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                if(strLine.split(";")[0].equals(component.getTasks().get(0))){
                    params = strLine.split(";")[1];
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fstream = new FileInputStream("src/main/resources/scripts/parameters_tags.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                for (String in : component.getInputs()) {
                    if(strLine.split(",")[0].equals(in)){
                        paramTagsIn.put(in,strLine.split(",")[1]);
                    }
                }
                for (String out : component.getOutputs()) {
                    if(strLine.split(",")[0].equals(out)){
                        paramTagsOut.put(out,strLine.split(",")[1]);
                    }
                }

            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(Map.Entry<String, String> entry : paramTagsIn.entrySet()) {
            ruleHeadParams.append(entry.getValue()).append(",");
        }
        for(Map.Entry<String, String> entry : paramTagsOut.entrySet()) {
            ruleHeadParams.append(entry.getValue()).append(",");
        }
        ruleHeadParams.setLength(ruleHeadParams.length()-1);
        ruleBodyParams.append(params.split("\\[")[1].split("\\]")[0]);
        StringBuilder view = new StringBuilder();
        ArrayList<String> tasks = component.getTasks();
        ArrayList<String> actualTasks = new ArrayList<>();

        BiMap<String, String> mappedTasks = commonService.getTaskRoleMapping(tasks);

        for (int i=0; i<tasks.size(); i++) {
            actualTasks.add(mappedTasks.inverse().get(tasks.get(i)));
        }
        ArrayList<String> inputs = component.getInputs();
        ArrayList<String> outputs = component.getOutputs();
        view.append(component.getComponentName()).append("(").append(ruleHeadParams).append(")");
        view.append(" :- ");
        for (int i = 0; i< actualTasks.size(); i++) {
            view.append(actualTasks.get(i)).append("(").append(ruleBodyParams).append(")");
            view.append(", ");
        }
        for (int i = 0; i< inputs.size(); i++) {
            view.append(inputs.get(i)).append("(").append(paramTagsIn.get(inputs.get(i))).append(")");
            view.append(", ");
        }
        for (int i = 0; i< outputs.size(); i++) {
            view.append(outputs.get(i)).append("(").append(paramTagsOut.get(outputs.get(i))).append(")");
            if (i < outputs.size()-1) {
                view.append(", ");
            }
        }
        view.append(", cost(2.5)");
        return view.toString();
    }
}
