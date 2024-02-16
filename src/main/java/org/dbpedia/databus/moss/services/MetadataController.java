package org.dbpedia.databus.moss.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import com.google.gson.Gson;

import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.atlas.json.io.parserjavacc.javacc.JSON_Parser;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.function.library.leviathan.reciprocal;

// @RequestMapping("api")
@RestController
public class MetadataController {

    public static final Pattern baseRegex = Pattern.compile("^(https?://[^/]+/annotations/)");
    MetadataService metadataService;
    JSON_Parser parser;
    Gson gson;

    public MetadataController(@Autowired MetadataService metadataService, @Autowired Gson gson) {
        this.metadataService = metadataService;
        this.gson = gson;
    }

    // http://localhost:8080/annotations/SimpleAnnotationMod/databus.testing.org/cement/annotations.jsonld
    // /annotations/{databus}/{user}/{group}/{file}
    @GetMapping("/annotations/**")
    public ResponseEntity<String> getGraph(HttpServletRequest request) {
        // String _endpoint = "http://localhost:3002/graph/read?repo=oeo&path=cement/annotation.jsonld";
        // String endpoint = this.metadataService.buildURL("read", "annotations", path);

        // Model model = ModelFactory.createDefaultModel();
        // model = metadataService.getModel(model, endpoint);

        // ByteArrayOutputStream out = new ByteArrayOutputStream();
        // model.write(out, "jsonld");

        // HttpHeaders header = new HttpHeaders();
        // ResponseEntity<String> response = ResponseEntity.ok()
        //         .headers(header)
        //         .contentType(MediaType.APPLICATION_JSON)
        //         .body(out.toString());
        // return response;

    // @GetMapping("all/**")
    // public String allDirectories(HttpServletRequest request) {
    //     return request.getRequestURI()
    //         .split(request.getContextPath() + "/all/")[1];
    // }

        String repo = "annotations";
        String path = request.getRequestURI().split(request.getContextPath() + "/" + repo + "/")[1];

        RestTemplate restTemplate = new RestTemplate();
        String endpoint = this.metadataService.buildURL("read", repo, path);
        endpoint = endpoint.replaceAll("%2F", "/");

        System.out.println(endpoint);

        @SuppressWarnings("null")
        ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);

        System.out.println("after");
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @RequestMapping(value = {"/annotate/simple"})
    public SimpleAnnotationRequest annotate(@RequestBody String json) {

        // Get simple request from json body
        SimpleAnnotationRequest annotationRequest = gson.fromJson(json, SimpleAnnotationRequest.class);
        metadataService.createAnnotation(annotationRequest);

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
            e.printStackTrace();
        }

        return null;
    }

    // @RequestMapping("/annotations/{databus}/{user}/{group}/{file}")
    // public ResponseEntity<Resource> getAnnotations(@PathVariable(value="databus") String databus,
    //         @PathVariable(value = "user") String user,
    //         @PathVariable(value = "group") String group,
    //         @PathVariable(value = "file") String file) throws IOException {

    //     String suffix = "activity.ttl";
    //     String volumePath = String.format("./volume/%s/%s/%s/%s/%s", databus, user, group, file, suffix);
    //     String p = "./volume/databus.testing.org/me/club/cement/activity.ttl";
    //     File doc = new File(p);

    //     InputStreamResource resource = new InputStreamResource(new FileInputStream(doc));
    //     HttpHeaders header = new HttpHeaders();

    //     return ResponseEntity.ok()
    //             .headers(header)
    //             .contentType(MediaType.TEXT_PLAIN)
    //             .body(resource);
    // }
}