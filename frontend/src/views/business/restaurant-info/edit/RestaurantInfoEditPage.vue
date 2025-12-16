<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { Upload, X } from 'lucide-vue-next';
import { RouterLink, useRouter, useRoute } from 'vue-router';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import Pagination from '@/components/ui/Pagination.vue';
import { useRestaurantStore } from '@/stores/restaurant';

const router = useRouter();
const route = useRoute();
const store = useRestaurantStore();

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
  openDate: '',
  openTime: '',
  closeTime: '',
  reservationLimit: '',
  roadAddress: '',
  detailAddress: '',
  holidayAvailable: false,
  preorderAvailable: false,
  description: '',
});

const restaurantImageFile = ref(null);
const restaurantImageUrl = ref(null);
const restaurantFileInput = ref(null);

const selectedClosedDays = ref([]);
const selectedTags = ref([]);

const dayOfWeekInverseMap = {
  일: 1,
  월: 2,
  화: 3,
  수: 4,
  목: 5,
  금: 6,
  토: 7,
};

const tagCategoryDisplayNames = {
  MENUTYPE: '식당 종류',
  TABLETYPE: '테이블 옵션',
  ATMOSPHERE: '식당 분위기',
  FACILITY: '편의시설',
};

// --- Menu Management State (from Pinia Store) ---
const allMenus = computed(() => store.menus);
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

const toggleClosedDay = (dayString) => {
  const dayNumber = dayOfWeekInverseMap[dayString];
  if (dayNumber === undefined) return;

  const index = selectedClosedDays.value.indexOf(dayNumber);
  if (index > -1) {
    selectedClosedDays.value.splice(index, 1);
  } else {
    selectedClosedDays.value.push(dayNumber);
  }
};

const isDaySelected = (dayString) => {
  const dayNumber = dayOfWeekInverseMap[dayString];
  return selectedClosedDays.value.includes(dayNumber);
};

const allTags = ref([]);
const tagCategories = ref({});

const toggleTag = (tag) => {
  const isRestaurantTypeTag = tag.category === 'MENUTYPE';
  const index = selectedTags.value.findIndex((st) => st.tagId === tag.tagId);

  if (isRestaurantTypeTag) {
    selectedTags.value = selectedTags.value.filter(
      (st) => st.category !== 'MENUTYPE'
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

const isTagSelected = (tag) => {
  return selectedTags.value.some((st) => st.tagId === tag.tagId);
};

const fetchTags = async () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve([
        { tagId: 1, content: '한식', category: 'MENUTYPE' },
        { tagId: 2, content: '중식', category: 'MENUTYPE' },
        { tagId: 3, content: '일식', category: 'MENUTYPE' },
        { tagId: 4, content: '양식', category: 'MENUTYPE' },
        { tagId: 5, content: '퓨전', category: 'MENUTYPE' },
        { tagId: 6, content: '셀프바', category: 'TABLETYPE' },
        { tagId: 7, content: '룸', category: 'TABLETYPE' },
        { tagId: 8, content: '칸막이', category: 'TABLETYPE' },
        { tagId: 9, content: '조용한', category: 'ATMOSPHERE' },
        { tagId: 10, content: '깔끔한', category: 'ATMOSPHERE' },
        { tagId: 11, content: '이국적/이색적', category: 'ATMOSPHERE' },
        { tagId: 12, content: '주차장 제공', category: 'FACILITY' },
        { tagId: 13, content: '와이파이', category: 'FACILITY' },
      ]);
    }, 100);
  });
};

const fetchRestaurantData = async (restaurantId) => {
  console.log(`Fetching data for restaurant ID: ${restaurantId}`);
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        restaurantId: restaurantId,
        name: '런치고 한정식 (수정)',
        phone: '02-9876-5432',
        openDate: '2022-10-20',
        openTime: '11:00',
        closeTime: '21:00',
        reservationLimit: 75,
        roadAddress: '서울특별시 종로구 종로 1',
        detailAddress: '5층',
        holidayAvailable: true,
        preorderAvailable: false,
        description: '수정된 식당 소개입니다. 더욱 맛있어졌습니다.',
        images: [{ imageUrl: '/korean-course-meal-plating.jpg' }],
        regularHolidays: [{ dayOfWeek: 1 }, { dayOfWeek: 7 }],
        tags: [
          { tagId: 1, content: '한식', category: 'MENUTYPE' },
          { tagId: 8, content: '조용한', category: 'ATMOSPHERE' },
          { tagId: 12, content: '주차장 제공', category: 'FACILITY' },
        ],
        menus: [
          { id: 101, name: '한정식 A코스', type: '주메뉴', price: 50000 },
          { id: 102, name: '한정식 B코스', type: '주메뉴', price: 75000 },
          { id: 103, name: '떡갈비', type: '서브메뉴', price: 25000 },
          { id: 104, name: '해물파전', type: '서브메뉴', price: 20000 },
          { id: 105, name: '수정과', type: '기타(디저트, 음료)', price: 5000 },
          { id: 106, name: '식혜', type: '기타(디저트, 음료)', price: 5000 },
        ],
      });
    }, 500);
  });
};

onMounted(async () => {
  const tagsFromApi = await fetchTags();
  allTags.value = tagsFromApi;
  const groupedTags = tagsFromApi.reduce((acc, tag) => {
    if (!acc[tag.category]) {
      acc[tag.category] = [];
    }
    acc[tag.category].push(tag);
    return acc;
  }, {});
  tagCategories.value = groupedTags;

  if (isEditMode.value) {
    const restaurantId = Number(route.params.id);
    // 스토어에 이미 로드된 식당과 다른 식당을 수정하려 할 때만 데이터 로드
    if (store.restaurantId !== restaurantId) {
      const restaurantData = await fetchRestaurantData(restaurantId);
      Object.assign(formData, restaurantData);

      if (restaurantData.images && restaurantData.images.length > 0) {
        restaurantImageUrl.value = restaurantData.images[0].imageUrl;
        restaurantImageFile.value = new File([], 'mock-restaurant-image.png');
      }

      selectedClosedDays.value = restaurantData.regularHolidays.map(
        (h) => h.dayOfWeek
      );
      selectedTags.value = restaurantData.tags;
      store.loadRestaurant(restaurantData); // 스토어 상태 설정
    }
  } else {
    // 새로 등록하는 경우, 이전에 수정하던 정보가 스토어에 남아있으면 초기화
    if (store.restaurantId !== null) {
      store.clearRestaurant();
    }
  }
});

const saveRestaurant = async () => {
  // 1. Reset previous errors
  for (const key in validationErrors) {
    validationErrors[key] = '';
  }

  // 2. Validate fields
  let isValid = true;
  if (!restaurantImageFile.value) {
    validationErrors.image = '식당 이미지를 등록해주세요.';
    isValid = false;
  }
  if (!formData.name.trim()) {
    validationErrors.name = '식당 이름을 입력해주세요.';
    isValid = false;
  }
  if (!formData.phone.trim()) {
    validationErrors.phone = '식당 전화번호를 입력해주세요.';
    isValid = false;
  }
  if (!formData.openDate) {
    validationErrors.openDate = '개업일을 입력해주세요.';
    isValid = false;
  }
  if (!formData.openTime) {
    validationErrors.openTime = '영업시작시간을 입력해주세요.';
    isValid = false;
  }
  if (!formData.closeTime) {
    validationErrors.closeTime = '영업종료시간을 입력해주세요.';
    isValid = false;
  }
  if (!formData.reservationLimit || formData.reservationLimit < 4) {
    validationErrors.reservationLimit = '예약가능인원을 4 이상 입력해주세요.';
    isValid = false;
  }
  if (!formData.roadAddress.trim()) {
    validationErrors.roadAddress = '도로명주소를 입력해주세요.';
    isValid = false;
  }
  if (!formData.detailAddress.trim()) {
    validationErrors.detailAddress = '상세주소를 입력해주세요.';
    isValid = false;
  }
  if (!formData.description.trim()) {
    validationErrors.description = '식당 소개를 입력해주세요.';
    isValid = false;
  }

  if (!isValid) {
    alert('필수 입력 항목을 모두 채워주세요.');
    return;
  }

  // 3. Prepare data for submission
  const dataToSubmit = {
    ...formData,
    regularHolidayNumbers: selectedClosedDays.value,
    selectedTagIds: selectedTags.value.map((tag) => tag.tagId),
    menus: store.menus, // Include menus from the store
  };

  try {
    if (isEditMode.value) {
      console.log('Updating restaurant:', route.params.id, dataToSubmit);
    } else {
      console.log('Creating new restaurant:', dataToSubmit);
    }
    alert('저장되었습니다.');
    router.push('/business/restaurant-info');
  } catch (error) {
    console.error('저장 실패:', error);
    alert('저장에 실패했습니다.');
  }
};

const validationErrors = reactive({
  image: '',
  name: '',
  phone: '',
  openDate: '',
  openTime: '',
  closeTime: '',
  reservationLimit: '',
  roadAddress: '',
  detailAddress: '',
  description: '',
});

watch(formData, (newState) => {
  if (newState.name) validationErrors.name = '';
  if (newState.phone) validationErrors.phone = '';
  if (newState.openDate) validationErrors.openDate = '';
  if (newState.openTime) validationErrors.openTime = '';
  if (newState.closeTime) validationErrors.closeTime = '';
  if (newState.reservationLimit >= 4) validationErrors.reservationLimit = '';
  if (newState.roadAddress) validationErrors.roadAddress = '';
  if (newState.detailAddress) validationErrors.detailAddress = '';
  if (newState.description) validationErrors.description = '';
});

watch(restaurantImageFile, (newFile) => {
  if (newFile) validationErrors.image = '';
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
              <p
                v-if="validationErrors.image"
                class="text-red-500 text-sm mt-1"
              >
                {{ validationErrors.image }}
              </p>
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
                  <p
                    v-if="validationErrors.name"
                    class="text-red-500 text-sm mt-1"
                  >
                    {{ validationErrors.name }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >식당전화번호</label
                  >
                  <input
                    type="tel"
                    placeholder="식당 전화번호를 입력하세요(하이픈('-') 사용 필수)"
                    v-model="formData.phone"
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  />
                  <p
                    v-if="validationErrors.phone"
                    class="text-red-500 text-sm mt-1"
                  >
                    {{ validationErrors.phone }}
                  </p>
                </div>
              </div>

              <!-- Opening Date -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >개업일</label
                >
                <input
                  type="date"
                  v-model="formData.openDate"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
                <p
                  v-if="validationErrors.openDate"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.openDate }}
                </p>
              </div>

              <!-- Business Hours -->
              <div class="grid grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >영업시작시간</label
                  >
                  <input
                    type="time"
                    v-model="formData.openTime"
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  />
                  <p
                    v-if="validationErrors.openTime"
                    class="text-red-500 text-sm mt-1"
                  >
                    {{ validationErrors.openTime }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >영업종료시간</label
                  >
                  <input
                    type="time"
                    v-model="formData.closeTime"
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  />
                  <p
                    v-if="validationErrors.closeTime"
                    class="text-red-500 text-sm mt-1"
                  >
                    {{ validationErrors.closeTime }}
                  </p>
                </div>
              </div>

              <!-- Reservation Limit -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >예약가능인원 상한</label
                >
                <input
                  type="number"
                  placeholder="숫자를 입력하세요"
                  v-model.number="formData.reservationLimit"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
                <p
                  v-if="validationErrors.reservationLimit"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.reservationLimit }}
                </p>
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
                <p
                  v-if="validationErrors.roadAddress"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.roadAddress }}
                </p>
              </div>

              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >상세주소</label
                >
                <input
                  type="text"
                  placeholder="상세주소를 입력하세요"
                  v-model="formData.detailAddress"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
                <p
                  v-if="validationErrors.detailAddress"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.detailAddress }}
                </p>
              </div>

              <!-- Checkboxes -->
              <div class="grid grid-cols-2 gap-6">
                <div class="flex items-center gap-3">
                  <input
                    type="checkbox"
                    id="holiday"
                    v-model="formData.holidayAvailable"
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
                    v-model="formData.preorderAvailable"
                    class="w-5 h-5 rounded border-[#dee2e6]"
                  />
                  <label for="preorder" class="text-sm text-[#1e3a5f]">
                    선주문/선결제 가능
                  </label>
                </div>
              </div>

              <!-- Regular Closed Days -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2">
                  정기휴무일
                </label>
                <div class="flex flex-wrap gap-3">
                  <button
                    v-for="dayString in [
                      '월',
                      '화',
                      '수',
                      '목',
                      '금',
                      '토',
                      '일',
                    ]"
                    :key="dayString"
                    @click="toggleClosedDay(dayString)"
                    :class="`px-4 py-2 rounded-lg border transition-colors ${
                      isDaySelected(dayString)
                        ? 'gradient-primary text-white border-transparent'
                        : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                    }`"
                  >
                    {{ dayString }}
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
                <p
                  v-if="validationErrors.description"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.description }}
                </p>
              </div>
            </div>
          </div>

          <!-- Restaurant Tags -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">
              식당 태그 선택
            </h3>

            <!-- Characteristics Tags -->
            <div
              v-for="(tags, categoryName) in tagCategories"
              :key="categoryName"
              class="mb-6"
            >
              <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">
                {{ tagCategoryDisplayNames[categoryName] || categoryName }}
              </h4>
              <div class="flex flex-wrap gap-3">
                <button
                  v-for="tag in tags"
                  :key="tag.tagId"
                  @click="toggleTag(tag)"
                  :class="`px-4 py-2 rounded-lg border transition-colors ${
                    isTagSelected(tag)
                      ? 'gradient-primary text-white border-transparent'
                      : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                  }`"
                >
                  #{{ tag.content }}
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
                    <td class="px-12 py-4 text-sm text-[#6c757d]">
                      {{ menu.type }}
                    </td>
                    <td class="px-12 py-4 text-sm text-[#6c757d]">
                      {{ menu.price.toLocaleString() }}원
                    </td>
                    <td class="px-4 py-4 text-left">
                      <div class="flex justify-start gap-2">
                        <RouterLink
                          :to="`/business/restaurant-info/menu/edit/${menu.id}`"
                          class="px-3 py-2 border border-[#dee2e6] rounded-lg text-[#1e3a5f] text-sm hover:bg-[#f8f9fa] transition-colors"
                        >
                          수정
                        </RouterLink>
                        <button
                          @click="store.deleteMenu(menu.id)"
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
