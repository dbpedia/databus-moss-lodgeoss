package org.dbpedia.databus.moss.views.search;


enum IDType {
    COLLECTION,
    GROUP,
    ARTIFACT,
    VERSION,
    FILE,
}

public class SearchResult {

    public IDType type;
    public String versionUri;
    public String databusFileUri;
    public String title;
    public String comment;


    public SearchResult(IDType type, String versionUri, String title, String databusFileUri, String comment) {
        this.type = type;
        this.versionUri = versionUri;
        this.title = title;
        this.databusFileUri = databusFileUri;
        this.comment = comment;
    }

    public IDType getType() {
        return type;
    }

    public void setType(IDType type) {
        this.type = type;
    }

    public String getDatabusFileUri() {
        return databusFileUri;
    }

    public String getVersionUri() {
        return versionUri;
    }

    public void setDatabusFileUri(String databusFileUri) {
        this.databusFileUri = databusFileUri;
    }

    public void setVersionUri(String versionUri) {
        this.versionUri = versionUri;
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
}
