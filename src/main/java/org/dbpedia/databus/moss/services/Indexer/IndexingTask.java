package org.dbpedia.databus.moss.services.Indexer;

import java.util.List;
import java.util.concurrent.Callable;

public class IndexingTask implements Callable<String> {

    String command;
    List<String> todos;
    String indexer;
    String path;

    public IndexingTask(String indexer, String command, List<String> todos, String jarPath) {
        this.todos = todos;
        this.path = jarPath;
        this.indexer = indexer;
        this.command = command;
    }

    @Override
    public String call() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", this.path);
        processBuilder.command().addAll(todos);

        // Start the process
        Process process = processBuilder.start();
        // Wait for the process to finish
        int exitCode = process.waitFor();

        // Print exit code for debugging
        return "JAR Execution finished with exit code: " + exitCode;
    }
}
