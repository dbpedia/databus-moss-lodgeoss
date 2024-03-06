package org.dbpedia.databus.moss.services;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.dbpedia.databus.moss.services.Indexer.IndexerManager;
import org.dbpedia.databus.moss.services.Indexer.IndexerManagerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import virtuoso.jena.driver.VirtDataset;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

import com.google.gson.Gson;

@Service
public class MetadataService {

    private final Logger log = LoggerFactory.getLogger(MetadataService.class);

    private final String virtUrl;
    private final String virtUsr;
    private final String virtPsw;
    private final File baseDir;
    private final String baseURI;
    private final String gStoreBaseURL;
    private final String regexResourcePrefix = "http[s]?://";
    @SuppressWarnings("unused")
    private IndexerManager indexerManager;
    private GstoreConnector gstoreConnector;

    public MetadataService(@Value("${virt.url}") String virtUrl,
            @Value("${virt.usr}") String virtUsr,
            @Value("${virt.psw}") String virtPsw,
            @Value("${file.vol}") String volume,
            @Value("${file.configPath}") String configPath,
            @Value("${file.indexerJarPath}") String indexerJarPath,
            @Value("${uri.base}") String baseURI,
            @Value("${uri.gstore}") String gstoreBaseURL
            ) {

        File file = new File(configPath);
        IndexerManagerConfig indexerConfig = IndexerManagerConfig.fromJson(file);

        this.gstoreConnector = new GstoreConnector(gstoreBaseURL);

        String configRootPath = file.getParentFile().getAbsolutePath();
        this.indexerManager = new IndexerManager(configRootPath, indexerJarPath, indexerConfig, gstoreConnector);
        this.virtUrl = virtUrl;
        this.virtUsr = virtUsr;
        this.virtPsw = virtPsw;
        this.baseDir = new File(volume);
        this.baseURI = baseURI;
        this.gStoreBaseURL = gstoreBaseURL;
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("Stopping.");
        this.indexerManager.stop();
    }

    void updateModelLegacy(String graphName, Model model, Boolean delete) {
        VirtDataset db = new VirtDataset(virtUrl, virtUsr, virtPsw);
        if (delete && db.containsNamedModel(graphName))
            db.removeNamedModel(graphName);
        db.addNamedModel(graphName, model, false);
        db.commit();
        db.close();
    }

    public String createSimpleAnnotation(SimpleAnnotationRequest annotationRequest) {
        // Get the resource to annotate
        String databusResourceURI = annotationRequest.getDatabusFile();

        // Create annotation data for the resource
        SimpleAnnotationModData modData = new SimpleAnnotationModData(this.baseURI, databusResourceURI);

        // Check what we already have in the database
        Model currentModel = getModel(modData.getFileURI());

        System.out.println("Loaded model from gstore");

        // Add annotation statements from the existing model
        modData.addSubjectsFromModel(currentModel);

        // Add new annotations, hashset will deduplicate
        for (String tag : annotationRequest.getTags()) {
            modData.addSubject(tag);
        }

        // Convert the data to jena model and save
        try {
            saveModel(modData.toModel(), createSaveURL(modData.getFileURI()));
            return modData.getId();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    public RDFAnnotationModData createRDFAnnotation(RDFAnnotationRequest request) {

        RDFAnnotationModData modData = new RDFAnnotationModData(this.baseURI, request);
        //FIXME: if modPath also required for model creation -> pass it into the RDFAnnotationModData constructor
        String modURI = request.getModPath();
        String saveURLRAW = modURI != null ? modURI : modData.getFileURI();

        try {
            saveModel(modData.toModel(), createSaveURL(saveURLRAW));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return modData;
    }

    public URL createSaveURL(String annotationFileURI) throws MalformedURLException {
        String path = annotationFileURI.replaceAll(baseURI, "");
        String uriString = this.gStoreBaseURL + "/g" + path;
        return URI.create(uriString).toURL();
    }

    private void saveModel(Model annotationModel, URL saveUrl) throws IOException {

        System.out.println("Saving with " + saveUrl.toString());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RDFDataMgr.write(outputStream, annotationModel, Lang.JSONLD);

        System.out.println("#########################################");
        RDFDataMgr.write(System.out, annotationModel, RDFFormat.JSONLD_COMPACT_PRETTY);
        System.out.println("+++++++++++++++++++++++++++++++++++++++++");
        RDFDataMgr.write(System.out, annotationModel, Lang.TURTLE);
        System.out.println("#########################################");

        String jsonString = outputStream.toString("UTF-8");
        System.out.println("jsonjsonjsonjsonjsonjsonjsonjsonjsonjson");
        System.out.println(jsonString);
        System.out.println("jsonjsonjsonjsonjsonjsonjsonjsonjsonjson");

        HttpURLConnection con = (HttpURLConnection) saveUrl.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept", "application/ld+json");
        con.setRequestProperty("Content-Type", "application/ld+json");

        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }
    }

    public void saveMod(String json) {
        Model annotationModel = ModelFactory.createDefaultModel();
        InputStream targetStream = new ByteArrayInputStream(json.getBytes());
        RDFDataMgr.read(annotationModel, targetStream, Lang.JSONLD);
        String provNamespace = "http://www.w3.org/ns/prov#";
        Property generatedProperty = annotationModel.getProperty(provNamespace + "generated");
        List<Resource> resources = annotationModel.listSubjectsWithProperty(generatedProperty).toList();

        try {
            Resource modURI = resources.get(0);
            URL modSaveURL = this.createSaveURL(modURI.toString());
            this.saveModel(annotationModel, modSaveURL);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    Model getModel(String fileId) {

        Model model = ModelFactory.createDefaultModel();
        try {
            model = this.gstoreConnector.read(fileId);
        } catch (URISyntaxException uriSyntaxException) {
            uriSyntaxException.printStackTrace();
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return model;
    }

    public File getFile(String databusIdPath, String result) {
        File file = new File(baseDir, databusIdPath + "/" + result);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    public String fetchAPIData(String identifier) {

        String uri = String.format("%s/fetch?id=%s&file=api-demo-data.ttl", this.baseURI,
                URLEncoder.encode(identifier, StandardCharsets.UTF_8));
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest req = HttpRequest.newBuilder().uri(
                    new URI(uri)).build();

            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (Exception e) {
            log.warn("Could not load turtle data for submission page: " + e);
            return "";
        }
    }

    public IndexerManager getIndexerManager() {
        return this.indexerManager;
    }
}