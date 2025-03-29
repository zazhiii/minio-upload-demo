import _axios from "@/utils/_axios";



const initTask = (identifier, totalSize, chunkSize, fileName) => {
    return _axios.post("api/upload", {}, {
        params: {
            identifier,
            totalSize,
            chunkSize,
            fileName
        }
    });
}

const getPresignedObjectUrl = (identifier, partNumber) => {
    return _axios.get(`api/upload/${identifier}/${partNumber}`)
}

const getTaskInfo = (identifier) => {
    return _axios.get(`api/upload/${identifier}`)
}

const merge = (identifier) => {
    return _axios.post(`api/upload/merge/${identifier}`)
}

export{
    initTask,
    getPresignedObjectUrl,
    getTaskInfo,
    merge
}