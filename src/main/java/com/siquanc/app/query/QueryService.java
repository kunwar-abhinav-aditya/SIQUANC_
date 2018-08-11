package com.siquanc.app.query;
import helper.Constants;
import helper.RDFQueryComponents;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
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

    String returnedQuery = "";
    
    /**
     *
     * @param queryRequest
     */
    public void getQueryResponse(QueryRequest queryRequest) {
        try {
            queryRequest.setQueryType(QueryType.FIXED);
            getResultFromQanary(queryRequest);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param queryRequest
     * @return
     */
    private void getResultFromQanary(QueryRequest queryRequest) {
        try {
            QanaryIntermediateResponse qanaryIntermediateResponse = getQuerySource(queryRequest);
            returnedQuery = queryInStardog(qanaryIntermediateResponse);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     * @param queryRequest
     */
    private QanaryIntermediateResponse getQuerySource(QueryRequest queryRequest) throws IOException {

        URL url = new URL(Constants.qanaryURL);
        QanaryIntermediateResponse qanaryIntermediateResponse = null;
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            con.setUseCaches(false);

            Map<String, String> params = new LinkedHashMap<>();
            params.put("question", queryRequest.getQueryRequestString());

            if (queryRequest.getQueryType().equals(QueryType.FIXED)) {
                params.put("componentlist[]", Constants.qanarySamplePipelineComponents[0]);
                params.put("componentlist[]", Constants.qanarySamplePipelineComponents[1]);
                params.put("componentlist[]", Constants.qanarySamplePipelineComponents[2]);
            }
            if (queryRequest.getQueryType().equals(QueryType.VARIABLE)) {
                for (int i=0; i<queryRequest.getComponents().size(); i++) {
                    params.put("componentlist[]", queryRequest.getComponents().get(i));
                }
            }
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
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
    private String queryInStardog(QanaryIntermediateResponse qanaryIntermediateResponse) throws InterruptedException, IOException, ParserConfigurationException, SAXException, TransformerException {
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
                    qanaryIntermediateResponse.getInGraph() +
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return queryResponse.toString();
    }

    /**
     *
     * @return
     */
    public String getCreatedDocument() {
        return returnedQuery;
    }
}