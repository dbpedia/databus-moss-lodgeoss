package org.dbpedia.databus.moss.services;


import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.dbpedia.databus.moss.views.search.SearchView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@RestController
@RequestMapping(value = "/pushmod")
public class ModPushService {

    private final Logger log = LoggerFactory.getLogger(ModPushService.class);

    @PutMapping(value = "/{publisher}/{group}/{artifact}/{version}/{fileName}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<String> put_jsonld_annotation(
            @PathVariable String publisher,
            @PathVariable String group,
            @PathVariable String artifact,
            @PathVariable String version,
            @PathVariable String fileName,
            @RequestBody String jsonld_string
    ) {

        // Check if the submitted file actually parses
        try {
            Model model = ModelFactory.createDefaultModel();

            RDFParser.create().fromString(jsonld_string).lang(RDFLanguages.JSONLD).parse(model);
        } catch (Exception e) {
            log.warn("exception during parsing: ", e);
            return new ResponseEntity<>("Failed: " + e, HttpStatus.BAD_REQUEST);
        }

        String databus_file_iri = "https://databus.dbpedia.org/" + publisher + "/" + group + "/" + artifact + "/" + version + "/" + fileName;
        System.out.println(databus_file_iri);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
