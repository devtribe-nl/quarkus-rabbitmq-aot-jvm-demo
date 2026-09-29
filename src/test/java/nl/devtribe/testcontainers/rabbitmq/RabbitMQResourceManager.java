package nl.devtribe.testcontainers.rabbitmq;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceConfigurableLifecycleManager;
import nl.devtribe.testcontainers.rabbitmq.exchanges.ExchangeType;
import nl.devtribe.testcontainers.rabbitmq.servers.RabbitMQContainerServer;
import nl.devtribe.testcontainers.rabbitmq.servers.RabbitMQInMemoryTestServer;
import nl.devtribe.testcontainers.rabbitmq.servers.TestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.rabbitmq.client.ConnectionFactory.*;

public class RabbitMQResourceManager
        implements QuarkusTestResourceConfigurableLifecycleManager<WithRabbitMQHybridServer>,
                DevServicesContext.ContextAware {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQResourceManager.class);

    private static final int AMQP_PORT = 5672;

    private static final String RABBITMQ_HOST_PROP = "rabbitmq-host";
    private static final String RABBITMQ_PORT_PROP = "rabbitmq-port";

    private static final String RABBITMQ_USERNAME_PROP = "rabbitmq-username";
    private static final String RABBITMQ_PASSWORD_PROP = "rabbitmq-password";

    private DevServicesContext context;

    private TestServer server;
    private RabbitMQContainerServer rabbitMQServer;
    private RabbitMQContainer rabbitMQContainer;

    private List<ExchangeType> exchanges;

    @Override
    public void init(WithRabbitMQHybridServer initArgs) {
        this.exchanges =
                Arrays.stream(initArgs.exchanges()).map(ExchangeType::toModel).toList();
    }

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        this.context = context;
    }

    @Override
    public Map<String, String> start() {

        this.server = new RabbitMQInMemoryTestServer();
        this.server.init(exchanges);
        rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.2-management"));
        context.containerNetworkId().ifPresent(rabbitMQContainer::withNetworkMode);

        rabbitMQContainer.start();
        final String host = context.containerNetworkId()
                .map(ctx ->
                        rabbitMQContainer.getCurrentContainerInfo().getConfig().getHostName())
                .orElse(DEFAULT_HOST);
        final int port =
                context.containerNetworkId().map(ctx -> AMQP_PORT).orElseGet(() -> rabbitMQContainer.getAmqpPort());

        rabbitMQServer = new RabbitMQContainerServer(DEFAULT_HOST, rabbitMQContainer.getAmqpPort());
        return Map.of(
                RABBITMQ_HOST_PROP, host,
                RABBITMQ_PORT_PROP, String.valueOf(port),
                RABBITMQ_USERNAME_PROP, DEFAULT_USER,
                RABBITMQ_PASSWORD_PROP, DEFAULT_PASS);
    }

    @Override
    public void stop() {
        try {
            if (rabbitMQServer != null && rabbitMQServer.connection().isOpen()) {
                rabbitMQServer.close();
            }
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void inject(TestInjector testInjector) {
        try {
            rabbitMQServer.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        testInjector.injectIntoFields(
                rabbitMQServer,
                new TestInjector.AnnotatedAndMatchesType(InjectRabbitMQServer.class, RabbitMQContainerServer.class));
    }
}
