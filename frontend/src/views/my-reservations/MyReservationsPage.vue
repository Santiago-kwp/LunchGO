<script setup>
import { ref, computed } from 'vue';
import {
  Calendar,
  Clock,
  Users,
  MapPin,
  ArrowLeft,
  Star,
} from 'lucide-vue-next';
import { RouterLink } from 'vue-router';
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';

// 탭 상태 관리
const activeTab = ref('upcoming'); // 'upcoming' 또는 'past'

// 즐겨찾기 목록 (식당 ID 배열)
const favorites = ref([1]); // 예시: 식당 ID 1은 이미 즐겨찾기됨

// 즐겨찾기 토글 함수
const toggleFavorite = (restaurantId) => {
  const index = favorites.value.indexOf(restaurantId);
  if (index > -1) {
    // 이미 즐겨찾기된 경우 제거
    favorites.value.splice(index, 1);
  } else {
    // 즐겨찾기 추가
    favorites.value.push(restaurantId);
  }
};

// 즐겨찾기 여부 확인
const isFavorite = (restaurantId) => {
  return favorites.value.includes(restaurantId);
};

// 예약 데이터
const allReservations = ref([
  // 예정된 예약
  {
    id: 1,
    confirmationNumber: '123123123123',
    restaurant: {
      id: 1,
      name: '식당명',
      address: '서울시 강남구 테헤란로 132',
    },
    booking: {
      date: '2025년 12월 15일 (금)',
      time: '18:00',
      partySize: 4,
    },
    reservationStatus: 'confirmed', // 'pending_payment', 'confirmed'
    status: 'upcoming',
  },
  {
    id: 2,
    confirmationNumber: '12312312312123',
    restaurant: {
      id: 1,
      name: '식당명',
      address: '서울시 강남구 테헤란로 132',
    },
    booking: {
      date: '2025년 12월 15일 (금)',
      time: '18:00',
      partySize: 4,
    },
    reservationStatus: 'pending_payment', // 'pending_payment', 'confirmed'
    status: 'upcoming',
  },
  // 지난 예약
  {
    id: 3,
    confirmationNumber: 'LG2024111500001',
    restaurant: {
      id: 1,
      name: '식당명',
      address: '서울시 강남구 테헤란로 132',
    },
    booking: {
      date: '2024년 11월 15일 (금)',
      time: '11:00',
      partySize: 4,
    },
    visitCount: 2, // n번째 방문
    daysSinceLastVisit: 70, // 마지막 방문 후 며칠만에 재방문
    payment: {
      amount: 85000,
    },
    reservationStatus: 'completed', // 'completed', 'refund_pending', 'refunded'
    status: 'past',
  },
  {
    id: 4,
    confirmationNumber: 'LG2024111500002',
    restaurant: {
      id: 1,
      name: '식당명',
      address: '서울시 강남구 테헤란로 132',
    },
    booking: {
      date: '2024년 11월 15일 (금)',
      time: '11:00',
      partySize: 4,
    },
    visitCount: 1, // 첫 방문
    daysSinceLastVisit: null,
    payment: {
      amount: 85000,
    },
    reservationStatus: 'refund_pending', // 'completed', 'refund_pending', 'refunded'
    status: 'past',
  },
  {
    id: 5,
    confirmationNumber: 'LG2024111500003',
    restaurant: {
      id: 1,
      name: '식당명',
      address: '서울시 강남구 테헤란로 132',
    },
    booking: {
      date: '2024년 11월 10일 (금)',
      time: '11:00',
      partySize: 2,
    },
    visitCount: 3,
    daysSinceLastVisit: 30,
    payment: {
      amount: 125000,
    },
    reservationStatus: 'refunded', // 'completed', 'refund_pending', 'refunded'
    status: 'past',
  },
]);

// 예약 상태별 정보 반환 함수
const getStatusInfo = (reservationStatus, isPast = false) => {
  if (isPast) {
    // 지난 예약 상태
    const statusMap = {
      completed: {
        text: '이용완료',
        bgColor: 'bg-emerald-50',
        textColor: 'text-emerald-600',
        borderColor: 'border-emerald-200',
      },
      refund_pending: {
        text: '환불대기',
        bgColor: 'bg-amber-50',
        textColor: 'text-amber-600',
        borderColor: 'border-amber-200',
      },
      refunded: {
        text: '환불완료',
        bgColor: 'bg-gray-50',
        textColor: 'text-gray-600',
        borderColor: 'border-gray-200',
      },
    };
    return statusMap[reservationStatus] || statusMap.completed;
  } else {
    // 예정된 예약 상태
    const statusMap = {
      pending_payment: {
        text: '결제대기',
        bgColor: 'bg-rose-50',
        textColor: 'text-rose-600',
        borderColor: 'border-rose-200',
      },
      confirmed: {
        text: '예약확정',
        bgColor: 'bg-blue-50',
        textColor: 'text-blue-600',
        borderColor: 'border-blue-200',
      },
    };
    return statusMap[reservationStatus] || statusMap.confirmed;
  }
};

// 방문 정보 텍스트 생성 함수
const getVisitInfoText = (visitCount, daysSinceLastVisit) => {
  if (visitCount === 1) {
    return '1번째 방문';
  } else if (daysSinceLastVisit) {
    return `${visitCount}번째, ${daysSinceLastVisit}일만의 방문`;
  } else {
    return `${visitCount}번째 방문`;
  }
};

// 예정된 예약 필터링
const upcomingReservations = computed(() =>
  allReservations.value.filter((r) => r.status === 'upcoming')
);

// 지난 예약 필터링
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
      <!-- 탭 네비게이션 -->
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

      <!-- 예정된 예약 -->
      <div v-show="activeTab === 'upcoming'" class="px-4 pt-5">
        <div v-if="upcomingReservations.length === 0" class="text-center py-12">
          <p class="text-[#6c757d] text-sm">예정된 예약이 없습니다.</p>
        </div>
        <div v-else class="space-y-3">
          <Card
            v-for="reservation in upcomingReservations"
            :key="reservation.id"
            class="overflow-hidden border-[#e9ecef] rounded-xl bg-white shadow-sm"
          >
            <div class="p-4">
              <div class="flex items-start justify-between mb-3">
                <div>
                  <h3 class="font-semibold text-[#1e3a5f] text-base mb-1">
                    {{ reservation.restaurant.name }}
                  </h3>
                  <p class="text-xs text-[#6c757d]">
                    예약번호: {{ reservation.confirmationNumber }}
                  </p>
                </div>
                <span
                  :class="[
                    'px-2.5 py-1 rounded-full border text-xs font-medium whitespace-nowrap',
                    getStatusInfo(reservation.reservationStatus, false).bgColor,
                    getStatusInfo(reservation.reservationStatus, false)
                      .textColor,
                    getStatusInfo(reservation.reservationStatus, false)
                      .borderColor,
                  ]"
                >
                  {{ getStatusInfo(reservation.reservationStatus, false).text }}
                </span>
              </div>

              <div class="space-y-2 mb-4">
                <div class="flex items-center gap-2 text-sm">
                  <Calendar class="w-4 h-4 text-[#6c757d]" />
                  <span class="text-[#495057]">{{
                    reservation.booking.date
                  }}</span>
                </div>
                <div class="flex items-center gap-2 text-sm">
                  <Clock class="w-4 h-4 text-[#6c757d]" />
                  <span class="text-[#495057]">{{
                    reservation.booking.time
                  }}</span>
                </div>
                <div class="flex items-center gap-2 text-sm">
                  <Users class="w-4 h-4 text-[#6c757d]" />
                  <span class="text-[#495057]"
                    >{{ reservation.booking.partySize }}명</span
                  >
                </div>
                <div class="flex items-start gap-2 text-sm">
                  <MapPin class="w-4 h-4 text-[#6c757d] mt-0.5 flex-shrink-0" />
                  <span class="text-[#495057] leading-relaxed">{{
                    reservation.restaurant.address
                  }}</span>
                </div>
              </div>

              <div class="flex gap-2">
                <RouterLink
                  :to="`/restaurant/${reservation.restaurant.id}/confirmation`"
                  class="flex-1"
                >
                  <Button
                    variant="outline"
                    class="w-full h-10 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg text-sm font-medium"
                  >
                    예약 상세
                  </Button>
                </RouterLink>
                <Button
                  variant="outline"
                  class="flex-1 h-10 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg text-sm font-medium"
                >
                  예약 취소
                </Button>
              </div>
            </div>
          </Card>
        </div>
      </div>

      <!-- 지난 예약 -->
      <div v-show="activeTab === 'past'" class="px-4 pt-5">
        <div v-if="pastReservations.length === 0" class="text-center py-12">
          <p class="text-[#6c757d] text-sm">지난 예약 내역이 없습니다.</p>
        </div>
        <div v-else class="space-y-3">
          <Card
            v-for="reservation in pastReservations"
            :key="reservation.id"
            class="overflow-hidden border-[#e9ecef] rounded-2xl bg-white shadow-sm"
          >
            <div class="p-4">
              <!-- 식당명 & 즐겨찾기 & 상태 -->
              <div class="flex items-start justify-between mb-3">
                <div class="flex items-center gap-2">
                  <h3 class="font-semibold text-[#1e3a5f] text-base">
                    {{ reservation.restaurant.name }}
                  </h3>
                  <button
                    @click="toggleFavorite(reservation.restaurant.id)"
                    class="transition-colors"
                  >
                    <Star
                      :class="[
                        'w-5 h-5',
                        isFavorite(reservation.restaurant.id)
                          ? 'fill-yellow-400 text-yellow-400'
                          : 'text-gray-300 hover:text-yellow-400',
                      ]"
                    />
                  </button>
                </div>
                <span
                  :class="[
                    'px-2.5 py-1 rounded-full border text-xs font-medium whitespace-nowrap',
                    getStatusInfo(reservation.reservationStatus, true).bgColor,
                    getStatusInfo(reservation.reservationStatus, true)
                      .textColor,
                    getStatusInfo(reservation.reservationStatus, true)
                      .borderColor,
                  ]"
                >
                  {{ getStatusInfo(reservation.reservationStatus, true).text }}
                </span>
              </div>

              <!-- 예약 정보 -->
              <div class="space-y-2 mb-3">
                <div class="flex items-start gap-2 text-sm">
                  <MapPin class="w-4 h-4 text-[#6c757d] mt-0.5 flex-shrink-0" />
                  <span class="text-[#495057] leading-relaxed">{{
                    reservation.restaurant.address
                  }}</span>
                </div>
                <div class="flex items-center justify-between text-sm">
                  <div class="flex items-center gap-2">
                    <Calendar class="w-4 h-4 text-[#6c757d]" />
                    <span class="text-[#495057]">{{
                      reservation.booking.date
                    }}</span>
                  </div>
                  <span class="text-[#495057] text-xs">
                    {{
                      getVisitInfoText(
                        reservation.visitCount,
                        reservation.daysSinceLastVisit
                      )
                    }}
                  </span>
                </div>
                <div class="flex items-center justify-between text-sm">
                  <div
                    v-if="reservation.booking.partySize"
                    class="flex items-center gap-2"
                  >
                    <Users class="w-4 h-4 text-[#6c757d]" />
                    <span class="text-[#495057]"
                      >{{ reservation.booking.partySize }}명</span
                    >
                  </div>
                  <div v-if="reservation.payment" class="text-[#495057]">
                    {{ reservation.payment.amount.toLocaleString() }} 원
                  </div>
                </div>
              </div>

              <!-- 버튼 그룹 -->
              <div class="flex gap-2">
                <RouterLink
                  :to="`/restaurant/${reservation.restaurant.id}`"
                  class="flex-1"
                >
                  <Button
                    variant="outline"
                    class="w-full h-10 border-blue-200 text-blue-600 bg-white hover:bg-blue-50 rounded-lg text-sm font-medium"
                  >
                    다시 예약
                  </Button>
                </RouterLink>
                <RouterLink
                  :to="`/restaurant/${reservation.restaurant.id}/confirmation`"
                  class="flex-1"
                >
                  <Button
                    variant="outline"
                    class="w-full h-10 border-gray-300 text-gray-700 bg-white hover:bg-gray-50 rounded-lg text-sm font-medium"
                  >
                    예약 내역
                  </Button>
                </RouterLink>
              </div>
              <div class="mt-2">
                <RouterLink
                  :to="`/restaurant/${reservation.restaurant.id}/reviews/write`"
                  class="block"
                >
                  <Button
                    variant="outline"
                    class="w-full h-10 border-orange-200 text-orange-600 bg-white hover:bg-orange-50 rounded-lg text-sm font-medium"
                  >
                    리뷰 쓰기
                  </Button>
                </RouterLink>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
