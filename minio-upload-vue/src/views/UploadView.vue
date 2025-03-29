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
import axios from 'axios';

export default {
    methods: {
        async handleHttpRequest(options) {
            // 查询文件有无对应上传任务
            const file = options.file;
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
            // console.log(task);
            // 处理上传
            if (task) {
                // 若上传任务存在且已经完成，直接返回
                const { finished, fileUrl, fileIdentifier, chunkNum, chunkSize, parts} = task;
                if (finished) {
                    this.$message.success('上传完成')
                    return fileUrl
                }
                // 上传每一个分片
                for (let partNum = 1; partNum <= chunkNum; partNum++) {
                    console.log(`正在上传分片：${partNum}/${chunkNum}`);
                    // 若分片已上传，则跳过；「实现断点续传的关键」
                    const part = parts.find(part => part.partNumber === partNum);
                    if (part) {
                        continue;
                    }
                    // TODO: 上传完分片更新进度，实现进度条
                    // 预上传地址
                    const { data } = await getPresignedObjectUrl(fileIdentifier, partNum);
                    const presignedUrl = data.data;
                    // 切片
                    const start = new Number(chunkSize) * (partNum - 1)
                    const end = start + new Number(chunkSize)
                    const blob = file.slice(start, end)
                    if (presignedUrl) {
                        try {
                            await axios.request(
                                {
                                    url: presignedUrl,
                                    method: 'PUT',
                                    data: blob,
                                    headers: { 'Content-Type': 'application/octet-stream' }
                                }
                            )
                        } catch (error) {
                            this.$message.error(`上传分片出错，分片编号:${partNum}`)
                        }
                    } else {
                        this.$message.error("获取上传地址出错")
                    }
                }
            } else {
                this.$message.error("文件上传错误")
            }

            try {
                await merge(identifier)
                this.$message.success('上传完成')
            } catch (error) {
                this.$message.error("合并文件分片出错")
            }
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