package nl.devtribe.out;

import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import java.util.function.Function;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Singleton
public class StapleMessageCreator implements Function<StapleMessage, Message<StapleMessage>> {
    @Override
    public Message<StapleMessage> apply(StapleMessage stapleMessage) {
        return Message.of(
                stapleMessage,
                Metadata.of(OutgoingRabbitMQMetadata.builder()
                        .withContentType(APPLICATION_JSON)
                        .withHeader("message.prefix", stapleMessage.prefix())
                        .withHeader("message.suffix", stapleMessage.suffix())
                        .build()));
    }
}
