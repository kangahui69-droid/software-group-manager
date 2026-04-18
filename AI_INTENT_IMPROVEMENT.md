# AI助手功能增强方案

## 一、问题分析

### 1.1 功能覆盖不全

| 问题 | 位置 | 现状 |
|------|------|------|
| `pending_feature` | ai_chat.jsp:894 | 返回"功能正在开发中" |
| `view_my_groups` | AIService.java:2197-2203 | 仅返回跳转，无实际数据 |
| 群聊功能 | 整个系统 | 群聊模块存在但AI无法交互 |
| 多轮对话 | buildOperationGuide | 仅支持单轮意图识别 |

### 1.2 意图识别能力弱

**现有方案问题**：
```java
// 现有方案：简单关键词匹配
private String buildOperationGuide(String userMessage, String userRole) {
    // 问题1: 关键词精确匹配，无法处理变体
    if (msg.contains("报名") && msg.contains("活动")) {
        return "[ACTION]apply_activity";
    }
    // 问题2: 无法处理同义词
    // 用户说"我想参加"无法匹配"报名"
    // 问题3: 无法处理模糊表达
    // 用户说"帮我弄个活动"无法识别意图
}
```

---

## 二、解决方案

### 2.1 功能增强计划

#### Phase 1: 修复缺失功能

| ACTION | 当前状态 | 修复方案 |
|--------|----------|----------|
| `view_my_groups` | 跳转但无数据 | 实现群聊列表查询 |
| `list_my_groups` | 不存在 | 新增群聊列表ACTION |
| `join_group` | 不存在 | 新增加群ACTION |
| `leave_group` | 不存在 | 新增退群ACTION |
| `view_group_detail` | 不存在 | 新增群详情ACTION |
| `list_group_members` | 不存在 | 新增成员列表ACTION |

#### Phase 2: 扩展现有功能

| ACTION | 扩展功能 |
|--------|----------|
| `list_activities` | 支持按类型/时间筛选 |
| `apply_activity` | 支持活动ID或名称 |
| `query_activity_detail` | 开放给所有用户 |

#### Phase 3: 多轮对话支持

```java
// 意图状态机：支持上下文记忆
public class IntentContext {
    String pendingIntent;      // 待确认的意图
    Map<String, Object> slots; // 已提取的参数
    int confirmStep;          // 确认步骤
}
```

---

### 2.2 意图识别增强方案

#### 方案A: 规则引擎 + 语义相似度（推荐）

```java
public class EnhancedIntentRecognizer {

    // 意图定义
    static class IntentDefinition {
        String name;
        String[] primaryKeywords;
        String[] synonyms;
        String[] patterns;
        String action;
        String[] requiredSlots;
    }

    // 预定义意图
    static List<IntentDefinition> INTENT_DEFINITIONS = Arrays.asList(
        new IntentDefinition("apply_activity", // 报名活动
            new String[]{"报名", "参加", "加入", "参与"},
            new String[]{"我要报名", "想参加", "去参加", "报名参加", "活动报名"},
            new String[]{".*活动.*报名", ".*报名.*活动"},
            "apply_activity",
            new String[]{"activity_id"}
        ),
        new IntentDefinition("list_activities", // 查看活动
            new String[]{"查看活动", "活动列表", "有什么活动"},
            new String[]{"最近有啥活动", "有没有活动", "看看活动", "活动有哪些"},
            new String[]{"^活动$"},
            "list_latest_activities",
            new String[]{}
        ),
        // ... 更多意图
    );
}
```

#### 方案B: 关键词扩展 + 权重计算

```java
public class WeightedIntentRecognizer {

    // 同义词词典
    Map<String, List<String>> SYNONYM_DICT = new HashMap() {{
        put("报名", Arrays.asList("参加", "参与", "加入", "去", "想参加", "要参加"));
        put("活动", Arrays.asList("讲座", "培训", "分享", "比赛", "workshop"));
        put("查看", Arrays.asList("看看", "查询", "找", "浏览", "显示"));
        put("项目", Arrays.asList("project", "作品", "代码"));
        put("新闻", Arrays.asList("资讯", "动态", "消息", "公告", "通知"));
    }};

    // 计算匹配权重
    double calculateMatchScore(String userMsg, IntentDefinition intent) {
        double score = 0.0;
        String normalizedMsg = normalize(userMsg);

        // 主关键词匹配
        for (String kw : intent.primaryKeywords) {
            if (normalizedMsg.contains(kw)) score += 0.5;
        }

        // 同义词匹配
        for (String kw : intent.synonyms) {
            if (normalizedMsg.contains(kw)) score += 0.3;
        }

        // 模式匹配
        for (String pattern : intent.patterns) {
            if (normalizedMsg.matches(pattern)) score += 0.4;
        }

        return score;
    }
}
```

#### 方案C: 多轮对话状态机

```java
public class ConversationStateMachine {

    enum State {
        IDLE,               // 初始状态
        INTENT_DETECTED,    // 意图已识别，待确认
        COLLECTING_PARAMS,  // 收集参数中
        CONFIRMING,         // 确认中
        EXECUTING,          // 执行中
        FOLLOW_UP           // 跟进中
    }

    // 状态转换规则
    Map<State, Map<String, State>> TRANSITIONS = new HashMap() {{
        put(IDLE, Map.of(
            "intent_detected", INTENT_DETECTED,
            "follow_up", FOLLOW_UP
        ));
        put(INTENT_DETECTED, Map.of(
            "confirm", COLLECTING_PARAMS,
            "cancel", IDLE
        ));
        put(COLLECTING_PARAMS, Map.of(
            "params_complete", CONFIRMING,
            "cancel", IDLE
        ));
        put(CONFIRMING, Map.of(
            "execute", EXECUTING,
            "cancel", IDLE
        ));
    }};

    // 示例对话流程
    // 用户: "我想报名参加活动"
    // AI: "好的，您想报名参加哪个活动？以下是当前可报名的活动列表..."
    // 状态: IDLE -> INTENT_DETECTED -> COLLECTING_PARAMS
    //
    // 用户: "第一个"
    // AI: "您选择的是「Java技术讲座」，确认报名吗？"
    // 状态: COLLECTING_PARAMS -> CONFIRMING
    //
    // 用户: "是的"
    // AI: "报名成功！"
    // 状态: CONFIRMING -> EXECUTING -> IDLE
}
```

---

## 三、具体实现

### 3.1 新增群聊相关ACTION

```java
// AIService.java 新增方法

private Map<String, Object> executeListMyGroups(Map<String, String> params, User user) {
    Map<String, Object> result = new HashMap<>();

    // 获取用户参与的群聊
    List<ActivityGroup> groups = groupDAO.findByUserId(user.getId());
    List<ActivityGroup> ownedGroups = groupDAO.findByOwnerId(user.getId());

    // 合并去重
    Set<Integer> addedIds = new HashSet<>();
    List<Map<String, Object>> groupList = new ArrayList<>();

    for (ActivityGroup g : ownedGroups) {
        if (!addedIds.contains(g.getId())) {
            groupList.add(buildGroupInfo(g, true));
            addedIds.add(g.getId());
        }
    }
    for (ActivityGroup g : groups) {
        if (!addedIds.contains(g.getId())) {
            groupList.add(buildGroupInfo(g, false));
            addedIds.add(g.getId());
        }
    }

    result.put("success", true);
    result.put("type", "group_list");
    result.put("data", groupList);
    result.put("message", "您共有 " + groupList.size() + " 个群聊");
    return result;
}

private Map<String, Object> executeViewGroupDetail(Map<String, String> params, User user) {
    Map<String, Object> result = new HashMap<>();
    String groupIdStr = params.get("group_id");

    if (groupIdStr == null || groupIdStr.trim().isEmpty()) {
        result.put("success", false);
        result.put("message", "请提供群聊ID (group_id)");
        return result;
    }

    try {
        int groupId = Integer.parseInt(groupIdStr);
        ActivityGroup group = groupDAO.findById(groupId);

        if (group == null) {
            result.put("success", false);
            result.put("message", "未找到该群聊");
            return result;
        }

        // 检查用户是否是成员或群主
        boolean isMember = userGroupDAO.isUserInGroup(user.getId(), groupId);
        boolean isOwner = group.getGroupOwnerId() == user.getId();

        if (!isMember && !isOwner) {
            result.put("success", false);
            result.put("message", "您不是该群聊的成员");
            return result;
        }

        List<User> members = groupMemberDAO.findByGroupId(groupId);

        result.put("success", true);
        result.put("type", "group_detail");
        result.put("data", buildGroupInfo(group, isOwner));
        result.put("members", members);
        result.put("memberCount", members.size());
        result.put("message", "群聊详情获取成功");
    } catch (NumberFormatException e) {
        result.put("success", false);
        result.put("message", "无效的群聊ID");
    }
    return result;
}

private Map<String, Object> executeJoinGroup(Map<String, String> params, User user) {
    Map<String, Object> result = new HashMap<>();
    String groupIdStr = params.get("group_id");

    if (groupIdStr == null || groupIdStr.trim().isEmpty()) {
        result.put("success", false);
        result.put("message", "请提供群聊ID (group_id)");
        return result;
    }

    try {
        int groupId = Integer.parseInt(groupIdStr);
        ActivityGroup group = groupDAO.findById(groupId);

        if (group == null) {
            result.put("success", false);
            result.put("message", "未找到该群聊");
            return result;
        }

        if (userGroupDAO.isUserInGroup(user.getId(), groupId)) {
            result.put("success", false);
            result.put("message", "您已在该群聊中");
            return result;
        }

        boolean success = userGroupDAO.insertUserToGroup(user.getId(), groupId);
        result.put("success", success);
        result.put("message", success ? "加入群聊成功" : "加入失败";
    } catch (NumberFormatException e) {
        result.put("success", false);
        result.put("message", "无效的群聊ID");
    }
    return result;
}

private Map<String, Object> executeLeaveGroup(Map<String, String> params, User user) {
    Map<String, Object> result = new HashMap<>();
    String groupIdStr = params.get("group_id");

    if (groupIdStr == null || groupIdStr.trim().isEmpty()) {
        result.put("success", false);
        result.put("message", "请提供群聊ID (group_id)");
        return result;
    }

    try {
        int groupId = Integer.parseInt(groupIdStr);
        ActivityGroup group = groupDAO.findById(groupId);

        if (group == null) {
            result.put("success", false);
            result.put("message", "未找到该群聊");
            return result;
        }

        if (group.getGroupOwnerId() == user.getId()) {
            result.put("success", false);
            result.put("message", "群主无法退出群聊，请先转让群主权限");
            return result;
        }

        boolean success = userGroupDAO.removeUserFromGroup(user.getId(), groupId);
        result.put("success", success);
        result.put("message", success ? "已退出群聊" : "退出失败";
    } catch (NumberFormatException e) {
        result.put("success", false);
        result.put("message", "无效的群聊ID");
    }
    return result;
}

private Map<String, Object> executeListGroupMembers(Map<String, String> params, User user) {
    Map<String, Object> result = new HashMap<>();
    String groupIdStr = params.get("group_id");

    if (groupIdStr == null || groupIdStr.trim().isEmpty()) {
        result.put("success", false);
        result.put("message", "请提供群聊ID (group_id)");
        return result;
    }

    try {
        int groupId = Integer.parseInt(groupIdStr);

        if (!userGroupDAO.isUserInGroup(user.getId(), groupId)) {
            result.put("success", false);
            result.put("message", "您不是该群聊的成员");
            return result;
        }

        List<User> members = groupMemberDAO.findByGroupId(groupId);

        result.put("success", true);
        result.put("type", "member_list");
        result.put("data", members);
        result.put("message", "该群聊共有 " + members.size() + " 名成员");
    } catch (NumberFormatException e) {
        result.put("success", false);
        result.put("message", "无效的群聊ID");
    }
    return result;
}
```

### 3.2 增强意图识别器

```java
// 新增文件: service/EnhancedIntentRecognizer.java

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

    // 同义词词典
    private static final Map<String, List<String>> SYNONYM_DICT = new HashMap() {{
        put("报名", Arrays.asList("参加", "参与", "加入", "去", "想参加", "要参加", "报名参加"));
        put("活动", Arrays.asList("讲座", "培训", "分享", "比赛", "workshop", "研讨会", "课程"));
        put("查看", Arrays.asList("看看", "查询", "找", "浏览", "显示", "获取", "显示"));
        put("项目", Arrays.asList("project", "作品", "代码", "开发"));
        put("新闻", Arrays.asList("资讯", "动态", "消息", "公告", "通知", "报道"));
        put("群聊", Arrays.asList("群", "群组", "小组", "聊天室", "交流群"));
        put("我的", Arrays.asList("我的", "个人", "自己", "本人"));
        put("创建", Arrays.asList("发起", "新建", "创建", "创建", "创建", "建立"));
        put("提交", Arrays.asList("提交", "发送", "投递", "发布", "投稿"));
        put("奖项", Arrays.asList("获奖", "荣誉", "证书", "奖状", "成绩"));
        put("问题", Arrays.asList("bug", "错误", "故障", "issue", "反馈", "建议"));
    }};

    // 意图定义
    private static final List<IntentDefinition> INTENT_DEFINITIONS = Arrays.asList(
        // 报名活动 - 最高优先级
        new IntentDefinition("apply_activity",
            new String[]{"报名", "参加", "参与", "加入活动"},
            new String[]{"我要报名", "想参加", "去参加", "活动报名", "报名参加活动", "我要参加"},
            new String[]{".*报名.*活动", ".*参加.*活动", ".*活动.*参加", ".*活动.*报名"},
            "apply_activity",
            new String[]{"activity_id"},
            10
        ),

        // 查看活动列表
        new IntentDefinition("list_activities",
            new String[]{"查看活动", "活动列表", "有哪些活动", "有什么活动"},
            new String[]{"最近有啥活动", "有没有活动", "看看活动", "活动有哪些", "现在有什么活动", "最近的活動"},
            new String[]{"^活动$", "^查看活动$"},
            "list_latest_activities",
            new String[]{},
            8
        ),

        // 创建活动
        new IntentDefinition("create_activity",
            new String[]{"创建活动", "发起活动", "举办活动", "组织活动"},
            new String[]{"我想办活动", "要办活动", "想搞活动", "开个活动", "怎样发起活动", "如何举办活动"},
            new String[]{".*创建.*活动", ".*发起.*活动", ".*办.*活动"},
            "create_activity_request",
            new String[]{"name", "time", "location"},
            9
        ),

        // 查看新闻
        new IntentDefinition("list_news",
            new String[]{"查看新闻", "新闻列表", "资讯", "动态"},
            new String[]{"最近有啥新闻", "有没有消息", "看看新闻", "新闻有哪些", "公告"},
            new String[]{"^新闻$", "^查看新闻$"},
            "list_all_news",
            new String[]{},
            7
        ),

        // 提交反馈
        new IntentDefinition("submit_feedback",
            new String[]{"提交反馈", "问题反馈", "提交问题", "提建议"},
            new String[]{"我要反馈", "有建议", "遇到问题", "报错", "bug"},
            new String[]{".*反馈.*", ".*问题.*", ".*建议.*"},
            "submit_feedback",
            new String[]{"content"},
            6
        ),

        // 查看我的群聊
        new IntentDefinition("list_my_groups",
            new String[]{"我的群聊", "我的群", "加入的群", "群聊列表"},
            new String[]{"我加入的群", "我的聊天室", "群消息", "查看群聊"},
            new String[]{"^群聊$", "^群$"},
            "list_my_groups",
            new String[]{},
            7
        ),

        // 查看我的项目
        new IntentDefinition("list_my_projects",
            new String[]{"我的项目", "项目列表", "我参与的项目"},
            new String[]{"我的项目申请", "项目申请记录", "查看项目"},
            new String[]{},
            "view_my_projects",
            new String[]{},
            7
        ),

        // 查看我的奖项
        new IntentDefinition("list_my_awards",
            new String[]{"我的奖项", "获奖记录", "我获得的奖项"},
            new String[]{"我的获奖", "奖项列表", "获奖情况"},
            new String[]{},
            "list_my_awards",
            new String[]{},
            7
        ),

        // 申请奖项
        new IntentDefinition("submit_award",
            new String[]{"申请奖项", "提交奖项", "获奖申请", "申报奖项"},
            new String[]{"我想申请奖项", "要申请奖项", "获奖申请"},
            new String[]{},
            "submit_award",
            new String[]{"competition", "level"},
            8
        ),

        // 查看我的活动
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

        // 置信度阈值
        if (bestScore < 0.3) {
            return new RecognitionResult(null, "unknown", 0.0);
        }

        return bestResult;
    }

    private double calculateMatchScore(String msg, IntentDefinition intent) {
        double score = 0.0;

        // 主关键词匹配 (最高权重)
        for (String kw : intent.primaryKeywords) {
            if (msg.contains(kw)) {
                score += 0.5;
                break;
            }
        }

        // 同义词匹配 (中等权重)
        for (String kw : intent.synonyms) {
            if (msg.contains(kw)) {
                score += 0.3;
                break;
            }
        }

        // 使用展开同义词再次检查
        for (Map.Entry<String, List<String>> entry : SYNONYM_DICT.entrySet()) {
            if (msg.contains(entry.getKey())) {
                for (String syn : entry.getValue()) {
                    if (msg.contains(syn)) {
                        // 检查这是否与当前意图相关
                        for (String kw : intent.primaryKeywords) {
                            if (isRelated(kw, entry.getKey())) {
                                score += 0.15;
                            }
                        }
                    }
                }
            }
        }

        // 模式匹配 (最高权重)
        for (String pattern : intent.patterns) {
            if (msg.matches(pattern)) {
                score += 0.6;
                break;
            }
        }

        // 意图优先级加权
        score = score * (1.0 + intent.priority * 0.05);

        return score;
    }

    private boolean isRelated(String keyword, String synonymGroup) {
        // 简单的关系判断
        Map<String, List<String>> RELATED_GROUPS = new HashMap() {{
            put("报名", Arrays.asList("活动", "项目", "群聊"));
            put("活动", Arrays.asList("报名", "参加"));
            put("查看", Arrays.asList("新闻", "活动", "项目", "群聊", "奖项"));
            put("群聊", Arrays.asList("加入", "退出", "查看"));
        }};

        List<String> related = RELATED_GROUPS.get(synonymGroup);
        if (related != null) {
            return Arrays.asList(related).contains(keyword);
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

        // 分析用户消息中的关键词
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
```

### 3.3 多轮对话上下文管理

```java
// 新增文件: service/ConversationContextManager.java

package service;

import java.util.*;

public class ConversationContextManager {

    public static class ConversationContext {
        public String sessionId;
        public String lastIntent;
        public Map<String, Object> slots;
        public String lastAction;
        public Map<String, String> lastActionParams;
        public int turnCount;
        public long lastUpdateTime;
        public List<String> recentIntents;
        public String pendingConfirmation;

        public ConversationContext(String sessionId) {
            this.sessionId = sessionId;
            this.slots = new HashMap<>();
            this.lastActionParams = new HashMap<>();
            this.recentIntents = new ArrayList<>();
            this.turnCount = 0;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public void update(String intent, String action, Map<String, Object> newSlots) {
            this.lastIntent = intent;
            this.lastAction = action;
            if (newSlots != null) {
                this.slots.putAll(newSlots);
            }
            this.turnCount++;
            this.lastUpdateTime = System.currentTimeMillis();

            this.recentIntents.add(intent);
            if (this.recentIntents.size() > 5) {
                this.recentIntents.remove(0);
            }
        }

        public boolean hasSlot(String key) {
            return slots.containsKey(key) && slots.get(key) != null;
        }

        public Object getSlot(String key) {
            return slots.get(key);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - lastUpdateTime > 30 * 60 * 1000; // 30分钟
        }
    }

    private Map<String, ConversationContext> contexts = new HashMap<>();
    private static ConversationContextManager instance;

    private ConversationContextManager() {}

    public static ConversationContextManager getInstance() {
        if (instance == null) {
            instance = new ConversationContextManager();
        }
        return instance;
    }

    public ConversationContext getOrCreateContext(String sessionId) {
        ConversationContext ctx = contexts.get(sessionId);
        if (ctx == null || ctx.isExpired()) {
            ctx = new ConversationContext(sessionId);
            contexts.put(sessionId, ctx);
        }
        return ctx;
    }

    public void updateContext(String sessionId, String intent, String action, Map<String, Object> slots) {
        ConversationContext ctx = getOrCreateContext(sessionId);
        ctx.update(intent, action, slots);
    }

    public ConversationContext getContext(String sessionId) {
        return contexts.get(sessionId);
    }

    public void clearContext(String sessionId) {
        contexts.remove(sessionId);
    }

    // 处理用户回复（用于多轮对话中的二次确认）
    public String handleFollowUp(String sessionId, String userReply) {
        ConversationContext ctx = getContext(sessionId);
        if (ctx == null) {
            return null;
        }

        String reply = userReply.toLowerCase().trim();

        // 处理确认回复
        if (isAffirmative(reply)) {
            ctx.pendingConfirmation = null;
            return "confirmed";
        }

        // 处理否定回复
        if (isNegative(reply)) {
            ctx.pendingConfirmation = null;
            ctx.slots.clear();
            return "cancelled";
        }

        // 处理数字选择（如"第一个"）
        if (reply.matches("\\d+") || reply.matches("第\\d+个")) {
            return "selected:" + reply;
        }

        return null;
    }

    private boolean isAffirmative(String reply) {
        return reply.matches(".*(是|对|好|可以|确定|没错|是的|好的|同意).*");
    }

    private boolean isNegative(String reply) {
        return reply.matches(".*(不|否|取消|算了|不要|不同意|拒绝).*");
    }

    // 从历史中推断意图
    public String inferIntentFromHistory(String sessionId, String newMessage) {
        ConversationContext ctx = getContext(sessionId);
        if (ctx == null) {
            return null;
        }

        String msg = newMessage.toLowerCase();

        // 如果上轮是选择活动
        if ("apply_activity".equals(ctx.lastIntent) && ctx.hasSlot("activities")) {
            // 尝试匹配数字或名称
            List<?> activities = (List<?>) ctx.getSlot("activities");
            for (int i = 0; i < activities.size(); i++) {
                if (msg.contains(String.valueOf(i + 1)) ||
                    msg.contains("第" + (i + 1))) {
                    return "select_activity:" + i;
                }
            }
        }

        return null;
    }
}
```

---

## 四、修改清单

### 4.1 需要修改的文件

| 文件 | 修改内容 |
|------|----------|
| `AIService.java` | 新增群聊相关方法、更新executeAction |
| `ai_chat.jsp` | 更新前端调用、显示群聊数据 |
| `IntentRecognizer.java` (新增) | 新的意图识别引擎 |

### 4.2 新增文件

| 文件 | 说明 |
|------|------|
| `EnhancedIntentRecognizer.java` | 增强的意图识别 |
| `ConversationContextManager.java` | 对话上下文管理 |
| `AI_INTENT_IMPROVEMENT.md` | 本文档 |

---

## 五、效果预期

### 5.1 功能覆盖

| 指标 | 当前 | 目标 |
|------|------|------|
| 可用ACTION数 | ~16 | ~25 |
| 群聊相关功能 | 0 | 5 |
| 多轮对话支持 | 无 | 完整状态机 |

### 5.2 意图识别

| 指标 | 当前 | 目标 |
|------|------|------|
| 意图识别准确率 | ~60% | >85% |
| 同义词覆盖 | 无 | 50+组 |
| 模糊表达识别 | 无 | 支持 |

### 5.3 示例对话

**修复后:**

```
用户: 我想报名参加活动
AI: 好的！以下是当前可报名的活动：
     1. Java技术讲座 (04-20 14:00) @教学楼301
     2. Python工作坊 (04-22 09:00) @实验楼202
    请告诉我您想报名哪个？（输入序号或名称）

用户: 第一个
AI: 您选择的是「Java技术讲座」，确认报名吗？

用户: 是的
AI: 报名成功！您已报名参加Java技术讲座，等待管理员审核。
```

---
