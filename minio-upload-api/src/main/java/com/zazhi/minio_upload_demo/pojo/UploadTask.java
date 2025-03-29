package com.zazhi.minio_upload_demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zazhi
 * @date 2025/3/25
 * @description: 上传任务
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadTask {
    Integer id;
    String uploadId; // 上传的任务 id
    String fileIdentifier; // 文件标识
    String fileName; // 文件名
    String bucketName; // 存储桶名
    String objectName; // 对象名(minio中存储的实际文件名, 例如: abcxxx.jpg)
    private Long totalSize; //文件大小（byte）
    private Long chunkSize; //每个分片大小（byte）
    private Integer chunkNum; //分片数量
}
