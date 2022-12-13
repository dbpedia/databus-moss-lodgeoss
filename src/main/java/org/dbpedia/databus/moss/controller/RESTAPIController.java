package org.dbpedia.databus.moss.controller;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.dbpedia.databus.moss.services.MetadataService;
import org.dbpedia.databus.moss.views.annotation.AnnotationURL;
import org.dbpedia.databus.utils.DatabusUtilFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api")
public class RESTAPIController {

    private final Logger log = LoggerFactory.getLogger(RESTAPIController.class);
    private final MetadataService ms;

    public RESTAPIController(@Autowired MetadataService ms) {
        this.ms = ms;
    }

    @GetMapping("/annotate")
    ResponseEntity<String> simpleIdAnnotation(@RequestParam String databusID, @RequestParam List<String> classes) {
        List<AnnotationURL> annotationURLs = new ArrayList<>();
        for (String classURI : classes) {
            annotationURLs.add(new AnnotationURL(classURI));
        }

        if (annotationURLs.isEmpty()) {
            return new ResponseEntity<>("Failed: No annotation classes provided" , HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<String> respEnt;

        if (!DatabusUtilFunctions.validate(databusID)) {
            try {
                ms.createAnnotation(databusID, annotationURLs);
                respEnt = new ResponseEntity<>("Successfully submitted annotations for " + databusID , HttpStatus.OK);
            } catch (IOException ioex) {
                respEnt = new ResponseEntity<>("Failed: INTERNAL SERVER ERROR" , HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            respEnt = new ResponseEntity<>("Failed: " + databusID + " is no valid Databus ID" , HttpStatus.BAD_REQUEST);
        }

        return respEnt;
    }

    @PutMapping("/metadata-graph-annotation")
    ResponseEntity<String> metadataGraphAnnotation(@RequestHeader(value = "content-type") String content_type, @RequestParam String id, @RequestBody String rdfString) {
        return annotateIDwithGraph(id, content_type, rdfString);
    }

    private ResponseEntity<String> annotateIDwithGraph(String identifier, String content_type, String content) {
        Lang rdf_lang;

        Model model = ModelFactory.createDefaultModel();
        // Check if the submitted file actually parses
        try {
            rdf_lang = RDFLanguages.contentTypeToLang(content_type);
            RDFParser.create().fromString(content).lang(rdf_lang).parse(model);
        } catch (Exception e) {
            log.warn("Exception during parsing: ", e);
            return new ResponseEntity<>("Failed: " + e, HttpStatus.BAD_REQUEST);
        }


        boolean is_id = DatabusUtilFunctions.validate(identifier);

        if (!is_id) {
            return new ResponseEntity<>("Failed: " + identifier + " is no valid Databus ID" , HttpStatus.BAD_REQUEST);
        }
        try {
            ms.submit_model(identifier, model);
        } catch (IOException ioex) {
            log.warn("Exception during pushing data: ", ioex);
            return new ResponseEntity<>("INTERNAL ERROR: Virtuoso unavailable", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
