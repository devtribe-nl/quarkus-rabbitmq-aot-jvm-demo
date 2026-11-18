package nl.devtribe.testcontainers.rabbitmq.servers;

import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import nl.devtribe.testcontainers.rabbitmq.exchanges.ExchangeType;
import nl.devtribe.testcontainers.rabbitmq.exchanges.SinkBinding;
import nl.devtribe.testcontainers.rabbitmq.exchanges.SourceBinding;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RabbitMQInMemoryTestServer implements TestServer{

    private Map<String, ExchangeConfiguration> config;

    public RabbitMQInMemoryTestServer() {}



    @Override
    public Map<String, String> init(List<ExchangeType> args) {
        config = args.stream()
                .map(
                        e ->
                )

                .collect(Collectors.toCollection());
        return args.stream()
                .map(e -> switch (e) {
                    case SinkBinding t -> InMemoryConnector.switchOutgoingChannelsToInMemory(t.type());
                    case SourceBinding t -> InMemoryConnector.switchIncomingChannelsToInMemory(t.type());
                })
                .flatMap(entry -> entry.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();

    }

    record ExchangeConfiguration(String name, String type, String destination) {
        static ExchangeConfiguration of(String name, String type) {
            return new ExchangeConfiguration(name, type, name);
        }
    }
}
