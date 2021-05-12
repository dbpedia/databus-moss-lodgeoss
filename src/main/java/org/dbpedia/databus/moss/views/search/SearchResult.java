package org.dbpedia.databus.moss.views.search;

public class SearchResult {

    public String versionUri;
    public String downloadUrl;
    public String databusFileUri;


    public SearchResult(String versionUri, String downloadUrl, String databusFileUri) {
        this.versionUri = versionUri;
        this.downloadUrl = downloadUrl;
        this.databusFileUri = databusFileUri;
    }

    public String getDatabusFileUri() {
        return databusFileUri;
    }

    public String getDownloadUrl() {
        return downloadUrl;
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

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
