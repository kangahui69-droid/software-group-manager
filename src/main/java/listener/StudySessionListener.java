package listener;

import util.StudySessionScheduler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 学习会话定时任务监听器
 * 在Web应用启动时自动启动22:00自动结束学习会话的定时任务
 */
public class StudySessionListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[StudySessionListener] Web应用启动，开始调度学习会话定时任务...");
        StudySessionScheduler.getInstance().start();
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[StudySessionListener] Web应用关闭，停止学习会话定时任务...");
        StudySessionScheduler.getInstance().stop();
    }
}
