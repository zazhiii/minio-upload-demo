<template>
    <div class="app">
        <el-upload class="upload-demo" drag action="https://jsonplaceholder.typicode.com/posts/" multiple
            :http-request=handleHttpRequest>
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        </el-upload>
    </div>
</template>

<script>
import md5 from '../utils/md5';
import {
    initTask,
    getTaskInfo,
    getPresignedObjectUrl,
    merge,
} from '../api/upload';
import { uploadSlice } from '../api/upload';

export default {
    data() {
        return {
            // progressMap: {} 
        }
    },
    methods: {
        async handleHttpRequest(options) {
            this.$message.info('正在上传中，请耐心等待^_^');
            this.upldateProgress(options, 0)
            // 查询文件有无对应上传任务
            const file = options.file
            const identifier = await md5(file);
            const { data } = await getTaskInfo(identifier);
            let task = data.data;

            // 没有任务则新建任务
            if (!task) {
                const { data } = await initTask(identifier, file.size, 5 * 1024 * 1024, file.name);
                if (data.data) {
                    task = data.data;
                } else {
                    this.$message.error('创建上传任务失败');
                }
            }
            // 处理上传
            if (task) {
                // 若上传任务存在且已经完成，直接返回
                const { finished, fileUrl, fileIdentifier, chunkNum, chunkSize, parts } = task;
                if (finished) {
                    return fileUrl
                }
                // 上传每一个分片
                await this.handleUpload(file, chunkNum, chunkSize, fileIdentifier, parts, options)
            } else {
                this.$message.error("文件上传错误")
            }

            // TODO： 分片上传数量对不上的话，提示重新上传

            try {
                await merge(identifier)
                this.$message.success('上传完成')
            } catch (error) {
                this.$message.error("合并文件分片出错")
            }
        },
        async handleUpload(file, chunkNum, chunkSize, fileIdentifier, parts, options) {
            let uploadNum = 0 // 已经上传完成的分片数量
            for (let partNum = 1; partNum <= chunkNum; partNum++) {
                console.log(`正在上传分片：${partNum}/${chunkNum}`);
                // 若分片已上传，则跳过；「实现断点续传的关键」
                const part = parts.find(part => part.partNumber === partNum);
                if (part) {
                    uploadNum++
                    this.upldateProgress(options, Math.floor(uploadNum / chunkNum * 100)) // 更新上传进度
                    continue
                }
                // 预上传地址
                const { data } = await getPresignedObjectUrl(fileIdentifier, partNum);
                const presignedUrl = data.data;
                // 切片
                const start = Number(chunkSize) * (partNum - 1)
                const end = start + Number(chunkSize)
                const slice = file.slice(start, end)
                if (presignedUrl) {
                    try {
                        await uploadSlice(slice, presignedUrl)
                        uploadNum++
                        this.upldateProgress(options, Math.floor(uploadNum / chunkNum * 100)) // 更新上传进度
                    } catch (error) {
                        this.$message.error(`上传分片出错，分片编号:${partNum}`)
                    }
                } else {
                    this.$message.error("获取上传地址出错")
                }
            }
        },
        upldateProgress(options, percent) {
            const { onProgress } = options
            onProgress({ percent })
        }
    }
}
</script>

<style scoped>
.app {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
}
</style>