package org.dbpedia.databus.moss.services;

import java.util.List;


public class SimpleAnnotationRequest {
    public String databusFile;
    public List<String> tags;
    public String version;

    public SimpleAnnotationRequest(String databusFile, List<String> tags){
        this.databusFile = databusFile;
        this.tags = tags;
        this.version = "1.0.0";
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    public String getDatabusFile() {
        return this.databusFile;
    }

    public void setDatabusFile(String databusFile) {
        this.databusFile = databusFile;
    }
}
