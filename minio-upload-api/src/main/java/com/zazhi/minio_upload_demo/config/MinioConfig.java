package com.zazhi.minio_upload_demo.config;

import com.zazhi.minio_upload_demo.minio.MinioConfigProperties;
import com.zazhi.minio_upload_demo.minio.PearlMinioClient;
import io.minio.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Autowired
    private MinioConfigProperties prop;

    // 异步客户端 分片上传用到这个
    @Bean
    public PearlMinioClient pearlMinioClient() {
        return new PearlMinioClient(
                MinioAsyncClient.builder()
                .endpoint(prop.getEndpoint())
                .credentials(prop.getAccessKey(), prop.getAccessSecret())
                .build()
        );
    }

    @Bean
    public MinioClient minioClient() {

        MinioClient minioClient = MinioClient.builder()
                .endpoint(prop.getEndpoint())
                .credentials(prop.getAccessKey(), prop.getAccessSecret())
                .build();

        // 检查存储桶是否存在，不存在则创建
        try {
            Boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(prop.getBucketName())
                            .build()
            );
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(prop.getBucketName())
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("minio检查或初始化失败");
        }
        return minioClient;
    }
}
