package com.zazhi.minio_upload_demo.service;

import com.zazhi.minio_upload_demo.pojo.TaskInfoVO;

public interface UploadService {

    /**
     * 初始化上传任务
     * @param identifier 任务标识
     * @param totalSize 文件总大小
     * @param chunkSize 分片大小
     * @param fileName 文件名
     */
    TaskInfoVO initTask(String identifier, Long totalSize, Long chunkSize, String fileName);

    /**
     * 获取预签名URL
     * @param identifier
     * @param partNumber
     * @return
     */
    String getPresignedObjectUrl(String identifier, Integer partNumber);

    /**
     * 获取上传进度
     * @param identifier 文件md5
     * @return
     */
    TaskInfoVO getTaskInfo(String identifier);

    /**
     * 合并分片
     * @param identifier
     */
    void merge(String identifier);
}
