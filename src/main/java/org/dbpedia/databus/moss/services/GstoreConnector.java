package org.dbpedia.databus.moss.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class GstoreConnector {

    private String baseURL;

    public GstoreConnector(String baseURL) {
        this.baseURL = baseURL;
    }

    public Model read(String targetURI) throws URISyntaxException, UnsupportedEncodingException {

        // String targetURI = this.baseURL + "/graph/read?repo=" + repo + "&path=" + path;

        Model model = ModelFactory.createDefaultModel();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/ld+json");
        headers.add("Content-Type", "application/ld+json");

        URI gstoreURI = new URI(targetURI);
        ResponseEntity<String> response = restTemplate.getForEntity(gstoreURI, String.class);
        String serverResponse = response.getBody();
        System.out.println("============= MODEL FROM GSTORE ================");
        System.out.println(serverResponse);
        System.out.println("============= MODEL FROM GSTORE ================");

        if (serverResponse != null) {
            ByteArrayInputStream targetStream = new ByteArrayInputStream(serverResponse.getBytes("UTF-8"));
            RDFParser.source(targetStream).forceLang(Lang.JSONLD).parse(model);
        }

        return model;
    }

    public Model read(String repo, String path) throws URISyntaxException, UnsupportedEncodingException {

        String targetURI = this.baseURL + "/graph/read?repo=" + repo + "&path=" + path;

        Model model = ModelFactory.createDefaultModel();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/ld+json");
        headers.add("Content-Type", "application/ld+json");

        URI gstoreURI = new URI(targetURI);
        ResponseEntity<String> response = restTemplate.getForEntity(gstoreURI, String.class);
        String serverResponse = response.getBody();
        System.out.println("============= MODEL FROM GSTORE ================");
        System.out.println(serverResponse);
        System.out.println("============= MODEL FROM GSTORE ================");

        if (serverResponse != null) {
            ByteArrayInputStream targetStream = new ByteArrayInputStream(serverResponse.getBytes("UTF-8"));
            RDFParser.source(targetStream).forceLang(Lang.JSONLD).parse(model);
        }

        return model;
    }

    public void write(String repo, String path, Model model) throws UnsupportedEncodingException, URISyntaxException {

        String targetURI = this.baseURL + "/graph/save?repo=" + repo + "&path=" + path;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RDFDataMgr.write(outputStream, model, Lang.JSONLD);
        String jsonString = outputStream.toString("UTF-8");
        // System.out.println("=============== SAVING JSON ==================");
        // System.out.println(jsonString);
        // System.out.println("=============== SAVING JSON ==================");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/ld+json");
        headers.add("Content-Type", "application/ld+json");

        HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
        URI endpoint = new URI(targetURI);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
        String serverResponse = response.getBody();
        System.out.println("----------------server response--------------------");
        System.out.println(serverResponse);
        System.out.println("------------------------------------");
    }
}
