package com.example.camel_boot;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

// @Component
public class MyRoutes extends RouteBuilder {

    @Override
    public void configure() {
        // Global: keep routes resilient and non-blocking
        onException(Exception.class)
            .handled(false)
            .log("[route-error] ${exception.class} - ${exception.message}");

        // Route 1: Reacts to file CREATE events, then offloads work to a thread pool and safely locks/reads the file
        from("file-watch:{{app.watch.dir}}?events=CREATE")
            .routeId("firstRoute")
            .log("[firstRoute] File event: ${header.CamelFileEventType} path=${header.CamelFilePath}")
            // Extract the filename from the absolute path so the file: endpoint can target it
            .process(e -> {
                System.out.println("In the process 111");
                String absolute = e.getMessage().getHeader("CamelFilePath", String.class);
                if (absolute != null) {
                    Path p = Paths.get(absolute);
                    e.getMessage().setHeader(Exchange.FILE_NAME, p.getFileName().toString()); // sets CamelFileName
                }
            })
            // Offload the potentially blocking work so the file-watch consumer can keep receiving further events
            .threads(2, 8)
            // Acquire and lock the file using marker-file locking to avoid concurrent processing, but don't wait forever
            .pollEnrich()
                .simple("file:{{app.watch.dir}}?fileName=${header.CamelFileName}&noop=true&readLock=markerFile")
                .timeout(10000)
            .end()
            .bean(MyService.class, "first");

        // Route 2: Reacts to file MODIFY events, then offloads work to a thread pool and safely locks/reads the file
        from("file-watch:{{app.watch.dir}}?events=MODIFY")
            .routeId("secondRoute")
            .log("[secondRoute] File event: ${header.CamelFileEventType} path=${header.CamelFilePath}")
            .process(e -> {
                System.out.println("In the process 2222");
                String absolute = e.getMessage().getHeader("CamelFilePath", String.class);
                if (absolute != null) {
                    Path p = Paths.get(absolute);
                    e.getMessage().setHeader(Exchange.FILE_NAME, p.getFileName().toString());
                }
            })
            .threads(2, 8)
            .pollEnrich()
                .simple("file:{{app.watch.dir}}?fileName=${header.CamelFileName}&noop=true&readLock=markerFile")
                .timeout(10000)
            .end()
            .bean(MyService.class, "second");
    }
}
