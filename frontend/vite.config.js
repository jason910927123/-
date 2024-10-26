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
        // port: 5173,  // 如果有需要，你可以啟用這個選項來指定開發伺服器的端口
        proxy: {
            '/api': {
                target: 'http://dev.rx-bear.work/',
                changeOrigin: true,
                secure: false,  // 如果目標是 HTTPS，但證書無效，這將忽略證書問題
            },
        },
        historyApiFallback: true,  // 確保前端路由正確處理
    },
})
