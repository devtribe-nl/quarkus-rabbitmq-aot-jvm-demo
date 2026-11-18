package nl.devtribe.testcontainers.rabbitmq.exchanges;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Exchange {
    String type();
    Kind kind();

    enum Kind {
        SOURCE,
        SINK
    }
}
