package org.dbpedia.databus.moss.services.Indexer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.jena.ext.com.google.common.io.Files;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


public class IndexingTask implements Runnable {

    List<String> todos;
    String configPath;
    String indexerJarPath;
    String indexEndpoint;

    public IndexingTask(String configPath, String indexEndpoint, List<String> todos, String indexerJarPath) {
        this.todos = todos;
        this.indexerJarPath = indexerJarPath;
        this.configPath = configPath;
        this.indexEndpoint = indexEndpoint;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void run() {

        System.out.println("Ich bims der runner auf thread " + Thread.currentThread().getId());

        try {
            File configFile = new File(configPath);
            System.out.println("Building index with config " + configFile.getAbsolutePath());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            RestTemplate restTemplate = new RestTemplate();

            byte[] configFileBytes = Files.toByteArray(configFile);

            // This nested HttpEntiy is important to create the correct
            // Content-Disposition entry with metadata "name" and "filename"
            MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
            ContentDisposition contentDisposition = ContentDisposition
                    .builder("form-data")
                    .name("config")
                    .filename(configFile.getAbsolutePath())
                    .build();
            fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            HttpEntity<byte[]> fileEntity = new HttpEntity<>(configFileBytes, fileMap);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("config", fileEntity);


            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            System.out.println(requestEntity);
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        this.indexEndpoint,
                        HttpMethod.POST,
                        requestEntity,
                        String.class);
                System.out.println(response);
            } catch (HttpClientErrorException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("gefahr");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("gefahr");
            e.printStackTrace();
        }
        System.out.println("Fertig auf " + Thread.currentThread().getId());
    }
}
