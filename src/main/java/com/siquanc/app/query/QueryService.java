package com.siquanc.app.query;
import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Service;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class QueryService {
    
    /**
     *
     * @param queryRequest
     */
    public QueryResponse getQueryResponse(String queryRequest) throws IOException {
        QueryRequest qr = new QueryRequest(queryRequest);
        String response = startQanary(qr);
        if (response.equals("started")) {
            return readResponseFromTTL();
        }
        return new QueryResponse("No Result");
    }

    private String startQanary(QueryRequest queryRequest) {
        try {
            //String[] command = {"/bin/bash", "/src/init.sh", queryRequest.getQueryRequestString()};
            String[] command = {"scripts\\init.bat", queryRequest.getQueryRequestString()};
            new ProcessBuilder(command).start();
            return "started";
        } catch (IOException e) {
            e.printStackTrace();
            return "failed";
        }
    }

    private QueryResponse readResponseFromTTL() {
        Model model = ModelFactory.createDefaultModel();
        try {
            model.read(new FileInputStream("scripts/test.ttl"),null,"TTL");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            model.write(out, RDFLanguages.strLangRDFJSON);
            QueryResponse queryResponse = new QueryResponse(out.toString());
            return queryResponse;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}