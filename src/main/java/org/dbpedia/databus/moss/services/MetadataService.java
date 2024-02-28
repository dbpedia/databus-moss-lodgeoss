package org.dbpedia.databus.moss.services;

import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.dbpedia.databus.moss.services.Indexer.IndexerManager;
import org.dbpedia.databus.moss.services.Indexer.IndexerManagerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import virtuoso.jena.driver.VirtDataset;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import java.util.ArrayList;
import java.util.List;

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
            @Value("${uri.gstore}") String gstoreBaseURL) {

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

    public String getGstoreURL(String repo, String path) {
        return this.gStoreBaseURL + "/g/" + repo + "/" + path;
    }

    public String buildURL(String baseURL, List<String> pathSegments) {
        String identifier = "";
        try {
            URIBuilder builder = new URIBuilder(baseURL);
            builder.setPathSegments(pathSegments);

            identifier = builder.build().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return identifier;
    }

    public String buildURL(String baseURL, String[] pathValues) {
        // String endpoint =
        // "http://localhost:3002/graph/save?repo=oeo&path=cement/annotation.jsonld";
        String identifier = "";
        String path = "graph/save";
        int lastIndex = pathValues.length;
        try {
            URIBuilder builder = new URIBuilder(baseURL);
            builder.setPath(path);
            builder.setParameter("repo", pathValues[lastIndex - 2]);
            builder.setParameter("path", pathValues[lastIndex - 1]);

            System.out.println(pathValues);

            identifier = builder.build().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return identifier.replaceAll("%2F", "/");
    }

    public String creatFileIdentifier(String baseURLRaw, String modType, String databusIdentifier) {
        List<String> pathSegments = new ArrayList<String>();

        databusIdentifier = databusIdentifier.replaceAll("http[s]?://", "");
        String[] resourceSegments = databusIdentifier.split("/");

        pathSegments.add("annotations");

        for (String segment : resourceSegments) {
            pathSegments.add(segment);
        }

        pathSegments.add(modType.toLowerCase() + ".jsonld");

        return buildURL(baseURLRaw, pathSegments);
    }

    public String createGStoreIdentifier(String fileIdentifier) {
        String segments = fileIdentifier.replace(this.baseURI + "/", "");
        return this.gStoreBaseURL + "/g/" + segments;
    }

    public URI createEndpointURL(String scheme, String gStoreBaseURL, String repository, String path) {
        List<String> pathSegments = new ArrayList<String>();
        pathSegments.add("graph");
        pathSegments.add("save");
        pathSegments.add("annotations.jsonld");
        URI endpoint = null;

        try {
            endpoint = new URI(buildURL(gStoreBaseURL, pathSegments));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return endpoint;
    }

    List<String> createPathSegments(String modType, String resourcePathRaw) {
        List<String> pathSegments = new ArrayList<String>();

        resourcePathRaw = resourcePathRaw.replaceAll(this.regexResourcePrefix, "");
        String[] resourceSegments = resourcePathRaw.split("/");

        pathSegments.add("mods");
        pathSegments.add(modType);

        for (String segment : resourceSegments) {
            pathSegments.add(segment);
        }
        pathSegments.add("annotations.jsonld");

        return pathSegments;
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

        try {
            saveModel(modData.toModel(), createSaveURL(modData.getFileURI()));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return modData;

    }

    public URL createSaveURL(String annotationFileURI) throws MalformedURLException {
        String path = annotationFileURI.replaceAll(baseURI + "/annotations/", "");
        String uriString = this.gStoreBaseURL + "/graph/save?repo=annotations&path=" + path;
        return URI.create(uriString).toURL();
    }

    private void saveModel(Model annotationModel, URL saveUrl) throws IOException {

        System.out.println("Saving with " + saveUrl.toString());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RDFDataMgr.write(outputStream, annotationModel, Lang.JSONLD);

        System.out.println("#########################################");
        RDFDataMgr.write(System.out, annotationModel, Lang.JSONLD);
        System.out.println("+++++++++++++++++++++++++++++++++++++++++");
        RDFDataMgr.write(System.out, annotationModel, Lang.TURTLE);
        System.out.println("#########################################");

        String jsonString = outputStream.toString("UTF-8");
        System.out.println("jsonjsonjsonjsonjsonjsonjsonjsonjsonjson");
        System.out.println(jsonString);
        System.out.println("jsonjsonjsonjsonjsonjsonjsonjsonjsonjson");
        //RestTemplate restTemplate = new RestTemplate();

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

        /* 
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/ld+json");
        headers.add("Content-Type", "application/ld+json");

        HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
        try {
            URI endpoint = new URI(gStoreEndpoint);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
            String serverResponse = response.getBody();
            System.out.println("----------------server response--------------------");
            System.out.println(serverResponse);
            System.out.println("------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    Model getModel(String fileId) {

        String gstoreIdentifier = createGStoreIdentifier(fileId);
        Model model = ModelFactory.createDefaultModel();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/ld+json");
        headers.add("Content-Type", "application/ld+json");

        try {
            URI gstoreURI = new URI(gstoreIdentifier);
            ResponseEntity<String> response = restTemplate.getForEntity(gstoreURI, String.class);
            String serverResponse = response.getBody();
            System.out.println("============= MODEL FROM GSTORE ================");
            System.out.println(serverResponse);
            System.out.println("============= MODEL FROM GSTORE ================");

            if (serverResponse != null) {
                ByteArrayInputStream targetStream = new ByteArrayInputStream(serverResponse.getBytes("UTF-8"));
                RDFParser.source(targetStream).forceLang(Lang.JSONLD).parse(model);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (HttpClientErrorException e) {
            // : Model not found -> return empty model
            return model;
        } catch (RestClientException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return model;
    }

    Model getModel(String fileId, String gStoreId) {

        String gstoreIdentifier = gStoreId;
        Model model = ModelFactory.createDefaultModel();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/ld+json");
        headers.add("Content-Type", "application/ld+json");

        try {
            URI gstoreURI = new URI(gstoreIdentifier);
            ResponseEntity<String> response = restTemplate.getForEntity(gstoreURI, String.class);
            String serverResponse = response.getBody();
            System.out.println("============= MODEL FROM GSTORE ================");
            System.out.println(serverResponse);
            System.out.println("============= MODEL FROM GSTORE ================");

            if (serverResponse != null) {
                ByteArrayInputStream targetStream = new ByteArrayInputStream(serverResponse.getBytes("UTF-8"));
                RDFParser.source(targetStream).forceLang(Lang.JSONLD).parse(model);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (HttpClientErrorException e) {
            // : Model not found -> return empty model
            return model;
        } catch (RestClientException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
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