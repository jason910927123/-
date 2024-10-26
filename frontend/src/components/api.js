import axios from 'axios';

// 創建 Axios 實例
const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// 通用的 POST 請求方法
export const fetchData = async (data) => {
    try {
        const response = await api.post(`/moreopenai`, data);  // 基於 Axios 的 POST 請求
        return response.data;
    } catch (error) {
        console.error('Error:', error);
        throw error;
    }
};

export default api;
