package service;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import dao.AIMessageStatusDAO;
import model.AIMessageStatus;
import model.User;
import util.RabbitMQUtil;
import util.AIClientUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * AI消息消费者
 * 后台线程从RabbitMQ消费AI消息，处理完成后更新状态
 */
public class AIMessageConsumer {

    private static final String EXCHANGE_NAME = "ai.exchange";
    private static final String QUEUE_NAME = "ai.message.queue";
    private static final String ROUTING_KEY = "ai.message";

    private final AIMessageStatusDAO statusDAO;
    private final AIService aiService;
    private final AIClientUtil aiClient;
    private final Gson gson;
    private Connection connection;
    private Channel channel;
    private volatile boolean running = false;

    public AIMessageConsumer() {
        this.statusDAO = new AIMessageStatusDAO();
        this.aiService = new AIService();
        this.aiClient = AIClientUtil.getInstance();
        this.gson = new Gson();
    }

    /**
     * 初始化 RabbitMQ 队列和交换机
     */
    public void initialize() throws IOException, TimeoutException {
        connection = RabbitMQUtil.getConnection();
        channel = connection.createChannel();

        // 声明交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);

        // 声明队列
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        // 绑定队列到交换机
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

        System.out.println("[AIMessageConsumer] RabbitMQ初始化完成，队列: " + QUEUE_NAME);
    }

    /**
     * 启动消费者
     */
    public void start() {
        if (running) {
            System.out.println("[AIMessageConsumer] 消费者已在运行中");
            return;
        }

        running = true;
        new Thread(this::consumeLoop, "AIMessageConsumer").start();
        System.out.println("[AIMessageConsumer] 消费者已启动");
    }

    /**
     * 停止消费者
     */
    public void stop() {
        running = false;
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (Exception e) {
            System.err.println("[AIMessageConsumer] 关闭连接失败: " + e.getMessage());
        }
        System.out.println("[AIMessageConsumer] 消费者已停止");
    }

    /**
     * 消费消息循环
     */
    private void consumeLoop() {
        try {
            // 设置预取数量
            channel.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String body = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("[AIMessageConsumer] 收到消息: " + body);

                try {
                    // 解析消息
                    Map<String, Object> message = gson.fromJson(body, Map.class);
                    String messageId = (String) message.get("messageId");
                    String userMessage = (String) message.get("message");
                    Integer userId = message.get("userId") != null ? ((Number) message.get("userId")).intValue() : null;
                    String sessionId = (String) message.get("sessionId");
                    String userRole = (String) message.get("userRole");

                    // 更新状态为处理中
                    statusDAO.updateStatus(messageId, AIMessageStatus.STATUS_PROCESSING, null, null);

                    // 调用AI服务获取响应
                    String aiResponse = aiService.getAIResponse(userMessage, sessionId, userId, userRole);

                    // 更新状态为完成
                    statusDAO.updateStatus(messageId, AIMessageStatus.STATUS_COMPLETED, aiResponse, null);

                    // 确认消息
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    System.out.println("[AIMessageConsumer] 消息处理完成: " + messageId);

                } catch (Exception e) {
                    System.err.println("[AIMessageConsumer] 处理消息失败: " + e.getMessage());
                    e.printStackTrace();

                    // 更新状态为失败
                    try {
                        Map<String, Object> message = gson.fromJson(body, Map.class);
                        String messageId = (String) message.get("messageId");
                        statusDAO.updateStatus(messageId, AIMessageStatus.STATUS_FAILED, null, e.getMessage());
                    } catch (Exception ex) {
                        System.err.println("[AIMessageConsumer] 更新失败状态失败: " + ex.getMessage());
                    }

                    // 拒绝消息（不重新入队）
                    try {
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                    } catch (Exception ex) {
                        System.err.println("[AIMessageConsumer] 拒绝消息失败: " + ex.getMessage());
                    }
                }
            };

            CancelCallback cancelCallback = consumerTag -> {
                System.err.println("[AIMessageConsumer] 消费被取消: " + consumerTag);
            };

            channel.basicConsume(QUEUE_NAME, false, deliverCallback, cancelCallback);
            System.out.println("[AIMessageConsumer] 开始消费消息...");

            // 保持运行直到停止
            while (running) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            System.err.println("[AIMessageConsumer] 消费循环异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送消息到队列（供 AIServlet 调用）
     */
    public static String sendToQueue(String message, Integer userId, String sessionId, String userRole) {
        String messageId = UUID.randomUUID().toString();

        try {
            // 保存状态到数据库
            AIMessageStatusDAO statusDAO = new AIMessageStatusDAO();
            AIMessageStatus status = new AIMessageStatus(messageId, userId, sessionId, message);
            statusDAO.insert(status);

            // 发送消息到RabbitMQ
            String json = new Gson().toJson(Map.of(
                "messageId", messageId,
                "message", message,
                "userId", userId,
                "sessionId", sessionId,
                "userRole", userRole != null ? userRole : "GUEST"
            ));

            RabbitMQUtil.sendMessage(EXCHANGE_NAME, ROUTING_KEY, json);
            System.out.println("[AIMessageConsumer] 消息已发送到队列: " + messageId);

            return messageId;
        } catch (Exception e) {
            System.err.println("[AIMessageConsumer] 发送消息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取消息状态（供前端轮询）
     */
    public static AIMessageStatus getMessageStatus(String messageId) {
        AIMessageStatusDAO statusDAO = new AIMessageStatusDAO();
        return statusDAO.findByMessageId(messageId);
    }

    public static void main(String[] args) {
        AIMessageConsumer consumer = new AIMessageConsumer();
        try {
            consumer.initialize();
            consumer.start();

            // 保持运行
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
