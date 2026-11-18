package nl.devtribe;

import io.quarkus.test.junit.QuarkusTest;
import nl.devtribe.testcontainers.rabbitmq.WithRabbitMQHybridServer;
import nl.devtribe.testcontainers.rabbitmq.exchanges.Exchange;
import nl.devtribe.testcontainers.rabbitmq.exchanges.SourceBinding;
import org.junit.jupiter.api.Test;

@QuarkusTest
@WithRabbitMQHybridServer(exchanges = {
        @Exchange(type = "foo", kind = Exchange.Kind.SOURCE),
        @Exchange(type = "bar", kind = Exchange.Kind.SOURCE)
})
public class CreateResourceTest {
    @Test
    void foo(){

    }
}
