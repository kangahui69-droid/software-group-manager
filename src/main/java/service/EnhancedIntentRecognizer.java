package service;

import java.util.*;

public class EnhancedIntentRecognizer {

    public static class IntentDefinition {
        public String name;
        public String[] primaryKeywords;
        public String[] synonyms;
        public String[] patterns;
        public String action;
        public String[] requiredSlots;
        public int priority;

        public IntentDefinition(String name, String[] pk, String[] syn,
                                String[] pat, String action, String[] slots, int priority) {
            this.name = name;
            this.primaryKeywords = pk;
            this.synonyms = syn;
            this.patterns = pat;
            this.action = action;
            this.requiredSlots = slots;
            this.priority = priority;
        }
    }

    private static final Map<String, List<String>> SYNONYM_DICT = new HashMap() {{
        put("报名", Arrays.asList("参加", "参与", "加入", "去", "想参加", "要参加", "报名参加"));
        put("活动", Arrays.asList("讲座", "培训", "分享", "比赛", "workshop", "研讨会", "课程"));
        put("查看", Arrays.asList("看看", "查询", "找", "浏览", "显示", "获取", "展示"));
        put("项目", Arrays.asList("project", "作品", "代码", "开发"));
        put("新闻", Arrays.asList("资讯", "动态", "消息", "公告", "通知", "报道"));
        put("群聊", Arrays.asList("群", "群组", "小组", "聊天室", "交流群"));
        put("我的", Arrays.asList("我的", "个人", "自己", "本人"));
        put("创建", Arrays.asList("发起", "新建", "创建", "建立"));
        put("提交", Arrays.asList("提交", "发送", "投递", "发布", "投稿"));
        put("奖项", Arrays.asList("获奖", "荣誉", "证书", "奖状", "成绩"));
        put("问题", Arrays.asList("bug", "错误", "故障", "issue", "反馈", "建议"));
    }};

    private static final List<IntentDefinition> INTENT_DEFINITIONS = Arrays.asList(
        new IntentDefinition("apply_activity",
            new String[]{"报名", "参加", "参与", "加入活动"},
            new String[]{"我要报名", "想参加", "去参加", "活动报名", "报名参加活动", "我要参加"},
            new String[]{".*报名.*活动", ".*参加.*活动", ".*活动.*参加", ".*活动.*报名"},
            "apply_activity",
            new String[]{"activity_id"},
            10
        ),
        new IntentDefinition("list_activities",
            new String[]{"查看活动", "活动列表", "有哪些活动", "有什么活动"},
            new String[]{"最近有啥活动", "有没有活动", "看看活动", "活动有哪些", "现在有什么活动", "最近的活動"},
            new String[]{"^活动$", "^查看活动$"},
            "list_latest_activities",
            new String[]{},
            8
        ),
        new IntentDefinition("create_activity",
            new String[]{"创建活动", "发起活动", "举办活动", "组织活动"},
            new String[]{"我想办活动", "要办活动", "想搞活动", "开个活动", "怎样发起活动", "如何举办活动"},
            new String[]{".*创建.*活动", ".*发起.*活动", ".*办.*活动"},
            "create_activity_request",
            new String[]{"name", "time", "location"},
            9
        ),
        new IntentDefinition("list_news",
            new String[]{"查看新闻", "新闻列表", "资讯", "动态"},
            new String[]{"最近有啥新闻", "有没有消息", "看看新闻", "新闻有哪些", "公告"},
            new String[]{"^新闻$", "^查看新闻$"},
            "list_all_news",
            new String[]{},
            7
        ),
        new IntentDefinition("submit_feedback",
            new String[]{"提交反馈", "问题反馈", "提交问题", "提建议"},
            new String[]{"我要反馈", "有建议", "遇到问题", "报错", "bug"},
            new String[]{".*反馈.*", ".*问题.*", ".*建议.*"},
            "submit_feedback",
            new String[]{"content"},
            6
        ),
        new IntentDefinition("list_my_groups",
            new String[]{"我的群聊", "我的群", "加入的群", "群聊列表"},
            new String[]{"我加入的群", "我的聊天室", "群消息", "查看群聊"},
            new String[]{"^群聊$", "^群$"},
            "list_my_groups",
            new String[]{},
            7
        ),
        new IntentDefinition("list_my_projects",
            new String[]{"我的项目", "项目列表", "我参与的项目"},
            new String[]{"我的项目申请", "项目申请记录", "查看项目"},
            new String[]{},
            "view_my_projects",
            new String[]{},
            7
        ),
        new IntentDefinition("list_my_awards",
            new String[]{"我的奖项", "获奖记录", "我获得的奖项"},
            new String[]{"我的获奖", "奖项列表", "获奖情况"},
            new String[]{},
            "list_my_awards",
            new String[]{},
            7
        ),
        new IntentDefinition("submit_award",
            new String[]{"申请奖项", "提交奖项", "获奖申请", "申报奖项"},
            new String[]{"我想申请奖项", "要申请奖项", "获奖申请"},
            new String[]{},
            "submit_award",
            new String[]{"competition", "level"},
            8
        ),
        new IntentDefinition("view_my_activities",
            new String[]{"我的活动", "报名记录", "我报名的活动"},
            new String[]{"已报名活动", "活动记录", "我的活动"},
            new String[]{},
            "view_my_activities",
            new String[]{},
            7
        )
    );

    public static class RecognitionResult {
        public String action;
        public String intentName;
        public double confidence;
        public Map<String, String> extractedSlots;
        public String followUpQuestion;

        public RecognitionResult(String action, String intentName, double confidence) {
            this.action = action;
            this.intentName = intentName;
            this.confidence = confidence;
            this.extractedSlots = new HashMap<>();
        }
    }

    public RecognitionResult recognize(String userMessage, String userRole) {
        String normalizedMsg = normalizeMessage(userMessage);

        RecognitionResult bestResult = null;
        double bestScore = 0.0;

        for (IntentDefinition intent : INTENT_DEFINITIONS) {
            double score = calculateMatchScore(normalizedMsg, intent);

            if (score > bestScore) {
                bestScore = score;
                bestResult = new RecognitionResult(intent.action, intent.name, score);
                bestResult.followUpQuestion = generateFollowUpQuestion(intent);
            }
        }

        if (bestScore < 0.3) {
            return new RecognitionResult(null, "unknown", 0.0);
        }

        return bestResult;
    }

    private double calculateMatchScore(String msg, IntentDefinition intent) {
        double score = 0.0;

        for (String kw : intent.primaryKeywords) {
            if (msg.contains(kw)) {
                score += 0.5;
                break;
            }
        }

        for (String kw : intent.synonyms) {
            if (msg.contains(kw)) {
                score += 0.3;
                break;
            }
        }

        for (Map.Entry<String, List<String>> entry : SYNONYM_DICT.entrySet()) {
            if (msg.contains(entry.getKey())) {
                for (String syn : entry.getValue()) {
                    if (msg.contains(syn)) {
                        for (String kw : intent.primaryKeywords) {
                            if (isRelated(kw, entry.getKey())) {
                                score += 0.15;
                            }
                        }
                    }
                }
            }
        }

        for (String pattern : intent.patterns) {
            if (msg.matches(pattern)) {
                score += 0.6;
                break;
            }
        }

        score = score * (1.0 + intent.priority * 0.05);

        return score;
    }

    private boolean isRelated(String keyword, String synonymGroup) {
        Map<String, List<String>> RELATED_GROUPS = new HashMap() {{
            put("报名", Arrays.asList("活动", "项目", "群聊"));
            put("活动", Arrays.asList("报名", "参加"));
            put("查看", Arrays.asList("新闻", "活动", "项目", "群聊", "奖项"));
            put("群聊", Arrays.asList("加入", "退出", "查看"));
        }};

        List<String> related = RELATED_GROUPS.get(synonymGroup);
        if (related != null) {
            return related.contains(keyword);
        }
        return false;
    }

    private String normalizeMessage(String msg) {
        return msg.toLowerCase()
                  .replace("？", "?")
                  .replace("！", "!")
                  .replace("。", ".")
                  .replace("，", ",")
                  .replace(" ", "");
    }

    private String generateFollowUpQuestion(IntentDefinition intent) {
        switch (intent.name) {
            case "apply_activity":
                return "请问您想报名参加哪个活动？请从列表中选择或告诉我活动名称。";
            case "create_activity":
                return "好的，请告诉我活动的名称、时间、地点等信息。";
            case "submit_feedback":
                return "请描述您遇到的问题或您的建议。";
            case "submit_award":
                return "请告诉我比赛名称、获奖等级等信息。";
            default:
                return null;
        }
    }

    public String buildEnhancedOperationGuide(String userMessage, String userRole) {
        RecognitionResult result = recognize(userMessage, userRole);

        if (result.action == null) {
            return buildFallbackGuide(userMessage);
        }

        StringBuilder guide = new StringBuilder();
        guide.append("【意图识别】").append(result.intentName)
             .append(" (置信度: ").append(String.format("%.0f%%", result.confidence * 100)).append(")\n");
        guide.append("- 执行操作：").append(result.action).append("\n");

        if (result.followUpQuestion != null) {
            guide.append("- 跟进问题：").append(result.followUpQuestion).append("\n");
        }

        return guide.toString();
    }

    private String buildFallbackGuide(String userMessage) {
        StringBuilder guide = new StringBuilder();
        guide.append("【无法确定意图】\n");

        String msg = userMessage.toLowerCase();
        List<String> detectedTopics = new ArrayList<>();

        if (containsAny(msg, new String[]{"活动", "讲座", "培训", "比赛"})) {
            detectedTopics.add("活动");
        }
        if (containsAny(msg, new String[]{"新闻", "资讯", "动态", "通知"})) {
            detectedTopics.add("新闻");
        }
        if (containsAny(msg, new String[]{"项目", "作品", "开发"})) {
            detectedTopics.add("项目");
        }
        if (containsAny(msg, new String[]{"群", "群聊", "小组"})) {
            detectedTopics.add("群聊");
        }
        if (containsAny(msg, new String[]{"奖项", "获奖", "证书"})) {
            detectedTopics.add("奖项");
        }

        if (!detectedTopics.isEmpty()) {
            guide.append("检测到您可能想了解：").append(String.join("、", detectedTopics)).append("\n");
            guide.append("您可以：\n");
            if (detectedTopics.contains("活动")) {
                guide.append("- 查看活动列表：'查看活动'\n");
                guide.append("- 报名参加活动：'报名参加[活动名]'\n");
            }
            if (detectedTopics.contains("新闻")) {
                guide.append("- 查看新闻：'查看新闻'\n");
            }
            if (detectedTopics.contains("项目")) {
                guide.append("- 查看项目：'查看项目'\n");
            }
            if (detectedTopics.contains("群聊")) {
                guide.append("- 查看我的群聊：'我的群聊'\n");
            }
            if (detectedTopics.contains("奖项")) {
                guide.append("- 查看我的奖项：'我的奖项'\n");
            }
        } else {
            guide.append("抱歉，我没有理解您的问题。\n");
            guide.append("您可以尝试：\n");
            guide.append("- '查看活动' - 查看可报名的活动\n");
            guide.append("- '查看新闻' - 查看最新资讯\n");
            guide.append("- '提交反馈' - 提交问题或建议\n");
        }

        return guide.toString();
    }

    private boolean containsAny(String text, String[] keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}