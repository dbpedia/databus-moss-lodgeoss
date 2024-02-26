package org.dbpedia.databus.moss.services;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.google.gson.Gson;

import org.apache.jena.atlas.json.io.parserjavacc.javacc.JSON_Parser;

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

        String repo = "annotations";
        String path = request.getRequestURI().split(request.getContextPath() + "/" + repo + "/")[1];

        RestTemplate restTemplate = new RestTemplate();
        String requestURL = this.metadataService.getGstoreURL(repo, path);

        @SuppressWarnings("null")
        ResponseEntity<String> response = restTemplate.getForEntity(requestURL, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @RequestMapping(value = {"/annotate/simple"})
    public SimpleAnnotationRequest annotate(@RequestBody String json) {

        // Get simple request from json body
        SimpleAnnotationRequest annotationRequest = gson.fromJson(json, SimpleAnnotationRequest.class);

        String modURI = metadataService.createSimpleAnnotation(annotationRequest);

        this.metadataService.getIndexerManager().updateIndices("SimpleAnnotationMod", modURI);
        return annotationRequest;
    }

    @RequestMapping(value = {"/annotate"})
    public SimpleAnnotationRequest complexAnnotate(@RequestParam String databusFile, @RequestBody MultipartFile annotationGraph) {

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