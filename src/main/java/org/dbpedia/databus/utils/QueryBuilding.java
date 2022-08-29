package org.dbpedia.databus.utils;

import java.util.List;

public final class QueryBuilding {


    private static String buildQuery(String modsPart, String databusEndpoint) {

        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX void: <http://rdfs.org/ns/void#>\n" +
                "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n" +
                "PREFIX dct:    <http://purl.org/dc/terms/>\n" +
                "PREFIX dcat:   <http://www.w3.org/ns/dcat#>\n" +
                "PREFIX db:     <https://databus.dbpedia.org/>\n" +
                "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX mods:   <http://mods.tools.dbpedia.org/>\n" +
                "PREFIX csvw: <http://www.w3.org/ns/csvw#>\n" +
                "PREFIX prov: <http://www.w3.org/ns/prov#>\n" +
                "\n" +
                "SELECT DISTINCT ?type ?title ?comment ?id ?versionURI ?annotation {\n" +
                modsPart +
                "SERVICE <" + databusEndpoint + "> {\n" +
                "    { ?dataset a ?type .\n" +
                "    \t#OPTIONAL { ?dataset dataid:group ?group . }\n" +
                "    \tOPTIONAL { ?dataset dataid:version ?versionURI . }\n" +
                "        ?dataset dcat:distribution ?distribution . \n" +
                "    \t?distribution dataid:file ?id .\n" +
                "    \t?dataset dct:title ?title .\n" +
                "    \t?dataset dct:abstract|rdfs:comment ?comment .\n" +
                "    } UNION {\n" +
                "\t\tVALUES ?type { dataid:Group dataid:Version <https://databus.dbpedia.org/system/voc/Collection> dataid:Collection }\n" +
                "      \t?id a ?type .\n" +
                "      \t?id dct:title ?title .\n" +
                "      \t?id dct:abstract ?comment .\n" +
                "  } UNION {\n" +
                "  \t?id a ?type .\n" +
                "    { \n" +
                "\t\t# Selects the latest version\n" +
                "\t\tSELECT DISTINCT (MAX(?v) as ?latestVersion) WHERE {\n" +
                "\t\t\t  ?dataset dataid:artifact ?id.\n" +
                "\t\t\t  ?dataset dcat:distribution ?distribution .\n" +
                "\t\t\t  ?dataset dct:hasVersion ?v .\n" +
                "\t\t\t} \n" +
                "\t}\n" +
                "    ?dataset dataid:artifact ?id .\n" +
                "    ?dataset dct:hasVersion ?latestVersion .\n" +
                "    #OPTIONAL { ?dataset dataid:group ?group . }\n" +
                "    OPTIONAL { ?dataset dataid:version ?versionURI . }\n" +
                "    ?dataset dcat:distribution ?distribution . \n" +
                "    ?dataset dct:title ?title .\n" +
                "    ?dataset dct:abstract|rdfs:comment ?comment .\n" +
                "  }\n" +
                "  }" +
                "}";
    }

    public static String buildAnnotationQuery(List<String> iris, String databusEndpoint, AggregationType aggType) {

        StringBuilder modsPart = new StringBuilder();

//        modsPart.append("SELECT DISTINCT ?type ?title ?comment ?id ?versionURI ?annotation WHERE {\n" +
//                "  {\n" +
//                "  SELECT DISTINCT ?id ?annotation WHERE {\n");


        if (aggType == AggregationType.OR) {

            modsPart.append("\tVALUES ?annotation { ");
            for (String iri : iris) {
                modsPart.append(iri).append(" ");
            }
            modsPart.append("}\n");

            modsPart.append("\t?s a <http://mods.tools.dbpedia.org/ns/demo#AnnotationMod> .\n" +
                    "\t?s <http://www.w3.org/ns/prov#used> ?id .\n" +
                    "\t?id <http://purl.org/dc/elements/1.1/subject> ?annotation .\n"
            );
        } else {
            modsPart.append("\t?s a <http://mods.tools.dbpedia.org/ns/demo#AnnotationMod> .\n" +
                    "\t?s <http://www.w3.org/ns/prov#used> ?id .\n");

            for (String iri : iris) {
                modsPart.append("\t?file <http://purl.org/dc/elements/1.1/subject> <").append(iri).append("> .\n");
            }
        }
        // close mod part again
        // modsPart.append("  }\n" + "  }\n");

        // adds https://databus.dbpedia.org/system/voc/Collection to possible values for backward compatibility with Databus1.0

        // log.info(query);

        return buildQuery(modsPart.toString(), databusEndpoint);
    }

    public static String buildVoidQuery(List<String> iris, AggregationType aggType) {

        StringBuilder builder = new StringBuilder();

        if (aggType == AggregationType.AND) {
            for (int i = 0; i < iris.size(); i++) {
                builder.append(" ?voidStats ?partition").append(i).append(" [\n").append("   ?p").append(i).append(" <").append(iris.get(i)).append("> ;\n").append("    void:triples ?triples").append(i).append(" \n").append(" ] .");
            }
        } else {
            builder.append("VALUES ?iris { ");
            for (String iri : iris) {
                builder.append("<").append(iri).append("> ");
            }
            builder.append("}\n");
            builder.append("?voidStats ?partition [\n" +
                    "  \t?p ?iris ;\n" +
                    "  \tvoid:triples ?triples \n" +
                    "  ] . ");
        }



        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX void: <http://rdfs.org/ns/void#>\n" +
                "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n" +
                "PREFIX dct:    <http://purl.org/dc/terms/>\n" +
                "PREFIX dcat:   <http://www.w3.org/ns/dcat#>\n" +
                "PREFIX db:     <https://databus.dbpedia.org/>\n" +
                "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX mods:   <http://mods.tools.dbpedia.org/>\n" +
                "\n" +
                "SELECT ?type ?title ?comment ?databusPage ?voidStats ?id {\n" +
                " SERVICE <https://mods.tools.dbpedia.org/sparql> {\n" +
                builder +
                "  \n" +
                " ?s <http://www.w3.org/ns/prov#used> ?id . # energy file\n" +
                " ?s <http://www.w3.org/ns/prov#generated> ?voidStats . # automatic content description\n" +
                "  \n" +
                " }\n" +
                "     ?dataset a ?type .\n" +
                "     ?dataset dataid:group ?group .\n" +
                "     ?dataset dcat:distribution ?distribution .\n" +
                "     ?dataset dataid:version ?databusPage .\n" +
                "     ?dataset dct:title ?title .\n" +
                "     ?dataset rdfs:comment ?comment .\n" +
                "     ?distribution dataid:file ?id .\n" +
                "\n" +
                "}";
    }

    public static String buildOEPMetadataQuery(List<String> iris, String databusEndpoint, AggregationType aggType) {

        StringBuilder modsPart = new StringBuilder();
        modsPart.append("\t\tGraph ?g {\n" + "\t\t\t?metadata csvw:table ?table .\n" + "\t\t\t?table csvw:tableSchema/csvw:column ?column .\n");

        if (aggType == AggregationType.AND) {
            for (String iri: iris) {
                modsPart.append("\t\t\t?column <http://purl.org/goodrelations/v1#valueReference> <").append(iri).append("> .\n");
            }
        } else {
            modsPart.append("\t\t\tVALUES ?topic { ");
            for (String iri: iris) {
                modsPart.append("<").append(iri).append("> ");
            }
            modsPart.append("}\n");
            modsPart.append("\t\t\t?column <http://purl.org/goodrelations/v1#valueReference> ?topic .");
        }

        modsPart.append("\t\t\t?activity a <http://mods.tools.dbpedia.org/ns/demo#ApiDemoMod>; \n" +
                "\t\t\t\tprov:used ?id .\n" +
                "\t\t}");

        return buildQuery(modsPart.toString(), databusEndpoint);
    }

}
