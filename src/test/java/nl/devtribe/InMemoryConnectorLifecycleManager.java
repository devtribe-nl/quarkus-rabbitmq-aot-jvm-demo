package nl.devtribe;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;

import java.util.HashMap;
import java.util.Map;

public class InMemoryConnectorLifecycleManager implements QuarkusTestResourceLifecycleManager {

    @Override
    public void init(Map<String, String> initArgs) {
        System.out.println(initArgs);
        QuarkusTestResourceLifecycleManager.super.init(initArgs);
    }

    @Override
    public Map<String, String> start() {
        Map<String, String> env = new HashMap<>();
        Map<String, String> props1 = InMemoryConnector.switchOutgoingChannelsToInMemory("staple-commands-out");

        env.putAll(props1);
        System.out.println("tests-----");
        System.out.println(env);

        System.out.println("tests-----");
        return env;
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }
}
