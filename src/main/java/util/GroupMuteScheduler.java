package util;

import dao.ActivityGroupDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 群聊禁言定时任务调度器
 * 每分钟检查一次，自动解除已过期的禁言
 */
public class GroupMuteScheduler {
    private static final Logger logger = LoggerFactory.getLogger(GroupMuteScheduler.class);
    
    private Timer timer;
    private static GroupMuteScheduler instance;
    private ActivityGroupDAO groupDAO = new ActivityGroupDAO();
    
    private GroupMuteScheduler() {
    }
    
    public static synchronized GroupMuteScheduler getInstance() {
        if (instance == null) {
            instance = new GroupMuteScheduler();
        }
        return instance;
    }
    
    /**
     * 启动定时调度器
     */
    public void start() {
        if (timer != null) {
            return; // 已启动
        }
        
        timer = new Timer("GroupMuteScheduler", true);
        
        // 每分钟执行一次，检查并解除已过期的禁言
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAndUnmuteExpiredGroups();
            }
        }, 0, 60 * 1000L); // 每分钟执行
        
        logger.info("群聊禁言调度器已启动，每分钟检查一次禁言状态");
    }
    
    /**
     * 停止调度器
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            logger.info("群聊禁言调度器已停止");
        }
    }
    
    /**
     * 手动触发一次检查（用于测试）
     */
    public void executeNow() {
        checkAndUnmuteExpiredGroups();
    }
    
    /**
     * 检查并解除已过期的禁言
     */
    private void checkAndUnmuteExpiredGroups() {
        try {
            int unmuteCount = groupDAO.unmuteExpiredGroups();
            if (unmuteCount > 0) {
                logger.info("已自动解除 " + unmuteCount + " 个已过期的群聊禁言");
            }
        } catch (Exception e) {
            logger.error("执行群聊禁言解除任务失败", e);
        }
    }
}
