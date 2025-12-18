<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { RouterLink, useRoute } from 'vue-router'; // Import useRoute to get dynamic params
import {
  ArrowLeft,
  MapPin,
  Star,
  Clock,
  Users,
  Phone,
  ChevronRight,
  ChevronLeft,
  X,
  Plus,
  Minus,
  Home as HomeIcon,
} from 'lucide-vue-next';
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';
import { loadKakaoMaps } from '@/utils/kakao';
import { restaurants as restaurantData, getRestaurantById } from '@/data/restaurants';

const route = useRoute();
const restaurantId = route.params.id || '1'; // Default to '1' if id is not available
const restaurantInfo = ref(getRestaurantById(restaurantId) || restaurantData[0]);

const currentImageIndex = ref(0);

const defaultGallery = [
  { url: '/modern-korean-restaurant-interior.jpg', alt: '식당 메인 이미지' },
  { url: '/elegant-dining-room-setup.jpg', alt: '식당 내부 전경' },
  { url: '/korean-course-meal-plating.jpg', alt: '대표 메뉴 이미지' },
  { url: '/restaurant-private-room-atmosphere.jpg', alt: '식당 분위기' },
];

const restaurantImages = ref(
  restaurantInfo.value?.gallery?.length
    ? restaurantInfo.value.gallery.map((url, index) => ({
        url,
        alt: `${restaurantInfo.value?.name || '식당'} 이미지 ${index + 1}`,
      }))
    : defaultGallery,
);

const handlePrevImage = () => {
  currentImageIndex.value =
    currentImageIndex.value === 0
      ? restaurantImages.value.length - 1
      : currentImageIndex.value - 1;
};

const handleNextImage = () => {
  currentImageIndex.value =
    currentImageIndex.value === restaurantImages.value.length - 1
      ? 0
      : currentImageIndex.value + 1;
};

const defaultMenus = [
  {
    name: 'A코스',
    price: '35,000원',
    description: '전채 + 메인 + 디저트',
    image: '/korean-appetizer-main-dessert.jpg',
  },
  {
    name: 'B코스',
    price: '45,000원',
    description: '전채 + 메인 + 디저트 + 와인',
    image: '/premium-course-meal-with-wine.jpg',
  },
  {
    name: '파스타',
    price: '18,000원',
    description: '단품',
    image: '/italian-pasta-dish.png',
  },
];

const representativeMenus = ref(
  restaurantInfo.value?.menus?.length ? restaurantInfo.value.menus : defaultMenus,
);
const isRestaurantFavorite = ref(false);

const highlightTags = computed(() => {
  if (!restaurantInfo.value?.topTags?.length) return '';
  return restaurantInfo.value.topTags
    .slice(0, 2)
    .map((tag) => tag.name)
    .join(', ');
});

const restaurantName = computed(() => restaurantInfo.value?.name || '식당명');

const ratingDisplay = computed(() => {
  const rating = restaurantInfo.value?.rating;
  if (typeof rating === 'number') {
    return rating.toFixed(1);
  }
  return '0.0';
});

const reviewCountDisplay = computed(() => restaurantInfo.value?.reviews ?? 0);
const addressDisplay = computed(() => restaurantInfo.value?.address || '주소 정보가 곧 업데이트됩니다.');
const phoneDisplay = computed(() => restaurantInfo.value?.phone || '문의처 준비중');
const hoursDisplay = computed(
  () => restaurantInfo.value?.hours || '영업시간 정보가 곧 업데이트됩니다.',
);
const capacityDisplay = computed(() => restaurantInfo.value?.capacity || '최소 4인 ~ 최대 12인');
const taglineDisplay = computed(
  () => restaurantInfo.value?.tagline || '대표 태그 정보가 곧 업데이트됩니다.',
);

const detailMapContainer = ref(null);
let detailMapInstance = null;
let detailMarker = null;
const detailMapDistanceSteps = Object.freeze([
  { label: '100m', level: 2 },
  { label: '250m', level: 3 },
  { label: '500m', level: 4 },
  { label: '1km', level: 5 },
  { label: '2km', level: 6 },
  { label: '3km', level: 7 },
]);
const detailDefaultMapDistanceIndex = detailMapDistanceSteps.findIndex(
  (step) => step.label === '500m',
);
const detailMapDistanceStepIndex = ref(
  detailDefaultMapDistanceIndex === -1 ? 0 : detailDefaultMapDistanceIndex,
);
const detailCurrentDistanceLabel = computed(
  () =>
    detailMapDistanceSteps[detailMapDistanceStepIndex.value]?.label ??
    detailMapDistanceSteps[0].label,
);
const detailDistanceSliderFill = computed(() => {
  if (detailMapDistanceSteps.length <= 1) return 0;
  return (
    ((detailMapDistanceSteps.length - 1 - detailMapDistanceStepIndex.value) /
      (detailMapDistanceSteps.length - 1)) *
    100
  );
});

const representativeReviews = ref([
  {
    id: 1,
    author: '김**',
    company: '네이버',
    visitCount: 3,
    rating: 5,
    date: '2024.01.15',
    content:
      '회식하기 정말 좋았어요. 음식도 맛있고 분위기도 최고였습니다! 특히 룸이 프라이빗해서 회사 동료들과 편하게 대화할 수 있었고, 음식 양도 정말 푸짐해서 배불리 먹었습니다. 다음에 또 방문하고 싶어요.',
    tags: ['룸이 있어 프라이빗해요', '대화하기 좋아요', '양이 푸짐해요'],
    images: [
      '/korean-appetizer-main-dessert.jpg',
      '/premium-course-meal-with-wine.jpg',
      '/korean-fine-dining.jpg',
    ],
    currentImageIndex: 0,
    isExpanded: false,
  },
  {
    id: 2,
    author: '이**',
    company: null,
    visitCount: 2,
    rating: 5,
    date: '2024.01.10',
    content:
      '직원분들이 친절하시고 코스 구성이 알차서 만족스러웠습니다. 예약 시간에 맞춰 테이블이 완벽하게 세팅되어 있었고, 서비스도 훌륭했습니다.',
    tags: ['사장님이 친절해요', '예약 시간 맞춰 세팅돼요'],
    images: [
      '/elegant-dining-room-setup.jpg',
      '/restaurant-private-room-atmosphere.jpg',
    ],
    currentImageIndex: 0,
    isExpanded: false,
  },
  {
    id: 3,
    author: '박**',
    company: '카카오',
    visitCount: 1,
    rating: 4,
    date: '2024.01.05',
    content: '가격 대비 훌륭한 퀄리티입니다. 다음에 또 방문할게요.',
    tags: ['법카 쓰기 좋은 가격대에요', '주차가 편해요'],
    images: [
      '/italian-pasta-dish.png',
      '/pasta-carbonara.png',
      '/italian-restaurant-dining.jpg',
    ],
    currentImageIndex: 0,
    isExpanded: false,
  },
]);

// 이미지 확대 모달 상태
const isImageModalOpen = ref(false);
const modalImageUrl = ref('');
const modalImageIndex = ref(0);
const modalImages = ref([]);

// 이미지 클릭 시 모달 열기
const openImageModal = (images, index) => {
  modalImages.value = images;
  modalImageIndex.value = index;
  modalImageUrl.value = images[index];
  isImageModalOpen.value = true;
};

const toggleRestaurantFavorite = () => {
  isRestaurantFavorite.value = !isRestaurantFavorite.value;
};

// 모달 닫기
const closeImageModal = () => {
  isImageModalOpen.value = false;
};

// 모달에서 이전/다음 이미지
const handleModalPrevImage = () => {
  modalImageIndex.value =
    modalImageIndex.value === 0
      ? modalImages.value.length - 1
      : modalImageIndex.value - 1;
  modalImageUrl.value = modalImages.value[modalImageIndex.value];
};

const handleModalNextImage = () => {
  modalImageIndex.value =
    modalImageIndex.value === modalImages.value.length - 1
      ? 0
      : modalImageIndex.value + 1;
  modalImageUrl.value = modalImages.value[modalImageIndex.value];
};

// 리뷰 텍스트 더보기/접기 토글 함수
const toggleReviewExpand = (review) => {
  review.isExpanded = !review.isExpanded;
};

// 리뷰 텍스트 길이 체크 (70자 이상이면 더보기 버튼 표시)
const shouldShowExpandButton = (content) => {
  return content.length > 70;
};

// 리뷰 텍스트 자르기 (접혀있을 때)
const truncateText = (content, isExpanded) => {
  if (isExpanded || content.length <= 70) {
    return content;
  }
  return content.substring(0, 70) + '...';
};

// 마우스 드래그 스크롤 기능
const setupDragScroll = (element) => {
  if (!element) return;

  let isDown = false;
  let startX;
  let scrollLeft;

  element.addEventListener('mousedown', (e) => {
    isDown = true;
    element.style.cursor = 'grabbing';
    startX = e.pageX - element.offsetLeft;
    scrollLeft = element.scrollLeft;
  });

  element.addEventListener('mouseleave', () => {
    isDown = false;
    element.style.cursor = 'grab';
  });

  element.addEventListener('mouseup', () => {
    isDown = false;
    element.style.cursor = 'grab';
  });

  element.addEventListener('mousemove', (e) => {
    if (!isDown) return;
    e.preventDefault();
    const x = e.pageX - element.offsetLeft;
    const walk = (x - startX) * 2;
    element.scrollLeft = scrollLeft - walk;
  });
};

const initializeDetailMap = async () => {
  if (!detailMapContainer.value || !restaurantInfo.value?.coords) return;
  try {
    const kakaoMaps = await loadKakaoMaps();
    const center = new kakaoMaps.LatLng(
      restaurantInfo.value.coords.lat,
      restaurantInfo.value.coords.lng,
    );

    detailMapInstance = new kakaoMaps.Map(detailMapContainer.value, {
      center,
      level: detailLevelForDistance(detailMapDistanceStepIndex.value),
    });
    detailMapInstance.setZoomable(false);

    detailMarker = new kakaoMaps.Marker({
      position: center,
      title: restaurantName.value,
    });

    detailMarker.setMap(detailMapInstance);
    applyDetailMapZoom();
  } catch (error) {
    console.error('식당 위치 지도를 불러오지 못했습니다.', error);
  }
};

const detailLevelForDistance = (stepIndex) => {
  const step = detailMapDistanceSteps[stepIndex] ?? detailMapDistanceSteps[0];
  return step.level;
};

const applyDetailMapZoom = () => {
  if (!detailMapInstance) return;
  detailMapInstance.setLevel(detailLevelForDistance(detailMapDistanceStepIndex.value), {
    animate: { duration: 300 },
  });
};

const changeDetailMapDistance = (delta) => {
  const next = Math.min(
    detailMapDistanceSteps.length - 1,
    Math.max(0, detailMapDistanceStepIndex.value + delta),
  );
  detailMapDistanceStepIndex.value = next;
};

// 컴포넌트 마운트 후 드래그 스크롤 및 지도 설정
onMounted(() => {
  const scrollContainers = document.querySelectorAll('.review-image-scroll');
  scrollContainers.forEach((container) => {
    setupDragScroll(container);
  });
  initializeDetailMap();
});

onBeforeUnmount(() => {
  if (detailMarker) {
    detailMarker.setMap(null);
  }
  detailMarker = null;
  detailMapInstance = null;
});

watch(detailMapDistanceStepIndex, () => {
  applyDetailMapZoom();
});
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink to="/" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">식당 정보</h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-24">
      <div
        class="relative w-full h-64 bg-gradient-to-br from-orange-400 to-pink-400 overflow-hidden"
      >
        <img
          :src="restaurantImages[currentImageIndex].url || '/placeholder.svg'"
          :alt="restaurantImages[currentImageIndex].alt"
          class="w-full h-full object-cover"
        />

        <!-- Left Arrow -->
        <button
          @click="handlePrevImage"
          class="absolute left-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/80 hover:bg-white flex items-center justify-center shadow-lg transition-all"
          aria-label="이전 이미지"
        >
          <ChevronLeft class="w-5 h-5 text-[#1e3a5f]" />
        </button>

        <!-- Right Arrow -->
        <button
          @click="handleNextImage"
          class="absolute right-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/80 hover:bg-white flex items-center justify-center shadow-lg transition-all"
          aria-label="다음 이미지"
        >
          <ChevronRight class="w-5 h-5 text-[#1e3a5f]" />
        </button>

        <!-- Image Indicators -->
        <div class="absolute bottom-3 left-1/2 -translate-x-1/2 flex gap-2">
          <button
            v-for="(_, idx) in restaurantImages"
            :key="idx"
            @click="currentImageIndex = idx"
            :class="`w-2 h-2 rounded-full transition-all ${
              idx === currentImageIndex
                ? 'bg-white w-6'
                : 'bg-white/50 hover:bg-white/75'
            }`"
            :aria-label="`이미지 ${idx + 1}로 이동`"
          />
        </div>
      </div>

      <!-- Restaurant Info -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
        <div class="flex items-start justify-between mb-3">
          <div class="flex-1">
            <h2 class="text-xl font-bold text-[#1e3a5f] mb-2">
              {{ restaurantName }}
            </h2>
            <div class="flex items-center gap-1 mb-2">
              <Star class="w-4 h-4 fill-[#ffc107] text-[#ffc107]" />
              <span class="text-base font-semibold text-[#1e3a5f]">{{ ratingDisplay }}</span>
              <span class="text-sm text-[#6c757d]">({{ reviewCountDisplay }}개 리뷰)</span>
            </div>
            <p class="text-sm text-[#6c757d] mb-3 leading-relaxed">
              {{ taglineDisplay }}
            </p>
            <p v-if="highlightTags" class="text-xs text-[#adb5bd] mb-3 leading-relaxed">
              대표 태그: {{ highlightTags }}
            </p>
          </div>
          <button
            type="button"
            class="p-2 rounded-full bg-[#f8f9fa] text-[#adb5bd] hover:text-[#ff6b4a] transition-colors"
            :aria-pressed="isRestaurantFavorite"
            @click="toggleRestaurantFavorite"
          >
            <Star
              class="w-5 h-5"
              :class="isRestaurantFavorite ? 'fill-current text-[#ff6b4a]' : 'fill-white'"
            />
            <span class="sr-only">
              {{ isRestaurantFavorite ? '즐겨찾기 해제' : '즐겨찾기에 추가' }}
            </span>
          </button>
        </div>

        <div class="space-y-2.5">
          <div class="flex items-start gap-2 text-sm">
            <MapPin class="w-4 h-4 text-[#6c757d] mt-0.5 flex-shrink-0" />
            <span class="text-[#495057] leading-relaxed">{{ addressDisplay }}</span>
          </div>
          <div class="flex items-start gap-2 text-sm">
            <Clock class="w-4 h-4 text-[#6c757d] mt-0.5 flex-shrink-0" />
            <span class="text-[#495057] leading-relaxed">{{ hoursDisplay }}</span>
          </div>
          <div class="flex items-start gap-2 text-sm">
            <Phone class="w-4 h-4 text-[#6c757d] mt-0.5 flex-shrink-0" />
            <span class="text-[#495057] leading-relaxed">{{ phoneDisplay }}</span>
          </div>
          <div class="flex items-start gap-2 text-sm">
            <Users class="w-4 h-4 text-[#6c757d] mt-0.5 flex-shrink-0" />
            <span class="text-[#495057] leading-relaxed">{{ capacityDisplay }}</span>
          </div>
        </div>
      </div>

      <div class="px-4 py-5 bg-white border-b border-[#e9ecef]">
        <h3 class="text-lg font-semibold text-[#1e3a5f] mb-3">위치 안내</h3>
        <div class="relative w-full h-56 rounded-xl border border-[#e9ecef] overflow-hidden">
          <div ref="detailMapContainer" class="absolute inset-0" />
          <div class="absolute top-3 right-3 z-10 flex flex-col items-center gap-2 pointer-events-auto">
            <button
              @click="changeDetailMapDistance(-1)"
              class="w-7 h-7 rounded-sm bg-white shadow-card flex items-center justify-center text-[#1e3a5f] hover:bg-[#f8f9fa]"
            >
              <Plus class="w-3.5 h-3.5" />
            </button>
            <div class="h-16 w-[5px] bg-white/80 rounded relative shadow-card overflow-hidden">
              <div
                class="absolute top-0 left-0 right-0 bg-[#ff6b4a] transition-all"
                :style="{ height: `${detailDistanceSliderFill}%` }"
              ></div>
            </div>
            <button
              @click="changeDetailMapDistance(1)"
              class="w-7 h-7 rounded-sm bg-white shadow-card flex items-center justify-center text-[#1e3a5f] hover:bg-[#f8f9fa]"
            >
              <Minus class="w-3.5 h-3.5" />
            </button>
            <div
              class="mt-1 px-2 py-1 rounded-full bg-white text-[11px] font-semibold text-[#1e3a5f] shadow-card"
            >
              반경 {{ detailCurrentDistanceLabel }}
            </div>
          </div>
        </div>
        <p class="text-xs text-[#6c757d] mt-2">{{ addressDisplay }}</p>
      </div>

      <!-- Representative Menus -->
      <div class="px-4 py-5 bg-white border-b border-[#e9ecef]">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-[#1e3a5f]">대표 메뉴</h3>
          <RouterLink
            :to="{ name: 'restaurant-menus', params: { id: restaurantId } }"
            class="flex items-center gap-1 text-sm text-[#ff6b4a] font-medium hover:text-[#ff8570] transition-colors"
          >
            메뉴 전체보기
            <ChevronRight class="w-4 h-4" />
          </RouterLink>
        </div>

        <div class="space-y-3">
          <Card
            v-for="(item, idx) in representativeMenus"
            :key="idx"
            class="p-3 border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-md transition-shadow"
          >
            <div class="flex items-center gap-3">
              <img
                :src="item.image || '/placeholder.svg'"
                :alt="item.name"
                class="w-20 h-20 rounded-lg object-cover flex-shrink-0"
              />
              <div class="flex-1 min-w-0">
                <p class="font-semibold text-[#1e3a5f] mb-1">{{ item.name }}</p>
                <p
                  v-if="item.description"
                  class="text-xs text-[#6c757d] leading-relaxed mb-2"
                >
                  {{ item.description }}
                </p>
                <p class="font-semibold text-[#ff6b4a]">{{ item.price }}</p>
              </div>
            </div>
          </Card>
        </div>
      </div>

      <!-- Representative Reviews -->
      <div class="px-4 py-5 bg-white border-b border-[#e9ecef]">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-[#1e3a5f]">리뷰</h3>
          <RouterLink
            :to="{ name: 'restaurant-reviews', params: { id: restaurantId } }"
            class="flex items-center gap-1 text-sm text-[#ff6b4a] font-medium hover:text-[#ff8570] transition-colors"
          >
            리뷰 전체보기
            <ChevronRight class="w-4 h-4" />
          </RouterLink>
        </div>

        <div class="space-y-3">
          <div v-for="review in representativeReviews" :key="review.id">
            <Card
              class="p-4 border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-md transition-shadow"
            >
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-2">
                  <div class="flex flex-col">
                    <div class="flex items-center gap-2">
                      <span class="font-semibold text-[#1e3a5f] text-sm">{{
                        review.author
                      }}</span>
                      <span
                        v-if="review.company"
                        class="font-semibold text-[#1e3a5f] text-sm"
                        >({{ review.company }})</span
                      >
                      <div class="flex items-center gap-1">
                        <Star
                          v-for="(_, i) in Array.from({
                            length: review.rating,
                          })"
                          :key="i"
                          class="w-3.5 h-3.5 fill-[#ffc107] text-[#ffc107]"
                        />
                      </div>
                    </div>
                    <span
                      v-if="review.visitCount"
                      class="text-xs text-[#6c757d] mt-0.5"
                    >
                      {{ review.visitCount }}번째 방문
                    </span>
                  </div>
                </div>
                <span class="text-xs text-[#6c757d]">{{ review.date }}</span>
              </div>

              <!-- 리뷰 이미지 갤러리 (스크롤 방식) -->
              <div
                v-if="review.images && review.images.length > 0"
                class="mb-3 -mx-4"
              >
                <div
                  class="flex gap-2 overflow-x-auto px-4 snap-x snap-mandatory scrollbar-hide review-image-scroll cursor-grab active:cursor-grabbing"
                >
                  <div
                    v-for="(image, idx) in review.images"
                    :key="idx"
                    class="flex-shrink-0 snap-start"
                    :class="
                      idx === 0 ? 'w-[calc(100%-3rem)]' : 'w-[calc(100%-6rem)]'
                    "
                  >
                    <div
                      class="relative rounded-lg overflow-hidden bg-[#f8f9fa] h-48 cursor-pointer hover:opacity-95 transition-opacity"
                      @click="openImageModal(review.images, idx)"
                    >
                      <img
                        :src="image || '/placeholder.svg'"
                        :alt="`리뷰 이미지 ${idx + 1}`"
                        class="w-full h-full object-cover pointer-events-none"
                      />
                      <!-- 이미지 카운터 -->
                      <div
                        class="absolute top-2 right-2 bg-black/60 text-white text-xs px-2 py-1 rounded-full"
                      >
                        {{ idx + 1 }} / {{ review.images.length }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 리뷰 내용 -->
              <div>
                <p class="text-sm text-[#495057] leading-relaxed mb-2">
                  {{ truncateText(review.content, review.isExpanded) }}
                </p>

                <!-- 더보기/접기 버튼 -->
                <button
                  v-if="shouldShowExpandButton(review.content)"
                  @click.prevent="toggleReviewExpand(review)"
                  class="text-xs text-[#6c757d] hover:text-[#ff6b4a] font-medium mb-3 transition-colors"
                >
                  {{ review.isExpanded ? '접기' : '더보기' }}
                </button>

                <!-- 태그 -->
                <RouterLink
                  :to="`/restaurant/${restaurantId}/reviews/${review.id}`"
                  class="block"
                >
                  <div
                    v-if="review.tags && review.tags.length > 0"
                    class="flex flex-wrap gap-1.5"
                  >
                    <span
                      v-for="(tag, idx) in review.tags"
                      :key="idx"
                      class="inline-block px-2.5 py-1 text-xs rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ffc4b8] text-white"
                    >
                      {{ tag }}
                    </span>
                  </div>
                </RouterLink>
              </div>
            </Card>
          </div>
        </div>
      </div>

      <!-- Additional Info -->
      <div class="bg-white px-4 py-5 border-t border-[#e9ecef]">
        <h3 class="text-base font-semibold text-[#1e3a5f] mb-3">예약 안내</h3>
        <div class="space-y-2 text-sm text-[#495057] leading-relaxed">
          <p>• 예약은 최소 1일 전까지 가능합니다.</p>
          <p>
            • 예약 취소는 1일 전까지 무료이며, 당일 취소 시 위약금이 발생할 수
            있습니다.
          </p>
          <p>• 인원 변경은 예약일 기준 2일 전까지 가능합니다.</p>
          <p>• 노쇼 시 향후 예약이 제한될 수 있습니다.</p>
        </div>
      </div>
    </main>

    <!-- Fixed Bottom Buttons -->
    <div class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg">
      <div class="relative max-w-[500px] mx-auto px-4 py-3">
        <RouterLink
          to="/"
          class="absolute -top-4 right-4 w-12 h-12 rounded-full bg-white border border-[#ffe0d6] flex items-center justify-center text-[#ff6b4a] shadow-card hover:bg-[#fff7f4] transition-colors"
          aria-label="홈으로 이동"
        >
          <HomeIcon class="w-5 h-5" />
        </RouterLink>
        <div class="flex gap-3">
          <RouterLink
            :to="`/restaurant/${restaurantId}/booking?type=preorder`"
            class="flex-1"
          >
            <Button
              class="w-full h-12 bg-white border-2 border-[#ff6b4a] text-[#ff6b4a] font-semibold text-base rounded-xl hover:bg-[#fff5f3] transition-colors"
            >
              선주문/선결제
            </Button>
          </RouterLink>
          <RouterLink
            :to="`/restaurant/${restaurantId}/booking?type=reservation`"
            class="flex-1"
          >
            <Button
              class="w-full h-12 gradient-primary text-white font-semibold text-base rounded-xl shadow-button-hover hover:shadow-button-pressed"
            >
              예약하기
            </Button>
          </RouterLink>
        </div>
      </div>
    </div>

    <!-- 이미지 확대 모달 -->
    <div
      v-if="isImageModalOpen"
      class="fixed inset-0 z-[100] bg-black/95 flex items-center justify-center"
      @click="closeImageModal"
    >
      <div class="relative w-full h-full flex items-center justify-center">
        <!-- 닫기 버튼 -->
        <button
          @click.stop="closeImageModal"
          class="absolute top-4 right-4 w-10 h-10 rounded-full bg-white/20 hover:bg-white/30 flex items-center justify-center transition-colors z-10"
          aria-label="닫기"
        >
          <X class="w-6 h-6 text-white" />
        </button>

        <!-- 이미지 -->
        <div class="relative max-w-[90vw] max-h-[90vh]" @click.stop>
          <img
            :src="modalImageUrl || '/placeholder.svg'"
            :alt="`확대 이미지 ${modalImageIndex + 1}`"
            class="max-w-full max-h-[90vh] object-contain rounded-lg"
          />

          <!-- 이미지가 2개 이상일 때만 화살표 표시 -->
          <template v-if="modalImages.length > 1">
            <!-- 왼쪽 화살표 -->
            <button
              @click.stop="handleModalPrevImage"
              class="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/30 hover:bg-white/50 flex items-center justify-center transition-colors"
              aria-label="이전 이미지"
            >
              <ChevronLeft class="w-6 h-6 text-white" />
            </button>

            <!-- 오른쪽 화살표 -->
            <button
              @click.stop="handleModalNextImage"
              class="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/30 hover:bg-white/50 flex items-center justify-center transition-colors"
              aria-label="다음 이미지"
            >
              <ChevronRight class="w-6 h-6 text-white" />
            </button>

            <!-- 이미지 카운터 -->
            <div
              class="absolute bottom-4 left-1/2 -translate-x-1/2 bg-black/60 text-white text-sm px-4 py-2 rounded-full"
            >
              {{ modalImageIndex + 1 }} / {{ modalImages.length }}
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 스크롤바 숨기기 */
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

/* 스크롤 스냅 부드럽게 */
.snap-x {
  scroll-behavior: smooth;
}

/* 마우스 드래그 시 텍스트 선택 방지 */
.review-image-scroll {
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

/* 이미지 드래그 방지 */
.review-image-scroll img {
  pointer-events: none;
  -webkit-user-drag: none;
  user-select: none;
}
</style>
