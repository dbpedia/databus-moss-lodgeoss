package org.dbpedia.databus.moss.services.Indexer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.util.ResourceUtils;

public class IndexingTask implements Runnable {

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
    public void run()  {

        System.out.println("Ich bims der runner auf thread " + Thread.currentThread().getId());
       
        try {
            File indexJar = ResourceUtils.getFile("classpath:lookup-indexer.jar");
            File configFile = ResourceUtils.getFile("classpath:" + this.indexer);

            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", indexJar.getAbsolutePath());
            processBuilder.command().add("-c");
            processBuilder.command().add(configFile.getAbsolutePath());
            processBuilder.command().add("-r");
            processBuilder.command().addAll(todos);
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
