import { fileURLToPath, URL } from 'node:url';

import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
// import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  assetsInclude: ['**/*.md'],
  plugins: [
    vue(),
    // vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    // host: '127.0.0.1',
    port: 5173,
    allowedHosts: ['all'],
    proxy: {
      // 이미지 경로 - Nginx 정적 서빙
      '/images': {
        target: 'http://127.0.0.1:80',
        changeOrigin: true,
      },
      // /api로 시작하는 요청은 백엔드 서버(8080)로 전달
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      }
    },
  },
});
