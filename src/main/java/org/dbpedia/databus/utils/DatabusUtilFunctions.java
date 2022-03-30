package org.dbpedia.databus.utils;

import org.apache.jena.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

public final class DatabusUtilFunctions {

    static final Logger log = LoggerFactory.getLogger(DatabusUtilFunctions.class);

    public static Boolean validate(String databusID) {

        String databusBase = MossUtilityFunctions.extractBaseFromURL(databusID);

        if (databusBase == null) {
            return false;
        }

        Query query = QueryFactory.create(
                "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n" +
                "PREFIX dct:    <http://purl.org/dc/terms/>\n" +
                "PREFIX dcat:   <http://www.w3.org/ns/dcat#>\n" +
                "PREFIX db:     <https://databus.dbpedia.org/>\n" +
                "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "SELECT DISTINCT ?s WHERE {\n" +
                "  BIND(<" + databusID + "> AS ?id)\n" +
                "  {\n" +
                "  ?s dataid:file ?id .\n" +
                "  } UNION {\n" +
                "    VALUES ?type { dataid:Group dataid:Artifact dataid:Version <https://databus.dbpedia.org/system/voc/Collection> dataid:Collection} \n" +
                "    ?id a ?type .\n" +
                "    ?id dct:publisher ?s .\n" +
                "  }\n" +
                "} LIMIT 1"
        );
        String redirectedUri = getFinalRedirectionURI(databusBase + "/sparql");
        if (redirectedUri == null) {
            log.error("Couldnt determine endpoint of " + databusBase);
            return false;
        } else {
            QueryExecution qexec = QueryExecutionFactory.sparqlService(redirectedUri, query);
            ResultSet rs = qexec.execSelect();
            Boolean exists = rs.hasNext();
            qexec.close();

            return exists;
        }
    }

    public static int checkIfValidDatabusId(String databusID) {

        String databusBase = MossUtilityFunctions.extractBaseFromURL(databusID);

        if (databusBase == null) {
            return -1;
        }

        String idRegex = "^" + Pattern.quote(databusBase) + "(/[^/]+){1,5}";
        if (!databusID.matches(idRegex))
            return 0;

        CookieHandler.setDefault(new CookieManager());
        try {
            URI uri = URI.create(databusID);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(uri).header("Accept", "application/*").method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            int status = response.statusCode();
            if (status >= 200 && status < 400)
                return 1;
            else
                return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Cacheable("databusEndpoints")
    public static String getFinalRedirectionURI(String uri) {
        try {
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(uri)).method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            return response.uri().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
