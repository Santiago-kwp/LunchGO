<script setup>
import { ref, reactive } from 'vue';
import { Upload } from 'lucide-vue-next'; // User, Bell은 Header로 이동
import { RouterLink, useRouter } from 'vue-router';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';

const router = useRouter(); // Vue Router's useRouter

const formData = reactive({
  name: '',
  phone: '',
  openingDate: '',
  startTime: '',
  endTime: '',
  capacity: '',
  roadAddress: '',
  detailAddress: '',
  holidayOpen: false,
  preOrderSupported: false,
  description: '',
});

const closedDays = ref([]);
const selectedTags = ref([]);

const toggleClosedDay = (day) => {
  const index = closedDays.value.indexOf(day);
  if (index > -1) {
    closedDays.value.splice(index, 1);
  } else {
    closedDays.value.push(day);
  }
};

const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag);
  if (index > -1) {
    selectedTags.value.splice(index, 1);
  } else {
    selectedTags.value.push(tag);
  }
};

const tagCategories = ref({
  특징: ['조용한', '깔끔한', '이국적/이색적'],
  편의: ['노키즈존', '주차장 제공', '셀프바', '칸막이', '룸'],
  규모: ['단체'],
});

const menuCategories = ref(['한식', '중식', '일식', '양식']);
const specialtyTags = ref(['점심', '저녁', '야식']);
const allergyTags = ref([
  '견과류',
  '우유',
  '계란',
  '밀',
  '대두',
  '갑각류',
  '조개류',
  '생선',
  '메밀',
]);

const navigateToRestaurantInfo = () => {
  router.push('/business/restaurant-info');
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
          <h2 class="text-3xl font-bold text-[#1e3a5f]">식당 정보 등록</h2>

          <!-- Restaurant Basic Info Card -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">
              식당 기본 정보
            </h3>

            <!-- Restaurant Image Upload -->
            <div class="mb-8">
              <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                >식당이미지</label
              >
              <div
                class="border-2 border-dashed border-[#dee2e6] rounded-xl p-8 text-center bg-[#f8f9fa] hover:bg-[#e9ecef] transition-colors cursor-pointer"
              >
                <Upload class="w-12 h-12 text-[#6c757d] mx-auto mb-3" />
                <p class="text-sm text-[#6c757d]">이미지를 업로드하세요</p>
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
                    placeholder="식당 이름을 입력하세요"
                    v-model="formData.name"
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  />
                </div>
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >식당전화번호</label
                  >
                  <input
                    type="tel"
                    placeholder="전화번호를 입력하세요"
                    v-model="formData.phone"
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  />
                </div>
              </div>

              <!-- Opening Date -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >개업일</label
                >
                <input
                  type="date"
                  v-model="formData.openingDate"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
              </div>

              <!-- Business Hours -->
              <div class="grid grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >영업시작시간</label
                  >
                  <input
                    type="time"
                    v-model="formData.startTime"
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  />
                </div>
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >영업종료시간</label
                  >
                  <input
                    type="time"
                    v-model="formData.endTime"
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
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
                  placeholder="숫자를 입력하세요"
                  v-model.number="formData.capacity"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
              </div>

              <!-- Address -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >도로명주소</label
                >
                <div class="flex gap-3">
                  <input
                    type="text"
                    placeholder="도로명주소 입력창"
                    v-model="formData.roadAddress"
                    class="flex-1 px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  />
                  <button
                    class="px-6 py-3 border border-[#dee2e6] rounded-lg text-[#1e3a5f] hover:bg-[#f8f9fa] transition-colors"
                  >
                    주소검색
                  </button>
                </div>
              </div>

              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >상세주소</label
                >
                <input
                  type="text"
                  placeholder="상세주소 입력창"
                  v-model="formData.detailAddress"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
              </div>

              <!-- Checkboxes -->
              <div class="grid grid-cols-2 gap-6">
                <div class="flex items-center gap-3">
                  <input
                    type="checkbox"
                    id="holiday"
                    v-model="formData.holidayOpen"
                    class="w-5 h-5 rounded border-[#dee2e6]"
                  />
                  <label for="holiday" class="text-sm text-[#1e3a5f]">
                    공휴일 운영 여부(체크박스)
                  </label>
                </div>
                <div class="flex items-center gap-3">
                  <input
                    type="checkbox"
                    id="preorder"
                    v-model="formData.preOrderSupported"
                    class="w-5 h-5 rounded border-[#dee2e6]"
                  />
                  <label for="preorder" class="text-sm text-[#1e3a5f]">
                    선주문/선결제 지원 여부(체크박스)
                  </label>
                </div>
              </div>

              <!-- Regular Closing Days -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-3"
                  >정기휴무일(요일) - 체크박스</label
                >
                <div class="flex gap-3">
                  <button
                    v-for="day in ['월', '화', '수', '목', '금', '토', '일']"
                    :key="day"
                    @click="toggleClosedDay(day)"
                    :class="`px-6 py-3 rounded-lg border transition-colors ${
                      closedDays.includes(day)
                        ? 'gradient-primary text-white border-transparent'
                        : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                    }`"
                  >
                    {{ day }}
                  </button>
                </div>
                <button
                  class="mt-3 px-6 py-2 border border-[#dee2e6] rounded-lg text-[#6c757d] text-sm hover:bg-[#f8f9fa] transition-colors"
                >
                  신택 요일 저장
                </button>
              </div>

              <!-- Restaurant Description -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >식당 사진</label
                >
                <button
                  class="mb-3 px-6 py-2 border border-[#dee2e6] rounded-lg text-[#1e3a5f] hover:bg-[#f8f9fa] transition-colors"
                >
                  파일 업로드
                </button>
                <label
                  class="block text-sm font-semibold text-[#1e3a5f] mb-2 mt-4"
                  >식당 소개</label
                >
                <textarea
                  placeholder="식당 소개글 입력창(텍스트박스, 선택사항)"
                  v-model="formData.description"
                  rows="5"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A] resize-none"
                />
              </div>
            </div>
          </div>

          <!-- Restaurant Tags -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">
              식당 태그(선택 가능한 모든 검색태그만 카테고리별로 표시)
            </h3>

            <!-- Characteristics Tags -->
            <div class="mb-6">
              <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">
                식당 특징(테크박스)
              </h4>
              <div class="flex flex-wrap gap-3">
                <template
                  v-for="(categoryTags, categoryName) in tagCategories"
                  :key="categoryName"
                >
                  <button
                    v-for="tag in categoryTags"
                    :key="tag"
                    @click="toggleTag(tag)"
                    :class="`px-6 py-3 rounded-lg border transition-colors ${
                      selectedTags.includes(tag)
                        ? 'gradient-primary text-white border-transparent'
                        : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                    }`"
                  >
                    {{ tag }}
                  </button>
                </template>
                <button
                  class="px-6 py-3 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
                >
                  ...
                </button>
              </div>
            </div>

            <!-- Menu Category -->
            <div class="mb-6">
              <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">
                제공 가능한 메뉴(체크박스)
              </h4>
              <div class="flex flex-wrap gap-3">
                <button
                  v-for="category in menuCategories"
                  :key="category"
                  @click="toggleTag(category)"
                  :class="`px-6 py-3 rounded-lg border transition-colors ${
                    selectedTags.includes(category)
                      ? 'gradient-primary text-white border-transparent'
                      : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                  }`"
                >
                  {{ category }}
                </button>
                <button
                  class="px-6 py-3 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
                >
                  ...
                </button>
              </div>
            </div>

            <!-- Specialty Tags -->
            <div class="mb-6">
              <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">
                식당 분위기(체크박스)
              </h4>
              <div class="flex flex-wrap gap-3">
                <button
                  v-for="tag in specialtyTags"
                  :key="tag"
                  @click="toggleTag(tag)"
                  :class="`px-6 py-3 rounded-lg border transition-colors ${
                    selectedTags.includes(tag)
                      ? 'gradient-primary text-white border-transparent'
                      : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                  }`"
                >
                  {{ tag }}
                </button>
                <button
                  class="px-6 py-3 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
                >
                  ...
                </button>
              </div>
            </div>

            <!-- Allergy Tags -->
            <div>
              <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">
                보유 관리식재료(체크박스)
              </h4>
              <div class="flex flex-wrap gap-3">
                <button
                  v-for="tag in allergyTags"
                  :key="tag"
                  @click="toggleTag(tag)"
                  :class="`px-6 py-3 rounded-lg border transition-colors ${
                    selectedTags.includes(tag)
                      ? 'gradient-primary text-white border-transparent'
                      : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                  }`"
                >
                  {{ tag }}
                </button>
                <button
                  class="px-6 py-3 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
                >
                  ...
                </button>
              </div>
            </div>

            <button
              class="mt-6 px-8 py-3 gradient-primary text-white rounded-lg font-semibold hover:opacity-90 transition-opacity"
            >
              저장
            </button>
          </div>

          <!-- Menu Management Section -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">
              식당메뉴 (데이터 시각 5개 메뉴 출력)
            </h3>

            <div class="overflow-x-auto">
              <table class="w-full">
                <thead class="bg-[#f8f9fa] border-b border-[#dee2e6]">
                  <tr>
                    <th
                      class="px-4 py-3 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      메뉴이름
                    </th>
                    <th
                      class="px-4 py-3 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      메뉴타입
                    </th>
                    <th
                      class="px-4 py-3 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      가격
                    </th>
                    <th
                      class="px-4 py-3 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      수량/상태
                    </th>
                    <th
                      class="px-4 py-3 text-center text-sm font-semibold text-[#1e3a5f]"
                    >
                      작업
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="index in [1, 2, 3]"
                    :key="index"
                    class="border-b border-[#e9ecef]"
                  >
                    <td class="px-4 py-4 text-sm text-[#1e3a5f]">
                      메뉴명 {{ index }}
                    </td>
                    <td class="px-4 py-4 text-sm text-[#6c757d]">메뉴타입</td>
                    <td class="px-4 py-4 text-sm text-[#6c757d]">15,000원</td>
                    <td class="px-4 py-4 text-sm text-[#6c757d]">재고 있음</td>
                    <td class="px-4 py-4 text-center">
                      <div class="flex justify-center gap-2">
                        <RouterLink
                          :to="`/business/restaurant-info/menu/edit/${index}`"
                          class="px-4 py-2 border border-[#dee2e6] rounded-lg text-[#1e3a5f] text-sm hover:bg-[#f8f9fa] transition-colors"
                        >
                          수정
                        </RouterLink>
                        <button
                          class="px-4 py-2 border border-[#dc3545] text-[#dc3545] rounded-lg text-sm hover:bg-[#fff5f5] transition-colors"
                        >
                          삭제
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="flex items-center justify-between mt-6">
              <div class="flex gap-2">
                <button
                  class="px-4 py-2 border border-[#dee2e6] rounded-lg text-[#1e3a5f] hover:bg-[#f8f9fa] transition-colors"
                >
                  1
                </button>
                <button
                  class="px-4 py-2 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
                >
                  2
                </button>
                <button
                  class="px-4 py-2 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
                >
                  3
                </button>
              </div>

              <RouterLink
                to="/business/restaurant-info/menu/add"
                class="px-6 py-3 gradient-primary text-white rounded-lg font-semibold hover:opacity-90 transition-opacity"
              >
                메뉴 추가
              </RouterLink>
            </div>
          </div>

          <!-- Save Button -->
          <div class="flex justify-end">
            <button
              @click="navigateToRestaurantInfo"
              class="px-8 py-3 gradient-primary text-white rounded-xl font-semibold hover:opacity-90 transition-opacity"
            >
              식당정보 편집
            </button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
