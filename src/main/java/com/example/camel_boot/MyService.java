package com.example.camel_boot;

import org.springframework.stereotype.Component;

@Component
public class MyService {

    public void first() {
        System.out.println("[MyService#first] Invoked by Camel route at " + java.time.ZonedDateTime.now());
    }

    public void second() {
        System.out.println("[MyService#second] Invoked by Camel route at " + java.time.ZonedDateTime.now());
    }
}
