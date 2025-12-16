<script setup>
import { ref, computed } from 'vue';
import AdminSidebar from '@/components/ui/AdminSideBar.vue';
import AdminHeader from '@/components/ui/AdminHeader.vue';

// Mock 데이터
const stats = ref({
  totalReservations: 1247,
  totalRestaurants: 149,
  totalMembers: 5832,
  monthlyRevenue: 24500000,
});

// 예약 현황 차트 데이터 (주간 트렌드)
const weeklyReservations = ref([
  { day: '월', count: 150 },
  { day: '화', count: 180 },
  { day: '수', count: 165 },
  { day: '목', count: 190 },
  { day: '금', count: 220 },
  { day: '토', count: 195 },
  { day: '일', count: 147 },
]);

// 최댓값 계산
const maxReservations = computed(() => {
  return Math.max(...weeklyReservations.value.map((d) => d.count));
});

// 식당별 매출 데이터
const restaurantSales = ref([
  { name: '강남 본점', sales: 3200000 },
  { name: '서초 지점', sales: 2800000 },
  { name: '판교 테크노밸리점', sales: 3500000 },
]);

// 최댓값 계산
const maxSales = computed(() => {
  return Math.max(...restaurantSales.value.map((r) => r.sales));
});

// 최근 활동 로그
const recentActivities = ref([
  {
    id: 1,
    type: 'reservation',
    title: '새로운 식당 "판교 소시집"가 등록 승인 대기 중입니다.',
    amount: 3200000,
    time: '2분전',
  },
  {
    id: 2,
    type: 'partner',
    title: '판교 점주미을 정산이 완료되었습니다 (3,200,000원)',
    amount: null,
    time: '1시간 전',
  },
  {
    id: 3,
    type: 'cancellation',
    title: '박주님이 회원가입했습니다.',
    amount: null,
    time: '1시간 전',
  },
  {
    id: 4,
    type: 'payment',
    title: '판교 참치상가 식당에 좌석을 완료했습니다. (1,200,000원)',
    amount: 1200000,
    time: '2시간 전',
  },
]);
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <AdminSidebar activeMenu="dashboard" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <AdminHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-8">
          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">대시보드</h2>
          </div>

          <!-- Stats Cards -->
          <div class="grid grid-cols-4 gap-6">
            <div
              class="bg-white rounded-xl border-2 border-[#1e3a5f] p-6 text-center hover:shadow-lg transition-shadow"
            >
              <p class="text-sm text-[#6c757d] mb-2">총 예약 건수</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">
                {{ stats.totalReservations.toLocaleString() }}건
              </p>
            </div>
            <div
              class="bg-white rounded-xl border-2 border-[#1e3a5f] p-6 text-center hover:shadow-lg transition-shadow"
            >
              <p class="text-sm text-[#6c757d] mb-2">총 등록 식당</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">
                {{ stats.totalRestaurants }}개
              </p>
            </div>
            <div
              class="bg-white rounded-xl border-2 border-[#1e3a5f] p-6 text-center hover:shadow-lg transition-shadow"
            >
              <p class="text-sm text-[#6c757d] mb-2">회원 수</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">
                {{ stats.totalMembers.toLocaleString() }}명
              </p>
            </div>
            <div
              class="bg-white rounded-xl border-2 border-[#1e3a5f] p-6 text-center hover:shadow-lg transition-shadow"
            >
              <p class="text-sm text-[#6c757d] mb-2">이번달 매출</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">
                {{ stats.monthlyRevenue.toLocaleString() }}원
              </p>
            </div>
          </div>

          <!-- Charts Section -->
          <div class="grid grid-cols-2 gap-6">
            <!-- 예약 현황 차트 -->
            <div class="bg-white rounded-2xl border border-[#e9ecef] p-6">
              <div class="flex items-center justify-between mb-6">
                <h3 class="text-xl font-bold text-[#1e3a5f]">예약 현황</h3>
                <span class="text-sm text-[#6c757d]">주간 예약 트렌드</span>
              </div>

              <!-- 간단한 라인 차트 (CSS로 구현) -->
              <div class="space-y-4">
                <div class="relative h-48">
                  <!-- Y축 라벨 -->
                  <div class="absolute left-0 top-0 text-xs text-[#6c757d]">
                    총 예약 수
                  </div>

                  <!-- 차트 영역 -->
                  <div class="absolute inset-0 pt-6 pl-12 pr-4 pb-8">
                    <svg
                      class="w-full h-full"
                      viewBox="0 0 700 200"
                      preserveAspectRatio="none"
                    >
                      <!-- 배경 그리드 라인 -->
                      <line
                        x1="0"
                        y1="0"
                        x2="700"
                        y2="0"
                        stroke="#e9ecef"
                        stroke-width="1"
                      />
                      <line
                        x1="0"
                        y1="50"
                        x2="700"
                        y2="50"
                        stroke="#e9ecef"
                        stroke-width="1"
                      />
                      <line
                        x1="0"
                        y1="100"
                        x2="700"
                        y2="100"
                        stroke="#e9ecef"
                        stroke-width="1"
                      />
                      <line
                        x1="0"
                        y1="150"
                        x2="700"
                        y2="150"
                        stroke="#e9ecef"
                        stroke-width="1"
                      />
                      <line
                        x1="0"
                        y1="200"
                        x2="700"
                        y2="200"
                        stroke="#e9ecef"
                        stroke-width="1"
                      />

                      <!-- 주간 트렌드 라인 (회색 - 이전 주) -->
                      <polyline
                        points="0,120 100,100 200,110 300,90 400,70 500,85 600,115"
                        fill="none"
                        stroke="#dee2e6"
                        stroke-width="3"
                        opacity="0.5"
                      />

                      <!-- 주간 트렌드 라인 (파란색 - 이번 주) -->
                      <polyline
                        :points="
                          weeklyReservations
                            .map(
                              (d, i) =>
                                `${i * 100},${
                                  200 - (d.count / maxReservations) * 180
                                }`
                            )
                            .join(' ')
                        "
                        fill="none"
                        stroke="#4A90E2"
                        stroke-width="3"
                      />
                    </svg>
                  </div>

                  <!-- X축 라벨 -->
                  <div
                    class="absolute bottom-0 left-12 right-4 flex justify-between"
                  >
                    <span
                      v-for="day in weeklyReservations"
                      :key="day.day"
                      class="text-xs text-[#6c757d]"
                    >
                      {{ day.day }}
                    </span>
                  </div>
                </div>

                <!-- X축 제목 -->
                <div class="text-center text-sm text-[#6c757d]">2주치 날짜</div>
              </div>
            </div>

            <!-- 식당별 매출 차트 -->
            <div class="bg-white rounded-2xl border border-[#e9ecef] p-6">
              <div class="flex items-center justify-between mb-6">
                <h3 class="text-xl font-bold text-[#1e3a5f]">식당별 매출</h3>
              </div>

              <!-- 수평 바 차트 -->
              <div class="space-y-6">
                <div class="text-left text-xs text-[#6c757d] mb-4">식당명</div>

                <div
                  v-for="restaurant in restaurantSales"
                  :key="restaurant.name"
                  class="space-y-2"
                >
                  <div class="flex items-center justify-between text-sm">
                    <span class="text-[#1e3a5f] font-medium">{{
                      restaurant.name
                    }}</span>
                    <span class="text-[#6c757d]"
                      >{{ restaurant.sales.toLocaleString() }}원</span
                    >
                  </div>
                  <div
                    class="relative w-full h-8 bg-[#e9ecef] rounded-lg overflow-hidden"
                  >
                    <div
                      class="absolute left-0 top-0 h-full bg-gradient-to-r from-[#4A90E2] to-[#357ABD] transition-all duration-500"
                      :style="{
                        width: `${(restaurant.sales / maxSales) * 100}%`,
                      }"
                    ></div>
                  </div>
                </div>

                <div class="text-center text-sm text-[#6c757d] mt-4">금액</div>
              </div>
            </div>
          </div>

          <!-- 최근 활동 -->
          <div class="bg-white rounded-2xl border border-[#e9ecef] p-6">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">최근 활동</h3>

            <div class="space-y-4">
              <div
                v-for="activity in recentActivities"
                :key="activity.id"
                class="flex items-start justify-between py-3 border-b border-[#e9ecef] last:border-b-0"
              >
                <div class="flex-1">
                  <p class="text-[#1e3a5f] leading-relaxed">
                    {{ activity.title }}
                  </p>
                  <p v-if="activity.amount" class="text-sm text-[#6c757d] mt-1">
                    {{ activity.amount.toLocaleString() }}원
                  </p>
                </div>
                <span class="text-sm text-[#6c757d] ml-4 flex-shrink-0">{{
                  activity.time
                }}</span>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* 스크롤바 스타일링 */
.overflow-y-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #ff6b4a;
  border-radius: 10px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #e55a39;
}
</style>
