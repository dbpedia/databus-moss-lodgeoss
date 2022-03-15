package org.dbpedia.databus.utils;

import org.apache.jena.riot.system.StreamRDF;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import com.atomgraph.etl.json.JsonStreamRDFWriter;
import org.apache.jena.riot.system.StreamRDFLib;

public final class MossUtilityFunctions {

    // this is intentionally without any # or / ending since json2rdf always appends # to the base uri
    public static final String json_rdf_base_uri = "http://mods.tools.dbpedia.org/ns/demo";

    public static String getValFromArray(String[] str_array) {
        if (str_array == null) {
            return "";
        } else {
            return str_array[0];
        }
    }



    public static String get_ntriples_from_json(String json_string) {

        Writer w = new StringWriter();
        Reader r = new StringReader(json_string);
        StreamRDF stream_rdf = StreamRDFLib.writer(w);
        JsonStreamRDFWriter json_rdf_writer = new JsonStreamRDFWriter(r, stream_rdf, json_rdf_base_uri);
        json_rdf_writer.convert();

        return w.toString();
    }

}
