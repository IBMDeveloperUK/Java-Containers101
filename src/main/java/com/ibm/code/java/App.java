package com.ibm.code.java;

import java.util.Arrays;
import org.jboss.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    private static Logger LOG = Logger.getLogger(App.class.getName() + " <Quick Exit>");

    public static void main(String[] args){
        SpringApplication.run(App.class, args);
        checkIfExit(args);
    }

    public static void checkIfExit(String[] args){
        if(Arrays.asList(args).contains("--exit")){
            LOG.info(Arrays.asList("Application started"));
            LOG.info(Arrays.asList("Java version: " + System.getProperty("java.version")));
            LOG.info(Arrays.asList("Application stoppped"));
            System.exit(0);
        }
    }
}