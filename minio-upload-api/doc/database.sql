-- 创建数据库
CREATE DATABASE IF NOT EXISTS `minio_upload_demo`
DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE minio_upload_demo;

-- 创建表
DROP TABLE IF EXISTS `upload_task`;
-- auto-generated definition
create table upload_task
(
    id              int auto_increment primary key,
    upload_id       varchar(255) not null comment '分片上传任务id',
    file_identifier varchar(500) not null comment '文件唯一标识（md5）',
    file_name       varchar(500) not null comment '文件名称',
    bucket_name     varchar(500) not null comment '桶名称',
    object_name     varchar(500) not null comment '对象名称，minio中文件实际名称（e.g. abcxxx.png）',
    total_size      mediumtext   not null comment '文件大小（byte）',
    chunk_size      mediumtext   not null comment '每个分片大小（byte）',
    chunk_num       int          null comment '分片数量'
) comment '分片上传任务';

