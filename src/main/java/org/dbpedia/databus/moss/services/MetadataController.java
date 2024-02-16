package org.dbpedia.databus.moss.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import com.google.gson.Gson;

import org.apache.jena.atlas.json.io.parserjavacc.javacc.JSON_Parser;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

@RestController
@RequestMapping("api")
public class MetadataController {

    public static final Pattern baseRegex = Pattern.compile("^(https?://[^/]+/annotations/)");
    MetadataService metadataService;
    JSON_Parser parser;
    Gson gson;

    public MetadataController(@Autowired MetadataService metadataService, @Autowired Gson gson) {
        this.metadataService = metadataService;
        this.gson = gson;
    }

    @RequestMapping("/get")
    public ResponseEntity<String> getGraph() {
        // String endpoint = "http://localhost:3002/graph/read?repo=oeo&path=cement/annotation.jsonld";
        String endpoint = "http://localhost:3002/graph/read?repo=oeo&path=cement/annotation.jsonld";

        Model model = ModelFactory.createDefaultModel();
        model = metadataService.getModel(model, endpoint);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.write(out, "jsonld");

        HttpHeaders header = new HttpHeaders();
        ResponseEntity<String> response = ResponseEntity.ok()
                .headers(header)
                .contentType(MediaType.TEXT_PLAIN)
                .body(out.toString());
        return response;
    }

    @RequestMapping(value = {"/annotate/simple"})
    public SimpleAnnotationRequest annotate(@RequestBody String json) {

        // Get simple request from json body
        SimpleAnnotationRequest annotationRequest = gson.fromJson(json, SimpleAnnotationRequest.class);
        // metadataService.createAnnotation(annotationRequest);

        this.metadataService.getIndexerManager().updateIndices("SimpleAnnotationMod", 
            annotationRequest.getDatabusFile());

        return annotationRequest;
    }

    @RequestMapping(value = {"/annotate"})
    public SimpleAnnotationRequest complexAnnotate(@RequestParam String databusFile, @RequestParam MultipartFile annotationGraph) {

        try {
            String content = new String(annotationGraph.getBytes(), StandardCharsets.UTF_8);
            System.out.println(content);
            InputStream graphInputStream = annotationGraph.getInputStream();

            metadataService.createComplexAnnotation(databusFile, graphInputStream);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return null;
    }

    @RequestMapping("/annotations/{databus}/{user}/{group}/{file}")
    public ResponseEntity<Resource> getAnnotations(@PathVariable(value="databus") String databus,
            @PathVariable(value = "user") String user,
            @PathVariable(value = "group") String group,
            @PathVariable(value = "file") String file) throws IOException {

        String suffix = "activity.ttl";
        String volumePath = String.format("./volume/%s/%s/%s/%s/%s", databus, user, group, file, suffix);
        String p = "./volume/databus.testing.org/me/club/cement/activity.ttl";
        File doc = new File(p);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(doc));
        HttpHeaders header = new HttpHeaders();

        return ResponseEntity.ok()
                .headers(header)
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }
}