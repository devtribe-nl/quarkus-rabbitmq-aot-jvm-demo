package nl.devtribe;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import nl.devtribe.out.StapleMessage;
import nl.devtribe.testcontainers.rabbitmq.InjectRabbitMQServer;
import nl.devtribe.testcontainers.rabbitmq.RabbitMQMessage;
import nl.devtribe.testcontainers.rabbitmq.RabbitMQResourceManager;
import nl.devtribe.testcontainers.rabbitmq.RabbitMQServer;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Queue;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@QuarkusTest
@QuarkusTestResource(value = RabbitMQResourceManager.class, restrictToAnnotatedClass = true)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class CreateResourceTest {

    @InjectRabbitMQServer
    private RabbitMQServer server;

    @Test
    void it_should_create_a_staple_request_containing_a_prefix_and_suffix() throws IOException {

        server.createQueue("staple-commands", "staple-handler", "direct", "");
        Queue<RabbitMQMessage> sink = server.subscribe("staple-handler");

        String prefix = "first";
        String suffix = "last";
        ResponseData responseData = given().when()
                .body(new StapleRequest(prefix, suffix))
                .contentType(APPLICATION_JSON)
                .post("/create")
                .then()
                .statusCode(200)
                .contentType(APPLICATION_JSON)
                .extract()
                .body()
                .as(ResponseData.class);

        assertThat(responseData.prefix()).isEqualTo("first");
        assertThat(responseData.suffix()).isEqualTo("last");

        await().atMost(Duration.ofSeconds(1))
                .untilAsserted(() -> assertThat(sink).satisfies(message -> {
                    assertThat(message).isNotEmpty();
                    assertThat(message).hasSize(1);
                }));
    }

    @Test
    void it_should_queue_the_message_on_the_exchange() throws IOException, InterruptedException {
        server.createQueue("staple-commands", "staple-handler", "direct", "");
        Queue<RabbitMQMessage> sink = server.subscribe("staple-handler");

        String prefix = "first";
        String suffix = "last";
        ResponseData responseData = given().when()
                .body(new StapleRequest(prefix, suffix))
                .contentType(APPLICATION_JSON)
                .post("/create")
                .then()
                .statusCode(200)
                .contentType(APPLICATION_JSON)
                .extract()
                .body()
                .as(ResponseData.class);

        await().atMost(Duration.ofSeconds(1))
                .untilAsserted(() -> assertThat(sink).isNotEmpty());

        RabbitMQMessage message = sink.remove();
        StapleMessage payload = message.body(StapleMessage.class);

        assertThat(payload.prefix()).isEqualTo("first");
        assertThat(payload.suffix()).isEqualTo("last");
        assertThat(message.headers())
                .containsExactlyInAnyOrderEntriesOf(Map.of("message.prefix", "first", "message.suffix", "last"));
    }

    private record StapleRequest(String prefix, String suffix) {}

    private record ResponseData(String prefix, String suffix) {}
}
