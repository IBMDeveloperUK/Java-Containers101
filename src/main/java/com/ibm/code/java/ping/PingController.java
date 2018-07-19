package com.ibm.code.java.ping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PingController{

    @GetMapping("/ping")
    public String getPing(){
        return "pong";
    }

    @GetMapping("/ping/{delay}")
    public String getPingWithDelayInMilliSeconds(@PathVariable int delay){
        try {
                        Thread.sleep(delay);
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }
        return "pong after " + delay + " milliseconds delay";
    }
}