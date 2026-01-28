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
      // 이미지 경로로 시작하는 요청을 Nginx(80포트)로 보냄
      '/restaurant': {
        target: 'http://127.0.0.1:80/images', // /images를 붙여서 Nginx의 alias와 맞춤
        changeOrigin: true,
      },
      // 2. 메뉴 이미지 (추가)
      '/menus': {
        target: 'http://127.0.0.1:80/images',
        changeOrigin: true,
      },
      // 3. 리뷰 이미지 (추가)
      '/reviews': {
        target: 'http://127.0.0.1:80/images',
        changeOrigin: true,
      },
      // /api로 시작하는 요청은 백엔드 서버(8080)로 전달
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, '') // 필요시 경로 수정
      }
    },
    },
});
