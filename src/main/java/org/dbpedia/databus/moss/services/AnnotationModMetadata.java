package org.dbpedia.databus.moss.services;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class AnnotationModMetadata {

    public String version;
    public String modType;
    public String databusMod;
    public String databusResourceUri;
    private String time;
    public InputStream annotationGraph;
    Map<String, String> nameSpaces; 
    String modFragment = "#mod";

    public AnnotationModMetadata(String databusResourceUri) {
        this.version = "1.0.0";
        this.modType = "SimpleAnnotationMod";
        this.databusResourceUri = databusResourceUri;
        this.time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        this.nameSpaces = Map.of("dc", "http://purl.org/dc/terms/",
                                "prov", "http://www.w3.org/ns/prov#",
                                "moss", "https://dataid.dbpedia.org/moss#",
                                "rdfs", RDFS.getURI(),
                                "mod", "http://dataid.dbpedia.org/ns/mod#");
    }

    public AnnotationModMetadata(String version, String modType, String databusResourceUri, InputStream annotationGraph) {
        this.version = version;
        this.modType = modType;
        this.databusResourceUri = databusResourceUri;
        this.annotationGraph = annotationGraph;
        this.time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        this.nameSpaces = Map.of("prov", "http://www.w3.org/ns/prov#",
                                "moss", "https://dataid.dbpedia.org/moss#",
                                "rdfs", RDFS.getURI(),
                                "mod", "http://dataid.dbpedia.org/ns/mod#");
    }

    public void annotateModel(Model annotationModel, String fileIdentifier, Resource databusResource, SimpleAnnotationRequest annotationRequest) {
        Resource annotationDocumentResource = ResourceFactory.createResource(fileIdentifier + modFragment);
        Resource modTypResource = ResourceFactory.createResource("https://dataid.dbpedia.org/moss#" + this.modType);

        for(String tag: annotationRequest.getTags()) {
            annotationModel.add(
                    databusResource,
                    DC.subject,
                    ResourceFactory.createResource(tag));
        }

        this.annotateModInfo(annotationModel, databusResource, annotationDocumentResource, modTypResource);
    }


    public void annotateModel(Model annotationModel, String fileIdentifier, Resource databusResource) {
        Resource annotationDocumentResource = ResourceFactory.createResource(fileIdentifier + modFragment);
        Resource modTypResource = ResourceFactory.createResource("https://dataid.dbpedia.org/moss#" + this.modType);

        RDFDataMgr.read(annotationModel, annotationGraph, Lang.TURTLE);
        this.annotateModInfo(annotationModel, databusResource, annotationDocumentResource, modTypResource);
    }

    public void annotateModInfo(Model annotationModel, Resource databusResource, Resource annotationDocumentResource, Resource modTypeResource) {
        String provNamespace = this.nameSpaces.get("prov");
        String modNamespace = this.nameSpaces.get("mod");

        annotationModel.setNsPrefixes(this.nameSpaces);

        annotationModel.add(annotationDocumentResource, RDF.type, modTypeResource);
        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(provNamespace, "used"), databusResource);
        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(provNamespace, "startedAtTime"), this.time);
        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(provNamespace, "generated"), annotationModel.createResource(""));
        annotationModel.add(modTypeResource, RDFS.subClassOf, ResourceFactory.createResource(modNamespace + "DatabusMod"));
        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(modNamespace, "version"), this.version);

        String endedAtTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(provNamespace, "endedAtTime"), endedAtTime);
    }
}
