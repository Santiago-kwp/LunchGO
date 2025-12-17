<script setup>
import { ref, onMounted, computed } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';

// 1. 라우터
const route = useRoute();

// 2. 상태
const restaurant = ref(null); // API로부터 받은 식당 정보를 저장할 ref

// 3. 요일 매핑
const dayOfWeekMap = {
  1: '일',
  2: '월',
  3: '화',
  4: '수',
  5: '목',
  6: '금',
  7: '토',
};

// 4. 계산된 속성
// 정기 휴무일 숫자 배열을 문자 배열로 변환
const closedDaysAsStrings = computed(() => {
  if (!restaurant.value || !restaurant.value.regularHolidays) return [];
  return restaurant.value.regularHolidays.map(
    holiday => dayOfWeekMap[holiday.dayOfWeek]
  );
});

// 대표 이미지 URL
const mainImageUrl = computed(() => {
  if (!restaurant.value || !restaurant.value.images || restaurant.value.images.length === 0) {
    return '/placeholder.svg'; // 기본 이미지
  }
  return restaurant.value.images[0].imageUrl;
});

// 5. 라이프사이클 훅
// 추후 API로부터 데이터를 받아오는 로직을 처리할 함수
onMounted(() => {
  // API: GET /api/my-restaurant
  const mockRestaurantDataFromApi = {
    restaurantId: 1,
    name: '런치고 한정식',
    phone: '02-1234-5678',
    roadAddress: '서울특별시 강남구 테헤란로 123',
    detailAddress: '4층',
    description: '전통 한정식의 맛을 현대적으로 재해석한 런치고 한정식입니다. 신선한 제철 재료로 만든 다채로운 한정식 코스를 즐겨보세요.',
    reservationLimit: 50,
    holidayAvailable: true,
    preorderAvailable: true,
    openTime: '10:00',
    closeTime: '22:00',
    openDate: '2020-01-01',
    // from restaurant_image table
    images: [
        { imageUrl: '/modern-korean-restaurant-interior.jpg' }
    ],
    // from restaurant_tag_map and tag tables
    tags: [
        { tagId: 20, content: '한식' },
        { tagId: 35, content: '조용한' },
        { tagId: 36, content: '깔끔한' },
        { tagId: 51, content: '룸' },
        { tagId: 62, content: '주차장 제공' },
        { tagId: 63, content: '와이파이' }
    ],
    // from regular_holiday table
    regularHolidays: [
        { dayOfWeek: 7 }, // 토요일
        { dayOfWeek: 1 }  // 일요일
    ]
  };

  restaurant.value = mockRestaurantDataFromApi;
});
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="restaurant-info" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <!-- v-if를 추가하여 restaurant 데이터가 로드된 후에만 렌더링 -->
        <div v-if="restaurant" class="max-w-4xl mx-auto space-y-8">
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
                  :src="mainImageUrl"
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
                  :value="restaurant.openDate"
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
                    :value="restaurant.openTime"
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
                    :value="restaurant.closeTime"
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
                  :value="restaurant.reservationLimit"
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
                    :checked="restaurant.holidayAvailable"
                    disabled
                    class="custom-checkbox w-5 h-5 rounded appearance-none cursor-not-allowed"
                  />
                  <label for="holiday" class="text-sm text-[#1e3a5f]">
                    공휴일 운영
                  </label>
                </div>
                <div class="flex items-center gap-3">
                  <input
                    type="checkbox"
                    id="preorder"
                    :checked="restaurant.preorderAvailable"
                    disabled
                    class="custom-checkbox w-5 h-5 rounded appearance-none cursor-not-allowed"
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
                      closedDaysAsStrings.includes(day)
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
                :key="tag.tagId"
                class="px-4 py-2 border-2 border-[#dee2e6] rounded-lg bg-white"
              >
                <span class="text-sm text-[#1e3a5f]">#{{ tag.content }}</span>
              </div>
            </div>
          </div>

          <!-- View All Menus Button -->
          <button
            class="w-full gradient-primary text-white py-4 rounded-xl text-lg font-semibold hover:opacity-90 transition-opacity"
          >
            식당메뉴 전체보기
          </button>

          <!-- Edit Button -->
          <div class="flex justify-end">
            <RouterLink :to="`/business/restaurant-info/edit/${restaurant.restaurantId}`">
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
.custom-checkbox {
  border-width: 1px;
  border-color: #dee2e6; /* Unchecked border color */
  background-color: #fff; /* Unchecked background */
}
.custom-checkbox:checked {
  border-color: rgba(255, 107, 74, 0.5); /* Primary color with opacity */
  background-color: rgba(255, 107, 74, 0.5); /* Primary color with opacity */
  background-image: url("data:image/svg+xml,%3csvg viewBox='0 0 16 16' fill='white' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='M12.207 4.793a1 1 0 010 1.414l-5 5a1 1 0 01-1.414 0l-2-2a1 1 0 011.414-1.414L6.5 9.086l4.293-4.293a1 1 0 011.414 0z'/%3e%3c/svg%3e");
  background-size: 100% 100%;
  background-position: center;
  background-repeat: no-repeat;
}
</style>
