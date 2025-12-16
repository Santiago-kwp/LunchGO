<script setup>
import { ref, computed } from 'vue';
import { Filter } from 'lucide-vue-next'; // Bell, User는 Header로 이동
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';

const selectedDate = ref(new Date().toISOString().split('T')[0]);

//필터 상태
const filterOpen = ref(false);
const statusFilter = ref('all'); // all | confirmed | pending | cancelled | refunded


const reservations = ref([
  {
    id: 1,
    name: '홍길동',
    phone: '010-1234-5678',
    date: '2025-12-25',
    time: '12:00',
    guests: 4,
    amount: 72000,
    status: 'confirmed',
    menus: ['확정', '대기'],
    orderType: '상세보기 / 확정 / 취소',
  },
  {
    id: 2,
    name: '김철수',
    phone: '010-2345-6789',
    date: '2025-12-25',
    time: '13:00',
    guests: 6,
    amount: 108000,
    status: 'confirmed',
    menus: ['확정'],
    orderType: '상세보기 / 확정 / 취소',
  },
  {
    id: 3,
    name: '이영희',
    phone: '010-3456-7890',
    date: '2025-12-25',
    time: '12:30',
    guests: 8,
    amount: 144000,
    status: 'pending',
    menus: ['대기'],
    orderType: '상세보기 / 확정 / 취소',
  },
  {
    id: 4,
    name: '박민수',
    phone: '010-4567-8901',
    date: '2025-12-25',
    time: '14:00',
    guests: 5,
    amount: 90000,
    status: 'confirmed',
    menus: ['확정'],
    orderType: '상세보기 / 확정 / 취소',
  },
  {
    id: 5,
    name: '정수진',
    phone: '010-5678-9012',
    date: '2025-12-25',
    time: '11:30',
    guests: 4,
    amount: 72000,
    status: 'pending',
    menus: ['대기'],
    orderType: '상세보기 / 확정 / 취소',
  },
  {
    id: 6,
    name: '최동훈',
    phone: '010-6789-0123',
    date: '2025-12-25',
    time: '13:30',
    guests: 7,
    amount: 126000,
    status: 'confirmed',
    menus: ['확정'],
    orderType: '상세보기 / 확정 / 취소',
  },
  {
    id: 7,
    name: '윤서연',
    phone: '010-7890-1234',
    date: '2025-12-25',
    time: '12:00',
    guests: 4,
    amount: 72000,
    status: 'cancelled',
    menus: ['취소'],
    orderType: '상세보기 / 확정 / 취소',
  },
]);

//필터 적용된 예약 리스트
const filteredReservations = computed(() => {
  if (statusFilter.value === 'all') return reservations.value;
  return reservations.value.filter((r) => r.status === statusFilter.value);
});

const stats = ref({
  total: reservations.value.length,
  confirmed: reservations.value.filter((r) => r.status === 'confirmed').length,
  pending: reservations.value.filter((r) => r.status === 'pending').length,
  cancelled: reservations.value.filter((r) => r.status === 'cancelled').length,
});

const salesStats = ref({
  totalSales: reservations.value
    .filter((r) => r.status === 'confirmed')
    .reduce((sum, r) => sum + r.amount, 0),
  totalReservations: reservations.value.filter((r) => r.status === 'confirmed')
    .length,
  averagePerPerson: 18000, // This value was hardcoded in React, keeping it for now
  totalGuests: reservations.value
    .filter((r) => r.status === 'confirmed')
    .reduce((sum, r) => sum + r.guests, 0),
});
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="dashboard" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-8">
          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">오늘의 예약 현황</h2>
            <div class="flex items-center gap-2">
              <span class="text-sm text-[#6c757d]">{{ selectedDate }}</span>
            </div>
          </div>

          <!-- Stats Cards -->
          <div class="grid grid-cols-4 gap-6">
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">전체 예약</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">
                {{ stats.total }}건
              </p>
            </div>
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">확정</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">
                {{ stats.confirmed }}건
              </p>
            </div>
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">대기</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">
                {{ stats.pending }}건
              </p>
            </div>
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">취소</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">
                {{ stats.cancelled }}건
              </p>
            </div>
          </div>

          <!-- Reservations Table -->
          <div
            class="bg-white rounded-xl border border-[#e9ecef] overflow-visible"
          >
            <div
              class="border-b border-[#e9ecef] p-6 flex items-center justify-between bg-[#f8f9fa]"
            >
              <h3 class="text-lg font-bold text-[#1e3a5f]">예약 목록</h3>
              <div class="relative">
              <button @click="filterOpen = !filterOpen" class="flex items-center gap-2 px-4 py-2 border border-[#dee2e6] rounded-lg text-[#1e3a5f] hover:bg-white transition-colors">
                <Filter class="w-4 h-4" />
                <span class="text-sm">필터</span>
              </button>

              <!-- 드롭다운 -->
              <div v-if="filterOpen" class="absolute right-0 mt-2 w-40 bg-white border border-[#e9ecef] rounded-lg shadow-md z-20 overflow-hidden">
                <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='confirmed'; filterOpen=false">확정</button>
                <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='pending'; filterOpen=false">대기</button>
                <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='cancelled'; filterOpen=false">취소</button>
                <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='refunded'; filterOpen=false">환불</button>
                <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='all'; filterOpen=false">전체</button>
              </div>
            </div>
            </div>
            <div class="overflow-x-auto miu-h-[240px]">
              <table class="w-full">
                <thead class="bg-[#f8f9fa] border-b border-[#e9ecef]">
                  <tr>
                    <th
                      class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      예약자
                    </th>
                    <th
                      class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      연락처
                    </th>
                    <th
                      class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      예약시간
                    </th>
                    <th
                      class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      인원
                    </th>
                    <th
                      class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      결제금액
                    </th>
                    <th
                      class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      상태
                    </th>
                    <th
                      class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      작업
                    </th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-[#e9ecef]">
                  <template v-if="filteredReservations.length">
                    <tr
                      v-for="reservation in filteredReservations"
                      :key="reservation.id"
                      class="hover:bg-[#f8f9fa] transition-colors"
                    >
                      <td class="px-6 py-4 text-sm text-[#1e3a5f]">
                        {{ reservation.name }}
                      </td>
                      <td class="px-6 py-4 text-sm text-[#6c757d]">
                        {{ reservation.phone }}
                      </td>
                      <td class="px-6 py-4 text-sm text-[#1e3a5f]">
                        {{ reservation.date }} {{ reservation.time }}
                      </td>
                      <td class="px-6 py-4 text-sm text-[#1e3a5f]">
                        {{ reservation.guests }}명
                      </td>
                      <td class="px-6 py-4 text-sm text-[#1e3a5f]">
                        {{ reservation.amount.toLocaleString() }}원
                      </td>
                      <td class="px-6 py-4">
                        <span
                          :class="`inline-flex px-3 py-1 rounded-full text-xs font-semibold ${
                            reservation.status === 'confirmed'
                              ? 'bg-[#d4edda] text-[#155724]'
                              : reservation.status === 'pending'
                              ? 'bg-[#fff3cd] text-[#856404]'
                              : 'bg-[#f8d7da] text-[#721c24]'
                          }`"
                        >
                          {{
                            reservation.status === 'confirmed'
                              ? '확정'
                              : reservation.status === 'pending'
                              ? '대기'
                              : '취소'
                          }}
                        </span>
                      </td>
                      <td class="px-6 py-4">
                        <div class="flex items-center gap-2">
                          <button
                            class="gradient-primary text-white px-3 py-2 rounded-lg text-xs hover:opacity-90 transition-opacity"
                          >
                            상세보기
                          </button>
                          <template v-if="reservation.status === 'pending'">
                            <button
                              class="bg-[#28a745] text-white px-3 py-2 rounded-lg text-xs hover:opacity-90 transition-opacity"
                            >
                              확정
                            </button>
                            <button
                              class="bg-[#dc3545] text-white px-3 py-2 rounded-lg text-xs hover:opacity-90 transition-opacity"
                            >
                              취소
                            </button>
                          </template>
                        </div>
                      </td>
                    </tr>
                  </template>

                  <tr v-else>
                    <td colspan="7" class="px-6 py-10 text-center text-sm text-[#6c757d]">
                      해당 상태의 예약이 없습니다.
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Daily Sales Stats -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">
              금일 매출 통계
            </h3>
            <div class="grid grid-cols-4 gap-6">
              <div class="text-center">
                <p class="text-sm text-[#6c757d] mb-2">총 매출</p>
                <p class="text-2xl font-bold gradient-primary-text">
                  {{ salesStats.totalSales.toLocaleString() }}원
                </p>
              </div>
              <div class="text-center">
                <p class="text-sm text-[#6c757d] mb-2">예약 건수</p>
                <p class="text-2xl font-bold text-[#1e3a5f]">
                  {{ salesStats.totalReservations }}건
                </p>
              </div>
              <div class="text-center">
                <p class="text-sm text-[#6c757d] mb-2">1인당 평균</p>
                <p class="text-2xl font-bold text-[#1e3a5f]">
                  {{ salesStats.averagePerPerson.toLocaleString() }}원
                </p>
              </div>
              <div class="text-center">
                <p class="text-sm text-[#6c757d] mb-2">총 방문 인원</p>
                <p class="text-2xl font-bold text-[#1e3a5f]">
                  {{ salesStats.totalGuests }}명
                </p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* Scoped styles can be added here if needed, but Tailwind handles most of it. */
/* For gradient-primary-text, you might need a global style or define it if not already in main.css */
.gradient-primary-text {
  background: linear-gradient(135deg, #ff6b4a 0%, #ffaa8d 50%, #ffc4b8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
}
</style>
