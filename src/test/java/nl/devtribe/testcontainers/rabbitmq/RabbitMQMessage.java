package nl.devtribe.testcontainers.rabbitmq;

import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.LongString;
import com.rabbitmq.client.impl.AMQBasicProperties;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record RabbitMQMessage(byte[] body, Envelope envelope, AMQBasicProperties properties) {
    public static RabbitMQMessage of(byte[] body, Envelope envelope, AMQBasicProperties properties) {
        return new RabbitMQMessage(body, envelope, properties);
    }

    @Override
    public byte[] body() {
        return body;
    }
    public <T> T body(Class<T> clazz){
        if (properties.getContentType().equals("application/json")) {
            return Json.decodeValue(Buffer.buffer(body), clazz);
        }
        throw new UnsupportedOperationException("It only supports application/json content-type");
    }

    public String routingKey() {
        return envelope.getRoutingKey();
    }

    public String contentType() {
        return properties.getContentType();
    }

    public Map<String, Object> headers() {
        final Map<String, Object> incomingHeaders = properties.getHeaders();
        return (incomingHeaders != null) ? incomingHeaders.entrySet().stream()
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), mapValue(e.getValue())), HashMap::putAll)
                : new HashMap<>();
    }

    private static Object mapValue(final Object v) {
        if (v instanceof LongString) {
            return v.toString();
        } else if (v instanceof List) {
            return ((List<?>) v).stream().map(RabbitMQMessage::mapValue).collect(Collectors.toList());
        } else {
            return v;
        }
    }
}
