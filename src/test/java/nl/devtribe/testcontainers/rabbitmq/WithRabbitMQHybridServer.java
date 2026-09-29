package nl.devtribe.testcontainers.rabbitmq;

import io.quarkus.test.common.QuarkusTestResource;
import nl.devtribe.testcontainers.rabbitmq.exchanges.Exchange;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@QuarkusTestResource(RabbitMQResourceManager.class)
public @interface WithRabbitMQHybridServer {

    Exchange[] exchanges();
}
