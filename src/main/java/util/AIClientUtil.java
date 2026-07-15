package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.Config;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class AIClientUtil {

    private static AIClientUtil instance;
    private final Gson gson;
    private final String provider;
    private final String apiUrl;
    private final String apiKey;
    private final String model;

    private AIClientUtil() {
        gson = new GsonBuilder().create();
        provider = Config.getProperty("ai.provider", "volcengine");
        apiUrl = Config.getProperty("ai.api.url", "https://ark.cn-beijing.volces.com/api/v3/chat/completions");
        apiKey = Config.getProperty("ai.api.key", "");
        model = Config.getProperty("ai.model", "doubao-pro-32k");
    }

    public static AIClientUtil getInstance() {
        if (instance == null) {
            synchronized (AIClientUtil.class) {
                if (instance == null) {
                    instance = new AIClientUtil();
                }
            }
        }
        return instance;
    }

    public String chat(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your_api_key_here")) {
            return getDefaultResponse(userMessage);
        }

        try {
            switch (provider) {
                case "minimax":
                    return chatWithMinimax(systemPrompt, userMessage);
                case "wenxin":
                    return chatWithWenxin(systemPrompt, userMessage);
                case "qwen":
                    return chatWithQwen(systemPrompt, userMessage);
                case "openai":
                    return chatWithOpenAI(systemPrompt, userMessage);
                case "volcengine":
                    return chatWithVolcEngine(systemPrompt, userMessage);
                default:
                    return getDefaultResponse(userMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，AI服务暂时不可用。请稍后再试或联系管理员。错误信息: " + e.getMessage();
        }
    }

    public void chatStream(String systemPrompt, String userMessage, javax.servlet.http.HttpServletResponse response) throws Exception {
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", systemPrompt));
        messages.add(createMessage("user", userMessage));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", true);

        HttpPost request = new HttpPost(apiUrl);
        request.setHeader("Authorization", "Bearer " + apiKey);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(gson.toJson(requestBody), StandardCharsets.UTF_8));

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000)
                .setSocketTimeout(60000)
                .setConnectionRequestTimeout(60000)
                .build();
        request.setConfig(requestConfig);

        CloseableHttpClient client = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();

        try {
            HttpResponse httpResponse = client.execute(request);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            
            if (statusCode != 200) {
                String errorBody = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                response.getWriter().write("data: [ERROR] " + statusCode + " - " + errorBody + "\n\n");
                response.getWriter().flush();
                return;
            }

            java.io.InputStream inputStream = httpResponse.getEntity().getContent();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;
            String fullContent = "";
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6);
                    if ("[DONE]".equals(data)) {
                        break;
                    }
                    try {
                        Map<String, Object> respMap = gson.fromJson(data, Map.class);
                        if (respMap != null && respMap.containsKey("choices")) {
                            List<Map<String, Object>> choices = (List<Map<String, Object>>) respMap.get("choices");
                            if (choices != null && !choices.isEmpty()) {
                                Map<String, Object> choice = choices.get(0);
                                if (choice.containsKey("delta")) {
                                    Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                                    if (delta != null && delta.containsKey("content")) {
                                        String content = (String) delta.get("content");
                                        fullContent += content;
                                        response.getWriter().write("data: " + escapeForSSE(content) + "\n\n");
                                        response.getWriter().flush();
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // 忽略解析错误，继续读取
                    }
                }
            }
            response.getWriter().write("data: [DONE]\n\n");
            response.getWriter().flush();
        } finally {
            client.close();
        }
    }

    private String escapeForSSE(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private String chatWithMinimax(String systemPrompt, String userMessage) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", systemPrompt));
        messages.add(createMessage("user", userMessage));
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        
        HttpPost request = new HttpPost(apiUrl);
        request.setHeader("Authorization", "Bearer " + apiKey);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(gson.toJson(requestBody), StandardCharsets.UTF_8));
        
        CloseableHttpClient client = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
        
        try {
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            System.out.println("[AIClient] MiniMax response status: " + statusCode);
            System.out.println("[AIClient] MiniMax response: " + responseBody);
            
            if (statusCode != 200) {
                return "MiniMax服务错误: HTTP " + statusCode + " - " + responseBody;
            }
            
            if (responseBody.trim().startsWith("{")) {
                Map<String, Object> respMap = gson.fromJson(responseBody, Map.class);
                
                if (respMap.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) respMap.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        if (choice.containsKey("message")) {
                            Map<String, Object> message = (Map<String, Object>) choice.get("message");
                            return (String) message.get("content");
                        }
                    }
                }
                
                if (respMap.containsKey("error")) {
                    Object error = respMap.get("error");
                    if (error instanceof Map) {
                        Map<String, Object> errorMap = (Map<String, Object>) error;
                        return "MiniMax服务错误: " + (errorMap.get("message") != null ? errorMap.get("message") : errorMap.toString());
                    }
                    return "MiniMax服务错误: " + error.toString();
                }
            }
            
            return responseBody;
        } finally {
            client.close();
        }
    }

    private String chatWithWenxin(String systemPrompt, String userMessage) throws Exception {
        String url = apiUrl + "?access_token=" + getWenxinAccessToken();
        
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", systemPrompt));
        messages.add(createMessage("user", userMessage));
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        
        return postJson(url, gson.toJson(requestBody));
    }

    private String chatWithQwen(String systemPrompt, String userMessage) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", systemPrompt));
        messages.add(createMessage("user", userMessage));
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        
        return postJson(apiUrl, gson.toJson(requestBody));
    }

    private String chatWithOpenAI(String systemPrompt, String userMessage) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", systemPrompt));
        messages.add(createMessage("user", userMessage));
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        
        HttpPost request = new HttpPost(apiUrl);
        request.setHeader("Authorization", "Bearer " + apiKey);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(gson.toJson(requestBody), StandardCharsets.UTF_8));
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            Map<String, Object> respMap = gson.fromJson(responseBody, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) respMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        }
        return "抱歉，无法获取AI回复。";
    }

    private String chatWithVolcEngine(String systemPrompt, String userMessage) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", systemPrompt));
        messages.add(createMessage("user", userMessage));
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        
        HttpPost request = new HttpPost(apiUrl);
        request.setHeader("Authorization", "Bearer " + apiKey);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(gson.toJson(requestBody), StandardCharsets.UTF_8));
        
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000)
                .setSocketTimeout(60000)
                .build();
        request.setConfig(requestConfig);
        
        try (CloseableHttpClient client = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build()) {
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            System.out.println("[AIClient] VolcEngine response status: " + statusCode);
            System.out.println("[AIClient] VolcEngine response: " + responseBody);
            
            if (statusCode != 200) {
                return "火山引擎服务错误: HTTP " + statusCode + " - " + responseBody;
            }
            
            Map<String, Object> respMap = gson.fromJson(responseBody, Map.class);
            
            if (respMap.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) respMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    if (choice.containsKey("message")) {
                        Map<String, Object> message = (Map<String, Object>) choice.get("message");
                        return (String) message.get("content");
                    }
                }
            }
            
            if (respMap.containsKey("error")) {
                Object error = respMap.get("error");
                if (error instanceof Map) {
                    Map<String, Object> errorMap = (Map<String, Object>) error;
                    return "火山引擎服务错误: " + (errorMap.get("message") != null ? errorMap.get("message") : errorMap.toString());
                }
                return "火山引擎服务错误: " + error.toString();
            }
            
            return responseBody;
        }
    }

    private String postJson(String url, String json) throws Exception {
        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            Map<String, Object> respMap = gson.fromJson(responseBody, Map.class);
            
            if (provider.equals("wenxin")) {
                if (respMap.containsKey("result")) {
                    return (String) respMap.get("result");
                }
            } else if (respMap.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) respMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            
            if (respMap.containsKey("error_msg")) {
                return "AI服务错误: " + respMap.get("error_msg");
            }
        }
        return "抱歉，无法获取AI回复。";
    }

    private String getWenxinAccessToken() {
        String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token";
        String grantType = "client_credentials";
        String clientId = Config.getProperty("ai.api.key", "");
        String clientSecret = Config.getProperty("ai.api.secret", "");
        
        String params = "grant_type=" + grantType + "&client_id=" + clientId + "&client_secret=" + clientSecret;
        
        try {
            HttpPost request = new HttpPost(tokenUrl + "?" + params);
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");
            
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpResponse response = client.execute(request);
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                Map<String, Object> respMap = gson.fromJson(responseBody, Map.class);
                if (respMap.containsKey("access_token")) {
                    return (String) respMap.get("access_token");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String getDefaultResponse(String userMessage) {
        String lowerMsg = userMessage.toLowerCase();
        
        if (lowerMsg.contains("发布新闻") || lowerMsg.contains("新闻管理")) {
            return "作为管理员，您可以这样管理新闻：\n1. 进入'新闻管理'页面\n2. 点击'发布新闻'创建新新闻\n3. 填写标题、内容、选择类型（获奖/活动/通知）\n4. 点击保存发布\n如有其他问题，请联系管理员。";
        } else if (lowerMsg.contains("奖项审核") || lowerMsg.contains("审核奖项")) {
            return "审核奖项申请的步骤：\n1. 进入'奖项管理'页面\n2. 点击'待审核'筛选\n3. 查看奖项详情\n4. 点击'通过'或'拒绝'按钮\n5. 填写审核意见";
        } else if (lowerMsg.contains("活动管理") || lowerMsg.contains("创建活动")) {
            return "创建新活动的步骤：\n1. 进入'活动管理'页面\n2. 点击'创建活动'\n3. 填写活动名称、时间、地点\n4. 设置报名起止时间\n5. 点击保存";
        } else if (lowerMsg.contains("招新") || lowerMsg.contains("报名审核")) {
            return "处理招新报名的步骤：\n1. 进入'招新管理'页面\n2. 查看待处理申请\n3. 点击'审核'查看详情\n4. 点击'同意'创建账号或'拒绝'驳回申请";
        } else if (lowerMsg.contains("成员管理") || lowerMsg.contains("重置密码")) {
            return "成员管理功能：\n1. 进入'成员管理'页面\n2. 查看所有成员列表\n3. 点击'重置密码'可重置成员密码\n4. 默认密码为123456";
        } else if (lowerMsg.contains("项目") || lowerMsg.contains("项目管理")) {
            return "项目管理说明：\n1. 成员可创建项目申请\n2. 管理员可审批项目\n3. 每个项目最多20名成员\n4. 每人每年最多3个项目";
        } else {
            return "您好！我是AI助手。\n\n我可以帮您解答以下问题：\n- 如何发布新闻\n- 如何审核奖项申请\n- 如何创建活动\n- 如何处理招新报名\n- 如何管理成员\n- 项目管理相关问题\n\n请告诉我您想了解的具体问题。";
        }
    }
}