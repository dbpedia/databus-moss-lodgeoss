package org.dbpedia.databus.moss.services.Indexer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashMap;


public class IndexerManager {

    // Alle indexer
    private List<ModIndexer> indexers;
    //Ein mod kann in 1 oder mehreren Indexern vorkommen -> rebuild index for entsprechenden indexern für die der mod wichtig ist
    private HashMap<String, List<ModIndexer>> indexerMappings;
    
    private ThreadPoolExecutor worker;

    private final int fixedPoolSize = 8;

    private ScheduledExecutorService scheduler;

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

        for(ModIndexer indexer : this.indexers) {
            for(String modType : indexer.getConfig().getMods()) {
                if(!this.indexerMappings.containsKey(modType)) {
                    this.indexerMappings.put(modType, new ArrayList<ModIndexer>());
                }

                this.indexerMappings.get(modType).add(indexer);
            }
        }

        // Schedule a task to run every second
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.scheduler.scheduleAtFixedRate(() -> tick(), 0, 1, TimeUnit.SECONDS); 
        
        this.worker = new ThreadPoolExecutor(fixedPoolSize, fixedPoolSize,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    }

    private void tick() {
        rebuildIndices();
    }

    /**
    public IndexerManager(List<String> annotationMods, List<String> indexers, HashMap<String, List<ModIndexer>> indexerMappings) {
        this.indexers = indexers;
        this.indexerMappings = indexerMappings;
        this.annotationModList = annotationMods;
    }
     */

    public void stop() {
        try {
            this.scheduler.shutdown();
        } catch (SecurityException e) {
            System.err.println(e);
        }
    }

    public HashMap<String,List<ModIndexer>> getIndexerMappings() {
        return this.indexerMappings;
    }

    public void setIndexerMappings(HashMap<String,List<ModIndexer>> indexerMappings) {
        this.indexerMappings = indexerMappings;
    }

    /**
     * Gehe über alle indexer mit todos und starte entsprechende index tasks
     */
    public void rebuildIndices() {

        // System.out.println("Ich manage auf thread " + Thread.currentThread().threadId());
        for(ModIndexer indexer : indexers) {
          
            if(indexer.getTodos().size() == 0) {
                // System.out.println("Nothing to do");
                continue;
            }

            // Hier haben wir etwas zu tun!
            // frage indexer, ob er gerade ein task bearbeitet
            if(indexer.isBusy()) {
                //  System.out.println("Am busy");
                continue;
                
            }

            // Läuft grad nischt für indexer
            // frage prozessmanager, ob wir kapazitäten haben für einen neuen prozess
            if(worker.getActiveCount() >= fixedPoolSize) {
                continue;
            }

            System.out.println("Ich würd denn mal losmachen mit todos: " + indexer.getTodos());
            indexer.run(worker);
        }
    }

    
    public void updateIndices(String modType, String resourceURI) {
        System.out.println("BITTE TÖTEN SIE MICH.");
        List<ModIndexer> correspondingIndexers = indexerMappings.get(modType);
        for (ModIndexer indexer : correspondingIndexers) {
            indexer.addTodo(resourceURI);
            System.out.println("Indexer " + indexer.getId() + "hat jetzt todos: " + indexer.getTodos());
        }
    }
}
