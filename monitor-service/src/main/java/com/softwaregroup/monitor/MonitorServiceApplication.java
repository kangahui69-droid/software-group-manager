package com.softwaregroup.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 监控服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.softwaregroup.monitor")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.softwaregroup.monitor.feign")
public class MonitorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorServiceApplication.class, args);
    }
}
