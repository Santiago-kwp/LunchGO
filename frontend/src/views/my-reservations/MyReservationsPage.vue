<script setup>
import { ref, computed, onMounted } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { ArrowLeft } from 'lucide-vue-next';

// 분리한 자식 컴포넌트 임포트
import ReservationHistory from '@/components/ui/ReservationHistory.vue'; // 예정된 예약 목록
import UsageHistory from '@/components/ui/UsageHistory.vue'; // 지난 예약(이용완료/환불) 목록

const route = useRoute();

// 탭 상태 관리 ('upcoming' | 'past')
const activeTab = ref('upcoming');

// URL 쿼리에 따라 초기 탭 설정 (예: ?tab=past)
onMounted(() => {
  //식당 정보 불러와야함
  if (route.query.tab === 'past') {
    activeTab.value = 'past';
  }
});

// 즐겨찾기 목록 상태 관리 (부모에서 관리하여 자식에게 전달)
const favorites = ref([1]);

const toggleFavorite = (restaurantId) => {
  const index = favorites.value.indexOf(restaurantId);
  if (index > -1) {
    favorites.value.splice(index, 1);
  } else {
    favorites.value.push(restaurantId);
  }
};

// 통합 예약 데이터
const allReservations = ref([
  // 1. 예정된 예약 (ReservationHistory로 감)
  {
    id: 1,
    confirmationNumber: '123123123123',
    restaurant: {
      id: 1,
      name: '식당명',
      address: '서울시 강남구 테헤란로 132',
    },
    booking: { date: '2025년 12월 15일 (금)', time: '18:00', partySize: 4 },
    reservationStatus: 'confirmed', // 예약확정
    status: 'upcoming', // [중요] upcoming 탭으로 분류
  },
  {
    id: 2,
    confirmationNumber: '12312312312123',
    restaurant: {
      id: 1,
      name: '식당명',
      address: '서울시 강남구 테헤란로 132',
    },
    booking: { date: '2025년 12월 15일 (금)', time: '18:00', partySize: 4 },
    reservationStatus: 'pending_payment', // 결제대기
    status: 'upcoming', // [중요] upcoming 탭으로 분류
  },

  // 2. 지난 예약 + 환불 관련 (UsageHistory로 감)
  {
    id: 3,
    confirmationNumber: 'LG2024111500001',
    restaurant: {
      id: 1,
      name: '식당명',
      address: '서울시 강남구 테헤란로 132',
    },
    booking: { date: '2024년 11월 15일 (금)', time: '11:00', partySize: 4 },
    visitCount: 2,
    daysSinceLastVisit: 70,
    payment: { amount: 85000 },
    reservationStatus: 'completed', // 이용완료
    status: 'past', // [중요] past 탭으로 분류
    review: {
      id: 'review-1',
      rating: 5,
      content: '회식하기 정말 좋았어요...',
      tags: ['인테리어가 세련돼요', '재료가 신선해요'],
      createdAt: '2024.11.16',
    },
  },
  {
    id: 4,
    confirmationNumber: 'LG2024111500002',
    restaurant: {
      id: 1,
      name: '식당명',
      address: '서울시 강남구 테헤란로 132',
    },
    booking: { date: '2024년 11월 15일 (금)', time: '11:00', partySize: 4 },
    visitCount: 1,
    daysSinceLastVisit: null,
    payment: { amount: 85000 },
    reservationStatus: 'refund_pending', // 환불대기 -> UsageHistory
    status: 'past', // [중요] past 탭으로 분류
    review: null,
  },
  {
    id: 5,
    confirmationNumber: 'LG2024111500003',
    restaurant: {
      id: 1,
      name: '식당명',
      address: '서울시 강남구 테헤란로 132',
    },
    booking: { date: '2024년 11월 10일 (금)', time: '11:00', partySize: 2 },
    visitCount: 3,
    daysSinceLastVisit: 30,
    payment: { amount: 125000 },
    reservationStatus: 'refunded', // 환불완료 -> UsageHistory
    status: 'past', // [중요] past 탭으로 분류
    review: {
      id: 'review-2',
      rating: 4,
      content: '가격 대비 훌륭한 퀄리티입니다.',
      tags: ['법카 쓰기 좋은 가격대에요'],
      createdAt: '2024.11.11',
    },
  },
]);

// 필터링: status가 'upcoming'인 것만 추출
const upcomingReservations = computed(() =>
  allReservations.value.filter((r) => r.status === 'upcoming')
);

// 필터링: status가 'past'인 것만 추출 (이용완료, 환불대기, 환불완료 포함)
const pastReservations = computed(() =>
  allReservations.value.filter((r) => r.status === 'past')
);
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink to="/" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">예약 내역</h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-5">
      <div class="bg-white border-b border-[#e9ecef] sticky top-14 z-40">
        <div class="flex">
          <button
            @click="activeTab = 'upcoming'"
            :class="[
              'flex-1 py-3 text-sm font-medium transition-colors relative',
              activeTab === 'upcoming'
                ? 'text-[#1e3a5f] font-semibold'
                : 'text-[#6c757d]',
            ]"
          >
            예약 내역
            <div
              v-if="activeTab === 'upcoming'"
              class="absolute bottom-0 left-0 right-0 h-0.5 bg-[#1e3a5f]"
            ></div>
          </button>
          <button
            @click="activeTab = 'past'"
            :class="[
              'flex-1 py-3 text-sm font-medium transition-colors relative',
              activeTab === 'past'
                ? 'text-[#1e3a5f] font-semibold'
                : 'text-[#6c757d]',
            ]"
          >
            지난 예약
            <div
              v-if="activeTab === 'past'"
              class="absolute bottom-0 left-0 right-0 h-0.5 bg-[#1e3a5f]"
            ></div>
          </button>
        </div>
      </div>

      <div class="px-4 pt-5">
        <ReservationHistory
          v-show="activeTab === 'upcoming'"
          :reservations="upcomingReservations"
        />

        <UsageHistory
          v-show="activeTab === 'past'"
          :reservations="pastReservations"
          :favorites="favorites"
          @toggle-favorite="toggleFavorite"
        />
      </div>
    </main>
  </div>
</template>
