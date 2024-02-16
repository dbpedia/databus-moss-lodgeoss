package org.dbpedia.databus.moss.services.Indexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public class ModIndexer {
    private String id;
    private HashSet<String> todos;
    // private ExecutorService worker;
    private ModIndexerConfig config;
    private Future indexingFuture;
    private String jarPath;
    // private final int fixedPoolSize = 1;

    public ModIndexer(ModIndexerConfig config, String jarPath) {
        this.config = config;
        this.jarPath = jarPath;
        this.todos = new HashSet<String>();
        this.id = UUID.randomUUID().toString();
        // this.worker = Executors.newFixedThreadPool(fixedPoolSize);
    }

    /*
    @Override
    protected void finalize() {
        try {
            this.worker.shutdown();
        } catch (SecurityException e) {
            System.err.println(e);
        }
    }
 */

    public String getId() {
        return id;
    }

    public ModIndexerConfig getConfig() {
        return this.config;
    }

    public void setConfig(ModIndexerConfig config) {
        this.config = config;
    }

    public HashSet<String> getTodos() {
        return this.todos;
    }

    public void setTodos(HashSet<String> todos) {
        this.todos = todos;
    }

    public void addTodo(String todo) {
        this.todos.add(todo);
    }

    public void clearTodos() {
        this.todos.clear();
    }

    public void run(ExecutorService executor) {
        List<String> resources = new ArrayList<String>();
        resources.addAll(this.todos);
        this.todos.clear();

        IndexingTask task = new IndexingTask(this.config.getConfigPath(), "command", resources, jarPath);
        this.indexingFuture = executor.submit(task);
    }


    public boolean isBusy() {
        return this.indexingFuture != null && !this.indexingFuture.isDone();
    }
}
