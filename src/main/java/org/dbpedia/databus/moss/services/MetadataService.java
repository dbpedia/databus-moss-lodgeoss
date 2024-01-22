package org.dbpedia.databus.moss.services;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.vocabulary.RDF;
import org.dbpedia.databus.moss.annotation.SVGBuilder;
import org.dbpedia.databus.moss.views.annotation.AnnotationURL;
import org.dbpedia.databus.utils.MossUtilityFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.helger.commons.io.stream.StringInputStream;

import virtuoso.jena.driver.VirtDataset;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import org.apache.commons.io.IOUtils; 
import org.springframework.util.ResourceUtils;
// import net.sf.json.JSONObject;
// import net.sf.json.JSONSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.cert.URICertStoreParameters;
import java.util.ArrayList;
import java.util.List;

@Service
public class MetadataService {

    private final Logger log = LoggerFactory.getLogger(MetadataService.class);

    private final String virtUrl;
    private final String virtUsr;
    private final String virtPsw;
    private final File baseDir;
    private final String baseURI;

    public MetadataService(@Value("${virt.url}") String virtUrl,
                           @Value("${virt.usr}") String virtUsr,
                           @Value("${virt.psw}") String virtPsw,
                           @Value("${file.vol}") String volume,
                           @Value("${uri.base}") String baseURI) {
        this.virtUrl = virtUrl;
        this.virtUsr = virtUsr;
        this.virtPsw = virtPsw;
        this.baseDir = new File(volume);
        this.baseURI = baseURI;
    }


    void updateModel(String graphName, Model model, Boolean delete) {
        VirtDataset db = new VirtDataset(virtUrl, virtUsr, virtPsw);
        if (delete && db.containsNamedModel(graphName)) db.removeNamedModel(graphName);
        db.addNamedModel(graphName, model, false);
        db.commit();
        db.close();
    }

    public String buildURL(String scheme, String baseURL, List<String> pathSegments) {
        String identifier = "";
        URIBuilder builder = new URIBuilder();
        try {
            builder.setScheme("http");
            builder.setHost(baseURL);
            builder.setPathSegments(pathSegments);

            identifier = builder.build().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return  identifier;
    }

    public String buildURL(String scheme, String baseURL, List<String> pathSegments, String repository, String pathParam) {
        //TODO: maybe sanitize?
        // String endpoint = "http://localhost:3002/graph/save?repo=oeo&path=cement/annotation.jsonld";
        String endpoint = new String(scheme + "://" + baseURL + "/graph" + "/save?" + "repo=" + repository + "&path=" + pathParam + "/annotation.jsonld");
        return endpoint;
    }


    public String creatFileIdentifier(String baseURLRaw, String annotationName, String resourcePathRaw) {
        final String regexResourcePrefix = "http[s]?://";
        List<String> pathSegments = new ArrayList<String>();
        pathSegments.add("annotations");
        pathSegments.add(annotationName);
        pathSegments.add(resourcePathRaw.replaceAll(regexResourcePrefix, ""));
        return buildURL("http", baseURLRaw.replaceAll(regexResourcePrefix, ""), pathSegments);
    }

    public URI createEndpointURL(String scheme, String gStoreBaseURL, String repository, String path) {
        // String endpoint = "http://localhost:3002/graph/save?repo=oeo&path=cement/annotation.jsonld";

        List<String> pathSegments = new ArrayList<String>();
        pathSegments.add("graph");
        pathSegments.add("save");
        pathSegments.add("annotations.jsonld");
        URI endpoint = null;

        try {
            endpoint = new URI(buildURL("http", gStoreBaseURL, pathSegments, repository, path));
        } catch (URISyntaxException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return endpoint;
    }


    public void createAnnotation(String databusIdentifier, List<AnnotationURL> annotationURLS) {

        //TODO: add read to check wheter annotationdocumentresource already exists
        Model annotationModel = ModelFactory.createDefaultModel();

        String annotatorName = "simple";
        String dcNamespace = "http://purl.org/dc/terms/";
        String mossNamespace = "https://dataid.dbpedia.org/moss#";

        String fileIdentifier = creatFileIdentifier(this.baseURI, annotatorName, databusIdentifier);
        fileIdentifier = "http://localhost:8080/data/annotations/simple/databus.testing.org/cement";

        // Create resources
        Resource databusResource = ResourceFactory.createResource(databusIdentifier);
        Resource annotationDocumentResource = ResourceFactory.createResource(fileIdentifier);

        for(AnnotationURL annotationURL: annotationURLS) {
            annotationModel.add(
                    databusResource,
                    ResourceFactory.createProperty(dcNamespace + "subject"),
                    ResourceFactory.createResource(annotationURL.getUri()));
        }

        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(dcNamespace + "relation"), databusResource);
        annotationModel.add(annotationDocumentResource, RDF.type, ResourceFactory.createResource("https://dataid.dbpedia.org/moss#AnnotationDocument"));
        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(mossNamespace + "annotatorName"), annotatorName);

        try {
            saveModel(annotationModel);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public List<AnnotationURL> getAnnotations(String df) {
        Query query = QueryFactory.create(
                "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
                        "SELECT ?annotation {\n" +
                        "  <"+df+"> dc:subject ?annotation .\n" +
                        "} \n"
        );
        QueryExecution qexec = QueryExecutionFactory.sparqlService("https://mods.tools.dbpedia.org/sparql",query);
        ResultSet rs = qexec.execSelect();

        ArrayList<AnnotationURL> list = new ArrayList<AnnotationURL>();
        while (rs.hasNext()) {
            list.add(new AnnotationURL(rs.next().getResource("annotation").getURI()));
        }
        qexec.close();
        return list;
    }


    private void saveAnnotationsSVG(String databusFilePath, List<AnnotationURL> annotationURLS) {
        String localBase = new String("/home/john/Documents/workspace/whk/moss");
        File annotationSVGFile = new File(localBase, "volume" + databusFilePath + "/" + "annotation.svg");
        try {
            FileOutputStream fos = new FileOutputStream(annotationSVGFile);
            IOUtils.write(SVGBuilder.svgString2dec.replace("#NO", String.valueOf(annotationURLS.size())),fos,StandardCharsets.UTF_8);
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    //TODO: determine correct path to save model
    private void saveModel(Model annotationModel) throws IOException {
        //FIXME: 
        /*
         * 1. determine correct group,
         * 2. repo path 
         * 3. filename
         */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RDFDataMgr.write(outputStream, annotationModel, Lang.JSONLD);

        String baseURL = "localhost:3002";
        String jsonString = outputStream.toString("UTF-8");

        URI endpoint = createEndpointURL("http", baseURL, "oeo", "cement");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/ld+json");
        headers.add("Content-Type", "application/ld+json");

        HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
        System.out.println(jsonString);
        try {
            // String endpoint = "http://localhost:3002/graph/save?repo=oeo&path=cement/annotation.jsonld";
            // URI url = new URI(endpoint);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
            String serverResponse = response.getBody();
            System.out.println(serverResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveModelLegacy(Model model, String databusIdPath, String result) throws IOException {
        File resultFile = new File(baseDir, databusIdPath + "/" + result);
        resultFile.getParentFile().mkdirs();
        FileOutputStream os = new FileOutputStream(resultFile);
        model.write(os, "TURTLE");
    }

    //TODO:
    /*
     *  1. gstore kriegt jsonld + repo + path im repo
     *     - body is jsonld + request parameter im post (path + repo)
     *  2. schreibt datei in den pfad + local gitrepo
     *  3. schreibt file content ins virtuoso
     * 
     *  D.h. reimplement
     *  1. saveModel
     *  2. getModel 
     */
    //Stackoverflow for Http request + json body https://stackoverflow.com/questions/7181534/http-post-using-json-in-java

    File[] listFiles(String path) {
        return new File(baseDir, path).listFiles();
    }

    Model getModel(String endpoint) {

        endpoint = "http://localhost:3002/graph/read?repo=oeo&path=cement/annotation.jsonld";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/ld+json");
        headers.add("Content-Type", "application/ld+json");

        Model model = ModelFactory.createDefaultModel();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
            String serverResponse = response.getBody();
            ByteArrayInputStream targetStream = new ByteArrayInputStream(serverResponse.getBytes("UTF-8"));

            model.read(targetStream, "", "jsonld");
            RDFDataMgr.write(System.out, model, Lang.JSONLD);
        } catch (RestClientException | UnsupportedEncodingException exception) {
            exception.printStackTrace();
        }

        return model;
    }

    Model getModelLegacy(String baseURI, String databusIdPath, String result) throws IOException {
        File resultFile = new File(baseDir, databusIdPath + "/" + result);
        if (resultFile.exists()) {
            String fqBaseURI = baseURI.replaceAll("/$","") + "/" + databusIdPath + "/";
            log.info("read "+fqBaseURI);
            Model model = ModelFactory.createDefaultModel();
            model.read(new FileInputStream(resultFile),fqBaseURI,"TURTLE");
            return model;
        } else {
            return null;
        }
    }

    public File getFile(String databusIdPath, String result) {
        File file = new File(baseDir,databusIdPath+"/"+result);
        if(file.exists()) {
            return file;
        } else {
            return null;
        }
    }


    public void submit_model(String df, Model push_model) throws IOException {

        String databusBase = MossUtilityFunctions.extractBaseFromURL(df);

        String graph_identifier = "#api-demo";

        ModActivityMetadata mam = new ModActivityMetadata(df, "http://mods.tools.dbpedia.org/ns/demo#ApiDemoMod");
        mam.addModResult("api-demo-data.ttl", "http://dataid.dbpedia.org/ns/mods/core#wasDerivedFrom");
        //svg
        mam.addModResult("api-demo-data.svg", "http://dataid.dbpedia.org/ns/mods/core#svgDerivedFrom");
        Model activityModel = mam.getModel();
        activityModel.add(
                ResourceFactory.createResource("api-demo-data.svg"),
                ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#seeAlso"),
                ResourceFactory.createResource("https://moss.tools.dbpedia.org/submit-data?dfid=" +
                        URLEncoder.encode(df, StandardCharsets.UTF_8)));

        String databusFilePath = df.replace(databusBase, "");

        //FIXME: functions signature change -> figure out appropriate refactoring
        // saveModel(activityModel,databusFilePath,"api-demo-activity.ttl");
        // saveModel(push_model,databusFilePath,"api-demo-data.ttl");

        // updateModel(df+graph_identifier,  getModel(baseURI,databusFilePath,"api-demo-activity.ttl"), true);
        // updateModel(df+graph_identifier, getModel(baseURI,databusFilePath,"api-demo-data.ttl"), false);
        log.info("loaded " + df+graph_identifier);


        File annotationSVGFile = new File(baseDir, databusFilePath + "/" + "api-demo-data.svg");
        try {
            FileOutputStream fos = new FileOutputStream(annotationSVGFile);
            IOUtils.write(SVGBuilder.api_demo_svg_base.replace("#NO", String.valueOf(push_model.size())),fos,StandardCharsets.UTF_8);
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }


//    public String fetchAPIData(String df) {
//
//        String databusBase = MossUtilityFunctions.extractBaseFromURL(df);
//
//        String[] id_split = df.replace(databusBase, "").split("/");
//
//        if (id_split.length != 5) {
//            log.warn("Error finding data for Databus Identifier " + df);
//            return "";
//        }
//
//        String pusblisher = id_split[0];
//        String group = id_split[1];
//        String artifact = id_split[2];
//        String version = id_split[3];
//        String filename = id_split[4];
//
//        try {
//            HttpClient client = HttpClient.newHttpClient();
//
//            HttpRequest req = HttpRequest.newBuilder().uri(
//                    new URI(String.format("https://moss.tools.dbpedia.org/data/%s/%s/%s/%s/%s/api-demo-data.ttl", pusblisher, group, artifact, version, filename))
//            ).build();
//
//            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
//
//            return response.body();
//        } catch (Exception e) {
//            log.warn("Could not load turtle data for submission page: " + e);
//            return "";
//        }
//    }

    public String fetchAPIData(String identifier) {

        String uri = String.format("%s/fetch?id=%s&file=api-demo-data.ttl", this.baseURI, URLEncoder.encode(identifier, StandardCharsets.UTF_8));
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest req = HttpRequest.newBuilder().uri(
                    new URI(uri)
            ).build();

            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (Exception e) {
            log.warn("Could not load turtle data for submission page: " + e);
            return "";
        }
    }
}