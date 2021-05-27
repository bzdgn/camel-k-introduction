import org.apache.camel.builder.RouteBuilder;

public class Routing extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:java?period=1000")
            .id("java-logger")
            .setBody()
                .simple("Hello Camel K from route: ${routeId}")
            .to("log:info");
    }

}