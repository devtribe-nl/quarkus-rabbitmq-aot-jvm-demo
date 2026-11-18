package nl.devtribe.testcontainers.rabbitmq.exchanges;

/// Outgoing
public record SinkBinding(String type) implements ExchangeType {
    static SinkBinding of(String type){
        return new SinkBinding(type);
    }

}
