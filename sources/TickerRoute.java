import org.apache.camel.builder.RouteBuilder;

public class TickerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:tick")
            .setBody()
                .simple("Hello Camel K! This written with Java DSL")
            .to("log:info");
    }

}