package helper;

public final class RDFQueryComponents {

    private RDFQueryComponents() {}

    private static final String firstHalf = "attachment=true&format=sparql-csv&query=prefix+rdf%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns" +
            "%23%3E%0Aprefix+owl%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23%3E%0Aprefix+xsd%3A+%3Chttp%3A%2F%2F" +
            "www.w3.org%2F2001%2FXMLSchema%23%3E%0Aprefix+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-" +
            "schema%23%3E%0A%0Aselect+*+from+%3C";

    private static String secondHalf = "%3E+where+%7B%3Fs+%3Fp+%3Fo+.%7D";

    public static String getFirstHalf() {
        return firstHalf;
    }

    public static String getSecondHalf() {
        return secondHalf;
    }
}
