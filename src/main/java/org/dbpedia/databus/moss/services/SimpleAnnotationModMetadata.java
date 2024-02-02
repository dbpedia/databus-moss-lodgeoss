package org.dbpedia.databus.moss.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.jena.vocabulary.RDFS;

public class SimpleAnnotationModMetadata {

    public String version;
    public String modType;
    public String databusMod;
    public String databusResourceUri;
    private String time;
    Map<String, String> nameSpaces; 

    public SimpleAnnotationModMetadata() {
        this.version = version;
        this.modType = modType;
        this.databusResourceUri = databusResourceUri;
        this.time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        this.nameSpaces = Map.of("dc", "http://purl.org/dc/terms/",
                                "prov", "http://www.w3.org/ns/prov#",
                                "moss", "https://dataid.dbpedia.org/moss#",
                                "rdfs", RDFS.getURI(),
                                "mod", "http://dataid.dbpedia.org/ns/mod#");
    }
}
