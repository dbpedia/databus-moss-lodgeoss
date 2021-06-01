package org.dbpedia.databus.moss.services;

import org.apache.commons.io.IOUtils;
import org.apache.jena.assembler.Mode;
import org.apache.jena.base.Sys;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.dbpedia.databus.moss.annotation.SVGBuilder;
import org.dbpedia.databus.moss.views.annotation.AnnotationURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import virtuoso.jena.driver.VirtDataset;

import javax.sound.midi.SysexMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
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

        String databusFilePath = df.replace("https://databus.dbpedia.org/","");
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


    public void push_model(String df, Model push_model) throws IOException {

        ModActivityMetadata mam = new ModActivityMetadata(df, "http://mods.tools.dbpedia.org/ns/demo#SubmissionMod");
        mam.addModResult("submitted-data.ttl", "http://dataid.dbpedia.org/ns/mods/core#wasDerivedFrom");
        //svg
        //mam.addModResult("annotation.svg", "http://dataid.dbpedia.org/ns/mods/core#svgDerivedFrom");
        Model activityModel = mam.getModel();
//        activityModel.add(
//                ResourceFactory.createResource("annotation.svg"),
//                ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#seeAlso"),
//                ResourceFactory.createResource("https://moss.tools.dbpedia.org/annotate?dfid=" +
//                        URLEncoder.encode(df, StandardCharsets.UTF_8)));

        String databusFilePath = df.replace("https://databus.dbpedia.org/","");

        saveModel(activityModel,databusFilePath,"activity.ttl");
        saveModel(push_model,databusFilePath,"submitted-data.ttl");

//        updateModel(df+"#submitted-data",  getModel(baseURI,databusFilePath,"activity.ttl"), true);
//        updateModel(df+"#submitted-data", getModel(baseURI,databusFilePath,"submitted-data.ttl"), false);
        log.info("loaded " + df+"#submitted-data");


//        File annotationSVGFile = new File(baseDir, databusFilePath + "/" + "annotation.svg");
//        try {
//            FileOutputStream fos = new FileOutputStream(annotationSVGFile);
//            IOUtils.write(SVGBuilder.svgString2dec.replace("#NO", String.valueOf(annotationURLS.size())),fos,StandardCharsets.UTF_8);
//            fos.close();
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }

    }
}