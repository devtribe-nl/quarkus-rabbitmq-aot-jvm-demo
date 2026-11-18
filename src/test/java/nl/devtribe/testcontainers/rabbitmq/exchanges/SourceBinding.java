package nl.devtribe.testcontainers.rabbitmq.exchanges;

/// Incoming
public record SourceBinding(String type) implements ExchangeType {
    public static SourceBinding of(String type){
        return new SourceBinding(type);
    }
}
