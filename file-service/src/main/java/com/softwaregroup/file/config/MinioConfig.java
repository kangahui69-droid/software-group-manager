package com.softwaregroup.file.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * MinIO 对象存储配置
 */
@Configuration
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    @Value("${spring.minio.endpoint}")
    private String endpoint;

    @Value("${spring.minio.access-key}")
    private String accessKey;

    @Value("${spring.minio.secret-key}")
    private String secretKey;

    @Value("${spring.minio.bucket}")
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 启动时检查并创建桶（如果不存在）
     */
    @PostConstruct
    public void initBucket() {
        try {
            MinioClient client = minioClient();
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );
            if (!exists) {
                client.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build()
                );
                log.info("MinIO bucket '{}' created successfully", bucket);
            } else {
                log.info("MinIO bucket '{}' already exists", bucket);
            }
        } catch (Exception e) {
            log.warn("Failed to check/create MinIO bucket: {}. MinIO may not be ready yet.", e.getMessage());
        }
    }
}
