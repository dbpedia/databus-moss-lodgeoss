package org.dbpedia.databus.moss.services;

import org.apache.jena.rdf.model.Model;


public class RDFAnnotationRequest {
    private String databusURI;
    private String modType;
    private Model additionalGraph;
    private String version;
    private String modPath;

    public RDFAnnotationRequest(String databusURI, String modType, Model additionalGraph, String version) {
        this.databusURI = databusURI;
        this.modType = modType;
        this.additionalGraph = additionalGraph;
        this.version = version;
        this.modPath = null;
    }

    public RDFAnnotationRequest(String databusURI, String modType, Model additionalGraph, String version, String modPath) {
        this.databusURI = databusURI;
        this.modType = modType;
        this.additionalGraph = additionalGraph;
        this.version = version;
        this.modPath = modPath;
    }

    public String getModPath() {
        return this.modPath;
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
