package org.dbpedia.databus.moss.services;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import virtuoso.jena.driver.VirtDataset;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class MetadataService {

    private Logger log = LoggerFactory.getLogger(MetadataService.class);

    private String virtUrl;
    private String virtUsr;
    private String virtPsw;

    public MetadataService(@Value("${virt.url}") String virtUrl,
                           @Value("${virt.usr}") String virtUsr,
                           @Value("${virt.psw}") String virtPsw) {
        this.virtUrl = virtUrl;
        this.virtUsr = virtUsr;
        this.virtPsw = virtPsw;
    }



    void updateModel(String graphName, Model model, Boolean delete) {
        log.info("url "+virtUrl);
        log.info("usr "+virtUsr);
        log.info("psw "+virtPsw);
        VirtDataset db = new VirtDataset(virtUrl,virtUsr,virtPsw);
        if(delete && db.containsNamedModel(graphName)) db.removeNamedModel(graphName);
        db.addNamedModel(graphName,model,false);
        db.commit();
        db.close();
        log.info("loaded "+graphName);
    }

    public void test() {
        ModActivityMetadata mam = new ModActivityMetadata("https://databus.dbpedia.org/vehnem/paper-supplements/demo-graph/20210301/demo-graph.png");
        mam.addModResult("annotation.ttl", "http://dataid.dbpedia.org/ns/mods/core#wasDerivedFrom");
        mam.addModResult("https://img.shields.io/badge/foo-bar-red", "http://dataid.dbpedia.org/ns/mods/core#svgDerivedFrom");
        Model model = mam.getModel();
        model.add(
                ResourceFactory.createResource("https://img.shields.io/badge/foo-bar-red"),
                ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#seeAlso"),
                ResourceFactory.createResource("https://moss.tools.dbpedia.org/annotate?dfid="+
                        URLEncoder.encode(
                                "https://databus.dbpedia.org/vehnem/paper-supplements/demo-graph/20210301/demo-graph.png",
                                StandardCharsets.UTF_8)));
        updateModel("http://example.org/test",model,true);
    }
}