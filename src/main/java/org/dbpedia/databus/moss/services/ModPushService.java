package org.dbpedia.databus.moss.services;


import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.dbpedia.databus.moss.views.search.SearchView;
import org.dbpedia.databus.utils.DatabusFileUtil;
import org.dbpedia.databus.utils.MossUtilityFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping(value = "/pushmod")
public class ModPushService {

    private final Logger log = LoggerFactory.getLogger(ModPushService.class);
    private final MetadataService ms;

    public ModPushService(@Autowired MetadataService ms) {
        this.ms = ms;
    }

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
        Model model = ModelFactory.createDefaultModel();;
        try {
            RDFParser.create().fromString(jsonld_string).lang(RDFLanguages.JSONLD).parse(model);
        } catch (Exception e) {
            log.warn("Exception during parsing: ", e);
            return new ResponseEntity<>("Failed: " + e, HttpStatus.BAD_REQUEST);
        }

        String databus_file_iri = "https://databus.dbpedia.org/" + publisher + "/" + group + "/" + artifact + "/" + version + "/" + fileName;
        boolean is_id = DatabusFileUtil.validate(databus_file_iri);

        if (!is_id) {
            return new ResponseEntity<>("Failed: " + databus_file_iri + " is no valid Databus ID" , HttpStatus.BAD_REQUEST);
        }
        try {
            ms.push_model(databus_file_iri, model);
        } catch (IOException ioex) {
            log.warn("Exception during pushing data: ", ioex);
            return new ResponseEntity<>("Failed: " + ioex, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
