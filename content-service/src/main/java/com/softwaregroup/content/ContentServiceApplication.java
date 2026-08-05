package com.softwaregroup.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 内容服务启动类
 *
 * 功能：
 * - 群聊管理（GroupService）
 * - 新闻管理（NewsService）
 *
 * 端口：8087
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.softwaregroup.content.feign")
@ComponentScan(basePackages = {"com.softwaregroup.content", "com.softwaregroup.common"})
public class ContentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentServiceApplication.class, args);
    }
}
