package nl.devtribe.testcontainers.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

public class RabbitMQServer implements AutoCloseable {

    // This POC doenst use a fancy full implemented rabbitmq server

    private final Set<String> consumerTags = Collections.synchronizedSet(new HashSet<>());

    private final Connection connection;
    private final Channel channel;

    RabbitMQServer(String host, int amqpPort) {
        ConnectionFactory factory = new ConnectionFactory();
        final Address address = new Address(host, amqpPort);
        try {
            connection = factory.newConnection(List.of(address));
            channel = connection.createChannel();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(
                    String.format("Woops! Can't connect to rabbitmq %s:%d", host, amqpPort), e);
        }
    }

    public void createQueue(String source, String destination, String type, String routingKey){
        try {
            channel.exchangeDeclare(source, type, true);
            channel.queueDeclare(destination, true, false, false, null);
            channel.queueBind(destination, source, routingKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BlockingQueue<RabbitMQMessage> subscribe(String queue) throws IOException {
        BlockingQueue<RabbitMQMessage> blockingQueue = new LinkedBlockingDeque<>();
        this.channel.basicConsume(queue, new DefaultConsumer(this.channel) {

            @Override
            public void handleConsumeOk(String consumerTag) {
                consumerTags.add(consumerTag);
                super.handleConsumeOk(consumerTag);
            }

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    blockingQueue.put(RabbitMQMessage.of(body, envelope, properties));
                    super.getChannel().basicAck(envelope.getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return blockingQueue;
    }
    public Connection connection() {
        return connection;
    }
    public Channel channel() { return channel; }

    public void close() throws IOException, TimeoutException {
        if (channel.isOpen()) {
            channel.close();
        }

        if (connection.isOpen()) {
            connection.close();
        }
    }

    public void reset() throws IOException {
        for (String tag : consumerTags) {
            channel.basicCancel(tag);
        }
        consumerTags.clear();
    }
}
