package org.dbpedia.databus.moss.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dbpedia.databus.utils.DatabusUtilFunctions;

/**
 * Java object representing the data of a simple annotation
 * (SimpleAnnotationMod)
 */
public class RDFAnnotationModData {

    Map<String, String> nameSpaces;
    private String databusURI;
    private String modType;
    private Model annotationModel;
    private String modVersion;
    private String fileURI;
    private String time;
    private String modURI;

    String modFragment = "#mod";

    public RDFAnnotationModData(String baseURI, RDFAnnotationRequest request) {
        this.databusURI = request.getDatabusURI();
        this.modType = request.getModType();
        this.annotationModel = request.getAnnotationModel();
        this.modVersion = request.getModVersion();
        this.fileURI = DatabusUtilFunctions.createAnnotationFileURI(baseURI, this.modType,
                this.databusURI);
        this.time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        this.nameSpaces = Map.of("dc", "http://purl.org/dc/terms/",
                "prov", "http://www.w3.org/ns/prov#",
                "moss", "https://dataid.dbpedia.org/moss#",
                "rdfs", RDFS.getURI(),
                "mod", "http://dataid.dbpedia.org/ns/mod#");
        this.modURI = this.fileURI + modFragment;
    }

    /**
     * Convert the entire object into a jena model
     * 
     * @return
     */
    public Model toModel() {
        Model model = ModelFactory.createDefaultModel();

        model.add(annotationModel);

        Resource documentResource = ResourceFactory.createResource(this.fileURI);
        Resource usedResource = ResourceFactory.createResource(this.databusURI);
        Resource modResource = ResourceFactory.createResource(this.fileURI + modFragment);
        Resource modTypeResource = ResourceFactory.createResource("https://dataid.dbpedia.org/moss#" + this.modType);

        String provNamespace = this.nameSpaces.get("prov");
        String modNamespace = this.nameSpaces.get("mod");

        model.setNsPrefixes(this.nameSpaces);

        model.add(modResource, RDF.type, modTypeResource);
        model.add(modResource, ResourceFactory.createProperty(provNamespace, "used"), usedResource);
        model.add(modResource, ResourceFactory.createProperty(provNamespace, "startedAtTime"), this.time);
        model.add(modResource, ResourceFactory.createProperty(provNamespace, "generated"), documentResource);
        model.add(modTypeResource, RDFS.subClassOf, ResourceFactory.createResource(modNamespace + "DatabusMod"));
        model.add(modResource, ResourceFactory.createProperty(modNamespace, "version"), this.modVersion);

        String endedAtTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        model.add(modResource, ResourceFactory.createProperty(provNamespace, "endedAtTime"), endedAtTime);

        return model;
    }

    /**
     * Get the document id
     * 
     * @return
     */
    public String getId() {
        return fileURI + modFragment;
    }

    public String getFileURI() {
        return fileURI;
    }

    public String getModType() {
        return modType;
    }

    public String getModURI() {
        return modURI;
    }

    public void setModURI(String modURI) {
        this.modURI = modURI;
    }


}
