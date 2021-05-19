package org.dbpedia.databus.utils;

import org.apache.jena.query.*;
import org.dbpedia.databus.moss.views.search.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

public class DatabusFileUtil {

    static final Logger log = LoggerFactory.getLogger(DatabusFileUtil.class);

    public static Boolean validate(String databusID) {

        Query query = QueryFactory.create(
                "PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>\n" +
                        "SELECT DISTINCT ?d WHERE {\n" +
                        "  ?d dataid:file <"+ databusID +"> .\n" +
                        "}"
        );
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://databus.dbpedia.org/repo/sparql", query);
        ResultSet rs = qexec.execSelect();
        Boolean exists = rs.hasNext();
        qexec.close();

        return exists;
    }
}
