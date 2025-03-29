package com.zazhi.minio_upload_demo.controller;

import com.zazhi.minio_upload_demo.pojo.Result;
import com.zazhi.minio_upload_demo.pojo.TaskInfoVO;
import com.zazhi.minio_upload_demo.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zazhi
 * @date 2025/3/25
 * @description: TODO
 */
@RestController
@RequestMapping("api/upload")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    /**
     * 初始化上传任务
     * @param identifier
     * @param totalSize
     * @param chunkSize
     * @param fileName
     * @return
     */
    @PostMapping
    public Result<TaskInfoVO> initTask(String identifier,
                                       Long totalSize,
                                       Long chunkSize,
                                       String fileName){
        return Result.success(uploadService.initTask(identifier, totalSize, chunkSize, fileName));
    }

    /**
     * 获取预签名URL
     * 前端通过该URL上传分片文件
     * ^这种方案也有一个问题, minio 服务地址暴露在前端, 有安全隐患
     * @param identifier
     * @param partNumber
     * @return
     */
    @GetMapping("/{identifier}/{partNumber}")
    public Result<String> getPresignedObjectUrl(@PathVariable("identifier") String identifier,
                                                @PathVariable("partNumber") Integer partNumber){
        return Result.success(uploadService.getPresignedObjectUrl(identifier, partNumber));
    }

    /**
     * 获取上传进度
     * @param identifier 文件md5
     * @return
     */
    @GetMapping("/{identifier}")
    public Result<TaskInfoVO> taskInfo (@PathVariable("identifier") String identifier) {
        return Result.success(uploadService.getTaskInfo(identifier));
    }

    /**
     * 合并分片
     * @param identifier
     * @return
     */
    @PostMapping("/merge/{identifier}")
    public Result<String> merge(@PathVariable("identifier") String identifier) {
        uploadService.merge(identifier);
        return Result.success();
    }
}
