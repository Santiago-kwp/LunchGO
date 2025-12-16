<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { Upload, X } from 'lucide-vue-next';
import { useRouter, useRoute } from 'vue-router';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';

// 1. 라우터 및 라우트
const router = useRouter();
const route = useRoute();

// 2. 모드 및 페이지 제목
const isEditMode = computed(() => !!route.params.id);
const pageTitle = computed(() =>
  isEditMode.value ? '식당 메뉴 정보 편집' : '식당 메뉴 정보 추가'
);

// 3. 상태 관련 변수
const imageFile = ref(null); // 실제 이미지 파일 객체
const imageUrl = ref(null); // 이미지 미리보기 URL
const fileInput = ref(null);
const menuData = reactive({
  name: '',
  category: '', // DB의 'category' ('MAIN', 'SUB', 'OTHER')
  price: 0,
});
// 선택된 재료 태그 (객체 배열)
const selectedIngredientTags = ref([]);

// 4. 데이터 변수
// DB의 'tag' 테이블 (category='INGREDIENT')에서 가져올 재료 태그 목록
const allIngredientTags = ref([]);
// DB의 'menu' 테이블 'category' 컬럼과 매핑
const menuTypes = ref([
  { value: 'MAIN', text: '주메뉴' },
  { value: 'SUB', text: '서브메뉴' },
  { value: 'OTHER', text: '기타(디저트, 음료)' },
]);

// 5. 함수
const handleFileChange = (e) => {
  const file = e.target.files[0];
  if (file) {
    imageFile.value = file;
    imageUrl.value = URL.createObjectURL(file);
  }
};

const triggerFileInput = () => {
  fileInput.value.click();
};

const clearImage = () => {
  imageFile.value = null;
  imageUrl.value = null;
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

const toggleIngredientTag = (tag) => {
  const index = selectedIngredientTags.value.findIndex(t => t.tagId === tag.tagId);
  if (index > -1) {
    selectedIngredientTags.value.splice(index, 1);
  } else {
    selectedIngredientTags.value.push(tag);
  }
};

const isTagSelected = (tag) => {
  return selectedIngredientTags.value.some(t => t.tagId === tag.tagId);
}

// 6. 라이프사이클 훅
onMounted(() => {
  // --- Mock API Fetch (DTO는 camelCase 사용) ---
  // 실제로는 여기서 API를 호출하여 데이터를 가져옵니다.

  // 1. (모든 메뉴 공통) 재료 태그 목록 가져오기 (원래는 페이지 진입 시 항상 실행)
  // GET /api/tags?category=INGREDIENT
  const mockAllIngredientTags = [
    { tagId: 1, content: '견과류' },
    { tagId: 2, content: '우유' },
    { tagId: 3, content: '계란' },
    { tagId: 4, content: '밀' },
    { tagId: 5, content: '대두' },
    { tagId: 6, content: '갑각류' },
    { tagId: 7, content: '조개류' },
    { tagId: 8, content: '생선' },
    { tagId: 9, content: '메밀' },
    { tagId: 10, content: '고수' },
    { tagId: 11, content: '오이' },
    { tagId: 12, content: '파프리카' },
    { tagId: 13, content: '미나리' },
    { tagId: 14, content: '당근' },
  ];
  allIngredientTags.value = mockAllIngredientTags;

  // 2. (수정 모드일 경우) 기존 메뉴 정보 가져오기
  if (isEditMode.value) {
    console.log('Editing menu with ID:', route.params.id);
    
    // GET /api/menus/{route.params.id}
    const mockMenuDataFromApi = {
      name: '기존 메뉴',
      category: 'MAIN', // DB ENUM 값
      price: 25000,
      imageUrl: '/italian-pasta-dish.png', // menu_image 테이블 -> DTO
      tags: [ // menu_tag_map과 tag 테이블 join 결과 -> DTO
        { tagId: 4, content: '밀' },
        { tagId: 3, content: '계란' },
      ],
    };

    // 상태에 데이터 채우기
    menuData.name = mockMenuDataFromApi.name;
    menuData.category = mockMenuDataFromApi.category;
    menuData.price = mockMenuDataFromApi.price;
    imageUrl.value = mockMenuDataFromApi.imageUrl;
    selectedIngredientTags.value = mockMenuDataFromApi.tags;
    
    // 실제 파일이 아니므로, 업로드된 것처럼 모의 파일 객체를 만듦
    if (mockMenuDataFromApi.imageUrl) {
        imageFile.value = new File([], 'mock-image.png'); 
    }
  }
});

// 7. 저장 함수
const saveMenu = () => {
  if (!imageFile.value) {
    alert('메뉴 사진을 등록해주세요.');
    return;
  }
  if (!menuData.name.trim()) {
    alert('메뉴 이름을 입력해주세요.');
    return;
  }
  if (!menuData.category) {
    alert('메뉴 타입을 선택해주세요.');
    return;
  }
  if (
    menuData.price === null ||
    menuData.price === undefined ||
    menuData.price <= 0
  ) {
    alert('가격을 올바르게 입력해주세요.');
    return;
  }
  
  // --- Mock API Call (DTO는 camelCase 사용) ---
  // 실제로는 여기서 API로 데이터를 전송합니다.
  const payload = {
    ...menuData,
    // imageFile.value는 별도로 FormData로 전송
    selectedTagIds: selectedIngredientTags.value.map(tag => tag.tagId),
  };
  console.log('Saving menu data:', payload);


  if (isEditMode.value) {
    alert('메뉴가 수정되었습니다.');
  } else {
    alert('메뉴가 추가되었습니다.');
  }
  router.back();
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
        <div class="max-w-2xl mx-auto">
          <!-- Page Title with Close Button -->
          <div class="flex items-center justify-between mb-8">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">
              {{ pageTitle }}
            </h2>
            <button
              @click="router.back()"
              class="p-2 hover:bg-[#f8f9fa] rounded-lg transition-colors"
            >
              <X class="w-6 h-6 text-[#6c757d]" />
            </button>
          </div>

          <!-- Menu Form Card -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <div class="space-y-6">
              <!-- Menu Image Upload -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >메뉴사진</label
                >
                <div
                  @click="triggerFileInput"
                  class="mb-8 border-2 border-[#e9ecef] rounded-xl overflow-hidden bg-[#f8f9fa] relative cursor-pointer"
                >
                  <div class="aspect-[2/1] flex items-center justify-center">
                    <div v-if="imageUrl" class="w-full h-full">
                      <img
                        :src="imageUrl"
                        alt="메뉴 이미지 미리보기"
                        class="w-full h-full object-cover rounded-lg"
                      />
                      <button
                        @click.stop="clearImage"
                        class="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full hover:bg-red-600 transition-colors"
                      >
                        <X class="w-4 h-4" />
                      </button>
                    </div>
                    <div v-else class="text-center">
                      <Upload class="w-12 h-12 text-[#6c757d] mx-auto mb-3" />
                      <p class="text-sm text-[#6c757d]">이미지</p>
                    </div>
                  </div>
                </div>
                <input
                  type="file"
                  ref="fileInput"
                  @change="handleFileChange"
                  class="hidden"
                  accept="image/*"
                  required
                />
              </div>

              <!-- Menu Name -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >메뉴이름</label
                >
                <input
                  type="text"
                  placeholder="메뉴이름 입력"
                  v-model="menuData.name"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  required
                />
              </div>

              <!-- Menu Type -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >메뉴타입</label
                >
                <select
                  v-model="menuData.category"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  required
                >
                  <option disabled value="">메뉴 타입 선택</option>
                  <option
                    v-for="typeOption in menuTypes"
                    :key="typeOption.value"
                    :value="typeOption.value"
                  >
                    {{ typeOption.text }}
                  </option>
                </select>
              </div>

              <!-- Menu Price -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >가격</label
                >
                <input
                  type="number"
                  placeholder="가격 입력"
                  v-model="menuData.price"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  required
                />
              </div>

              <!-- Ingredient Tags -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-3">
                  재료 특이사항
                </label>
                <div class="flex flex-wrap gap-3">
                  <button
                    v-for="tag in allIngredientTags"
                    :key="tag.tagId"
                    @click="toggleIngredientTag(tag)"
                    :class="`px-4 py-2 rounded-lg border transition-colors ${
                      isTagSelected(tag)
                        ? 'gradient-primary text-white border-transparent'
                        : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                    }`"
                  >
                    {{ tag.content }}
                  </button>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex justify-end gap-3 mt-8">
              <button
                @click="router.back()"
                class="px-8 py-3 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
              >
                취소
              </button>
              <button
                @click="saveMenu"
                class="px-8 py-3 gradient-primary text-white rounded-lg font-semibold hover:opacity-90 transition-opacity"
              >
                저장
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
select {
  -webkit-appearance: none; /* Safari and Chrome */
  -moz-appearance: none; /* Firefox */
  appearance: none; /* Other modern browsers */
  background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20' fill='none' stroke='%236c757d' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  background-size: 1.25em auto;
  padding-right: 2.5rem; /* 화살표 공간 확보 */
}
</style>
