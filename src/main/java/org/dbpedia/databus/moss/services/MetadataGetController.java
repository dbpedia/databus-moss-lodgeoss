package org.dbpedia.databus.moss.services;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import org.apache.jena.atlas.json.io.parserjavacc.javacc.JSON_Parser;

@RequestMapping("/")
@RestController
public class MetadataGetController {

    public static final Pattern baseRegex = Pattern.compile("^(https?://[^/]+/annotations/)");
    MetadataService metadataService;
    JSON_Parser parser;
    Gson gson;

    public MetadataGetController(@Autowired MetadataService metadataService, @Autowired Gson gson) {
        this.metadataService = metadataService;
        this.gson = gson;
    }

    // http://localhost:8080/annotations/SimpleAnnotationMod/databus.testing.org/cement/annotations.jsonld
    // /annotations/{databus}/{user}/{group}/{file}
    @GetMapping("/g/**")
    public ResponseEntity<String> getGraph(HttpServletRequest request) {

        RestTemplate restTemplate = new RestTemplate();
        String requestURL = this.metadataService.getGStoreBaseURL() + request.getRequestURI();

        @SuppressWarnings("null")
        ResponseEntity<String> response = restTemplate.getForEntity(requestURL, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}