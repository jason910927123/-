import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    // build: {
    //     outDir: "../src/main/resources/static"
    // },
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src'),
            '@components': path.resolve(__dirname, 'src/Components'),
            '@pages': path.resolve(__dirname, 'src/Pages'),
            '@css': path.resolve(__dirname, 'src/css'),
        },
    },
    server: {
        port: 5173,  // 自定義開發伺服器的端口
        proxy: {
            '/api': {
                target: 'http://localhost:8080/api',  // 指定後端伺服器的完整URL，包括端口號
                changeOrigin: true,
                secure: false,  // 忽略 SSL 證書問題（如果是 HTTPS）
                rewrite: (path) => path.replace(/^\/api/, '')  // 可選：如果後端不需要 `/api` 前綴，可以移除它
            },
        },
    },
})
