package com.example.camel_boot;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MyRoutesConflict extends RouteBuilder {

    @Override
    public void configure() {
        // Global: keep routes resilient and non-blocking
        // onException(Exception.class)
        //     .handled(false)
        //     .log("[route-error] ${exception.class} - ${exception.message}");

        from("file-watch:/home/przemek/workspaces/camel-boot/target/inbox")
                .startupOrder(1)
                .routeId("file-watch")
                .choice()
                // .when(simple("${header.CamelFileName} !contains 'camel'"))
                .log("[just watching] File event: ${header.CamelFileEventType} path=${header.CamelFilePath} at ${header.CamelFileLastModified}")
                .endChoice();


        from("file://target/inbox")
    }
}
