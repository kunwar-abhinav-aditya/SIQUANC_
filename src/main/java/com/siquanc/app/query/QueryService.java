package com.siquanc.app.query;
import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Service;
import org.apache.jena.rdf.model.ModelFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class QueryService {
    
    /**
     *
     * @param queryRequest
     */
    public QueryResponse getQueryResponse(QueryRequest queryRequest) throws IOException {
        String response = startQanary(queryRequest);
        if (response.equals("started")) {
            readResponseFromTTL();
        }
        return null;
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

    private String[] readResponseFromTTL() {
        Model model = ModelFactory.createDefaultModel();
        try {
            model.read(new FileInputStream("test.ttl"),null,"TTL");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

}