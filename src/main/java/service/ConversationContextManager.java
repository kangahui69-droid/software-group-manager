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
            return System.currentTimeMillis() - lastUpdateTime > 30 * 60 * 1000;
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

    public String handleFollowUp(String sessionId, String userReply) {
        ConversationContext ctx = getContext(sessionId);
        if (ctx == null) {
            return null;
        }

        String reply = userReply.toLowerCase().trim();

        if (isAffirmative(reply)) {
            ctx.pendingConfirmation = null;
            return "confirmed";
        }

        if (isNegative(reply)) {
            ctx.pendingConfirmation = null;
            ctx.slots.clear();
            return "cancelled";
        }

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

    public String inferIntentFromHistory(String sessionId, String newMessage) {
        ConversationContext ctx = getContext(sessionId);
        if (ctx == null) {
            return null;
        }

        String msg = newMessage.toLowerCase();

        if ("apply_activity".equals(ctx.lastIntent) && ctx.hasSlot("activities")) {
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