package org.dbpedia.databus.moss.services.Indexer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.dbpedia.databus.moss.services.GstoreConnector;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.google.gson.JsonParser;

/**
 * Loads data into the database and runs an indexer on startup
 */
public class DataLoader {

    private GstoreConnector gstoreConnector;
    private String collectionURI;
    private ModIndexer indexer;

    public DataLoader(DataLoaderConfig config, GstoreConnector gstoreConnector,
            String configRootPath, String lookupBaseURL) {
        this.gstoreConnector = gstoreConnector;
        this.collectionURI = config.getCollectionURI();
        this.indexer = new ModIndexer(config.getIndexer(), configRootPath, lookupBaseURL);
    }

    private String[] loadCollectionFileURIs() throws URISyntaxException {

        try {
            URI uri = new URI(collectionURI);
            String baseUrl = uri.getScheme() + "://" + uri.getHost();

            HttpClient httpClient = HttpClients.createDefault();

            // Get SPARQL query from the collection
            HttpGet collectionQueryGet = new HttpGet(collectionURI);
            collectionQueryGet.addHeader("Accept", "text/sparql");
            HttpResponse response = httpClient.execute(collectionQueryGet);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String sparqlQueryFromCollection = EntityUtils.toString(entity);

                // Run SPARQL query against SPARQL endpoint
                HttpPost sparqlHttpPost = new HttpPost(baseUrl + "/sparql");

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("query", sparqlQueryFromCollection));
                sparqlHttpPost.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse sparqlResponse = httpClient.execute(sparqlHttpPost);
                HttpEntity sparqlEntity = sparqlResponse.getEntity();

                if (sparqlEntity != null) {
                    String result = EntityUtils.toString(sparqlEntity);
                    return extractFileURIs(result);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String[] extractFileURIs(String jsonResponse) {
        List<String> fileUris = new ArrayList<>();

        JsonElement jsonElement = JsonParser.parseString(jsonResponse);

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonObject resultsObject = jsonObject.getAsJsonObject("results");
        JsonArray bindingsArray = resultsObject.getAsJsonArray("bindings");

        for (JsonElement element : bindingsArray) {
            JsonObject binding = element.getAsJsonObject();
            JsonObject fileObject = binding.getAsJsonObject("file");
            String uri = fileObject.get("value").getAsString();
            fileUris.add(uri);
        }

        return fileUris.toArray(new String[0]);
    }

    public void load() {

        // get collection file uris
        try {
            String[] fileURIs = loadCollectionFileURIs();

            if (fileURIs != null) {

                for (int i = 0; i < fileURIs.length; i++) {
                    System.out.println("Loading " + fileURIs[i]);
                    Model model = cleanModel(loadModel(fileURIs[i]));
                    URI fileURI = new URI(fileURIs[i]);

                    String filePath = fileURI.getPath();
                    String newPath = filePath.substring(0, filePath.lastIndexOf('.')) + ".jsonld";
                    String gstorePath = fileURI.getHost() + newPath;

                    System.out.println("Saving to " + gstorePath);

                    gstoreConnector.write("loaded", gstorePath, model);
                }
            }
            System.out.println("Indexing...");
            indexer.run(null);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static boolean containsNonUnicode(String str) {
        if (str == null) {
            return false;
        }

        return !Pattern.matches("\\A\\p{ASCII}*\\z", str);
    }

    private Model cleanModel(Model model) {
        Model cleanedModel = ModelFactory.createDefaultModel();

        StmtIterator iterator = model.listStatements();

        while (iterator.hasNext()) {
            Statement statement = iterator.next();

            if (containsNonUnicode(statement.getSubject().getURI())) {
                continue;
            }

            if (containsNonUnicode(statement.getPredicate().getURI())) {
                continue;
            }

            if (statement.getObject().isResource()) {
                if (containsNonUnicode(statement.getObject().asResource().getURI())) {
                    continue;
                }
            }

            cleanedModel.add(statement);
        }

        return cleanedModel;
    }

    private Model loadModel(String fileURI) throws UnsupportedEncodingException, URISyntaxException {
        Model model = ModelFactory.createDefaultModel();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/ld+json");
        headers.add("Content-Type", "application/ld+json");

        URI gstoreURI = new URI(fileURI);
        ResponseEntity<String> response = restTemplate.getForEntity(gstoreURI, String.class);
        String serverResponse = response.getBody();

        if (serverResponse != null) {
            ByteArrayInputStream targetStream = new ByteArrayInputStream(serverResponse.getBytes("UTF-8"));
            RDFParser.source(targetStream).lang(Lang.TTL).parse(model);
        }

        return model;
    }
}