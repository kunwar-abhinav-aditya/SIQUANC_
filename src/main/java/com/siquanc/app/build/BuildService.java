package com.siquanc.app.build;

import com.siquanc.app.common.CommonService;
import com.siquanc.app.query.QueryRequest;
import com.siquanc.app.query.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        Map<String, ArrayList<String>> mappingFinal = new HashMap<>();
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
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream("src/main/resources/scripts/components.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                String[] strg = strLine.split(":-");
                String taskName = strg[1].split(",")[0];
                String taskNameCleaned = taskName.split("\\(")[0].trim();
                if (mapping.containsKey(taskNameCleaned)) {
                    mapping.get(taskNameCleaned).add(strg[0].split("\\(")[0].trim());
                }
                else {
                    ArrayList<String> values = new ArrayList<String>();
                    values.add(strg[0].split("\\(")[0].trim());
                    mapping.put(taskNameCleaned, values);
                }
            }
            br.close();
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
}
