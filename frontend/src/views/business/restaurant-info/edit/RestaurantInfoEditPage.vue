<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { Upload, X } from 'lucide-vue-next';
import { RouterLink, useRouter, useRoute } from 'vue-router';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import Pagination from '@/components/ui/Pagination.vue';

const router = useRouter();
const route = useRoute();

const isEditMode = computed(() => !!route.params.id);
const pageTitle = computed(() =>
  isEditMode.value ? '식당 정보 수정' : '식당 정보 등록'
);
const submitButtonText = computed(() =>
  isEditMode.value ? '수정하기' : '등록하기'
);

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

const restaurantImageFile = ref(null);
const restaurantImageUrl = ref(null);
const restaurantFileInput = ref(null);

const closedDays = ref([]);
const selectedTags = ref([]);

// --- Menu Management State ---
const allMenus = ref(
  Array.from({ length: 25 }, (_, i) => ({
    id: i + 1,
    name: `더미 메뉴 ${i + 1}`,
    type:
      i % 3 === 0
        ? '주메뉴'
        : i % 3 === 1
        ? '서브메뉴'
        : '기타(디저트, 음료)',
    price: `${(10 + i).toFixed(3)}원`,
  }))
);
const currentPage = ref(1);
const itemsPerPage = ref(5);

const totalPages = computed(() =>
  Math.ceil(allMenus.value.length / itemsPerPage.value)
);

const paginatedMenus = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return allMenus.value.slice(start, end);
});

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
};

// --- End of Menu Management State ---

const handleRestaurantFileChange = (e) => {
  const file = e.target.files[0];
  if (file) {
    restaurantImageFile.value = file;
    restaurantImageUrl.value = URL.createObjectURL(file);
  }
};

const triggerRestaurantFileInput = () => {
  restaurantFileInput.value.click();
};

const clearRestaurantImage = () => {
  restaurantImageFile.value = null;
  restaurantImageUrl.value = null;
  if (restaurantFileInput.value) {
    restaurantFileInput.value.value = '';
  }
};

const openPostcodeSearch = () => {
  new window.daum.Postcode({
    oncomplete: (data) => {
      formData.roadAddress = data.roadAddress;
    },
  }).open();
};

const toggleClosedDay = (day) => {
  const index = closedDays.value.indexOf(day);
  if (index > -1) {
    closedDays.value.splice(index, 1);
  } else {
    closedDays.value.push(day);
  }
};

const toggleTag = (tag) => {
  const restaurantTypeTags = tagCategories.value['식당종류'] || [];
  const isRestaurantTypeTag = restaurantTypeTags.includes(tag);
  const index = selectedTags.value.indexOf(tag);

  if (isRestaurantTypeTag) {
    selectedTags.value = selectedTags.value.filter(
      (selected) => !restaurantTypeTags.includes(selected)
    );
    if (index === -1) {
      selectedTags.value.push(tag);
    }
  } else {
    if (index > -1) {
      selectedTags.value.splice(index, 1);
    } else {
      selectedTags.value.push(tag);
    }
  }
};

const tagCategories = ref({});

const fetchTags = async () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve([
        { id: 1, name: '한식', category: '식당종류' },
        { id: 2, name: '중식', category: '식당종류' },
        { id: 3, name: '일식', category: '식당종류' },
        { id: 4, name: '양식', category: '식당종류' },
        { id: 5, name: '셀프바', category: '테이블 옵션' },
        { id: 6, name: '룸', category: '테이블 옵션' },
        { id: 7, name: '칸막이', category: '테이블 옵션' },
        { id: 8, name: '조용한', category: '식당 분위기' },
        { id: 9, name: '깔끔한', category: '식당 분위기' },
        { id: 10, name: '이국적/이색적', category: '식당 분위기' },
        { id: 11, name: '노키즈존', category: '편의사항' },
        { id: 12, name: '주차장 제공', category: '편의사항' },
        { id: 13, name: '와이파이', category: '편의사항' },
      ]);
    }, 100);
  });
};

const fetchRestaurantData = async (id) => {
  console.log(`Fetching data for restaurant ID: ${id}`);
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        name: '런치고 한정식 (수정)',
        phone: '02-9876-5432',
        openingDate: '2022-10-20',
        startTime: '11:00',
        endTime: '21:00',
        capacity: 75,
        roadAddress: '서울특별시 종로구 종로 1',
        detailAddress: '5층',
        holidayOpen: true,
        preOrderSupported: false,
        description: '수정된 식당 소개입니다. 더욱 맛있어졌습니다.',
        imageUrl: '/korean-course-meal-plating.jpg',
        closedDays: ['일'],
        tags: ['한식', '조용한', '주차장 제공'],
      });
    }, 500);
  });
};

onMounted(async () => {
  // 1. 태그 목록 불러오기
  const tagsFromApi = await fetchTags();
  const groupedTags = tagsFromApi.reduce((acc, tag) => {
    if (!acc[tag.category]) {
      acc[tag.category] = [];
    }
    acc[tag.category].push(tag.name);
    return acc;
  }, {});
  tagCategories.value = groupedTags;

  // 2. 수정 모드인 경우, 식당 데이터 불러와서 폼에 채우기
  if (isEditMode.value) {
    const restaurantData = await fetchRestaurantData(route.params.id);
    formData.name = restaurantData.name;
    formData.phone = restaurantData.phone;
    formData.openingDate = restaurantData.openingDate;
    formData.startTime = restaurantData.startTime;
    formData.endTime = restaurantData.endTime;
    formData.capacity = restaurantData.capacity;
    formData.roadAddress = restaurantData.roadAddress;
    formData.detailAddress = restaurantData.detailAddress;
    formData.holidayOpen = restaurantData.holidayOpen;
    formData.preOrderSupported = restaurantData.preOrderSupported;
    formData.description = restaurantData.description;
    restaurantImageUrl.value = restaurantData.imageUrl;
    closedDays.value = restaurantData.closedDays;
    selectedTags.value = restaurantData.tags;
  }
});

const saveRestaurant = async () => {
  // 실제 저장 로직
  const dataToSubmit = {
    ...formData,
    closedDays: closedDays.value,
    tags: selectedTags.value,
    // restaurantImageFile.value // 이미지 파일도 함께 전송
  };

  try {
    if (isEditMode.value) {
      // 수정 API 호출
      console.log('Updating restaurant:', route.params.id, dataToSubmit);
      // await api.updateRestaurant(route.params.id, dataToSubmit);
    } else {
      // 등록 API 호출
      console.log('Creating new restaurant:', dataToSubmit);
      // await api.createRestaurant(dataToSubmit);
    }
    alert('저장되었습니다.');
    router.push('/business/restaurant-info');
  } catch (error) {
    console.error('저장 실패:', error);
    alert('저장에 실패했습니다.');
  }
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
          <h2 class="text-3xl font-bold text-[#1e3a5f]">{{ pageTitle }}</h2>

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
                @click="triggerRestaurantFileInput"
                class="border-2 border-[#e9ecef] rounded-xl overflow-hidden bg-[#f8f9fa] relative cursor-pointer"
              >
                <div class="aspect-[2/1] flex items-center justify-center">
                  <div v-if="restaurantImageUrl" class="w-full h-full">
                    <img
                      :src="restaurantImageUrl"
                      alt="식당 이미지 미리보기"
                      class="w-full h-full object-cover rounded-lg"
                    />
                    <button
                      @click.stop="clearRestaurantImage"
                      class="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full hover:bg-red-600 transition-colors"
                    >
                      <X class="w-4 h-4" />
                    </button>
                  </div>
                  <div v-else class="text-center">
                    <Upload class="w-12 h-12 text-[#6c757d] mx-auto mb-3" />
                    <p class="text-sm text-[#6c757d]">이미지를 업로드하세요</p>
                  </div>
                </div>
              </div>
              <input
                type="file"
                ref="restaurantFileInput"
                @change="handleRestaurantFileChange"
                class="hidden"
                accept="image/*"
              />
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
                    placeholder="식당 전화번호를 입력하세요(예시: XXX-XXXX-XXXX)"
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
                    placeholder="우측 '주소검색' 버튼을 클릭하세요"
                    v-model="formData.roadAddress"
                    readonly
                    class="flex-1 px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A] bg-[#f8f9fa]"
                  />
                  <button
                    @click="openPostcodeSearch"
                    type="button"
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
                  placeholder="상세주소 입력"
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
                    공휴일 운영
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
                    선주문/선결제 가능
                  </label>
                </div>
              </div>

              <!-- Regular Closing Days -->
              <div>
                <div class="flex flex-wrap gap-3">
                  <button
                    v-for="day in ['월', '화', '수', '목', '금', '토', '일']"
                    :key="day"
                    @click="toggleClosedDay(day)"
                    :class="`px-4 py-2 rounded-lg border transition-colors ${
                      closedDays.includes(day)
                        ? 'gradient-primary text-white border-transparent'
                        : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                    }`"
                  >
                    {{ day }}
                  </button>
                </div>
              </div>

              <!-- Restaurant Description -->
              <div>
                <label
                  class="block text-sm font-semibold text-[#1e3a5f] mb-2 mt-4"
                  >식당 소개</label
                >
                <textarea
                  placeholder="식당 소개글 입력창"
                  v-model="formData.description"
                  rows="5"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A] resize-none"
                ></textarea>
              </div>
            </div>
          </div>

          <!-- Restaurant Tags -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">식당 태그</h3>

            <!-- Characteristics Tags -->
            <div
              v-for="(tags, categoryName) in tagCategories"
              :key="categoryName"
              class="mb-6"
            >
              <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">
                {{ categoryName }}
              </h4>
              <div class="flex flex-wrap gap-3">
                <button
                  v-for="tag in tags"
                  :key="tag"
                  @click="toggleTag(tag)"
                  :class="`px-4 py-2 rounded-lg border transition-colors ${
                    selectedTags.includes(tag)
                      ? 'gradient-primary text-white border-transparent'
                      : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                  }`"
                >
                  {{ tag }}
                </button>
              </div>
            </div>
          </div>

          <!-- Menu Management Section -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">식당메뉴</h3>

            <div class="overflow-x-auto">
              <table class="w-full">
                <thead class="bg-[#f8f9fa] border-b border-[#dee2e6]">
                  <tr>
                    <th
                      class="px-12 py-3 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      메뉴이름
                    </th>
                    <th
                      class="px-12 py-3 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      메뉴타입
                    </th>
                    <th
                      class="px-12 py-3 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      가격
                    </th>
                    <th
                      class="px-4 py-3 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      수정/삭제
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="menu in paginatedMenus"
                    :key="menu.id"
                    class="border-b border-[#e9ecef]"
                  >
                    <td class="px-12 py-4 text-sm text-[#1e3a5f]">
                      {{ menu.name }}
                    </td>
                    <td class="px-12 py-4 text-sm text-[#6c757d]">{{ menu.type }}</td>
                    <td class="px-12 py-4 text-sm text-[#6c757d]">{{ menu.price }}</td>
                    <td class="px-4 py-4 text-left">
                      <div class="flex justify-start gap-2">
                        <RouterLink
                          :to="`/business/restaurant-info/menu/edit/${menu.id}`"
                          class="px-3 py-2 border border-[#dee2e6] rounded-lg text-[#1e3a5f] text-sm hover:bg-[#f8f9fa] transition-colors"
                        >
                          수정
                        </RouterLink>
                        <button
                          class="px-3 py-2 border border-[#dc3545] text-[#dc3545] rounded-lg text-sm hover:bg-[#fff5f5] transition-colors"
                        >
                          삭제
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- 페이지네이션 적용 위치 -->
            <div class="flex items-center justify-between mt-6">
              <Pagination
                :current-page="currentPage"
                :total-pages="totalPages"
                @change-page="changePage"
              />

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
              @click="saveRestaurant"
              class="px-8 py-3 gradient-primary text-white rounded-xl font-semibold hover:opacity-90 transition-opacity"
            >
              {{ submitButtonText }}
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
