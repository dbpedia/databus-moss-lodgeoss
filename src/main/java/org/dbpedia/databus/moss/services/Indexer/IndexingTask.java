package org.dbpedia.databus.moss.services.Indexer;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class IndexingTask implements Runnable {

    List<String> todos;
    String configPath;
    String indexerJarPath;

    public IndexingTask(String configPath, List<String> todos, String indexerJarPath) {
        this.todos = todos;
        this.indexerJarPath = indexerJarPath;
        this.configPath = configPath;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void run() {

        System.out.println("Ich bims der runner auf thread " + Thread.currentThread().getId());

        try {
            File configFile = new File(configPath);

            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", indexerJarPath);
            processBuilder.command().add("-c");
            processBuilder.command().add(configFile.getAbsolutePath());

            if (todos.size() > 0) {
                processBuilder.command().add("-v");
                processBuilder.command().addAll(todos);
            }
            
            processBuilder.inheritIO();

            System.out.println("Starting process");
            Process process = processBuilder.start();

            // Wait for the process to finish
            process.waitFor();

        } catch (IOException e) {
            System.out.println("gefahr");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("gefahr 2");
            e.printStackTrace();
        }
        System.out.println("Fertig auf " + Thread.currentThread().getId());

        // Print exit code for debugging
    }
}
