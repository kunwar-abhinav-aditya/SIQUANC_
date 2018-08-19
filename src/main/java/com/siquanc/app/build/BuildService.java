package com.siquanc.app.build;

import com.siquanc.app.common.CommonService;
import com.siquanc.app.query.QueryRequest;
import com.siquanc.app.query.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BuildService {

    @Autowired
    QueryService queryService;

    @Autowired
    CommonService commonService;

    /**
     *
     * @return
     */
    public ArrayList<String> getAllTasks() {
        ArrayList<String> listOfTasks = new ArrayList<>();
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream("src/main/resources/scripts/tasks.txt");
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
     * @param buildRequest
     */
    public Map<String, ArrayList<String>> getComponents(BuildRequest buildRequest) {
        Map<String, ArrayList<String>> mapping = mapTasksToComponents();
        Map<String, String> tasksRoleMapping = commonService.getTaskRoleMapping(buildRequest.getSelectedTasks());
        //Using linkedhashmap to ensure insertion order, earlier it was just hashmap
        Map<String, ArrayList<String>> mappingFinal = new LinkedHashMap<>();
        for (String key: tasksRoleMapping.keySet()) {
            mappingFinal.put(tasksRoleMapping.get(key), mapping.get(key));
        }
        return mappingFinal;
    }

    /**
     *
     * @return
     */
    private Map<String, ArrayList<String>> mapTasksToComponents() {
        Map<String, ArrayList<String>> mapping = new HashMap<>();
        Map<String, Map<String, Double>> mappingWithCost = new HashMap<>();
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream("src/main/resources/scripts/components.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                String[] strg = strLine.split(":-");
                String taskName = strg[1].split(",")[0];
                String taskNameCleaned = taskName.split("\\(")[0].trim();
                String cost = strg[1].split("cost")[1];
                cost = cost.replace("(", "");
                cost = cost.replace(")", "");
                double costNumeric = Double.parseDouble(cost);
                if (mappingWithCost.containsKey(taskNameCleaned)) {
                    mappingWithCost.get(taskNameCleaned).put(strg[0].split("\\(")[0].trim(), costNumeric);
                }
                else {
                    Map<String, Double> values = new HashMap<>();
                    values.put(strg[0].split("\\(")[0].trim(), costNumeric);
                    mappingWithCost.put(taskNameCleaned, values);
                }
            }
            br.close();
            mapping = reorderBasedOnCost(mappingWithCost);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mapping;
    }

    /**
     *
     * @param buildPipeline
     * @return
     */
    public void buildPipelineRunQuery(BuildPipeline buildPipeline) {
        QueryRequest queryRequest = buildPipeline.getQueryRequest();
        queryService.getQueryResponse(queryRequest);
    }

    /**
     *
     * @param mappingWithCost
     * @return
     */
    private Map<String,ArrayList<String>> reorderBasedOnCost(Map<String, Map<String, Double>> mappingWithCost) {

        for (Map.Entry taskComp: mappingWithCost.entrySet()) {
            Map<String, Double> compCosts = (Map<String, Double>) taskComp.getValue();
            Map<String, Double> sorted = compCosts.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            taskComp.setValue(sorted);
        }
        //Now that sorting has been done as per the cost, we just return the map with task and component names in decreasing
        //order of costs
        Map<String, ArrayList<String>> mapToReturn = new HashMap<>();
        for (Map.Entry taskComp: mappingWithCost.entrySet()) {
            ArrayList<String> components = new ArrayList<>();
            for (Map.Entry compCosts: ((Map<String, Double>)taskComp.getValue()).entrySet()) {
                components.add((String) compCosts.getKey());
            }
            mapToReturn.put((String) taskComp.getKey(), components);
        }
        return mapToReturn;
    }
}
