package com.siquanc.app.query;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ListMultimap;
import helper.Constants;
import helper.RDFQueryComponents;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.core.HttpHeaders;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.*;
import java.nio.file.*;
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
        queryRequest.setQueryType(QueryType.FIXED);
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

            ArrayList<String> components = new ArrayList<>();
            components = getStaticComponents();
            queryRequest.setTasks(getStaticTasks());
            for (String component : components) {
                params.put("componentlist[]", component);
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
        int numResources = 0;
        if (qr.getRequiresQueryBuilding()) {
            for (int i = 0; i < componentResultsSize; i++) {
                if ((i + numberOfComponents + 1) >= componentResultsSize) {
                    if (i != componentResultsSize - 2) {
                        toKeep.add(componentResults.get(i));
                    }
                }
            }
            String res = toKeep.get(toKeep.size() - 1);
            String resURLs = res.substring(2, res.length()-2);
            resURLs = resURLs.replace("\"\"", "\"");
            resURLs = resURLs.replace(" ", "");
            toKeep.remove(toKeep.size()-1);
            if (res.contains(Constants.qbDelimiter)) {
                JSONObject jsonObj = new JSONObject(resURLs);
                JSONObject bindings = (JSONObject) jsonObj.get("results");
                JSONArray resourcesList = (JSONArray) bindings.get("bindings");
                if (resourcesList.isNull(0)) {
                    numResources+=1;
                    toKeep.add("No result found");
                } else {
                    for (Object resource : resourcesList) {
                        numResources++;
                        JSONObject jsonObject = (JSONObject) resource;
                        JSONObject uri = new JSONObject();
                        if (jsonObject.has("uri")) {
                            uri = (JSONObject) ((JSONObject) resource).get("uri");
                        }
                        if (jsonObject.has("c")) {
                            uri = (JSONObject) ((JSONObject) resource).get("c");
                        }
                        String url = (String) uri.get("value");
                        if (url.contains(",_")) {
                            toKeep.add(url.split(",_")[0]);
                        } else {
                            toKeep.add(url);
                        }
                    }
                }
            } else {
                toKeep.add("No result found");
            }
        }
        else {
            for (int i = 0; i < componentResultsSize; i++) {
                if ((i + numberOfComponents) >= componentResultsSize) {
                    toKeep.add(componentResults.get(i));
                }
            }
        }
        for (int i = 0; i < toKeep.size(); i++) {
            if (qr.getRequiresQueryBuilding()) {
                if (i < (toKeep.size() - numResources)) {
                    toKeep.set(i, toKeep.get(i).split(",")[1]);
                }
            } else {
                toKeep.set(i, toKeep.get(i).split(",")[1]);
            }
        }
        if (qr.getTasks().contains("NER")) {
            int nerLocation = qr.getTasks().indexOf("NER");
            toKeep.set(nerLocation, "No output");
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
     * @param resourceURLs
     * @return
     * @throws IOException
     */
    public ArrayList<DBPediaResource> getLeadAndAbstract(ArrayList<String> resourceURLs) throws IOException {
        ArrayList<DBPediaResource> dbPediaResources = new ArrayList<DBPediaResource>();
        for (String resourceURL : resourceURLs) {
            DBPediaResource dbPediaResource = new DBPediaResource();
            Document doc = Jsoup.connect(resourceURL).get();
            String name = doc.select("#title > a").text();
            Elements leadText = doc.select(".lead");
            Element abstractTextEnglish = doc.select("table").select("span[xml:lang=\"en\"]").first();
            if (name != null) {
                dbPediaResource.setName(name);
            }
            if (leadText != null) {
                dbPediaResource.setLeadText(leadText.html());
            }
            if (abstractTextEnglish != null) {
                dbPediaResource.setAbstractText(abstractTextEnglish.html());
            }
            dbPediaResources.add(dbPediaResource);
        }
        //String wikiURL = doc.select("span.literal").last().child(0).attr("href");
        return dbPediaResources;
    }
}