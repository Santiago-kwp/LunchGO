<script setup>
import { RouterLink } from 'vue-router';
import { computed } from 'vue';
import { useRestaurantStore } from '@/stores/restaurant';

// 'activeMenu' 라는 이름의 prop을 정의합니다.
defineProps({
  activeMenu: {
    type: String,
    required: true,
  },
});

const restaurantStore = useRestaurantStore();
const restaurantId = computed(
  () => restaurantStore.restaurantInfo?.restaurantId || 1
); // 스토어에 ID가 없을 경우 임시로 1을 사용
</script>

<template>
  <aside class="w-64 bg-white border-r border-[#e9ecef] flex flex-col">
    <div class="px-6 py-5 border-b border-[#e9ecef]">
      <RouterLink to="/business/dashboard">
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
            to="/business/dashboard"
            :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'dashboard'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            오늘의 예약 현황
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/business/reservations"
            :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'reservations'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            전체 예약 관리
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/business/notifications"
            :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'notifications'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            알림 내역
          </RouterLink>
        </li>
        <li>
          <RouterLink
            :to="`/business/restaurant-info/${restaurantId}`"
            :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'restaurant-info'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            식당 정보
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/business/staff"
            :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'staff'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            임직원 현황
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/business/reviews"
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
            :to="`/business/promotion`"
            :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'business-promotion'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            프로모션 관리
          </RouterLink>
        </li>
        <li>
          <RouterLink
            :to="`/business/mypage`"
            :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'business-mypage'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            마이페이지
          </RouterLink>
        </li>
      </ul>
    </nav>
  </aside>
</template>
