package com.siquanc.app.common;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class CommonService {

    public BiMap<String, String> getTaskRoleMapping(ArrayList<String> tasks) {
        BiMap<String, String> tasksRoleMapping = HashBiMap.create();
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream("src/main/resources/scripts/tasks_role_mapping.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                String[] strg = strLine.split(",");
                if (tasks.contains(strg[1].trim())) {
                    tasksRoleMapping.put(strg[0], strg[1]);
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tasksRoleMapping;
    }
}
