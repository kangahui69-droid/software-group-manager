package util;

import com.rabbitmq.client.*;
import config.Config;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ 消息队列工具类
 * 提供简单的消息队列操作封装
 */
public class RabbitMQUtil {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";
    private static final String DEFAULT_VHOST = "/";

    private static ConnectionFactory connectionFactory;

    static {
        initConnectionFactory();
    }

    private static void initConnectionFactory() {
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(Config.getProperty("rabbitmq.host", DEFAULT_HOST));
        connectionFactory.setPort(Config.getIntProperty("rabbitmq.port", DEFAULT_PORT));
        connectionFactory.setUsername(Config.getProperty("rabbitmq.username", DEFAULT_USERNAME));
        connectionFactory.setPassword(Config.getProperty("rabbitmq.password", DEFAULT_PASSWORD));
        connectionFactory.setVirtualHost(Config.getProperty("rabbitmq.virtualHost", DEFAULT_VHOST));

        // 连接池配置
        connectionFactory.setConnectionTimeout(10000);
    }

    /**
     * 获取连接
     */
    public static Connection getConnection() throws IOException, TimeoutException {
        return connectionFactory.newConnection();
    }

    /**
     * 获取 Channel
     */
    public static Channel getChannel() throws IOException, TimeoutException {
        Connection connection = getConnection();
        return connection.createChannel();
    }

    /**
     * 声明交换机
     * @param exchangeName 交换机名称
     * @param type 交换机类型 (direct, fanout, topic, headers)
     * @param durable 是否持久化
     */
    public static void declareExchange(String exchangeName, String type, boolean durable) throws IOException, TimeoutException {
        try (Channel channel = getChannel()) {
            channel.exchangeDeclare(exchangeName, type, durable);
        }
    }

    /**
     * 声明队列
     * @param queueName 队列名称
     * @param durable 是否持久化
     */
    public static void declareQueue(String queueName, boolean durable) throws IOException, TimeoutException {
        try (Channel channel = getChannel()) {
            channel.queueDeclare(queueName, durable, false, false, null);
        }
    }

    /**
     * 绑定队列到交换机
     * @param queueName 队列名称
     * @param exchangeName 交换机名称
     * @param routingKey 路由键
     */
    public static void bindQueue(String queueName, String exchangeName, String routingKey) throws IOException, TimeoutException {
        try (Channel channel = getChannel()) {
            channel.queueBind(queueName, exchangeName, routingKey);
        }
    }

    /**
     * 发送消息
     * @param exchangeName 交换机名称
     * @param routingKey 路由键
     * @param message 消息内容
     */
    public static void sendMessage(String exchangeName, String routingKey, String message) throws IOException, TimeoutException {
        try (Channel channel = getChannel()) {
            channel.basicPublish(exchangeName, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.getBytes("UTF-8"));
        }
    }

    /**
     * 发送消息到指定队列
     * @param queueName 队列名称
     * @param message 消息内容
     */
    public static void sendMessageToQueue(String queueName, String message) throws IOException, TimeoutException {
        try (Channel channel = getChannel()) {
            channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.getBytes("UTF-8"));
        }
    }

    /**
     * 消费消息
     * @param queueName 队列名称
     * @param callback 消息处理回调
     */
    public static void consume(String queueName, DeliverCallback callback) throws IOException, TimeoutException {
        Connection connection = getConnection();
        Channel channel = connection.createChannel();

        // 设置预取数量
        channel.basicQos(1);

        // 开始消费
        channel.basicConsume(queueName, false, callback, consumerTag -> {});
    }

    /**
     * 关闭连接（应用关闭时调用）
     */
    public static void close() {
        // 由于使用 try-with-resources，连接会在使用后自动关闭
        // 如果有长期存在的连接，需要在这里关闭
    }
}
