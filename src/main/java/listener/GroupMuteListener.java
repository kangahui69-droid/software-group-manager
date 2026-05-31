package listener;

import util.GroupMuteScheduler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 群聊禁言定时任务监听器
 * 在Web应用启动时自动启动群聊禁言检查的定时任务
 */
public class GroupMuteListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[GroupMuteListener] Web应用启动，开始调度群聊禁言定时任务...");
        GroupMuteScheduler.getInstance().start();
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[GroupMuteListener] Web应用关闭，停止群聊禁言定时任务...");
        GroupMuteScheduler.getInstance().stop();
    }
}
