package org.dbpedia.databus.moss.services;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

@CrossOrigin(origins = "*")
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

    // @GetMapping("/secured")
    // public ResponseEntity<String> hello(Principal principal, HttpServletRequest request) {
    //     OAuth2User user = ((OAuth2User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    //     return new ResponseEntity<String>("Hello World!", HttpStatus.OK);
    // }

    @PreAuthorize("hasRole('user')")
    @GetMapping("/secure")
    public ResponseEntity<String> secureEndpoint() {
        return ResponseEntity.ok("Access granted to secure endpoint.");
    }

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("Access granted to public endpoint.");
    }

   
}