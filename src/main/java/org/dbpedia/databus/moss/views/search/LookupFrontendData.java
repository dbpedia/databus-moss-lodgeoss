package org.dbpedia.databus.moss.views.search;

public class LookupFrontendData {

    private String resource;
    private String label;
    private String definition;

    public LookupFrontendData(String resource, String label, String definition) {
        this.label = label;
        this.resource = resource;
        this.definition = definition;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
