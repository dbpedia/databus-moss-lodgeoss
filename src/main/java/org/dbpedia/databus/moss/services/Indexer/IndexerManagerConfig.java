package org.dbpedia.databus.moss.services.Indexer;

import java.util.List;

import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.File;
import java.io.IOException;


public class IndexerManagerConfig {
    private List<ModIndexerConfig> indexers;

    public List<ModIndexerConfig> getIndexers() {
        return indexers;
    }

    public void setIndexers(List<ModIndexerConfig> indexerConfigs) {
        this.indexers = indexerConfigs;
    }

    public static IndexerManagerConfig fromJson(File file) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES));
        
            return mapper.readValue(file, IndexerManagerConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}