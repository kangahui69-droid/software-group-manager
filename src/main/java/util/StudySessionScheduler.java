package util;

import dao.StudySessionDAO;
import model.StudySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 学习会话定时任务调度器
 * 每天22:00自动结束所有进行中的学习会话
 */
public class StudySessionScheduler {
    private static final Logger logger = LoggerFactory.getLogger(StudySessionScheduler.class);
    private static final int END_HOUR = 22; // 22:00自动结束
    private static final int END_MINUTE = 0;
    
    private Timer timer;
    private static StudySessionScheduler instance;
    private StudySessionDAO studyDAO = new StudySessionDAO();
    
    private StudySessionScheduler() {
    }
    
    public static synchronized StudySessionScheduler getInstance() {
        if (instance == null) {
            instance = new StudySessionScheduler();
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
        
        timer = new Timer("StudySessionScheduler", true);
        
        // 计算距离下次22:00的时间
        long delay = calculateDelayToTargetTime(END_HOUR, END_MINUTE);
        
        // 每天22:00执行一次
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                endAllActiveSessions();
            }
        }, delay, 24 * 60 * 60 * 1000L); // 24小时
        
        logger.info("学习会话调度器已启动，下次执行时间: " + getNextExecutionTime());
    }
    
    /**
     * 停止调度器
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            logger.info("学习会话调度器已停止");
        }
    }
    
    /**
     * 立即执行一次（用于测试）
     */
    public void executeNow() {
        endAllActiveSessions();
    }
    
    /**
     * 结束所有进行中的学习会话
     */
    private void endAllActiveSessions() {
        logger.info("开始执行22:00自动结束学习会话任务...");
        
        try {
            List<StudySession> activeSessions = studyDAO.getAllActiveSessions();
            int count = activeSessions.size();
            
            if (count > 0) {
                int updated = studyDAO.endAllActiveSessions();
                logger.info("已自动结束 " + updated + " 个进行中的学习会话");
                
                for (StudySession session : activeSessions) {
                    logger.info("  - 用户ID: " + session.getUserId() + ", 开始时间: " + session.getCheckInTime());
                }
            } else {
                logger.info("没有需要自动结束的学习会话");
            }
        } catch (Exception e) {
            logger.error("执行22:00自动结束学习会话任务失败", e);
        }
    }
    
    /**
     * 计算距离目标时间还有多少毫秒
     */
    private long calculateDelayToTargetTime(int hour, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);
        
        // 如果今天已经过了目标时间，则设为明天
        if (now.after(target)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return target.getTimeInMillis() - now.getTimeInMillis();
    }
    
    /**
     * 获取下次执行时间
     */
    private String getNextExecutionTime() {
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, END_HOUR);
        target.set(Calendar.MINUTE, END_MINUTE);
        target.set(Calendar.SECOND, 0);
        
        if (Calendar.getInstance().after(target)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return String.format("%04d-%02d-%02d %02d:%02d:00",
            target.get(Calendar.YEAR),
            target.get(Calendar.MONTH) + 1,
            target.get(Calendar.DAY_OF_MONTH),
            target.get(Calendar.HOUR_OF_DAY),
            target.get(Calendar.MINUTE));
    }
}
