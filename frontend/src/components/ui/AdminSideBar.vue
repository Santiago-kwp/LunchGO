<script setup>
import { RouterLink } from 'vue-router';

// 'activeMenu' 라는 이름의 prop을 정의합니다.
defineProps({
  activeMenu: {
    type: String,
    required: true,
  },
});
</script>

<template>
  <aside class="w-64 bg-white border-r border-[#e9ecef] flex flex-col">
    <div class="p-6 border-b border-[#e9ecef]">
      <RouterLink to="/admin/dashboard">
        <img
          src="/images/lunch-go-whitebg.png"
          alt="LunchGo"
          class="w-auto h-12"
          width="150"
          height="60"
        />
      </RouterLink>
    </div>

    <nav class="flex-1 p-4">
      <ul class="space-y-2">
        <li>
          <RouterLink
            to="/admin/dashboard"
            :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'dashboard'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            대시보드
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/admin/reservations"
            :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'reservations'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            예약 등록 현황
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/admin/franchises"
            :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'franchises'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            가맹점 관리
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/admin/reviews"
            :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'reviews'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            리뷰 관리
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/admin/members"
            :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'members'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            회원 관리
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/admin/finance"
            :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'finance'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            재무 관리
          </RouterLink>
        </li>
      </ul>
    </nav>
  </aside>
</template>
