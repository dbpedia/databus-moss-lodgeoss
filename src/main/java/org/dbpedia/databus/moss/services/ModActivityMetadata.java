package org.dbpedia.databus.moss.services;

import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.Calendar;

public class ModActivityMetadata {

    private Model model = ModelFactory.createDefaultModel();

    Model getModel() {
        return model;
    }

    public ModActivityMetadata(String databusId) {

      addStmtToModel("activity.ttl","http://www.w3.org/ns/prov#used", ResourceFactory.createResource(databusId));

      addStmtToModel("activity.ttl","http://www.w3.org/ns/prov#startedAtTime",new XSDDateTime(Calendar.getInstance()));
      addStmtToModel("activity.ttl","http://www.w3.org/ns/prov#endedAtTime",new XSDDateTime(Calendar.getInstance()));
      addStmtToModel("activity.ttl","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
              ResourceFactory.createResource("http://mods.tools.dbpedia.org/ns/demo#AnnotationMod"));
    }

    private void addStmtToModel(String s, String p, Object o) {
        if(o instanceof Resource) {
            model.add(ResourceFactory.createResource(s), ResourceFactory.createProperty(p),(Resource) o);
        } else {
            model.add(ResourceFactory.createResource(s), ResourceFactory.createProperty(p),ResourceFactory.createTypedLiteral(o));
        }
    }

    void addModResult(String name, String link) {
        addStmtToModel("activity.ttl","http://www.w3.org/ns/prov#generated",ResourceFactory.createResource(name));
        model.add(
                ResourceFactory.createResource(name),
                ResourceFactory.createProperty(link),
                ResourceFactory.createResource("activity.ttl")
        );
    }
}
