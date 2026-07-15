package nl.devtribe.testcontainers.rabbitmq;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.rabbitmq.client.ConnectionFactory.*;

public class RabbitMQResourceManager implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQResourceManager.class);

    private static final int AMQP_PORT = 5672;

    private static final String RABBITMQ_HOST_PROP = "rabbitmq-host";
    private static final String RABBITMQ_PORT_PROP = "rabbitmq-port";

    private static final String RABBITMQ_USERNAME_PROP = "rabbitmq-username";
    private static final String RABBITMQ_PASSWORD_PROP = "rabbitmq-password";

    private DevServicesContext context;
    private RabbitMQServer rabbitMQServer;
    private RabbitMQContainer rabbitMQContainer;

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        this.context = context;
    }

    @Override
    public Map<String, String> start() {

        rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.2-management"));
        context.containerNetworkId().ifPresent(rabbitMQContainer::withNetworkMode);

        rabbitMQContainer.start();
        final String host = context.containerNetworkId()
                .map(ctx ->
                        rabbitMQContainer.getCurrentContainerInfo().getConfig().getHostName())
                .orElse(DEFAULT_HOST);
        final int port =
                context.containerNetworkId().map(ctx -> AMQP_PORT).orElseGet(() -> rabbitMQContainer.getAmqpPort());

        rabbitMQServer = new RabbitMQServer(DEFAULT_HOST, rabbitMQContainer.getAmqpPort());
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
                new TestInjector.AnnotatedAndMatchesType(InjectRabbitMQServer.class, RabbitMQServer.class));
    }
}
