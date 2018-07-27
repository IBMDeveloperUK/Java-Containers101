package com.ibm.code.java.ping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PingController{

    @GetMapping("/ping")
    public String getPing(){
        return "pong";
    }
}