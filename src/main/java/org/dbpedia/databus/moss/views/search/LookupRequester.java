package org.dbpedia.databus.moss.views.search;

import java.net.http.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.URI;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public final class LookupRequester {

    private static final String lookup_endpoint = "http://tools.dbpedia.org:9274/lookup-application/api/search?query=%s";

    public static List<LookupObject> getResult(String query) throws Exception {

        Gson gson = new Gson();

        String encoded_query = URLEncoder.encode(query, StandardCharsets.UTF_8);
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest req = HttpRequest.newBuilder().uri(new URI(String.format(lookup_endpoint, encoded_query))).build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

        Map<String, List<LookupObject>> result_map = gson.fromJson(response.body(), new TypeToken<Map<String, List<LookupObject>>>() {}.getType());

        return result_map.get("docs");
    }
}


