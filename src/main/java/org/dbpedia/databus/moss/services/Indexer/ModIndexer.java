package org.dbpedia.databus.moss.services.Indexer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;

public class ModIndexer {
    private String id;
    private List<String> todos;
    private ExecutorService worker;
    private ModIndexerConfig config;
    private Future<String> workerStatus;
    private final int fixedPoolSize = 1;

    public ModIndexer(ModIndexerConfig config, List<String> todos) {
        this.config = config;
        this.todos = todos;
        this.id = UUID.randomUUID().toString();
        this.worker = Executors.newFixedThreadPool(fixedPoolSize);
    }

    @Override
    protected void finalize() {
        try {
            this.worker.shutdown();
        } catch (SecurityException e) {
            System.err.println(e);
        }
    }

    public String getId() {
        return id;
    }

    public ModIndexerConfig getConfig() {
        return this.config;
    }

    public void setConfig(ModIndexerConfig config) {
        this.config = config;
    }

    public List<String> getTodos() {
        return this.todos;
    }

    public void setTodos(List<String> todos) {
        this.todos = todos;
    }

    public void addTodo(String todo) {
        this.todos.add(todo);
    }

    public void clearTodos() {
        this.todos.clear();
    }

    public void rebuildIndex(String resourceURI) {
        if (!this.isBusy()) {
            IndexingTask task = new IndexingTask(this.id, "command", this.getTodos(), "jarPath");
            this.workerStatus = this.worker.submit(task);
            this.clearTodos();
        } else {
            this.todos.add(resourceURI);
        }
    }

    public boolean isBusy() {
        return this.workerStatus.isDone();
    }
}
