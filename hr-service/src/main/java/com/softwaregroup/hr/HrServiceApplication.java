package com.softwaregroup.hr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 人力资源服务启动类
 *
 * 功能：
 * - 招聘申请管理（提交、审批、列表查询）
 * - 简历管理（CRUD、教育经历、技能、项目经历、获奖情况）
 *
 * 端口：8090
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.softwaregroup.hr.feign")
@ComponentScan(basePackages = {"com.softwaregroup.hr", "com.softwaregroup.common"})
public class HrServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrServiceApplication.class, args);
    }
}
