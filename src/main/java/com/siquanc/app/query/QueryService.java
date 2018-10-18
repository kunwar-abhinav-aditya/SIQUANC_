package com.siquanc.app.query;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ListMultimap;
import helper.Constants;
import helper.RDFQueryComponents;
import org.apache.tomcat.util.http.fileupload.FileUtils;
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
    private static int zipCounter = 0;

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
     * @param
     * @return
     */
    public String bulkQuery(MultipartFile file, ArrayList<String> components, boolean requiresQueryBuilding) throws InterruptedException {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        try {
            br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            while ((line = br.readLine()) != null) {
                String[] question = line.split(cvsSplitBy);
                String questionId = question[0];
                String questionText = question[1];
                //Creating query request object
                QueryRequest qr = new QueryRequest(questionText);
                if (components.size() > 0) {
                    qr.setQueryType(QueryType.VARIABLE);
                    qr.setComponents(components);
                }
                else {
                    qr.setQueryType(QueryType.FIXED);
                    qr.setComponents(getStaticComponents());
                }
                qr.setRequiresQueryBuilding(requiresQueryBuilding);
                //Create a qanaryResponse object and get endPoint and outGraph
                QanaryIntermediateResponse qanaryResponse = getQuerySource(qr);
                if (qanaryResponse != null) {
                    String endpoint = qanaryResponse.getEndpoint();
                    String namedGraph = qanaryResponse.getOutGraph();
                    // dump the data
                    String exportFilename = "src/main/resources/bulk/" + "dump_" + questionId + ".ttl";
                    dumpGraphAndDeleteGraph(namedGraph, exportFilename);
                }
            }
            createZippedFile();

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
        return "uploaded";
    }

    private void dumpGraphAndDeleteGraph(String namedGraph, String exportFilename) throws IOException {
        // shell command (linux)
        String commandDump = Constants.starDogBinPath + "stardog data export -g " + namedGraph + " --format TURTLE qanary "
                + exportFilename + "";
        String commandDelete = Constants.starDogBinPath + "stardog data remove -g " + namedGraph + " qanary ";

        executeCommandOnShellAndLogOutput(commandDump);
        executeCommandOnShellAndLogOutput(commandDelete);
    }


    /**
     * run the dump and remove command on Stardog
     *
     * @param commandDump
     * @throws IOException
     */
    private void executeCommandOnShellAndLogOutput(String commandDump) throws IOException {
        Process proc = Runtime.getRuntime().exec(commandDump);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        while(stdInput.readLine() != null){
        }
        stdInput.close();
        stdError.close();
        proc.destroy();
    }

    /**
     *
     * @return
     */
    public BiMap<String, String> getCompNameDirectoryMapping() {
        BiMap<String, String> compNameDirectoryMapping = HashBiMap.create();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/scripts/component_paths.txt")));
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


    /**
     *
     * @return
     */
    public void createZippedFile() throws IOException {
        zipCounter++;
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        final Path path = Paths.get("src/main/resources/bulk/results.zip");
        final URI uri = URI.create("jar:file:" + path.toUri().getPath());
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            File folder = new File("src/main/resources/bulk/");
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isFile() && !fileEntry.getName().equals("results.zip")) {
                    Path externalTxtFile = Paths.get("src/main/resources/bulk/"+fileEntry.getName());
                    Path pathInZipfile = zipfs.getPath(fileEntry.getName());
                    Files.copy(externalTxtFile, pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            //URL url= new File("src/main/resources/bulk/results"+zipCounter+".zip").toURI().toURL();
        }
    }

    /**
     *
     * @return
     */
    public ResponseEntity<Resource> getTTLs() {
        String filePath = "src/main/resources/bulk/results.zip";
        Resource resource = new FileSystemResource(filePath);
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        String contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


    /**
     *
     * @return
     */
    public boolean deleteGeneratedFiles() throws IOException {
        File folder = new File("src/main/resources/bulk/");
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()) {
                fileEntry.delete();
            }
        }
        FileUtils.deleteDirectory(new File("src/main/resources/bulk/results.zip/"));
        //for (int i=0; i<zipCounter; i++) {
        //    FileUtils.deleteDirectory(new File("src/main/resources/bulk/results" + zipCounter + ".zip/"));
        //}
        return true;
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