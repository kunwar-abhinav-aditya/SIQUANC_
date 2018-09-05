package com.siquanc.app.build;

import com.siquanc.app.common.CommonService;
import com.siquanc.app.query.QueryRequest;
import com.siquanc.app.query.QueryService;
import helper.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
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
        //  LinkedHashMap to ensure insertion order, earlier it was just hashmap
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
        Map<String, ArrayList<String>> mapping = new LinkedHashMap<>();
        Map<String, Map<String, Double>> mappingWithCost = new LinkedHashMap<>();
        Map<String, Map<String, Double>> reorderedMappingWithCost = new LinkedHashMap<>();
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
                    Map<String, Double> values = new LinkedHashMap<>();
                    values.put(strg[0].split("\\(")[0].trim(), costNumeric);
                    mappingWithCost.put(taskNameCleaned, values);
                }
            }
            br.close();
            reorderedMappingWithCost = reorderBasedOnCost(mappingWithCost);
            mapping = removeCost(reorderedMappingWithCost);

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
    private Map<String, Map<String, Double>> reorderBasedOnCost(Map<String, Map<String, Double>> mappingWithCost) {

        for (Entry taskComp : mappingWithCost.entrySet()) {
            Map<String, Double> compCosts = (Map<String, Double>) taskComp.getValue();
            Map<String, Double> sorted = compCosts.entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            taskComp.setValue(sorted);
        }
        return mappingWithCost;
    }

    /**
     *
     * @param mappingWithCost
     * @return
     */
    private Map<String,ArrayList<String>> removeCost(Map<String, Map<String, Double>> mappingWithCost) {
        //Now that sorting has been done as per the cost, we just return the map with task and component names in decreasing
        //order of costs
        Map<String, ArrayList<String>> mapToReturn = new LinkedHashMap<>();
        for (Entry taskComp: mappingWithCost.entrySet()) {
            ArrayList<String> components = new ArrayList<>();
            for (Entry compCosts: ((Map<String, Double>)taskComp.getValue()).entrySet()) {
                components.add((String) compCosts.getKey());
            }
            mapToReturn.put((String) taskComp.getKey(), components);
        }
        return mapToReturn;
    }


    /**
     *
     * @param reorderedMapping
     * @return
     */
    private ArrayList<ArrayList<String>> createPipelines(Map<String, ArrayList<String>> reorderedMapping) {
        ArrayList<ArrayList<String>> mappingInListForm = new ArrayList<>();
        ArrayList<ArrayList<String>> componentCompositions = new ArrayList<>();
        for (ArrayList<String> taskComp: reorderedMapping.values()) {
            mappingInListForm.add(taskComp);
        }
        GeneratePermutations(mappingInListForm, componentCompositions, 0, new ArrayList<String>());
        return componentCompositions;
    }

    /**
     *
     * @param original
     * @param result
     * @param depth
     * @param current
     */
    private void GeneratePermutations(ArrayList<ArrayList<String>> original, ArrayList<ArrayList<String>> result, int depth, ArrayList<String> current) {
        // if depth equals number of original collections, final reached, add and return
        if(depth == original.size())
        {
            result.add(current);
            return;
        }

        // iterate from current collection and copy 'current' element N times, one for each element
        Collection<String> currentCollection = original.get(depth);
        for (String element : currentCollection) {
            ArrayList<String> copy = new ArrayList<>(current);
            copy.add(element);
            GeneratePermutations(original, result, depth + 1, copy);
        }
    }

    /**
     *
     * @param pipelines
     */
    private Map<ArrayList<String>, Double> putCostToPipelines(ArrayList<ArrayList<String>> pipelines) {
        Map<ArrayList<String>, Double> pipelinesWithCost = new LinkedHashMap<ArrayList<String>, Double>();
        for (ArrayList<String> pipeline : pipelines) {
            double costProduct = 1;
            for (String component : pipeline) {
                costProduct = costProduct * getCost(component);
            }
            pipelinesWithCost.put(pipeline, costProduct);
        }
        return pipelinesWithCost;
    }

    /**
     *
     * @param component
     * @return
     */
    private double getCost(String component) {
        return 1.0;
    }

    /**
     *
     * @param pipelinesWithCost
     * @return
     */
    private Map<ArrayList<String>, Double> reorderPipelinesBasedOnCost(Map<ArrayList<String>, Double> pipelinesWithCost) {

        LinkedHashMap<ArrayList<String>, Double> sorted = pipelinesWithCost.entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return sorted;
    }

    /**
     *
     * @param componentsPerTask
     * @return
     */
    public ArrayList<ArrayList<String>> getBestPipelines(Map<String, ArrayList<String>> componentsPerTask) {
        ArrayList<ArrayList<String>> pipelines = createPipelines(componentsPerTask);
        Map<ArrayList<String>, Double> pipelinesWithCost = new LinkedHashMap<>();
        pipelinesWithCost = putCostToPipelines(pipelines);
        Map<ArrayList<String>, Double> orderedPipelinesWithCost = reorderPipelinesBasedOnCost(pipelinesWithCost);
        ArrayList<ArrayList<String>> topPipelines = new ArrayList<>();
        int i = 0;
        for (Entry taskComp: orderedPipelinesWithCost.entrySet()) {
            if (i == Constants.DESIRED_NUMBER_OF_BEST_PIPELINES) {
                break;
            }
            topPipelines.add((ArrayList<String>)taskComp.getKey());
            i++;
        }
        return topPipelines;
    }
}
