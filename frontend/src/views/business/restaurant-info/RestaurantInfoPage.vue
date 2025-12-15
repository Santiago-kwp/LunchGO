<script setup>
import { RouterLink } from 'vue-router';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';

const restaurant = {
  id: 1, // Add a mock ID for routing
  name: '런치고 한정식',
  phone: '02-1234-5678',
  openingDate: '2020-01-01',
  startTime: '10:00', // Re-add startTime
  endTime: '22:00',
  capacity: 50,
  roadAddress: '서울특별시 강남구 테헤란로 123',
  detailAddress: '4층',
  holidayOpen: true,
  preOrderSupported: true,
  closedDays: ['토', '일'],
  description:
    '전통 한정식의 맛을 현대적으로 재해석한 런치고 한정식입니다. 신선한 제철 재료로 만든 다채로운 한정식 코스를 즐겨보세요.',
  image: '/modern-korean-restaurant-interior.jpg', // Placeholder for image
  tags: ['조용한', '깔끔한', '주차장 제공', '룸', '한식', '점심', '저녁'], // Example tags
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="restaurant-info" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-4xl mx-auto space-y-8">
          <!-- Page Title -->
          <h2 class="text-3xl font-bold text-[#1e3a5f]">식당 정보 조회</h2>

          <!-- Restaurant Basic Info Card -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">
              식당 기본 정보
            </h3>

            <!-- Restaurant Image -->
            <div
              class="mb-8 border-2 border-[#e9ecef] rounded-xl overflow-hidden bg-[#f8f9fa]"
            >
              <div class="aspect-[2/1] flex items-center justify-center">
                <img
                  :src="restaurant.image"
                  :alt="restaurant.name"
                  class="w-full h-full object-cover"
                />
              </div>
            </div>

            <!-- Form Fields -->
            <div class="space-y-6">
              <!-- Restaurant Name & Phone -->
              <div class="grid grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >식당명</label
                  >
                  <input
                    type="text"
                    :value="restaurant.name"
                    readonly
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                  />
                </div>
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >식당전화번호</label
                  >
                  <input
                    type="text"
                    :value="restaurant.phone"
                    readonly
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                  />
                </div>
              </div>

              <!-- Opening Date -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >개업일</label
                >
                <input
                  type="text"
                  :value="restaurant.openingDate"
                  readonly
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                />
              </div>

              <!-- Business Hours -->
              <div class="grid grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >영업시작시간</label
                  >
                  <input
                    type="text"
                    :value="restaurant.startTime"
                    readonly
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                  />
                </div>
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >영업종료시간</label
                  >
                  <input
                    type="text"
                    :value="restaurant.endTime"
                    readonly
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                  />
                </div>
              </div>

              <!-- Reservation Capacity -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >예약인원 상한</label
                >
                <input
                  type="number"
                  :value="restaurant.capacity"
                  readonly
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                />
              </div>

              <!-- Address -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >도로명주소</label
                >
                <input
                  type="text"
                  :value="restaurant.roadAddress"
                  readonly
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                />
              </div>

              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >상세주소</label
                >
                <input
                  type="text"
                  :value="restaurant.detailAddress"
                  readonly
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                />
              </div>

              <!-- Checkboxes -->
              <div class="grid grid-cols-2 gap-6">
                <div class="flex items-center gap-3">
                  <input
                    type="checkbox"
                    id="holiday"
                    v-model="restaurant.holidayOpen"
                    disabled
                    class="w-5 h-5 rounded border-[#dee2e6]"
                  />
                  <label for="holiday" class="text-sm text-[#1e3a5f]">
                    공휴일 운영
                  </label>
                </div>
                <div class="flex items-center gap-3">
                  <input
                    type="checkbox"
                    id="preorder"
                    v-model="restaurant.preOrderSupported"
                    disabled
                    class="w-5 h-5 rounded border-[#dee2e6]"
                  />
                  <label for="preorder" class="text-sm text-[#1e3a5f]">
                    선주문/선결제 가능
                  </label>
                </div>
              </div>

              <!-- Regular Closing Days -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2">
                  정기휴무일
                </label>
                <div class="flex gap-3">
                  <button
                    v-for="day in ['월', '화', '수', '목', '금', '토', '일']"
                    :key="day"
                    :class="`px-4 py-2 rounded-lg border transition-colors ${
                      restaurant.closedDays.includes(day)
                        ? 'gradient-primary text-white border-transparent'
                        : 'border-[#dee2e6] text-[#1e3a5f] bg-[#f8f9fa]'
                    }`"
                    disabled
                  >
                    {{ day }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Restaurant Introduction -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-4">식당 소개</h3>
            <div
              class="border-2 border-[#e9ecef] rounded-xl p-6 bg-[#f8f9fa] min-h-[120px]"
            >
              <p class="text-sm text-[#6c757d]">{{ restaurant.description }}</p>
            </div>
          </div>

          <!-- Restaurant Tags -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-4">식당 태그</h3>
            <div class="flex flex-wrap gap-3">
              <div
                v-for="tag in restaurant.tags"
                :key="tag"
                class="px-4 py-2 border-2 border-[#dee2e6] rounded-lg bg-white"
              >
                <span class="text-sm text-[#1e3a5f]">{{ tag }}</span>
              </div>
            </div>
          </div>

          <!-- View All Menus Button -->
          <RouterLink
            :to="`/restaurant/${restaurant.id}/menus`"
            class="w-full gradient-primary text-white py-4 rounded-xl text-lg font-semibold hover:opacity-90 transition-opacity block text-center"
          >
            식당메뉴 전체보기
          </RouterLink>

          <!-- Edit Button -->
          <div class="flex justify-end">
            <RouterLink to="/business/restaurant-info/edit">
              <button
                class="px-8 py-3 border-2 border-[#FF6B4A] text-[#FF6B4A] rounded-xl font-semibold hover:bg-[#fff5f2] transition-colors"
              >
                식당정보 편집
              </button>
            </RouterLink>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
