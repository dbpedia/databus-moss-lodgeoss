package org.dbpedia.databus.moss.services;

import org.apache.jena.rdf.model.Model;


public class RDFAnnotationRequest {
    private String databusURI;
    private String modType;
    private Model additionalGraph;
    private String version;


    
    public RDFAnnotationRequest(String databusURI, String modType, Model additionalGraph, String version) {
        this.databusURI = databusURI;
        this.modType = modType;
        this.additionalGraph = additionalGraph;
        this.version = version;
    }
    public String getDatabusURI() {
        return databusURI;
    }
    public void setDatabusURI(String databusURI) {
        this.databusURI = databusURI;
    }
    public String getModType() {
        return modType;
    }
    public void setModType(String modType) {
        this.modType = modType;
    }
    public Model getAnnotationModel() {
        return additionalGraph;
    }
    public void setAdditionalGraph(Model additionalGraph) {
        this.additionalGraph = additionalGraph;
    }
    public String getModVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

   
}
