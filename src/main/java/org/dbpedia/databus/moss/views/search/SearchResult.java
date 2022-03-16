package org.dbpedia.databus.moss.views.search;


import org.apache.jena.query.QuerySolution;

enum IDType {
    COLLECTION,
    GROUP,
    ARTIFACT,
    VERSION,
    FILE,
}

public class SearchResult {

    public IDType type;
    public String databusPage;
    public String databusID;
    public String title;
    public String comment;


    public SearchResult(IDType type, String databusPage, String title, String databusID, String comment) {
        this.type = type;
        this.databusPage = databusPage;
        this.title = title;
        this.databusID = databusID;
        this.comment = comment;
    }

    public IDType getType() {
        return type;
    }

    public void setType(IDType type) {
        this.type = type;
    }

    public String getDatabusFileUri() {
        return databusID;
    }

    public String getVersionUri() {
        return databusPage;
    }

    public void setDatabusFileUri(String databusFileUri) {
        this.databusID = databusFileUri;
    }

    public void setVersionUri(String versionUri) {
        this.databusPage = versionUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getColorCode() {
        String color;
        switch (this.type) {
            case FILE:
                color = "#D3D3D3";
                break;
            case GROUP:
                color = "CC66CC";
                break;
            case VERSION:
                color = "66CCFF";
                break;
            case ARTIFACT:
                color = "FFFF99";
                break;
            default:
                color = "99FFCC";
                break;
        }
        return color;
    }

    public static SearchResult createFromQuerySolution(QuerySolution qs) {
        String identifier = qs.get("id").toString();
        String databusPage = qs.contains("databusPage") ? qs.get("databusPage").toString() : identifier;
        IDType type = getIDTypeFromClass(qs.get("type").toString());
        String title = qs.get("title").toString();
        String comment = qs.get("comment").toString();

        return new SearchResult(type, databusPage, title, identifier, comment);
    }

    private static IDType getIDTypeFromClass(String classURI) {
        IDType result;
        switch(classURI) {
            case "https://databus.dbpedia.org/system/voc/Collection":
                result = IDType.COLLECTION;
                break;
            case "http://dataid.dbpedia.org/ns/core#Artifact":
                result = IDType.ARTIFACT;
                break;
            case "http://dataid.dbpedia.org/ns/core#Group":
                result = IDType.GROUP;
                break;
            case "http://dataid.dbpedia.org/ns/core#Version":
                result = IDType.VERSION;
                break;
            default:
                result = IDType.FILE;
                break;
        }
        return result;
    }
}
