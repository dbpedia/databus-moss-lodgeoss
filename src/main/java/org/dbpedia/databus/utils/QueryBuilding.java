package org.dbpedia.databus.utils;

import java.util.List;

public final class QueryBuilding {

    public static String buildAnnotationQuery(List<String> iris, String databusEndpoint, AggregationType aggType) {

        StringBuilder modsPart = new StringBuilder();

        modsPart.append("SELECT DISTINCT ?type ?title ?comment ?id ?versionURI ?annotation WHERE {\n" +
                "  {\n" +
                "  SELECT DISTINCT ?id ?annotation WHERE {\n");


        if (aggType == AggregationType.OR) {

            modsPart.append("\tVALUES ?annotation { ");
            for (String iri : iris) {
                modsPart.append(iri).append(" ");
            }
            modsPart.append("}\n");

            modsPart.append("    ?s a <http://mods.tools.dbpedia.org/ns/demo#AnnotationMod> .\n" +
                    "    ?s <http://www.w3.org/ns/prov#used> ?id .\n" +
                    "  \t?id <http://purl.org/dc/elements/1.1/subject> ?annotation .\n"
            );
        } else {
            modsPart.append("?s a <http://mods.tools.dbpedia.org/ns/demo#AnnotationMod> .\n" +
                    "    ?s <http://www.w3.org/ns/prov#used> ?id .");

            for (String iri : iris) {
                modsPart.append("  ?file <http://purl.org/dc/elements/1.1/subject> <").append(iri).append("> .\n");
            }
        }
        // close mod part again
        modsPart.append("  }\n" + "  }\n");

        // adds https://databus.dbpedia.org/system/voc/Collection to possible values for backward compatibility with Databus1.0

        // log.info(query);
        return "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX void: <http://rdfs.org/ns/void#>\n" +
                "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n" +
                "PREFIX dct:    <http://purl.org/dc/terms/>\n" +
                "PREFIX dcat:   <http://www.w3.org/ns/dcat#>\n" +
                "PREFIX db:     <https://databus.dbpedia.org/>\n" +
                "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX mods:   <http://mods.tools.dbpedia.org/>\n" +
                "\n" +
                "\n" +
                modsPart +
                "  SERVICE <" + databusEndpoint + "> {\n" +
                "    {\n" +
                "    \t?dataset a ?type .\n" +
                "    \t#OPTIONAL { ?dataset dataid:group ?group . }\n" +
                "    \tOPTIONAL { ?dataset dataid:version ?versionURI . }\n" +
                "        ?dataset dcat:distribution ?distribution . \n" +
                "    \t?distribution dataid:file ?id .\n" +
                "    \t?dataset dct:title ?title .\n" +
                "    \t?dataset dct:abstract|rdfs:comment ?comment .\n" +
                "    } UNION {\n" +
                "\t\tVALUES ?type { dataid:Group dataid:Artifact dataid:Version <https://databus.dbpedia.org/system/voc/Collection> dataid:Collection }\n" +
                "      \t?id a ?type .\n" +
                "      \t?id dct:title ?title .\n" +
                "      \t?id dct:abstract ?comment .\n" +
                "    }\n" +
                "\t}\n" +
                "}";
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

}
