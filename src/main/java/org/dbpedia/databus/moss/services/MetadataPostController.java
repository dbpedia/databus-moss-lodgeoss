package org.dbpedia.databus.moss.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.json.io.parserjavacc.javacc.JSON_Parser;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;


@RequestMapping("api")
@RestController
public class MetadataPostController {

    public static final Pattern baseRegex = Pattern.compile("^(https?://[^/]+/annotations/)");
    MetadataService metadataService;
    JSON_Parser parser;
    Gson gson;

    public MetadataPostController(@Autowired MetadataService metadataService, @Autowired Gson gson) {
        this.metadataService = metadataService;
        this.gson = gson;
    }

    /**
     * TODO: Remove this and replace with client-side call to /annotate
     * @param json
     * @return
     */
    @RequestMapping(value = { "/annotate/simple" })
    public SimpleAnnotationRequest annotate(@RequestBody String json) {

        // Get simple request from json body
        SimpleAnnotationRequest annotationRequest = gson.fromJson(json, SimpleAnnotationRequest.class);

        String modURI = metadataService.createSimpleAnnotation(annotationRequest);

        this.metadataService.getIndexerManager().updateIndices("SimpleAnnotationMod", modURI);
        return annotationRequest;
    }

    /**
     * The main API route to annotate Databus resources
     * @param databusURI
     * @param modURI
     * @param modType
     * @param modVersion
     * @param annotationGraph
     * @return
     */
    @RequestMapping(value = { "/annotate" })
    public SimpleAnnotationRequest complexAnnotate(@RequestParam String databusURI,
            @RequestParam(required = false) String modURI,
            @RequestParam String modType,
            @RequestParam String modVersion,
            @RequestParam MultipartFile annotationGraph) {

        Model annotationModel = ModelFactory.createDefaultModel();

        try {
            InputStream annotationGraphInputStream = annotationGraph.getInputStream();
            RDFDataMgr.read(annotationModel, annotationGraphInputStream, Lang.JSONLD);
            RDFAnnotationRequest request = new RDFAnnotationRequest(databusURI,
                    modType,
                    annotationModel,
                    modVersion,
                    modURI);

            RDFAnnotationModData modData = metadataService.createRDFAnnotation(request);
            this.metadataService.getIndexerManager().updateIndices(modData.getModType(), modData.getModURI());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    /**
     * 
     * @param json
     */
    @RequestMapping(value = { "/save" })
    public void save(@RequestBody String json) {
        this.metadataService.saveMod(json);
    }
}