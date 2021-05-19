package org.dbpedia.databus.moss.views.search;

public class SearchResult {

    public String versionUri;
    public String databusFileUri;
    public String title;
    public String comment;


    public SearchResult(String versionUri, String title, String databusFileUri, String comment) {
        this.versionUri = versionUri;
        this.title = title;
        this.databusFileUri = databusFileUri;
        this.comment = comment;
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
}
