package org.dbpedia.databus.moss.services.Indexer;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class IndexingTask implements Runnable {

    String command;
    List<String> todos;
    String indexer;
    String directory;

    public IndexingTask(String indexer, String command, List<String> todos, String directory) {
        this.todos = todos;
        this.directory = directory;
        this.indexer = indexer;
        this.command = command;
    }

    @Override
    public void run()  {

        System.out.println("Ich bims der runner auf thread " + Thread.currentThread().getId());
       
        try {
            File configFile = new File(directory + "/" + this.indexer);

            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", directory + "/lookup-indexer.jar");
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
