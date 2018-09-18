package helper;

import java.lang.reflect.Array;
import java.util.ArrayList;

public final class Constants {

    private Constants(){}

    public static String starDogURL = "http://localhost:5820/annex/qanary/sparql/query";
    public static String basicAuth = "Basic YWRtaW46YWRtaW4=";
    public static String qanaryURL = "http://localhost:8080/startquestionansweringwithtextquestion";

    //public static String starDogPath = "/Users/SyalMac/Downloads/stardog-4.1.3/";
    public static String starDogPath = "/data/aditya/stardog";
    //public static String starDogBinPath = "/Users/SyalMac/Downloads/stardog-4.1.3/bin/";
    public static String starDogBinPath = "/data/aditya/stardog/bin/";

    public static String[] qanarySamplePipelineComponents = new String[]{"NED-DBpediaSpotlight","EarlRelationLinking","QueryBuilder"};
    public static String[] qanarySamplePipelineRespectiveTasks = new String[]{"NED","Relation Linker","Query Builder"};
    public static int DESIRED_NUMBER_OF_BEST_PIPELINES = 3;

    public static String responseLocater = "http://www.w3.org/ns/openannotation/core/hasBody";
    public static String qbDelimiter = "\"\"results\"\":";

}
