package com.softwaregroup.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 文件服务启动类
 *
 * 功能：
 * - 文件上传下载（MinIO对象存储）
 * - 文件元数据管理（MySQL）
 * - 提供Feign接口供其他服务调用
 *
 * 端口：8081
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.softwaregroup.file.feign")
@ComponentScan(basePackages = {"com.softwaregroup.file", "com.softwaregroup.common"})
public class FileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }
}
