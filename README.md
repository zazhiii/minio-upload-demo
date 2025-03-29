# MinIO-Upload-Demo
## 介绍
前端：Vue2, Element-UI \
后端: SpringBoot3.1.3, Mybatis \
存储服务：MinIO \
注意：后端的minio依赖版本是8.5.17，不同的版本api有差异，主要是学习实现思路\
## 后端部署
1. 数据库表初始化\
   运行 minio-upload-api/doc/database.sql 中的SQL \
2. 修改配置 \
   修改 Application.yml 中数据库和MinIO的配置为你的配置 \
   注意：确保配置的桶已经在MinIO中创建
3. 运行SpringBoot
## 前端部署
```cmd
cd minio-upload-vue // 进入前端项目的路径

npm install // 安装依赖

npm run serve // 启动服务

```
访问`localhost:7070`

## 参考资料：
https://blog.csdn.net/eagles_on/article/details/132104188 \
https://gitee.com/Gary2016/minio-upload

## TODO
- 增加上传进度条
- 实现上传重试
