package org.dbpedia.databus.moss.services;

import java.util.HashMap;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class MetadataAnnotator {

    Query queryAllVersions = QueryFactory.create(
        "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n" +
        "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
        "PREFIX dcat:   <http://www.w3.org/ns/dcat#>\n" +
        "PREFIX dct:    <http://purl.org/dc/terms/>\n" +
        "PREFIX dcv: <https://dataid.dbpedia.org/databus-cv#>\n" +
        "PREFIX databus: <https://dataid.dbpedia.org/databus#>\n" +

        "SELECT ?dataFile ?metadataFile WHERE\n" +
        "{\n" +
        "        GRAPH ?g\n" +
        "        {\n" +
        "        ?version a databus:Version .\n" +

        "                ?version dcat:distribution ?data .\n" +
        "                ?data <http://purl.org/dc/terms/hasVersion> ?hasVersion. \n" +
        "                ?data <https://dataid.dbpedia.org/databus-cv#type> 'data' . \n" +
        "                ?data databus:file ?dataFile .\n" +

        "        ?version dcat:distribution ?metadata .\n" +
        "                ?metadata <http://purl.org/dc/terms/hasVersion> ?hasVersion. \n" +
        "                ?metadata <https://dataid.dbpedia.org/databus-cv#type> 'metadata' . \n" +
        "                ?metadata databus:file ?metadataFile .\n" +
        "        }\n" +
        "}"
    );

    Query queryLatestVersion = QueryFactory.create(
        "PREFIX databus: <https://dataid.dbpedia.org/databus#>\n" +
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
        "PREFIX dct: <http://purl.org/dc/terms/>\n" +
        "PREFIX dcat: <http://www.w3.org/ns/dcat#>\n" +
        "PREFIX sec: <https://w3id.org/security#>\n" +
        "PREFIX cert: <http://www.w3.org/ns/auth/cert#>\n" +
        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
        "PREFIX databus-cv: <https://dataid.dbpedia.org/databus-cv#>\n" +
        "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
        "\n" +
        "SELECT ?dataFile ?metaFile WHERE {\n" +
        "\n" +
        "    ?v databus:artifact ?artifact .\n" +
        "    ?v <http://purl.org/dc/terms/hasVersion> ?latestVersion .\n" +
        "  \n" +
        "    ?v dcat:distribution ?dataDist .\n" +
        "    ?dataDist <https://dataid.dbpedia.org/databus-cv#type> 'data' . \n" +
        "    ?dataDist databus:file ?dataFile .\n" +
        "  \n" +
        "  	 ?v dcat:distribution ?metaDist .\n" +
        "    ?metaDist <https://dataid.dbpedia.org/databus-cv#type> 'metadata' . \n" +
        "    ?metaDist databus:file ?metaFile .\n" +
        "\n" +
        "	{\n" +
        "      SELECT ?artifact (MAX(?hasVersion) AS ?latestVersion) WHERE {\n" +
        "        GRAPH ?g {\n" +
        "          ?artifact a databus:Artifact .\n" +
        "          ?version databus:artifact ?artifact .\n" +
        "          ?version dcat:distribution ?data .\n" +
        "          ?data <http://purl.org/dc/terms/hasVersion> ?hasVersion. \n" +
        "          ?data <https://dataid.dbpedia.org/databus-cv#type> 'data' . \n" +
        "          ?data databus:file ?dataFile .\n" +
        "        }\n" +
        "      } GROUP BY ?artifact\n" +
        "    }\n" +
        "}"
    );

    Query queryLatestDistribution = QueryFactory.create(
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
        "PREFIX databus: <https://dataid.dbpedia.org/databus#>\n" +
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
        "PREFIX dct: <http://purl.org/dc/terms/>\n" +
        "PREFIX dcat: <http://www.w3.org/ns/dcat#>\n" +
        "PREFIX sec: <https://w3id.org/security#>\n" +
        "PREFIX cert: <http://www.w3.org/ns/auth/cert#>\n" +
        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
        "PREFIX databus-cv: <https://dataid.dbpedia.org/databus-cv#>\n" +
        "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +

        "SELECT ?dataDist ?metaDist WHERE {\n" +

            "?v databus:artifact ?artifact .\n" +
            "?v <http://purl.org/dc/terms/hasVersion> ?latestVersion .\n" +
        
            "?v dcat:distribution ?dataDist .\n" +
            "?dataDist <https://dataid.dbpedia.org/databus-cv#type> 'data' . \n" +
            "?dataDist databus:file ?dataFile .\n" +
        
            "?v dcat:distribution ?metaDist .\n" +
            "?metaDist <https://dataid.dbpedia.org/databus-cv#type> 'metadata' . \n" +
            "?metaDist databus:file ?metaFile .\n" +

            "{\n" +
            "SELECT ?distribution (MAX(?hasVersion) AS ?latestVersion) WHERE {\n" +
                "GRAPH ?g {\n" +
                "?artifact a databus:Artifact .\n" +
                "?version databus:artifact ?artifact .\n" +
                "?version dcat:distribution ?distribution .\n" +
                "?distribution <http://purl.org/dc/terms/hasVersion> ?hasVersion. \n" +
                "?distribution <https://dataid.dbpedia.org/databus-cv#type> 'data' . \n" +
                "?distribution databus:file ?dataFile .\n" +
                "}\n" +
            "} GROUP BY ?distribution\n" +
            "}\n" +
        "}\n"
    );

    public HashMap<String, String> fetchOEMetadata() {
        String databusBase = "https://databus.openenergyplatform.org/sparql";

        // String redirectedUri = getFinalRedirectionURI(databusBase + "/sparql");
        String redirectedUri = databusBase;
        System.out.println(redirectedUri);

        QueryExecution qexec = QueryExecutionFactory.sparqlService(redirectedUri, this.queryLatestVersion);
        ResultSet resultIterator = qexec.execSelect();
        HashMap<String, String> map = new HashMap<String, String>();
        String dataFile = "dataFile";
        String metadataFile = "metaFile";
        QuerySolution solution;

        while (resultIterator.hasNext()) {
            solution = resultIterator.next();
            map.put(solution.get(dataFile).toString(), solution.get(metadataFile).toString());
        }

        qexec.close();
        return map;
    }
}
