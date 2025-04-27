import axios from "axios";

const _axios = axios.create({
    baseURL: "http://localhost:8080",
})

// 比如在这里添加统一的 headers
_axios.interceptors.request.use(
    function(config) {
        
        config.headers = {
            Authorization: "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJ6YXpoaSIsImV4cCI6MTc0NDg5MzY3Mn0.PolzfAZTPwbP_Rh2PBigkV6kDG3_wT_tLxOq4rDD9j8"
        }
        return config;
    });

export default _axios