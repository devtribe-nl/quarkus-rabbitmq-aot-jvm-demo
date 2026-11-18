package nl.devtribe.testcontainers.rabbitmq.servers;

import nl.devtribe.testcontainers.rabbitmq.exchanges.ExchangeType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface TestServer {

    Map<String, String> init(List<ExchangeType> args);
    void stop();
    void reset() throws IOException;
}
