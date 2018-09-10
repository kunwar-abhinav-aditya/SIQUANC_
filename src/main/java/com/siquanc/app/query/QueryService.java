package com.siquanc.app.query;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ListMultimap;
import helper.Constants;
import helper.RDFQueryComponents;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

@Service
public class QueryService {

    private StringBuilder returnedQuery;

    /**
     *
     * @param queryRequest
     */
    public QueryResponse getQueryResponse(QueryRequest queryRequest) {
        returnedQuery = new StringBuilder();
        ArrayList<String> response = new ArrayList<>();
        QueryResponse queryResponse = null;
        if (queryRequest.getComponents().size() == 0) {
            queryRequest.setQueryType(QueryType.FIXED);
        } else {
            queryRequest.setQueryType(QueryType.VARIABLE);
        }
        try {
            response = getResultFromQanary(queryRequest);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        queryResponse = new QueryResponse(response);
        return queryResponse;
    }


    /**
     * 
     * @param queryRequest
     * @return
     */
    private ArrayList<String> getResultFromQanary(QueryRequest queryRequest) {
        ArrayList<String> response = new ArrayList<>();
        try {
            QanaryIntermediateResponse qanaryIntermediateResponse = getQuerySource(queryRequest);
            response = queryInStardog(queryRequest, qanaryIntermediateResponse);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     *
     * @param queryRequest
     */
    private QanaryIntermediateResponse getQuerySource(QueryRequest queryRequest) throws IOException, InterruptedException {

        URL url;
        BiMap<String, String> compNamePathMapping = getCompNameDirectoryMapping();
        /**
        ArrayList<String> CMD_ARRAY = new ArrayList<>();
        CMD_ARRAY.add("src/main/resources/scripts/init.sh");
        for (int i = 0; i< queryRequest.getComponents().size(); i++) {
            CMD_ARRAY.add(compNamePathMapping.get(queryRequest.getComponents().get(i)));
        }
        if (queryRequest.getQueryType().equals(QueryType.VARIABLE)) {
            ProcessBuilder pb = new ProcessBuilder(CMD_ARRAY);
            Process p = pb.start();
            BufferedReader br=new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line=br.readLine())!=null){
                System.out.println(line);
                if (line.contains("exit")) {
                    br.close();
                    break;
                }
            }
        }*/
        url = new URL(Constants.qanaryURL);
        QanaryIntermediateResponse qanaryIntermediateResponse = null;
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            con.setUseCaches(false);

            ListMultimap<String, String> params = ArrayListMultimap.create();
            params.put("question", queryRequest.getQueryRequestString());
            if (queryRequest.getQueryType().equals(QueryType.FIXED)) {
                ArrayList<String> components = new ArrayList<>();
                components = getStaticComponents();
                queryRequest.setTasks(getStaticTasks());
                for (String component : components) {
                    params.put("componentlist[]", component);
                }
            }
            if (queryRequest.getQueryType().equals(QueryType.VARIABLE)) {
                for (int i=0; i<queryRequest.getComponents().size(); i++) {
                    params.put("componentlist[]", queryRequest.getComponents().get(i));
                }
            }
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : params.entries()) {
                if (postData.length() != 0) {
                    postData.append('&');
                }
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            con.getOutputStream().write(postDataBytes);
            Reader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            StringBuilder queryIntermediateResponse = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                queryIntermediateResponse.append((char) c);
            con.disconnect();
            JSONObject jsonObject = new JSONObject(queryIntermediateResponse.toString());
            qanaryIntermediateResponse = new QanaryIntermediateResponse(jsonObject.get("endpoint").toString(), jsonObject.get("inGraph").toString(), jsonObject.get("outGraph").toString(), jsonObject.get("question").toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return qanaryIntermediateResponse;
    }

    /**
     *
     * @param qanaryIntermediateResponse
     */
    private ArrayList<String> queryInStardog(QueryRequest qr, QanaryIntermediateResponse qanaryIntermediateResponse) throws InterruptedException, IOException, ParserConfigurationException, SAXException, TransformerException {
        URL url = new URL(Constants.starDogURL);
        String basicAuth = Constants.basicAuth;
        StringBuilder queryResponse = new StringBuilder();
        //Create URL connection to simulate querying as on stardog interface
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            con.setUseCaches(false);
            String value = RDFQueryComponents.getFirstHalf() +
                    URLEncoder.encode(qanaryIntermediateResponse.getInGraph(), "UTF-8") +
                    RDFQueryComponents.getSecondHalf();
            con.setRequestProperty("query", value);
            con.setRequestProperty("Authorization", basicAuth);

            StringBuilder postData = new StringBuilder();
            postData.append(value);
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            con.getOutputStream().write(postDataBytes);

            Reader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            for (int c; (c = in.read()) >= 0; )
                queryResponse.append((char) c);
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extractComponentResultsFromResponse(qr, queryResponse);
    }

    /**
     *
     * @param qr
     * @param queryResponse
     * @return
     */
    private ArrayList<String> extractComponentResultsFromResponse(QueryRequest qr, StringBuilder queryResponse) {
        String[] lines = queryResponse.toString().split("\n");
        String result = "";
        ArrayList<String> componentResults = new ArrayList<>();
        for (int i = 0; i<lines.length; i++) {
            returnedQuery.append(lines[i]);
            if (lines[i].contains(Constants.responseLocater)) {
                result = lines[i].split(Constants.responseLocater)[1];
                componentResults.add(result);
            }
        }
        int numberOfComponents;
        if (qr.getQueryType().equals(QueryType.VARIABLE)) {
            numberOfComponents = qr.getComponents().size();
        } else {
            numberOfComponents = Constants.qanarySamplePipelineComponents.length;
            //Since in our static pipeline, we DO HAVE a query builder as a part of the pipeline
            qr.setRequiresQueryBuilding(true);
        }
        int componentResultsSize = componentResults.size();
        ArrayList<String> toKeep = new ArrayList<>();
        if (qr.getRequiresQueryBuilding()) {
            if (qr.getComponents().contains("OntoTextNED")) {
                for (int i = 0; i < componentResultsSize; i++) {
                    if ((i + numberOfComponents + 1) >= componentResultsSize) {
                        if ((i != componentResultsSize - 2) && (i != componentResultsSize - numberOfComponents - 1)) {
                            toKeep.add(componentResults.get(i));
                        }
                    }
                }
            }
            else {
                for (int i = 0; i < componentResultsSize; i++) {
                    if ((i + numberOfComponents + 1) >= componentResultsSize) {
                        if (i != componentResultsSize - 2) {
                            toKeep.add(componentResults.get(i));
                        }
                    }
                }
            }
            String res = toKeep.get(toKeep.size() - 1);
            String bindingValue = res.split(Constants.qbDelimiter1)[1].split(Constants.qbDelimiter2)[1];
            toKeep.set(toKeep.size() - 1, bindingValue);
        }
        else {
            if (qr.getComponents().contains("OntoTextNED")) {
                for (int i = 0; i < componentResultsSize; i++) {
                    if (((i + numberOfComponents + 1) >= componentResultsSize) && (i != componentResultsSize - numberOfComponents)) {
                        toKeep.add(componentResults.get(i));
                    }
                }
            }
            else {
                for (int i = 0; i < componentResultsSize; i++) {
                    if ((i + numberOfComponents) >= componentResultsSize) {
                        toKeep.add(componentResults.get(i));
                    }
                }
            }
        }
        for (int i = 0; i < toKeep.size(); i++) {
            if (qr.getRequiresQueryBuilding()) {
                if (i < toKeep.size() - 1) {
                    toKeep.set(i, toKeep.get(i).split(",")[1]);
                }
            } else {
                toKeep.set(i, toKeep.get(i).split(",")[1]);
            }
        }

        for (int i = 0; i < toKeep.size(); i++) {
            if (qr.getTasks().get(i).equals("NER")) {
                toKeep.set(i, "No output");
            }
        }
        return toKeep;
    }

    /**
     *
     * @return
     */
    public String getCreatedDocument() {
        return returnedQuery.toString();
    }

    /**
     *
     * @param feedback
     * @return
     */
    public String receiveFeedback(Feedback feedback) {
        ArrayList<String> components = new ArrayList<>();
        if (feedback.getComponents().isEmpty()) {
            components = getStaticComponents();
        } else {
            components = feedback.getComponents();
        }
        String fileName = null;
        try {
            fileName = "src/main/resources/scripts/feedback.txt";
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            if (feedback.getRating() == null) {
                feedback.setRating("0");
            }
            String view = feedback.getQuestion() + ", " + components + ", " + feedback.getRating();
            writer.write(view);
            writer.write("\n");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Response recorded";
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getStaticComponents() {
        ArrayList<String> staticComponents = new ArrayList<>();
        for (int i=0; i<Constants.qanarySamplePipelineComponents.length; i++) {
            staticComponents.add(Constants.qanarySamplePipelineComponents[i]);
        }
        return staticComponents;
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getStaticTasks() {
        ArrayList<String> staticTasks = new ArrayList<>();
        for (int i=0; i<Constants.qanarySamplePipelineRespectiveTasks.length; i++) {
            staticTasks.add(Constants.qanarySamplePipelineRespectiveTasks[i]);
        }
        return staticTasks;
    }
    /**
     *
     * @param
     * @return
     */
    public String bulkQuery(MultipartFile file) throws IOException {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        try {

            br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] question = line.split(cvsSplitBy);

                System.out.println("Question [id= " + question[0] + " , query=" + question[1] + "]");

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    public BiMap<String, String> getCompNameDirectoryMapping() {
        BiMap<String, String> compNameDirectoryMapping = HashBiMap.create();
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream("src/main/resources/scripts/component_paths.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                String[] strg = strLine.split(",");
                compNameDirectoryMapping.put(strg[0], strg[1]);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compNameDirectoryMapping;
    }
}