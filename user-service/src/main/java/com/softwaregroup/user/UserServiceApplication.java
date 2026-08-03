package com.softwaregroup.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户服务启动类
 *
 * 功能：
 * - 用户认证和JWT签发
 * - 成员档案管理
 * - 提供Feign接口供其他服务调用
 *
 * 端口：8082
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.softwaregroup.user.feign")
@ComponentScan(basePackages = {"com.softwaregroup.user", "com.softwaregroup.common"})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
