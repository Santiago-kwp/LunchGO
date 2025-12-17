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

// 페이지네이션
const currentPage = ref(1);
const itemsPerPage = 5; // 한 페이지에 5개씩 보여주기

const totalPages = computed(() => {
  return Math.ceil(store.menus.length / itemsPerPage);
});

const paginatedMenus = computed(() => {
  const startIndex = (currentPage.value - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  return store.menus.slice(startIndex, endIndex);
});

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

const isEditMode = computed(() => !!route.params.id);
const pageTitle = computed(() =>
  isEditMode.value ? '식당 정보 수정' : '식당 정보 등록'
);
const submitButtonText = computed(() =>
  isEditMode.value ? '수정하기' : '등록하기'
);

const avgMainPrice = computed(() => {
  const mainMenus = store.menus.filter((menu) => menu.type === '주메뉴');
  if (mainMenus.length === 0) {
    return 0;
  }
  const totalPrice = mainMenus.reduce((sum, menu) => sum + menu.price, 0);
  return Math.round(totalPrice / mainMenus.length);
});

const formData = reactive({
  name: '',
  phone: '',
  roadAddress: '',
  detailAddress: '',
  description: '',
  avgMainPrice: 0,
  reservationLimit: '',
  holidayAvailable: false,
  preorderAvailable: false,
  openTime: '',
  closeTime: '',
  openDate: '',
});

const restaurantImageFile = ref(null);
const restaurantFileInput = ref(null);

// restaurantImageUrl을 store와 연동된 computed 속성으로 변경
const restaurantImageUrl = computed(() => store.restaurantInfo?.images?.[0]?.imageUrl || null);

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

// 유효한 전화번호 접두사 목록 (지역번호 및 휴대폰)
const tagCategoryDisplayNames = {
  MENUTYPE: '식당 종류',
  TABLETYPE: '테이블 옵션',
  ATMOSPHERE: '식당 분위기',
  FACILITY: '편의시설',
};

const handleRestaurantFileChange = (e) => {
  const file = e.target.files[0];
  if (file) {
    restaurantImageFile.value = file;
    // 로컬 URL을 생성하고 store 상태를 업데이트
    const localUrl = URL.createObjectURL(file);
    store.setDraftImageUrl(localUrl);
  }
};

const triggerRestaurantFileInput = () => {
  console.log('triggerRestaurantFileInput called.');
  console.log('restaurantFileInput ref value:', restaurantFileInput.value);
  if (restaurantFileInput.value) {
    restaurantFileInput.value.click();
  } else {
    console.error('restaurantFileInput ref is null. Cannot trigger click.');
  }
};

const clearRestaurantImage = () => {
  restaurantImageFile.value = null;
  // store의 이미지 URL 상태를 업데이트
  store.setDraftImageUrl(null);
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

// API: GET /api/tags
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

// API: GET /api/restaurants/{id}
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
          { id: 101, name: '한정식 A코스', type: '주메뉴', price: 50000, imageUrl: '/korean-course-meal-plating.jpg' },
          { id: 102, name: '한정식 B코스', type: '주메뉴', price: 75000, imageUrl: '/korean-fine-dining.jpg' },
          { id: 103, name: '떡갈비', type: '서브메뉴', price: 25000, imageUrl: '/placeholder.svg' },
          { id: 104, name: '해물파전', type: '서브메뉴', price: 20000, imageUrl: '/placeholder.svg' },
          { id: 105, name: '수정과', type: '기타(디저트, 음료)', price: 5000, imageUrl: '/placeholder.svg' },
          { id: 106, name: '식혜', type: '기타(디저트, 음료)', price: 5000, imageUrl: '/placeholder.svg' },
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
    // 스토어에 정보가 없거나 다른 식당 정보가 들어있으면 새로 API 호출
    if (!store.restaurantInfo || store.restaurantInfo.restaurantId !== restaurantId) {
      const restaurantData = await fetchRestaurantData(restaurantId);
      store.loadRestaurant(restaurantData); // API 응답으로 스토어 전체를 설정
    }
    
    // 항상 스토어의 데이터로 폼과 UI 상태를 채움
    if (store.restaurantInfo) {
      Object.assign(formData, store.restaurantInfo);
      if (store.restaurantInfo.images && store.restaurantInfo.images.length > 0) {
        // This line is now redundant because of the computed property, but safe to keep
        // restaurantImageUrl.value = store.restaurantInfo.images[0].imageUrl; 
        restaurantImageFile.value = new File([], 'mock-restaurant-image.png');
      }
      selectedClosedDays.value = store.restaurantInfo.regularHolidays.map(
        (h) => h.dayOfWeek
      );
      selectedTags.value = store.restaurantInfo.tags;
    }
  } else {
    // 등록 모드에서는 페이지를 다시 방문해도 store의 상태를 유지합니다.
    // store는 router.beforeEach 네비게이션 가드에 의해 관리됩니다.
    // 또한, 폼 데이터가 store에 이미 있을 수 있으므로 onMounted에서 다시 채워줍니다.
    if (store.restaurantInfo) {
      Object.assign(formData, store.restaurantInfo);
       if (store.restaurantInfo.images && store.restaurantInfo.images.length > 0) {
        restaurantImageFile.value = new File([], 'mock-restaurant-image.png');
      }
      selectedClosedDays.value = store.restaurantInfo.regularHolidays || [];
      selectedTags.value = store.restaurantInfo.tags || [];
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
  } else {
    // 유효한 접두사와 전화번호 형식을 모두 검사하는 정규식
    const phoneRegex =
      /^(02|010|011|01[6-9]|03[1-3]|04[1-4]|05[1-5]|06[1-4]|070|080|050)-\d{3,4}-\d{4}$/;
    if (!phoneRegex.test(formData.phone)) {
      validationErrors.phone =
        '유효하지 않은 전화번호 형식 또는 지역번호입니다.';
      isValid = false;
    }
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
    validationErrors.reservationLimit = '예약가능인원을 4인 이상 입력해주세요.';
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

  const hasMainMenu = store.menus.some((menu) => menu.type === '주메뉴');
  if (!hasMainMenu) {
    validationErrors.menus = '주메뉴를 1개 이상 등록해주세요.';
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

  // 추후 API를 통해 등록/수정 요청을 보낼 부분
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
  menus: '',
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

watch(
  () => store.menus,
  (newMenus) => {
    if (newMenus.some((menu) => menu.type === '주메뉴')) {
      validationErrors.menus = '';
    }
  },
  { deep: true }
);

// avgMainPrice computed 속성의 변화를 감지하여 formData.avgMainPrice 업데이트
watch(avgMainPrice, (newAvg) => {
  formData.avgMainPrice = newAvg;
});

// 현재 페이지의 메뉴가 모두 삭제되었을 때 이전 페이지로 이동
watch(paginatedMenus, (newPaginatedMenus) => {
  if (newPaginatedMenus.length === 0 && currentPage.value > 1) {
    currentPage.value--;
  }
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
                    placeholder="하이픈(-)을 포함하여 입력하세요"
                    v-model="formData.phone"
                    maxlength="13"
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
            <div class="flex items-center justify-between mb-6">
              <h3 class="text-xl font-bold text-[#1e3a5f]">식당메뉴</h3>
              <span class="text-lg font-semibold text-[#FF6B4A]">
                주메뉴 평균가: {{ avgMainPrice.toLocaleString() }}원
              </span>
            </div>
            <p
              v-if="validationErrors.menus"
              class="text-red-500 text-sm mb-4 text-center"
            >
              {{ validationErrors.menus }}
            </p>

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
