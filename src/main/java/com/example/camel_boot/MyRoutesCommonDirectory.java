package com.example.camel_boot;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class MyRoutesCommonDirectory extends RouteBuilder {

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
                // .process(_ -> {
                //     System.out.println("processing");
                //     System.out.println("processing");
                // })
                .endChoice();


        from("file:/home/przemek/workspaces/camel-boot/target/inbox?doneFileName=${file:name}.flg"
            + "readLock=changed&readLockCheckInterval=1000&readLockTimeout=10000&delete=false&recursive=true&noop=true&idempotent=true&readLockMinAge=5000"
        ).startupOrder(2)
                .threads(3, 5)
                .routeId("processing route")
                .log("[process route] File event: ${header.CamelFileEventType} path=${header.CamelFilePath}")
                .multicast()
                .parallelProcessing()
                .stopOnException()
                .process(e -> {
                    System.out.println("In the process 111");
                    String absolute = e.getMessage().getHeader("CamelFilePath", String.class);
                    if (absolute != null) {
                        Path p = Paths.get(absolute);
                        e.getMessage().setHeader(Exchange.FILE_NAME, p.getFileName().toString()); // sets CamelFileName
                    }
                })
                .end();
    }
}
