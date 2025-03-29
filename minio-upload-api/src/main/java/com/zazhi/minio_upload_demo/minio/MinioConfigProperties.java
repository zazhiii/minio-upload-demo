package com.zazhi.minio_upload_demo.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zazhi
 * @date 2025/3/20
 * @description: Minio配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfigProperties {
    private String endpoint;
    private String accessKey;
    private String accessSecret;
    private String bucketName;
}
