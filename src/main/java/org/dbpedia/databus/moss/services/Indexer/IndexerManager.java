package org.dbpedia.databus.moss.services.Indexer;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;


public class IndexerManager {

    // Alle indexer
    private List<ModIndexer> indexers;
    //Ein mod kann in 1 oder mehreren Indexern vorkommen -> rebuild index for entsprechenden indexern für die der mod wichtig ist
    private HashMap<String, List<ModIndexer>> indexerMappings;


    public IndexerManager(IndexerManagerConfig config) {

        this.indexers = new ArrayList<ModIndexer>();
        this.indexerMappings = new HashMap<String, List<ModIndexer>>();

        if(config == null){
            return;
        }

        for(ModIndexerConfig indexerConfig : config.getIndexers()) {

            ModIndexer modIndexer = new ModIndexer(indexerConfig);

            this.indexers.add(modIndexer);
            System.out.println("Created indexer with id " + modIndexer.getId());
            System.out.println("Config path: " + modIndexer.getConfig().getConfigPath());
            System.out.println("Mods: " + modIndexer.getConfig().getMods());
        }

    }

    /**
    public IndexerManager(List<String> annotationMods, List<String> indexers, HashMap<String, List<ModIndexer>> indexerMappings) {
        this.indexers = indexers;
        this.indexerMappings = indexerMappings;
        this.annotationModList = annotationMods;
    }
     */

    public HashMap<String,List<ModIndexer>> getIndexerMappings() {
        return this.indexerMappings;
    }

    public void setIndexerMappings(HashMap<String,List<ModIndexer>> indexerMappings) {
        this.indexerMappings = indexerMappings;
    }

    public void loadIndexers() {

    }

    public void updateIndices(String modType, String resourceURI) {
        List<ModIndexer> correspondingIndexers = indexerMappings.get(resourceURI);
        for (ModIndexer indexer : correspondingIndexers) {
            indexer.rebuildIndex(resourceURI);
        }
    }

}
