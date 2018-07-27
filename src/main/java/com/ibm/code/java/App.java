package com.ibm.code.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.jboss.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    private static Logger LOG;

    public static void main(String[] args){
        SpringApplication.run(App.class, args);
        checkArgs(args);
    }

    public static void checkArgs(String[] args){
        if(Arrays.asList(args).contains("--exit")){
            LOG = Logger.getLogger(App.class.getName() + " <Quick Exit>");
            killApp();
        }
        if(Arrays.asList(args).contains("--memory-test")){
            LOG = Logger.getLogger(App.class.getName() + " <Memory Test>");
            memoryTest();
        }
    }

    public static void killApp(){
        LOG.info(Arrays.asList("Application started"));
        LOG.info(Arrays.asList("Java version: " + System.getProperty("java.version")));
        LOG.info(Arrays.asList("Application stoppped"));
        System.exit(0);
    }

    public static void memoryTest() {
        final float MEMORY_CAP = 0.75f;
        final int ONE_MB = 1024 * 1024; 
        List<Object> memoryEater = new ArrayList<>();
        Runtime runtime = Runtime.getRuntime();

        long maxMemoryBytes = runtime.maxMemory();
        long usedMemoryBytes = runtime.totalMemory() - runtime.freeMemory();
        long freeMemoryBytes = runtime.maxMemory() - usedMemoryBytes;
        int cappedBytes = Math.round(freeMemoryBytes * MEMORY_CAP);

        LOG.info("Initial free memory: " + freeMemoryBytes/ONE_MB + "MB");
        LOG.info("Maximum memory: " + maxMemoryBytes/ONE_MB + "MB");
        LOG.info("Allocatable memory: " + cappedBytes/ONE_MB + "MB");
        try{
            for (int i = 0; i < cappedBytes / ONE_MB; i++){
                memoryEater.add(new byte[ONE_MB]);
            }
    
            usedMemoryBytes = runtime.totalMemory() - runtime.freeMemory();
            freeMemoryBytes = runtime.maxMemory() - usedMemoryBytes;
    
            LOG.info("Free memory: " + freeMemoryBytes/ONE_MB + "MB");
        }catch(Exception e){
            LOG.error(e.getCause().getMessage());
        }
    }
}