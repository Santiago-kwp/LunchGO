<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, X, Upload, Plus } from 'lucide-vue-next';
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';

const route = useRoute();
const router = useRouter();
const restaurantId = route.params.id || '1';

// 단계 관리 (1: 태그 선택, 2: 리뷰 작성)
const currentStep = ref(1);

// 영수증 업로드 모달
const isReceiptModalOpen = ref(false);

// 사진 업로드 모달
const isPhotoModalOpen = ref(false);

// 방문 정보
const visitInfo = ref({
  restaurantName: '식당명',
  visitNumber: 1, // n번째 방문
});

// 영수증 정보
const receipt = ref({
  date: '2025년 11월 15일 (금)',
  partySize: 8,
  totalAmount: 111000,
  uploaded: false, // 영수증 업로드 여부
  items: [
    { name: '메뉴명1', quantity: 1, price: 18000 },
    { name: '메뉴명2', quantity: 2, price: 9000 },
    { name: '메뉴명3', quantity: 3, price: 11000 },
  ],
});

// 선택된 태그
const selectedTags = ref([]);

// 태그 카테고리
const tagCategories = ref([
  {
    id: 'speed',
    icon: '🏃',
    name: '속도/효율성',
    tags: [
      '주문 즉시 조리 시작해요',
      '계산이 빨라요',
      '웨이팅 관리가 잘 돼요',
      '음식이 동시에 나와요',
    ],
  },
  {
    id: 'space',
    icon: '🪑',
    name: '공간/분위기',
    tags: [
      '인테리어가 세련돼요',
      '조명이 아늑해요',
      '아이 동반하기 좋아요',
      '야외 테라스가 있어요',
      '음악이 적당해요',
    ],
  },
  {
    id: 'taste',
    icon: '🍲',
    name: '맛/가성비',
    tags: [
      '재료가 신선해요',
      '가격 대비 만족스러워요',
      '시그니처 메뉴가 있어요',
      '디저트가 맛있어요',
      '술과 안주 궁합이 좋아요',
    ],
  },
  {
    id: 'service',
    icon: '🤝',
    name: '서비스/기타',
    tags: [
      '직원들이 적극적으로 도와줘요',
      '메뉴 설명을 잘 해줘요',
      '결제 방식이 다양해요 (QR, 간편결제 등)',
      '반려동물 동반 가능해요',
      '청결 관리가 잘 돼요',
    ],
  },
]);

// 리뷰 작성 데이터
const reviewPhotos = ref([]);
const reviewText = ref('');

// 태그 선택/해제
const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag);
  if (index > -1) {
    selectedTags.value.splice(index, 1);
  } else {
    if (selectedTags.value.length < 5) {
      selectedTags.value.push(tag);
    }
  }
};

// 태그 선택 여부 확인
const isTagSelected = (tag) => {
  return selectedTags.value.includes(tag);
};

// 사진 업로드 모달 열기
const openPhotoModal = () => {
  isPhotoModalOpen.value = true;
};

// 사진 업로드 모달 닫기
const closePhotoModal = () => {
  isPhotoModalOpen.value = false;
};

// 사진 추가
const handlePhotoAdd = (event) => {
  const files = event.target.files;
  if (!files || files.length === 0) return;

  Array.from(files).forEach((file) => {
    if (reviewPhotos.value.length < 5) {
      const reader = new FileReader();
      reader.onload = (e) => {
        reviewPhotos.value.push({
          id: Date.now() + Math.random(),
          url: e.target.result,
          file: file,
        });
      };
      reader.readAsDataURL(file);
    }
  });

  closePhotoModal();
};

// 사진 제거
const removePhoto = (photoId) => {
  reviewPhotos.value = reviewPhotos.value.filter((p) => p.id !== photoId);
};

// 다음 단계로 이동
const goToNextStep = () => {
  if (currentStep.value === 1) {
    currentStep.value = 2;
  }
};

// 이전 단계로 이동
const goToPreviousStep = () => {
  if (currentStep.value === 2) {
    currentStep.value = 1;
  } else {
    router.back();
  }
};

// 리뷰 등록
const submitReview = () => {
  // 실제 구현 시 API 호출
  console.log('리뷰 등록:', {
    tags: selectedTags.value,
    photos: reviewPhotos.value,
    text: reviewText.value,
  });
  router.push(`/restaurant/${restaurantId}/reviews`);
};

// 영수증 업로드 모달 열기
const openReceiptModal = () => {
  isReceiptModalOpen.value = true;
};

// 영수증 업로드 모달 닫기
const closeReceiptModal = () => {
  isReceiptModalOpen.value = false;
};

// 영수증 업로드
const handleReceiptUpload = () => {
  // 실제 구현 시 파일 업로드 로직
  receipt.value.uploaded = true;
  closeReceiptModal();
};

// 마우스 드래그 스크롤 설정
const setupDragScroll = () => {
  nextTick(() => {
    const scrollContainers = document.querySelectorAll('.tag-category-scroll');

    scrollContainers.forEach((container) => {
      let isDown = false;
      let startX;
      let scrollLeft;

      container.addEventListener('mousedown', (e) => {
        isDown = true;
        container.classList.add('cursor-grabbing');
        startX = e.pageX - container.offsetLeft;
        scrollLeft = container.scrollLeft;
      });

      container.addEventListener('mouseleave', () => {
        isDown = false;
        container.classList.remove('cursor-grabbing');
      });

      container.addEventListener('mouseup', () => {
        isDown = false;
        container.classList.remove('cursor-grabbing');
      });

      container.addEventListener('mousemove', (e) => {
        if (!isDown) return;
        e.preventDefault();
        const x = e.pageX - container.offsetLeft;
        const walk = (x - startX) * 2;
        container.scrollLeft = scrollLeft - walk;
      });
    });
  });
};

onMounted(() => {
  setupDragScroll();
});
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- 헤더 -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div
        class="max-w-[500px] mx-auto px-4 h-14 flex items-center justify-end"
      >
        <button @click="router.push(`/restaurant/${restaurantId}/reviews`)">
          <X class="w-6 h-6 text-[#1e3a5f]" />
        </button>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-24">
      <!-- Step 1: 태그 선택 -->
      <div v-if="currentStep === 1" class="p-4">
        <!-- 식당명 & 방문 횟수 -->
        <h1 class="text-2xl font-bold text-[#1e3a5f] mb-2">
          {{ visitInfo.restaurantName }}
        </h1>
        <p class="text-sm text-[#495057] mb-4">
          {{ visitInfo.visitNumber }} 번째 방문이네요!
        </p>

        <!-- 영수증 정보 카드 -->
        <Card class="mb-4 p-4 rounded-2xl border-[#e9ecef]">
          <div class="flex items-start justify-between mb-2">
            <div>
              <h2 class="text-base font-semibold text-[#1e3a5f] mb-1">
                {{ receipt.date }}
              </h2>
              <p class="text-sm text-[#495057]">
                · {{ receipt.partySize }}명 ·
                {{ receipt.totalAmount.toLocaleString() }} 원
              </p>
            </div>
            <button
              v-if="receipt.uploaded"
              class="px-3 py-1.5 rounded-full bg-purple-600 text-white text-xs font-medium whitespace-nowrap"
            >
              영수증 업로드
            </button>
            <span
              v-else
              class="px-3 py-1.5 rounded-full bg-gray-100 text-gray-500 text-xs font-medium whitespace-nowrap"
            >
              영수증 업로드 대기
            </span>
          </div>

          <!-- 메뉴 상세 내역 -->
          <div class="space-y-2 mb-3">
            <div
              class="grid grid-cols-12 gap-2 text-xs font-semibold text-[#6c757d] pb-2 border-b border-[#e9ecef]"
            >
              <div class="col-span-6">메뉴이름</div>
              <div class="col-span-2 text-center">수량</div>
              <div class="col-span-4 text-right">단가</div>
            </div>
            <div
              v-for="(item, index) in receipt.items"
              :key="index"
              class="grid grid-cols-12 gap-2 text-xs text-[#495057]"
            >
              <div class="col-span-6">· {{ item.name }}</div>
              <div class="col-span-2 text-center">{{ item.quantity }}</div>
              <div class="col-span-4 text-right">
                {{ (item.price * item.quantity).toLocaleString() }}원
              </div>
            </div>
          </div>

          <!-- 총 금액 -->
          <div
            class="flex justify-between items-center pt-3 border-t-2 border-[#1e3a5f]"
          >
            <span class="text-sm font-semibold text-[#1e3a5f]">총 금액</span>
            <span class="text-base font-bold text-[#1e3a5f]"
              >{{ receipt.totalAmount.toLocaleString() }}원</span
            >
          </div>

          <!-- 영수증 업로드 버튼 (업로드 안 된 경우만) -->
          <button
            v-if="!receipt.uploaded"
            @click="openReceiptModal"
            class="w-full mt-3 py-3 border-2 border-[#dee2e6] rounded-xl text-sm font-medium text-[#495057] hover:border-purple-300 hover:bg-purple-50 transition-colors"
          >
            영수증 업로드 하기
          </button>
        </Card>

        <!-- 태그 선택 안내 & 카테고리 -->
        <Card class="mb-4 p-4 rounded-2xl border-[#e9ecef]">
          <p class="text-xs text-[#495057] mb-2 text-center">
            이 방문 정보는 리뷰와 함께 공개됩니다.<br />
            다른 이용자에게 도움이 되도록 선택해주세요!
          </p>
          <p class="text-sm font-semibold text-[#1e3a5f] text-center mb-4">
            어떤 점이 좋았나요?<br />
            이 곳에 어울리는 키워드를 골라주세요. (1개 ~ 5개)
          </p>

          <!-- 태그 카테고리 (가로 스크롤, 카테고리별 세로 배치) -->
          <div
            class="overflow-x-auto hide-scrollbar tag-category-scroll cursor-grab select-none"
          >
            <div class="flex gap-6 pb-2">
              <div
                v-for="category in tagCategories"
                :key="category.id"
                class="flex-shrink-0"
              >
                <h4
                  class="text-xs font-semibold text-[#1e3a5f] mb-2 whitespace-nowrap"
                >
                  {{ category.icon }} {{ category.name }}
                </h4>
                <!-- 태그 버튼들 (세로 나열) -->
                <div class="flex flex-col gap-2">
                  <button
                    v-for="tag in category.tags"
                    :key="tag"
                    @click="toggleTag(tag)"
                    :class="[
                      'px-3 py-1.5 rounded-full text-xs font-medium border transition-colors whitespace-nowrap',
                      isTagSelected(tag)
                        ? 'bg-rose-50 text-rose-600 border-rose-200'
                        : 'bg-white text-[#495057] border-[#dee2e6] hover:border-rose-200',
                    ]"
                  >
                    {{ tag }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </Card>
      </div>

      <!-- Step 2: 리뷰 작성 -->
      <div v-if="currentStep === 2" class="p-4">
        <!-- 식당명 -->
        <h1 class="text-2xl font-bold text-[#1e3a5f] mb-3">
          {{ visitInfo.restaurantName }}
        </h1>

        <!-- 방문 정보 -->
        <div class="mb-3 text-sm text-[#495057]">
          <p class="mb-1">{{ receipt.date }}</p>
          <p>
            · {{ receipt.partySize }}명 ·
            {{ receipt.totalAmount.toLocaleString() }} 원
          </p>
        </div>

        <!-- 선택된 태그 표시 -->
        <div class="mb-4">
          <div class="flex flex-wrap gap-2">
            <span
              v-for="tag in selectedTags"
              :key="tag"
              class="px-3 py-1.5 rounded-full text-xs font-medium bg-rose-50 text-rose-600 border border-rose-200"
            >
              {{ tag }}
            </span>
          </div>
        </div>

        <!-- 사진 추가 영역 -->
        <Card class="mb-3 p-4 rounded-2xl border-[#e9ecef]">
          <h3 class="text-sm font-semibold text-[#1e3a5f] mb-2">
            사진을 추가해주세요
          </h3>
          <div class="flex gap-2 overflow-x-auto pb-2">
            <!-- 기존 사진들 -->
            <div
              v-for="photo in reviewPhotos"
              :key="photo.id"
              class="relative flex-shrink-0 w-32 h-32 rounded-lg overflow-hidden bg-gray-100"
            >
              <img
                :src="photo.url"
                alt="리뷰 사진"
                class="w-full h-full object-cover"
              />
              <button
                @click="removePhoto(photo.id)"
                class="absolute top-1 right-1 w-6 h-6 bg-black/50 rounded-full flex items-center justify-center"
              >
                <X class="w-4 h-4 text-white" />
              </button>
            </div>
            <!-- 사진 추가 버튼 -->
            <button
              v-if="reviewPhotos.length < 5"
              @click="openPhotoModal"
              class="flex-shrink-0 w-48 h-32 border-2 border-dashed border-[#dee2e6] rounded-lg flex flex-col items-center justify-center hover:border-green-300 hover:bg-green-50 transition-colors"
            >
              <Plus
                class="w-12 h-12 text-green-500 bg-green-50 rounded-full p-2 mb-1"
              />
              <span class="text-xs text-[#6c757d]"
                >최대 5장까지 업로드 가능</span
              >
            </button>
          </div>
        </Card>

        <!-- 리뷰 작성 영역 -->
        <Card class="mb-4 p-4 rounded-2xl border-[#e9ecef]">
          <h3 class="text-sm font-semibold text-[#1e3a5f] mb-2">
            리뷰를 작성해 주세요
          </h3>
          <p class="text-xs text-[#6c757d] mb-2 leading-relaxed">
            리뷰 작성 시 유의사항 한 번 확인하기!<br />
            욕설, 비방, 명예훼손성 표현은 누군가에게 상처가 될 수 있습니다.
          </p>
          <textarea
            v-model="reviewText"
            placeholder=""
            maxlength="400"
            class="w-full h-32 p-3 border border-[#dee2e6] rounded-lg text-sm resize-none focus:outline-none focus:border-purple-300"
          ></textarea>
          <div class="text-right text-xs text-[#6c757d] mt-1">
            {{ reviewText.length }}/400
          </div>
        </Card>
      </div>
    </main>

    <!-- 영수증 업로드 모달 -->
    <div
      v-if="isReceiptModalOpen"
      class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
      @click="closeReceiptModal"
    >
      <div class="bg-white rounded-2xl p-6 max-w-md w-full" @click.stop>
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-[#1e3a5f]">영수증 업로드</h3>
          <button @click="closeReceiptModal">
            <X class="w-6 h-6 text-[#6c757d]" />
          </button>
        </div>

        <div class="mb-4">
          <label
            class="flex flex-col items-center justify-center w-full h-48 border-2 border-dashed border-[#dee2e6] rounded-xl cursor-pointer hover:border-purple-300 hover:bg-purple-50 transition-colors"
          >
            <Upload class="w-12 h-12 text-[#6c757d] mb-2" />
            <span class="text-sm text-[#495057]">클릭하여 파일 선택</span>
            <span class="text-xs text-[#6c757d] mt-1"
              >JPG, PNG (최대 10MB)</span
            >
            <input
              type="file"
              class="hidden"
              accept="image/*"
              @change="handleReceiptUpload"
            />
          </label>
        </div>

        <div class="flex gap-2">
          <button
            @click="closeReceiptModal"
            class="flex-1 py-3 border border-[#dee2e6] rounded-lg text-sm font-medium text-[#495057] hover:bg-gray-50"
          >
            취소
          </button>
          <button
            @click="handleReceiptUpload"
            class="flex-1 py-3 bg-purple-600 text-white rounded-lg text-sm font-medium hover:bg-purple-700"
          >
            업로드
          </button>
        </div>
      </div>
    </div>

    <!-- 리뷰 사진 업로드 모달 -->
    <div
      v-if="isPhotoModalOpen"
      class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
      @click="closePhotoModal"
    >
      <div class="bg-white rounded-2xl p-6 max-w-md w-full" @click.stop>
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-[#1e3a5f]">리뷰 사진 업로드</h3>
          <button @click="closePhotoModal">
            <X class="w-6 h-6 text-[#6c757d]" />
          </button>
        </div>

        <div class="mb-4">
          <label
            class="flex flex-col items-center justify-center w-full h-48 border-2 border-dashed border-[#dee2e6] rounded-xl cursor-pointer hover:border-green-300 hover:bg-green-50 transition-colors"
          >
            <Upload class="w-12 h-12 text-green-600 mb-2" />
            <span class="text-sm text-[#495057]">클릭하여 사진 선택</span>
            <span class="text-xs text-[#6c757d] mt-1"
              >JPG, PNG (최대 5장, 각 10MB)</span
            >
            <input
              type="file"
              class="hidden"
              accept="image/*"
              multiple
              @change="handlePhotoAdd"
            />
          </label>
        </div>

        <p class="text-xs text-[#6c757d] mb-4 text-center">
          현재 {{ reviewPhotos.length }}/5장 업로드됨
        </p>

        <div class="flex gap-2">
          <button
            @click="closePhotoModal"
            class="flex-1 py-3 border border-[#dee2e6] rounded-lg text-sm font-medium text-[#495057] hover:bg-gray-50"
          >
            취소
          </button>
        </div>
      </div>
    </div>

    <!-- Step 1: 우측 하단 다음 버튼 -->
    <button
      v-if="currentStep === 1"
      @click="goToNextStep"
      :disabled="selectedTags.length === 0"
      :class="[
        'fixed bottom-6 right-6 z-50 w-16 h-16 rounded-full shadow-lg font-semibold text-lg transition-all',
        selectedTags.length > 0
          ? 'bg-blue-600 text-white hover:bg-blue-700 hover:scale-110'
          : 'bg-gray-300 text-gray-500 cursor-not-allowed',
      ]"
    >
      다음
    </button>

    <!-- Step 2: 하단 버튼 -->
    <div
      v-if="currentStep === 2"
      class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-40"
    >
      <div class="max-w-[500px] mx-auto px-4 py-3 flex gap-2">
        <button
          @click="goToPreviousStep"
          class="flex-1 h-12 bg-white border border-[#dee2e6] text-[#495057] rounded-lg font-medium hover:bg-gray-50"
        >
          &lt; 이전
        </button>
        <button
          @click="submitReview"
          :disabled="reviewText.trim().length === 0"
          :class="[
            'flex-1 h-12 rounded-lg font-medium transition-colors',
            reviewText.trim().length > 0
              ? 'bg-blue-600 text-white hover:bg-blue-700'
              : 'bg-gray-200 text-gray-400 cursor-not-allowed',
          ]"
        >
          등록
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 스크롤바 숨기기 */
.hide-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.hide-scrollbar::-webkit-scrollbar {
  display: none;
}

/* 드래그 스크롤 커서 */
.cursor-grab {
  cursor: grab;
}

.cursor-grabbing {
  cursor: grabbing;
}

/* 드래그 중 텍스트 선택 방지 */
.select-none {
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}
</style>
