package org.dbpedia.databus.utils;

public class LookupObject {
    private String[] score;
    private String[] label;
    private String[] resource;
    private String[] definition;
    private String[] altLabel;
    private String[] type;
    private String[] comment;

    public LookupObject() {};

    public String[] getType() {
        return type;
    }

    public void setType(String[] type) {
        this.type = type;
    }

    public String[] getAltLabel() {
        return altLabel;
    }

    public void setAltLabel(String[] altLabel) {
        this.altLabel = altLabel;
    }

    public String[] getDefinition() {
        return definition;
    }

    public void setDefinition(String[] definition) {
        this.definition = definition;
    }

    public String[] getLabel() {
        return label;
    }

    public void setLabel(String[] label) {
        this.label = label;
    }

    public String[] getResource() {
        return resource;
    }

    public void setResource(String[] resource) {
        this.resource = resource;
    }

    public String[] getScore() {
        return score;
    }

    public void setScore(String[] score) {
        this.score = score;
    }

    public void setComment(String[] comment) {
        this.comment = comment;
    }

    public String[] getComment() {
        return comment;
    }
}
