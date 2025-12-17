<script setup>
import { RouterLink } from 'vue-router';
</script>

<template>
  <footer class="bg-white border-t border-[#e9ecef] px-4 py-6 mt-8">
    <div class="text-xs text-[#6c757d] space-y-1 leading-relaxed">
      <p class="font-semibold text-[#1e3a5f]">(주) 런치고</p>
      <p>주소 : 경기도 성남시 분당구 대왕판교로 ~~</p>
      <p>
        서비스 이용 약관 | 개인정보 처리방침 | 위치정보 이용약관 | 환불 정책 |
        <RouterLink to="/partner" class="text-[#FF6B4A] hover:underline">
          입점 문의
        </RouterLink>
      </p>
    </div>
  </footer>
</template>

<style scoped></style>
