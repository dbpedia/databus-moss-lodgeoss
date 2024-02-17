package org.dbpedia.databus.moss.services;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dbpedia.databus.utils.DatabusUtilFunctions;

/**
 * Java object representing the data of a simple annotation (SimpleAnnotationMod)
 */
public class SimpleAnnotationModData {

    Map<String, String> nameSpaces; 
    public String fileURI;
    public String version;
    public String modType;
    public String used;
    private String time;
    private HashSet<String> subjects;


    public InputStream annotationGraph;
    String modFragment = "#mod";

    public SimpleAnnotationModData(String baseURL, String databusResourceUri) {
        this.version = "1.0.0";
        this.modType = "SimpleAnnotationMod";
        this.used = databusResourceUri;
        this.time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        this.nameSpaces = Map.of("dc", "http://purl.org/dc/terms/",
                                "prov", "http://www.w3.org/ns/prov#",
                                "moss", "https://dataid.dbpedia.org/moss#",
                                "rdfs", RDFS.getURI(),
                                "mod", "http://dataid.dbpedia.org/ns/mod#");
        this.fileURI = DatabusUtilFunctions.createAnnotationFileIdentifier(baseURL, modType, databusResourceUri);
        this.subjects = new HashSet<String>();
    }

    /**
     * Add subject entries from an existing model
     * @param model
     */
    public void addSubjectsFromModel(Model model) {
       StmtIterator iter = model.listStatements();

        // print out the predicate, subject and object of each statement
        while (iter.hasNext()) {
            
            System.out.println("Searching Model");
            Statement stmt      = iter.nextStatement();  // get next statement
            Property  predicate = stmt.getPredicate();   // get the predicate
            RDFNode   object    = stmt.getObject();      // get the object

            System.out.println(predicate.getURI());
            if(predicate.getURI() == DC.subject.getURI()) {
                System.out.println("Adding subject: " + object.toString());
                subjects.add(object.toString());
            }
        }
    }

    /**
     * Add a subject directly
     */
    public void addSubject(String subject) {
        this.subjects.add(subject);
    }

    /**
     * Convert the entire object into a jena model
     * @return
     */
    public Model toModel() {
        Model model = ModelFactory.createDefaultModel();
        Resource documentResource = ResourceFactory.createResource(this.fileURI);
        Resource modResource = ResourceFactory.createResource(this.fileURI + modFragment);
        Resource modTypeResource = ResourceFactory.createResource("https://dataid.dbpedia.org/moss#" + this.modType);
        Resource usedResource = ResourceFactory.createResource(this.used);

        String provNamespace = this.nameSpaces.get("prov");
        String modNamespace = this.nameSpaces.get("mod");

        model.setNsPrefixes(this.nameSpaces);

        model.add(modResource, RDF.type, modTypeResource);
        model.add(modResource, ResourceFactory.createProperty(provNamespace, "used"), usedResource);
        model.add(modResource, ResourceFactory.createProperty(provNamespace, "startedAtTime"), this.time);
        model.add(modResource, ResourceFactory.createProperty(provNamespace, "generated"), documentResource);
        model.add(modTypeResource, RDFS.subClassOf, ResourceFactory.createResource(modNamespace + "DatabusMod"));
        model.add(modResource, ResourceFactory.createProperty(modNamespace, "version"), this.version);

        String endedAtTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        model.add(modResource, ResourceFactory.createProperty(provNamespace, "endedAtTime"), endedAtTime);

        for(String subject : this.subjects) {
            model.add(usedResource, DC.subject, ResourceFactory.createResource(subject));
        }

        return model;
    }

    /**
     * Get the document id
     * @return
     */
    public String getId() {
        return fileURI + modFragment;
    }

    public String getFileURI() {
        return fileURI;
    }
}