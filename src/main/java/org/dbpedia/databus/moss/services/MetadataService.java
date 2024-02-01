package org.dbpedia.databus.moss.services;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDF;
import org.dbpedia.databus.moss.annotation.SVGBuilder;
import org.dbpedia.databus.moss.views.annotation.AnnotationURL;
import org.dbpedia.databus.utils.MossUtilityFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import virtuoso.jena.driver.VirtDataset;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.time.format.DateTimeFormatter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.URLEncoder;
import java.net.URI;
import java.time.LocalDateTime;
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
    private final String regexResourcePrefix = "http[s]?://";

    public MetadataService(@Value("${virt.url}") String virtUrl,
                           @Value("${virt.usr}") String virtUsr,
                           @Value("${virt.psw}") String virtPsw,
                           @Value("${file.vol}") String volume,
                           @Value("${uri.base}") String baseURI) {
        this.virtUrl = virtUrl;
        this.virtUsr = virtUsr;
        this.virtPsw = virtPsw;
        this.baseDir = new File(volume);
        this.baseURI = baseURI;
    }


    void updateModelLegacy(String graphName, Model model, Boolean delete) {
        VirtDataset db = new VirtDataset(virtUrl, virtUsr, virtPsw);
        if (delete && db.containsNamedModel(graphName)) db.removeNamedModel(graphName);
        db.addNamedModel(graphName, model, false);
        db.commit();
        db.close();
    }

    void updateModel() {
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
        return  identifier;
    }

    public String buildURL(String baseURL, String[] pathValues) {
        // String endpoint = "http://localhost:3002/graph/save?repo=oeo&path=cement/annotation.jsonld";
        String identifier = "";
        String path = "graph/save";
        int lastIndex = pathValues.length;
        try {
            URIBuilder builder = new URIBuilder(baseURL);
            builder.setPath(path);
            builder.setParameter("repo", pathValues[lastIndex - 2]);
            builder.setParameter("path", pathValues[lastIndex - 1]);

            identifier = builder.build().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return  identifier.replaceAll("%2F", "/");
    }


    public String creatFileIdentifier(String baseURLRaw, String annotationName, String databusIdentifier) {
        List<String> pathSegments = new ArrayList<String>();

        databusIdentifier = databusIdentifier.replaceAll(this.regexResourcePrefix, "");
        String[] resourceSegments = databusIdentifier.split("/");

        pathSegments.add("annotations");
        pathSegments.add(annotationName);

        for (String segment : resourceSegments) {
            pathSegments.add(segment);
        }

        pathSegments.add("annotations.jsonld");

        return buildURL(baseURLRaw, pathSegments);
    }

    public String createGStoreIdentifier(String[] pathValues) {
        String baseURL = "http://localhost:3002";
        return buildURL(baseURL, pathValues);
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

    List<String> createPathSegments(String annotationName, String resourcePathRaw) {
        List<String> pathSegments = new ArrayList<String>();

        resourcePathRaw = resourcePathRaw.replaceAll(this.regexResourcePrefix, "");
        String[] resourceSegments = resourcePathRaw.split("/");

        pathSegments.add("annotations");
        pathSegments.add(annotationName);

        for (String segment : resourceSegments) {
            pathSegments.add(segment);
        }
        pathSegments.add("annotations.jsonld");

        return pathSegments;
    }

    public void createAnnotation(String databusIdentifier, List<AnnotationURL> annotationURLs) {

        String modVersion = "1.0.0";
        String annotatorName = "simple";
        String dcNamespace = "http://purl.org/dc/terms/";
        String provNamespace = "http://www.w3.org/ns/prov#";
        String mossNamespace = "https://dataid.dbpedia.org/moss#";
        String modFragment = "#mod";
        String fileIdentifier = creatFileIdentifier(this.baseURI, annotatorName, databusIdentifier);
        String segments = fileIdentifier.replace(this.baseURI + "/", "");
        String[] pathValues = segments.split("/", 2);
        String gStoreIdentifier = createGStoreIdentifier(pathValues);

        Model annotationModel = ModelFactory.createDefaultModel();

        annotationModel = getModel(annotationModel, gStoreIdentifier);

        PrefixMapping prefixMapping = annotationModel.getGraph().getPrefixMapping();

        prefixMapping.setNsPrefix("dc", dcNamespace);
        prefixMapping.setNsPrefix("prov", provNamespace);
        prefixMapping.setNsPrefix("moss", mossNamespace);

        // Create resources
        Resource databusResource = ResourceFactory.createResource(databusIdentifier);
        Resource annotationDocumentResource = ResourceFactory.createResource(modFragment);

        for(AnnotationURL annotationURL: annotationURLs) {
            annotationModel.add(
                    databusResource,
                    ResourceFactory.createProperty(dcNamespace, "subject"),
                    ResourceFactory.createResource(annotationURL.getUri()));
        }
        
        String startedAtTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());

        annotationModel.add(annotationDocumentResource, RDF.type, ResourceFactory.createResource("https://dataid.dbpedia.org/moss#SimpleAnnotationMod"));
        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(provNamespace, "used"), databusResource);
        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(provNamespace, "startedAtTime"), startedAtTime);
        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(provNamespace, "endedAtTime"), startedAtTime);
        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(provNamespace, "generated"), annotationModel.createResource(""));

        try {
            saveModel(annotationModel, gStoreIdentifier);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createComplexAnnotation(String databusIdentifier, ByteArrayInputStream graphInputStream, String modType) {

        String modVersion = "1.0.0";
        String annotatorName = "complex";
        String dcNamespace = "http://purl.org/dc/terms/";
        String provNamespace = "http://www.w3.org/ns/prov#";
        String mossNamespace = "https://dataid.dbpedia.org/moss#";
        String modFragment = "#" + modType;
        String fileIdentifier = creatFileIdentifier(this.baseURI, annotatorName, databusIdentifier);
        String segments = fileIdentifier.replace(this.baseURI + "/", "");
        String[] pathValues = segments.split("/", 2);
        String gStoreIdentifier = createGStoreIdentifier(pathValues);

        Model annotationModel = ModelFactory.createDefaultModel();

        annotationModel = getModel(annotationModel, gStoreIdentifier);

        PrefixMapping prefixMapping = annotationModel.getGraph().getPrefixMapping();

        prefixMapping.setNsPrefix("dc", dcNamespace);
        prefixMapping.setNsPrefix("prov", provNamespace);
        prefixMapping.setNsPrefix("moss", mossNamespace);

        // Create resources
        RDFDataMgr.read(annotationModel, graphInputStream, Lang.TURTLE);

        RDFDataMgr.write(System.out, annotationModel, Lang.TURTLE);

        Resource databusResource = ResourceFactory.createResource(databusIdentifier);
        Resource annotationDocumentResource = ResourceFactory.createResource(modFragment);

        String startedAtTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());

        annotationModel.add(annotationDocumentResource, RDF.type, ResourceFactory.createResource("https://dataid.dbpedia.org/moss#" + modType));


        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(provNamespace, "used"), databusResource);
        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(provNamespace, "startedAtTime"), startedAtTime);
        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(provNamespace, "endedAtTime"), startedAtTime);
        annotationModel.add(annotationDocumentResource, ResourceFactory.createProperty(provNamespace, "generated"), annotationModel.createResource(""));

        try {
            saveModel(annotationModel, gStoreIdentifier);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public List<AnnotationURL> getAnnotations(String df) {
        Query query = QueryFactory.create(
                "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
                        "SELECT ?annotation {\n" +
                        "  <"+df+"> dc:subject ?annotation .\n" +
                        "} \n"
        );
        QueryExecution qexec = QueryExecutionFactory.sparqlService("https://mods.tools.dbpedia.org/sparql",query);
        ResultSet rs = qexec.execSelect();

        ArrayList<AnnotationURL> list = new ArrayList<AnnotationURL>();
        while (rs.hasNext()) {
            list.add(new AnnotationURL(rs.next().getResource("annotation").getURI()));
        }
        qexec.close();
        return list;
    }


    private void saveAnnotationsSVG(String databusFilePath, List<AnnotationURL> annotationURLS) {
        String localBase = new String("/home/john/Documents/workspace/whk/moss");
        File annotationSVGFile = new File(localBase, "volume" + databusFilePath + "/" + "annotation.svg");
        try {
            FileOutputStream fos = new FileOutputStream(annotationSVGFile);
            IOUtils.write(SVGBuilder.svgString2dec.replace("#NO", String.valueOf(annotationURLS.size())),fos,StandardCharsets.UTF_8);
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void saveModel(Model annotationModel, String gStoreEndpoint) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RDFDataMgr.write(outputStream, annotationModel, Lang.JSONLD);

        System.out.println("#########################################");
        RDFDataMgr.write(System.out, annotationModel, Lang.TURTLE);
        System.out.println("+++++++++++++++++++++++++++++++++++++++++");
        RDFDataMgr.write(System.out, annotationModel, Lang.JSONLD);
        System.out.println("#########################################");

        String jsonString = outputStream.toString("UTF-8");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/ld+json");
        headers.add("Content-Type", "application/ld+json");

        HttpEntity<String> entity = new HttpEntity<String>(jsonString, headers);
        System.out.println(jsonString);
        try {
            // String endpoint = "http://localhost:3002/graph/save?repo=oeo&path=/simple/cement/annotation.jsonld";
            URI endpoint = new URI(gStoreEndpoint);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
            String serverResponse = response.getBody();
            System.out.println("----------------server response--------------------");
            System.out.println(serverResponse);
            System.out.println("------------------------------------");
            RDFDataMgr.write(System.out, annotationModel, Lang.TURTLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Model getModel(Model model, String gStoreEndpoint) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/ld+json");
        headers.add("Content-Type", "application/ld+json");

        try {
            gStoreEndpoint = gStoreEndpoint.replaceAll("save", "read");
            URI endpoint = new URI(gStoreEndpoint);
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
            String serverResponse = response.getBody();
            System.out.println(serverResponse);
            ByteArrayInputStream targetStream = new ByteArrayInputStream(serverResponse.getBytes("UTF-8"));

            RDFParser.source(targetStream).forceLang(Lang.JSONLD).parse(model);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (HttpClientErrorException e) {
            //: Model not found -> return empty model
            return model;
        } catch (RestClientException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return model;
    }

    public File getFile(String databusIdPath, String result) {
        File file = new File(baseDir,databusIdPath+"/"+result);
        if(file.exists()) {
            return file;
        } else {
            return null;
        }
    }


    public void submit_model(String df, Model push_model) throws IOException {

        String graph_identifier = "#api-demo";
        String databusBase = MossUtilityFunctions.extractBaseFromURL(df);
        ModActivityMetadata mam = new ModActivityMetadata(df, "http://mods.tools.dbpedia.org/ns/demo#ApiDemoMod");
        mam.addModResult("api-demo-data.ttl", "http://dataid.dbpedia.org/ns/mods/core#wasDerivedFrom");
        //svg
        mam.addModResult("api-demo-data.svg", "http://dataid.dbpedia.org/ns/mods/core#svgDerivedFrom");
        Model activityModel = mam.getModel();
        activityModel.add(
                ResourceFactory.createResource("api-demo-data.svg"),
                ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#seeAlso"),
                ResourceFactory.createResource("https://moss.tools.dbpedia.org/submit-data?dfid=" +
                        URLEncoder.encode(df, StandardCharsets.UTF_8)));

        String databusFilePath = df.replace(databusBase, "");

        //FIXME: functions signature change -> figure out appropriate refactoring
        // saveModel(activityModel,databusFilePath,"api-demo-activity.ttl");
        // saveModel(push_model,databusFilePath,"api-demo-data.ttl");

        // updateModel(df+graph_identifier,  getModel(baseURI,databusFilePath,"api-demo-activity.ttl"), true);
        // updateModel(df+graph_identifier, getModel(baseURI,databusFilePath,"api-demo-data.ttl"), false);
        log.info("loaded " + df+graph_identifier);


        File annotationSVGFile = new File(baseDir, databusFilePath + "/" + "api-demo-data.svg");
        try {
            FileOutputStream fos = new FileOutputStream(annotationSVGFile);
            IOUtils.write(SVGBuilder.api_demo_svg_base.replace("#NO", String.valueOf(push_model.size())),fos,StandardCharsets.UTF_8);
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }


    public String fetchAPIData(String identifier) {

        String uri = String.format("%s/fetch?id=%s&file=api-demo-data.ttl", this.baseURI, URLEncoder.encode(identifier, StandardCharsets.UTF_8));
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest req = HttpRequest.newBuilder().uri(
                    new URI(uri)
            ).build();

            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (Exception e) {
            log.warn("Could not load turtle data for submission page: " + e);
            return "";
        }
    }
}