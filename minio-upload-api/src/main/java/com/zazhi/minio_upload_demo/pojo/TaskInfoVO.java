package com.zazhi.minio_upload_demo.pojo;

import io.minio.messages.Part;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author zazhi
 * @date 2025/3/25
 * @description: 上传任务信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskInfoVO {
    Integer id;
    String uploadId; // 上传的任务 id
    String fileIdentifier; // 文件标识
    String fileName; // 文件名
    String bucketName; // 存储桶名
    String objectName; // 对象名(minio中存储的实际文件名, 例如: abcxxx.jpg)
    private Long totalSize; //文件大小（byte）
    private Long chunkSize; //每个分片大小（byte）
    private Integer chunkNum; //分片数量

    private boolean finished;
    private String fileUrl;
    private List<Part> parts;
}
