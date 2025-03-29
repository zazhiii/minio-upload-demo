package com.zazhi.minio_upload_demo.service.impl;

import com.zazhi.minio_upload_demo.mapper.UploadMapper;
import com.zazhi.minio_upload_demo.minio.MinioConfigProperties;
import com.zazhi.minio_upload_demo.minio.MinioUtil;
import com.zazhi.minio_upload_demo.minio.PearlMinioClient;
import com.zazhi.minio_upload_demo.pojo.TaskInfoVO;
import com.zazhi.minio_upload_demo.pojo.UploadTask;
import com.zazhi.minio_upload_demo.service.UploadService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import io.minio.messages.Part;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author zazhi
 * @date 2025/3/25
 * @description: TODO
 */
@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private PearlMinioClient pearlMinioClient;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfigProperties minioConfigProp;

    @Autowired
    private UploadMapper uploadMapper;

    @Autowired
    private MinioUtil minioUtil;

    /**
     * 初始化上传任务
     * @param identifier 任务标识
     * @param totalSize 文件总大小
     * @param chunkSize 分片大小
     * @param fileName 文件名
     */
    @Override
    public TaskInfoVO initTask(String identifier, Long totalSize, Long chunkSize, String fileName) {
        // 构造 objectName: 日期/uuid.后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String objectName = currentDate + "/" + UUID.randomUUID() + "." + suffix;
        // 创建分片上传任务、获取 uploadId
        String uploadId = null;
        try {
            uploadId = pearlMinioClient.createMultipartUploadAsync(
                            minioConfigProp.getBucketName(),
                            null,
                            objectName,
                            null,
                            null).get().result().uploadId();
        } catch (Exception e) {
            throw new RuntimeException("创建分片上传失败");
        }
        // 创建 uploadTask 记录、存入数据库
        UploadTask uploadTask = UploadTask.builder()
                .uploadId(uploadId)
                .fileIdentifier(identifier)
                .fileName(fileName)
                .objectName(objectName)
                .totalSize(totalSize)
                .chunkSize(chunkSize)
                .chunkNum((int)((totalSize + chunkSize - 1) / chunkSize)) // 计算分片数量 上取整
                .bucketName(minioConfigProp.getBucketName())
                .build();
        uploadMapper.insert(uploadTask);

        // 返回 taskInfoVO
        TaskInfoVO taskInfoVO = new TaskInfoVO();
        BeanUtils.copyProperties(uploadTask, taskInfoVO);
        taskInfoVO.setFinished(false);
        taskInfoVO.setParts(new ArrayList<>());
        taskInfoVO.setFileUrl(minioConfigProp.getEndpoint() + "/" + minioConfigProp.getBucketName() + "/" + objectName);
        return taskInfoVO;
    }

    /**
     * 获取预签名URL
     * @param identifier
     * @param partNumber
     * @return
     */
    @Override
    public String getPresignedObjectUrl(String identifier, Integer partNumber){
        UploadTask uploadTask = uploadMapper.getByIdentifier(identifier);
        if(uploadTask == null){
            throw new RuntimeException("上传任务不存在");
        }
        Map<String, String> params = new HashMap<>();
        params.put("partNumber", String.valueOf(partNumber));
        params.put("uploadId", uploadTask.getUploadId());
        String presignedObjectUrl = null;
        try {
            presignedObjectUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minioConfigProp.getBucketName())
                            .object(uploadTask.getObjectName())
                            .method(Method.PUT)
                            .expiry(1, TimeUnit.DAYS)
                            .extraQueryParams(params)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("获取预签名URL失败");
        }
        return presignedObjectUrl;
    }

    /**
     * 获取上传进度
     * @param identifier 文件md5
     * @return
     */
    @Override
    public TaskInfoVO getTaskInfo(String identifier) {
        UploadTask uploadTask = uploadMapper.getByIdentifier(identifier);
        if(uploadTask == null){
            return null; // 任务不存在, 返回null, 前端得知任务不存在, 调用初始化接口
        }
        TaskInfoVO taskInfoVO = new TaskInfoVO();
        BeanUtils.copyProperties(uploadTask, taskInfoVO);
        // 已经完成上传;「秒传」的实现就在这一步文件已经存在则直接返回
        if(minioUtil.checkFileIsExist(minioConfigProp.getBucketName(), uploadTask.getObjectName())){
            taskInfoVO.setFinished(true);
            return taskInfoVO;
        }
        // 没有完成上传, 则携带上传完成的分片集合返回
        List<Part> parts = null;
        try {
            parts = pearlMinioClient.listMultipart(
                    uploadTask.getBucketName(),
                    null,
                    uploadTask.getObjectName(),
                    10000,
                    0,
                    uploadTask.getUploadId(),
                    null,
                    null
            ).get().result().partList();
        } catch (Exception e) {
            throw new RuntimeException("获取上传进度失败");
        }
        taskInfoVO.setFinished(false);
        taskInfoVO.setParts(parts);
        return taskInfoVO;
    }

    /**
     * 合并分片
     * @param identifier
     */
    @Override
    public void merge(String identifier) {
        UploadTask uploadTask = uploadMapper.getByIdentifier(identifier);
        if(uploadTask == null){
            throw new RuntimeException("上传任务不存在");
        }

        List<Part> partList = null;
        try {
            partList = pearlMinioClient.listMultipart(
                    uploadTask.getBucketName(),
                    null,
                    uploadTask.getObjectName(),
                    10000,
                    0,
                    uploadTask.getUploadId(),
                    null,
                    null
            ).get().result().partList();
        } catch (Exception e) {
            throw new RuntimeException("获取上传进度失败");
        }

        if(partList.size() != uploadTask.getChunkNum()){
            throw new RuntimeException("分片缺失，请重新上传");
        }

        // 合并分片
        Part[] parts = new Part[1000];
        for(int partNumber = 1; partNumber <= partList.size(); partNumber++){
            parts[partNumber - 1] = new Part(partNumber, partList.get(partNumber - 1).etag());
        }
        try {
            pearlMinioClient.completeMultipartUploadAsync(
                    uploadTask.getBucketName(),
                    null,
                    uploadTask.getObjectName(),
                    uploadTask.getUploadId(),
                    parts,
                    null,
                    null
            ).get();
        } catch (Exception e) {
            throw new RuntimeException("合并分片失败");
        }
    }
}
