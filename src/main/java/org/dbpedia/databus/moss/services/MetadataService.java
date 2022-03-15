package org.dbpedia.databus.moss.services;

import org.apache.commons.io.IOUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.dbpedia.databus.moss.annotation.SVGBuilder;
import org.dbpedia.databus.moss.views.annotation.AnnotationURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import virtuoso.jena.driver.VirtDataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class MetadataService {

    private final Logger log = LoggerFactory.getLogger(MetadataService.class);

    private final String virtUrl;
    private final String virtUsr;
    private final String virtPsw;
    private final File baseDir;
    private final String baseURI;

    private final DatabusUtilService dbFileUtil;

    public MetadataService(@Value("${virt.url}") String virtUrl,
                           @Value("${virt.usr}") String virtUsr,
                           @Value("${virt.psw}") String virtPsw,
                           @Value("${file.vol}") String volume,
                           @Value("${uri.base}") String baseURI,
                           @Autowired DatabusUtilService dbFileUtil) {
        this.virtUrl = virtUrl;
        this.virtUsr = virtUsr;
        this.virtPsw = virtPsw;
        this.baseDir = new File(volume);
        this.baseURI = baseURI;
        this.dbFileUtil = dbFileUtil;
    }


    void updateModel(String graphName, Model model, Boolean delete) {
        VirtDataset db = new VirtDataset(virtUrl, virtUsr, virtPsw);
        if (delete && db.containsNamedModel(graphName)) db.removeNamedModel(graphName);
        db.addNamedModel(graphName, model, false);
        db.commit();
        db.close();
    }

    public void createAnnotation(String df, List<AnnotationURL> annotationURLS) {

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

        String databusFilePath = df.replace(dbFileUtil.DATABUS_BASE, "");
        try {
            saveModel(activityModel,databusFilePath,"activity.ttl");
            saveModel(annotationModel,databusFilePath,"annotation.ttl");

            updateModel(df+"#annotation",  getModel(baseURI,databusFilePath,"activity.ttl"), true);
            updateModel(df+"#annotation", getModel(baseURI,databusFilePath,"annotation.ttl"), false);
            log.info("loaded " + df+"#annotation");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        File annotationSVGFile = new File(baseDir, databusFilePath + "/" + "annotation.svg");
        try {
            FileOutputStream fos = new FileOutputStream(annotationSVGFile);
            IOUtils.write(SVGBuilder.svgString2dec.replace("#NO", String.valueOf(annotationURLS.size())),fos,StandardCharsets.UTF_8);
            fos.close();
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


    private void saveModel(Model model, String databusIdPath, String result) throws IOException {
        File resultFile = new File(baseDir, databusIdPath + "/" + result);
        resultFile.getParentFile().mkdirs();
        FileOutputStream os = new FileOutputStream(resultFile);
        model.write(os, "TURTLE");
    }

    File[] listFiles(String path) {
        return new File(baseDir, path).listFiles();
    }

    Model getModel(String baseURI, String databusIdPath, String result) throws IOException {
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
        System.out.println(file.getAbsolutePath());
        if(file.exists()) {
            return file;
        } else {
            return null;
        }
    }


    public void submit_model(String df, Model push_model) throws IOException {

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

        String databusFilePath = df.replace(dbFileUtil.DATABUS_BASE, "");

        saveModel(activityModel,databusFilePath,"api-demo-activity.ttl");
        saveModel(push_model,databusFilePath,"api-demo-data.ttl");

        updateModel(df+graph_identifier,  getModel(baseURI,databusFilePath,"api-demo-activity.ttl"), true);
        updateModel(df+graph_identifier, getModel(baseURI,databusFilePath,"api-demo-data.ttl"), true);
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


    public String get_api_data(String df) {

        String[] id_split = df.replace(dbFileUtil.DATABUS_BASE, "").split("/");

        if (id_split.length != 5) {
            log.warn("Error finding data for Databus Identifier " + df);
            return "";
        }

        String pusblisher = id_split[0];
        String group = id_split[1];
        String artifact = id_split[2];
        String version = id_split[3];
        String filename = id_split[4];

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest req = HttpRequest.newBuilder().uri(
                    new URI(String.format("https://moss.tools.dbpedia.org/data/%s/%s/%s/%s/%s/api-demo-data.ttl", pusblisher, group, artifact, version, filename))
            ).build();

            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (Exception e) {
            log.warn("Could not load turtle data for submission page: " + e);
            return "";
        }
    }


}