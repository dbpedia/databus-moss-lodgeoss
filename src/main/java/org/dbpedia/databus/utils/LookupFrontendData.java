package org.dbpedia.databus.utils;

import com.vaadin.flow.component.Html;

public class LookupFrontendData {

    private String resource;
    private String label;
    private String definition;
    private String comment;

    public LookupFrontendData(String resource, String label, String definition, String comment) {
        this.label = label;
        this.resource = resource;
        this.definition = definition;
        this.comment = comment;
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

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public Html generate_html_repr() {

        String description;

        if (this.definition.equals("")) {
            description = this.comment;
        } else {
            description = this.definition;
        }

        return new Html(
                "<span>" +
                        "<u>" + this.getLabel() + "</u>" +
                        "<br>" +
                        "<a href=\""+ this.getResource() + "\">"+ this.getResource() + "</a>" +
                        "<br>" +
                        description + "</span>");
    }
}
