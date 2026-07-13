package nl.devtribe;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import nl.devtribe.out.StapleMessage;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@QuarkusTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class CreateResourceTest {

    @Inject
    @Any
    InMemoryConnector connector;

    @BeforeEach
    void setup() {
        stapleCommandOut().clear();
    }

    @Test
    void it_should_create_a_staple_request_containing_a_prefix_and_suffix() {

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
                .untilAsserted(() -> assertThat(stapleCommandOut().received()).satisfies(message -> {
                    assertThat(message).isNotEmpty();
                    assertThat(message).hasSize(1);
                }));
    }

    @Test
    void it_should_queue_the_message_on_the_exchange() {

        String prefix = "first";
        String suffix = "last";
        given().when()
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
                .untilAsserted(() -> assertThat(stapleCommandOut().received()).isNotEmpty());

        Message<StapleMessage> first = stapleCommandOut().received().get(0);
        StapleMessage payload = first.getPayload();
        assertThat(payload.prefix()).isEqualTo("first");
        assertThat(payload.suffix()).isEqualTo("last");
        assertThat(first.getMetadata(OutgoingRabbitMQMetadata.class).get().getHeaders())
                .containsExactlyInAnyOrderEntriesOf(Map.of("message.prefix", "first", "message.suffix", "last"));
    }

    private InMemorySink<StapleMessage> stapleCommandOut() {
        return connector.sink("staple-commands-out");
    }

    private record StapleRequest(String prefix, String suffix) {}

    private record ResponseData(String prefix, String suffix) {}
}
