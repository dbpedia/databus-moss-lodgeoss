package org.dbpedia.databus.moss.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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

    @RequestMapping(value = { "/annotate/simple" })
    public SimpleAnnotationRequest annotate(@RequestBody String json) {

        // Get simple request from json body
        SimpleAnnotationRequest annotationRequest = gson.fromJson(json, SimpleAnnotationRequest.class);

        String modURI = metadataService.createSimpleAnnotation(annotationRequest);

        this.metadataService.getIndexerManager().updateIndices("SimpleAnnotationMod", modURI);
        return annotationRequest;
    }

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

    @RequestMapping(value = { "/save" })
    public void save(@RequestBody String json) {
        this.metadataService.saveMod(json);
    }

    @RequestMapping(value = { "/fetch" })
    public void fetch() {
        MetadataAnnotator annotator = new MetadataAnnotator();
        HashMap<String, String> annotationMappings = annotator.fetchOEMetadata();
        ArrayList<String> failedDatabusIds = new ArrayList<String>();

        for (String databusIdentifier : annotationMappings.keySet()) {
            String metadataFile = annotationMappings.get(databusIdentifier);
            try {
                parseToAnnotationModData(databusIdentifier, metadataFile);
            } catch (MalformedURLException malformedURLException) {
                System.err.println(malformedURLException);
            } catch (IOException ioException) {
                System.err.println(ioException);
            } catch (RiotException riotException) {https://www.youtube.com/watch?v=-Wd_bVNy2KE
                System.err.println(riotException);
                failedDatabusIds.add(databusIdentifier);
            }
        }

        System.out.println("Done");
        System.out.println("Files " + annotationMappings.size());
        System.out.println("Failed " + failedDatabusIds.size());
    }

    public void parseToAnnotationModData(String databusIdentifier, String metadataFile) throws MalformedURLException, IOException, RiotException {
        URL metadataURL = new URL(metadataFile);
        String jsonString = IOUtils.toString(metadataURL, Charset.forName("UTF8"));
        System.out.println(jsonString);
        Model annotationModel = ModelFactory.createDefaultModel();

        InputStream annotationGraphInputStream = IOUtils.toInputStream(jsonString, Charset.forName("UTF8"));
        RDFDataMgr.read(annotationModel, annotationGraphInputStream, Lang.JSONLD);
        RDFAnnotationRequest request = new RDFAnnotationRequest(
                databusIdentifier,
                "OEMetadataMod",
                annotationModel,
                "1.0.0");

        RDFAnnotationModData modData = metadataService.createRDFAnnotation(request);
        this.metadataService.getIndexerManager().updateIndices(modData.getModType(), modData.getModURI());
    }

}