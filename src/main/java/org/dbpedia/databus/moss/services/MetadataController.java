package org.dbpedia.databus.moss.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import org.dbpedia.databus.moss.views.annotation.AnnotationURL;

@RestController
@RequestMapping("api")
public class MetadataController {

    MetadataService metadataService;
    Gson gson;

    public MetadataController(@Autowired MetadataService metadataService, @Autowired Gson gson) {
        this.metadataService = metadataService;
        this.gson = gson;
    }

    @RequestMapping(value = {"/annotate"})
    public AnnotationRequest annotate(@RequestBody String json) {

        AnnotationRequest annotationRequest = gson.fromJson(json, AnnotationRequest.class);
        List<String> tags = annotationRequest.getTags();
        List<AnnotationURL> annotationURLs = new ArrayList<AnnotationURL>();

        for (String tag : tags) {
            AnnotationURL annotationURL = new AnnotationURL(tag);
            annotationURLs.add(annotationURL);
        }

        metadataService.createAnnotation(annotationRequest.databusFile, annotationURLs);

        return annotationRequest;
    }
}