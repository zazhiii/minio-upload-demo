package com.zazhi.minio_upload_demo.minio;

import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zazhi
 * @date 2025/3/29
 * @description: minio 工具类
 */
@Component
public class MinioUtil {
    @Autowired
    private MinioClient minioClient;

    /**
     * 判断文件是否存在
     *
     * @param bucketName 桶名称
     * @param objectName 文件名称, 如果要带文件夹请用 / 分割, 例如 /help/index.html
     * @return true存在, 反之
     */
    public Boolean checkFileIsExist(String bucketName, String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(objectName).build()
            );
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
