package org.dbpedia.databus.moss.services;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.vocabulary.RDF;
import org.dbpedia.databus.moss.annotation.SVGBuilder;
import org.dbpedia.databus.moss.views.annotation.AnnotationURL;
import org.dbpedia.databus.utils.MossUtilityFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

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
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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

    public void createAnnotation(String df, List<AnnotationURL> annotationURLS) {

        String databusBase = MossUtilityFunctions.extractBaseFromURL(df);

        ModActivityMetadata mam = new ModActivityMetadata(df, "http://mods.tools.dbpedia.org/ns/demo#AnnotationMod");
        mam.addModResult("annotation.ttl", "http://dataid.dbpedia.org/ns/mods/core#wasDerivedFrom");
        //svg
        mam.addModResult("annotation.svg", "http://dataid.dbpedia.org/ns/mods/core#svgDerivedFrom");
        Model activityModel = mam.getModel();
        activityModel.add(
                ResourceFactory.createResource("annotation.svg"),
                ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#seeAlso"),
                ResourceFactory.createResource("https://moss.tools.dbpedia.org/annotate?dfid=" +
                        URLEncoder.encode(df, StandardCharsets.UTF_8)));


        Model annotationModel = ModelFactory.createDefaultModel();
        for(AnnotationURL annotationURL: annotationURLS) {
            annotationModel.add(
                    ResourceFactory.createResource(df),
                    ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/subject"),
                    ResourceFactory.createResource(annotationURL.getUri()));
        }

        annotationModel.setNsPrefix("moss", "https://dataid.dbpedia.org/moss#");
        annotationModel.setNsPrefix("subject", "http://purl.org/dc/elements/1.1/subject");
        annotationModel.setNsPrefix("annotatorName", "moss:annotatorName");
        annotationModel.setNsPrefix("AnnotationDocument", "moss:AnnotationDocument");
        annotationModel.setNsPrefix("topic", "https://dfalksdjflksdfjksldfj/topic");

        annotationModel.add(
            ResourceFactory.createResource(df),
            ResourceFactory.createProperty(RDF.type.toString()),
            ResourceFactory.createResource("AnnotationDocument")
        );

        annotationModel.add(
            ResourceFactory.createResource(df),
            ResourceFactory.createProperty("moss:annotatorName"),
            ResourceFactory.createResource("Simple")
        );

        // System.out.println(RDF.type.toString());
        // annotationModel.write(System.out, "JSON-LD");
        // RDFDataMgr.write(System.out, annotationModel, Lang.TTL);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        RDFDataMgr.write(outputStream, annotationModel, Lang.JSONLD);

        String databusFilePath = df.replace(databusBase, "");
        try {
            // saveModel(activityModel,databusFilePath,"activity.jsonld");
            // saveModel(annotationModel,databusFilePath,"annotation.jsonld");

            String jsonString = outputStream.toString("UTF-8");
            saveModel(annotationModel, jsonString);

            // String localBase = new String("/home/john/Documents/workspace/whk/moss");
            // Model getActivityModel = getModel(localBase, databusFilePath, "activity.jsonld");
            // Model getAnnotationModel = getModel(localBase, databusFilePath, "annotation.jsonld");
            log.info("databusFilePath");
            // updateModel(df+"#annotation",  getModel(baseURI,databusFilePath,"activity.ttl"), true);
            // updateModel(df+"#annotation", getModel(baseURI,databusFilePath,"annotation.ttl"), false);
            // log.info("loaded " + df+"#annotation");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // saveAnnotationsSVG(databusFilePath, annotationURLS);

        // File annotationSVGFile = new File(baseDir, databusFilePath + "/" + "annotation.svg");
        // try {
        //     FileOutputStream fos = new FileOutputStream(annotationSVGFile);
        //     IOUtils.write(SVGBuilder.svgString2dec.replace("#NO", String.valueOf(annotationURLS.size())),fos,StandardCharsets.UTF_8);
        //     fos.close();
        // } catch (IOException ioe) {
        //     ioe.printStackTrace();
        // }

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
    private void saveModel(Model model, String jsonString) throws IOException {
        // String localBase = new String("/home/john/Documents/workspace/whk/moss");
        // File resultFile = new File(localBase, "volume" + databusIdPath + "/" + result);

        // File modelDir = resultFile.getParentFile();
        // modelDir.mkdirs();
        // FileOutputStream os = new FileOutputStream(resultFile);
        // RDFDataMgr.write(os, model, RDFFormat.JSONLD);

        // Gson gson = new Gson();
        // FileReader reader = new FileReader(resultFile);
        // JsonElement json = gson.fromJson(reader, JsonElement.class);

        // String jsonString = gson.toJson(json);

        //FIXME: 
        /*
         * 1. determine correct group,
         * 2. repo path 
         * 3. filename
         * 4. Build correct endpoint
         * 5. execute http request
         */

        String endpoint = "http://localhost:3002/graph/save?repo=oeo&path=cement/annotation.jsonld";


        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/ld+json");
        headers.add("Content-Type", "application/ld+json");

        HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
        System.out.println(jsonString);
        try {
            URI url = new URI(endpoint);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
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

    Model getModel(String baseURI, String databusIdPath, String result) throws IOException {
        File resultFile = new File(baseDir, databusIdPath + "/" + result);
        if (resultFile.exists()) {
            String fqBaseURI = baseURI.replaceAll("/$","") + "/" + databusIdPath + "/";
            log.info("read "+fqBaseURI);
            Model model = ModelFactory.createDefaultModel();
            // model.read(new FileInputStream(resultFile),fqBaseURI,"TURTLE");
            model.read(new FileInputStream(resultFile),fqBaseURI,"JSON-LD");
            return model;
        } else {
            return null;
        }
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

        saveModel(activityModel,databusFilePath,"api-demo-activity.ttl");
        saveModel(push_model,databusFilePath,"api-demo-data.ttl");

        updateModel(df+graph_identifier,  getModel(baseURI,databusFilePath,"api-demo-activity.ttl"), true);
        updateModel(df+graph_identifier, getModel(baseURI,databusFilePath,"api-demo-data.ttl"), false);
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