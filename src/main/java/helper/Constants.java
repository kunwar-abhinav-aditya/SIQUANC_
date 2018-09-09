package helper;

import java.lang.reflect.Array;
import java.util.ArrayList;

public final class Constants {

    private Constants(){}

    public static String starDogURL = "http://localhost:5820/annex/qanary/sparql/query";
    public static String basicAuth = "Basic YWRtaW46YWRtaW4=";
    public static String qanaryURL = "http://localhost:8080/startquestionansweringwithtextquestion";

    public static String[] qanarySamplePipelineComponents = new String[]{"AmbiverseNed","AnnotationofSpotProperty","DiambiguationClass","QueryBuilder"};
    public static int DESIRED_NUMBER_OF_BEST_PIPELINES = 3;

    public static String responseLocater = "http://www.w3.org/ns/openannotation/core/hasBody";
    public static String qbDelimiter1 = "\"\"value\"\":";
    public static String qbDelimiter2 = "\"\"";




}
