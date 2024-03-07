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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@RequestMapping("api")
@ConditionalOnProperty(prefix = "debug.fetch", name = "enabled")
@RestController
public class MetadataFetchController {

    MetadataService metadataService;

    public MetadataFetchController(@Autowired MetadataService metadataService) {
        this.metadataService = metadataService;
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
            } catch (RiotException riotException) {
                System.err.println(riotException);
                failedDatabusIds.add(databusIdentifier);
            } catch (URISyntaxException e) {
                System.err.println(e);
            }
        }

        System.out.println("Done");
        System.out.println("Files " + annotationMappings.size());
        System.out.println("Failed " + failedDatabusIds.size());
    }

    public void parseToAnnotationModData(String databusIdentifier, String metadataFile) throws MalformedURLException, IOException, RiotException, URISyntaxException {
        URL metadataURL = new URI(metadataFile).toURL();
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
