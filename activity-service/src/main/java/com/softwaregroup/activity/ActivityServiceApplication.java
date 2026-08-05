package com.softwaregroup.activity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 活动服务启动类
 *
 * 功能：
 * - 活动管理（创建、更新、删除、审核）
 * - 活动报名与审批
 * - 考勤管理（签到/签退、补签申请）
 * - 学习时段管理
 *
 * 端口：8084
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.softwaregroup.activity.feign")
@ComponentScan(basePackages = {"com.softwaregroup.activity", "com.softwaregroup.common"})
public class ActivityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivityServiceApplication.class, args);
    }
}
