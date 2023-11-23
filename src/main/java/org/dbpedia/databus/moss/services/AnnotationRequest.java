package org.dbpedia.databus.moss.services;

import java.util.List;


public class AnnotationRequest {
    public String databusFile;
    public List<String> tags;

    public AnnotationRequest(String databusFile, List<String> tags){
        this.databusFile = databusFile;
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDatabusFile() {
        return this.databusFile;
    }

    public void setDatabusFile(String databusFile) {
        this.databusFile = databusFile;
    }
}
