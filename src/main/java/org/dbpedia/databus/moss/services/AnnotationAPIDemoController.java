package org.dbpedia.databus.moss.services;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.dbpedia.databus.utils.DatabusUtilFunctions;
import org.dbpedia.databus.utils.MossUtilityFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping(value = "/annotation-api-demo")
public class AnnotationAPIDemoController {

    private final Logger log = LoggerFactory.getLogger(AnnotationAPIDemoController.class);
    private final MetadataService ms;

    public AnnotationAPIDemoController(@Autowired MetadataService ms) {
        this.ms = ms;
    }

//    @PutMapping(value = {"/{publisher}/{group}/{artifact}/{version}/{fileName}", "/{publisher}/{group}/{artifact}/{version}", "/{publisher}/{group}/{artifact}", "/{publisher}/{group}"})
//    ResponseEntity<String> put_jsonld_annotation(
//            @RequestHeader(value = "content-type") String content_type,
//            @PathVariable String publisher,
//            @PathVariable String group,
//            @PathVariable(required = false) String artifact,
//            @PathVariable(required = false) String version,
//            @PathVariable(required = false) String fileName,
//            @RequestBody String rdf_string
//    ) {
//        StringBuilder sb = new StringBuilder(dbFileUtil.DATABUS_BASE + "/" + publisher + "/" + group);
//
//        for (String pathPart : new String[]{artifact, version, fileName}) {
//            if (pathPart != null) sb.append("/").append(pathPart);
//        }
//        return annotateIdentifier(sb.toString(), content_type, rdf_string);
//    }

    @PutMapping("/submit")
    ResponseEntity<String> submitContent(@RequestHeader(value = "content-type") String content_type, @RequestParam String id, @RequestBody String rdfString) {
        return annotateIdentifier(id, content_type, rdfString);
    }

    private ResponseEntity<String> annotateIdentifier(String identifier, String content_type, String content) {
        Lang rdf_lang;

        // catch json input and convert it
        // set language to ntriples since this is returned by json2rdf
        if (content_type.equals("application/json")) {
            content = MossUtilityFunctions.get_ntriples_from_json(content);
            rdf_lang = RDFLanguages.NTRIPLES;
        } else {
            rdf_lang = RDFLanguages.contentTypeToLang(content_type);
        }
        // Check if the submitted file actually parses
        Model model = ModelFactory.createDefaultModel();
        try {
            RDFParser.create().fromString(content).lang(rdf_lang).parse(model);
        } catch (Exception e) {
            log.warn("Exception during parsing: ", e);
            return new ResponseEntity<>("Failed: " + e, HttpStatus.INTERNAL_SERVER_ERROR);
        }


        boolean is_id = DatabusUtilFunctions.validate(identifier);

        if (!is_id) {
            return new ResponseEntity<>("Failed: " + identifier + " is no valid Databus ID" , HttpStatus.BAD_REQUEST);
        }
        try {
            ms.submit_model(identifier, model);
        } catch (IOException ioex) {
            log.warn("Exception during pushing data: ", ioex);
            return new ResponseEntity<>("Failed: " + ioex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
