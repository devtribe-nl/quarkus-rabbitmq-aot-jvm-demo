package nl.devtribe.testcontainers.rabbitmq.exchanges;

public sealed interface ExchangeType permits SinkBinding, SourceBinding {

    String type();

    static ExchangeType toModel(Exchange exchange){
        return switch (exchange.kind()){
            case SINK -> SinkBinding.of(exchange.type());
            case SOURCE -> SourceBinding.of(exchange.type());
        };
    }
}
