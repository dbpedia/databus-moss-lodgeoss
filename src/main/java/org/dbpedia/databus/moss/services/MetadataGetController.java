package org.dbpedia.databus.moss.services;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

@RequestMapping("/")
@RestController
public class MetadataGetController {

    private MetadataService metadataService;
     Gson gson;

    public MetadataGetController(@Autowired MetadataService metadataService, @Autowired Gson gson) {
        this.metadataService = metadataService;
        this.gson = gson;
    }

    /**
     * Route requests with /g directly to the gstore
     * @param request
     * @return
     */
    @GetMapping("/g/**")
    public ResponseEntity<String> getGraph(HttpServletRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        String requestURL = this.metadataService.getGStoreBaseURL() + request.getRequestURI();
        ResponseEntity<String> response = restTemplate.getForEntity(requestURL, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}