package org.dbpedia.databus.moss.views.search;

public class SearchResult {

    public String iri;
    public int triples;

    public SearchResult(String iri, int triples) {
        this.iri = iri;
        this.triples = triples;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public int getTriples() {
        return triples;
    }

    public void setTriples(int triples) {
        this.triples = triples;
    }
}
