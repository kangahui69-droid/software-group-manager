package com.softwaregroup.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 项目奖项服务启动类
 *
 * 功能：
 * - 项目管理（ProjectService）
 * - 奖项管理（AwardService）
 *
 * 端口：8088
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.softwaregroup.project.feign")
@ComponentScan(basePackages = {"com.softwaregroup.project", "com.softwaregroup.common"})
public class ProjectAwardServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectAwardServiceApplication.class, args);
    }
}
