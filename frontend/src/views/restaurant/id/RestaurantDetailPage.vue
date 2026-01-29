<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
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
import FavoriteStarButton from '@/components/ui/FavoriteStarButton.vue';
import { useFavorites } from '@/composables/useFavorites';
import { useRestaurantDetailMap } from '@/composables/useRestaurantDetailMap';
import { useImageGallery, useImageModal } from '@/composables/useImageGallery';
import { geocodeAddress } from '@/utils/kakao';
import {
  formatRouteDistance,
  formatRouteDurationMinutes,
  formatDate,
} from '@/utils/formatters';
import { formatReviewTag } from '@/utils/reviewTagEmojis';
import { normalizeImageUrl, normalizeImageUrls } from '@/utils/image';
import { useAccountStore } from '@/stores/account';
import httpRequest from '@/router/httpRequest.js';

const accountStore = useAccountStore();
const isLoggedIn = computed(() => accountStore.loggedIn);
const { fetchFavorites, clearFavorites, userId } = useFavorites();

const route = useRoute();
const restaurantId = route.params.id || 1;
const loginRedirectPath = computed(
  () =>
    `/login?next=${encodeURIComponent(
      `/restaurant/${restaurantId}/booking?select=1`
    )}`
);

const restaurantInfo = ref(null);
const isLoading = ref(true);
const error = ref(null);

const defaultGallery = [
  { url: '/modern-korean-restaurant-interior.jpg', alt: '식당 메인 이미지' },
  { url: '/elegant-dining-room-setup.jpg', alt: '식당 내부 전경' },
  { url: '/korean-course-meal-plating.jpg', alt: '대표 메뉴 이미지' },
  { url: '/restaurant-private-room-atmosphere.jpg', alt: '식당 분위기' },
];

const restaurantImages = ref(defaultGallery);
const representativeMenus = ref([]);
const representativeReviews = ref([]);
const restaurantReviewSummary = ref(null);

const restaurantName = computed(() => restaurantInfo.value?.name || '식당명');

const ratingDisplay = computed(() => {
  const summaryRating = restaurantReviewSummary.value?.avgRating;
  const rawRating = summaryRating ?? restaurantInfo.value?.rating;
  return Number(rawRating).toFixed(1);
});

const reviewCountDisplay = computed(
  () => restaurantReviewSummary.value?.reviewCount ?? restaurantInfo.value?.reviews ?? 0,
);

const addressDisplay = computed(
  () => restaurantInfo.value?.address || '주소 정보가 곧 업데이트됩니다.',
);
const phoneDisplay = computed(() => restaurantInfo.value?.phone || '문의처 준비중');
const hoursDisplay = computed(
  () => restaurantInfo.value?.hours || '영업시간 정보가 곧 업데이트됩니다.',
);
const capacityDisplay = computed(
  () => restaurantInfo.value?.capacity || '최소 4인 ~ 최대 12인',
);
const taglineDisplay = computed(
  () => restaurantInfo.value?.tagline || '대표 태그 정보가 곧 업데이트됩니다.',
);
const highlightTags = computed(() => {
  if (!restaurantInfo.value?.tags?.length) return '';
  return restaurantInfo.value.tags
    .map((tag) => tag.content || tag.name)
    .join(', ');
});

// Image Gallery Composable
const {
  currentImageIndex,
  handlePrevImage,
  handleNextImage,
} = useImageGallery(restaurantImages);

// Image Modal Composable
const {
  isImageModalOpen,
  modalImageUrl,
  modalImageIndex,
  modalImages,
  openImageModal,
  closeImageModal,
  handleModalPrevImage,
  handleModalNextImage,
} = useImageModal();

// Fetch company address for route calculation
const fetchCompanyAddress = async () => {
  const id = userId.value;
  if (!id) return null;
  try {
    const response = await httpRequest.get(`/api/info/user/${id}`);
    return response.data?.companyAddress || null;
  } catch {
    return null;
  }
};

// Map Composable
const {
  detailMapContainer,
  routeInfo,
  routeError,
  isRouteLoading,
  detailCurrentDistanceLabel,
  detailDistanceSliderFill,
  changeDetailMapDistance,
  handleCheckRoute: mapHandleCheckRoute,
} = useRestaurantDetailMap({
  restaurantInfo,
  restaurantName,
  fetchCompanyAddress,
  isLoggedIn,
});

const handleCheckRoute = () => mapHandleCheckRoute(httpRequest);

const mapReviewItem = (item) => ({
  id: item.reviewId,
  author: item.author || '익명',
  company: item.company || null,
  visitCount: item.visitCount ?? null,
  rating: item.rating ?? 0,
  date: formatDate(item.createdAt),
  content: item.isBlinded
    ? '관리자에 의해 블라인드 처리된 리뷰입니다.'
    : item.content || '',
  tags: (item.tags || []).map((tag) => tag.name || tag),
  images: normalizeImageUrls(item.images || []),
  currentImageIndex: 0,
  isExpanded: false,
  isBlinded: Boolean(item.isBlinded),
  blindReason: item.blindReason || '관리자에 의해 블라인드 처리된 리뷰입니다.',
});

const toggleReviewExpand = (review) => {
  review.isExpanded = !review.isExpanded;
};

const shouldShowExpandButton = (content) => content.length > 70;

const truncateText = (content, isExpanded) => {
  if (isExpanded || content.length <= 70) return content;
  return content.substring(0, 70) + '...';
};

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

const fetchRestaurantDetail = async () => {
  isLoading.value = true;
  error.value = null;
  try {
    const response = await httpRequest.get(`/api/restaurants/${restaurantId}`);
    const details = response.data;

    const coords = await geocodeAddress(details.roadAddress);

    restaurantInfo.value = {
      id: details.restaurantId,
      name: details.name,
      phone: details.phone,
      address: `${details.roadAddress} ${details.detailAddress || ''}`.trim(),
      hours: `${details.openTime} - ${details.closeTime}`,
      capacity: `최대 ${details.reservationLimit}인`,
      preorderAvailable: details.preorderAvailable,
      tagline: details.description,
      tags: details.tags,
      coords: coords,
      rating: restaurantReviewSummary.value?.avgRating || 0,
      reviews: restaurantReviewSummary.value?.reviewCount || 0,
    };

    if (details.menus?.length) {
      representativeMenus.value = details.menus
        .filter((menu) => menu.category.code === 'MAIN')
        .slice(0, 3)
        .map((menu) => ({
          ...menu,
          price: `${menu.price.toLocaleString()}원`,
          imageUrl: normalizeImageUrl(menu.imageUrl),
        }));
    } else {
      representativeMenus.value = [];
    }

    if (details.images?.length) {
      restaurantImages.value = details.images.map((img, index) => ({
        url: normalizeImageUrl(img.imageUrl),
        alt: `${details.name} 이미지 ${index + 1}`,
      }));
    } else {
      restaurantImages.value = defaultGallery;
    }
  } catch (err) {
    console.error('식당 상세 정보를 불러오는 데 실패했습니다:', err);
    error.value = '데이터를 불러올 수 없습니다.';
  } finally {
    isLoading.value = false;
  }
};

const loadRepresentativeReviews = async () => {
  try {
    const response = await httpRequest.get(`/api/restaurants/${restaurantId}/reviews`, {
      params: {
        page: 1,
        size: 3,
        sort: 'RECOMMEND',
      },
    });
    const data = response.data?.data ?? response.data;
    restaurantReviewSummary.value = data?.summary ?? null;
    representativeReviews.value = (data?.items || []).slice(0, 3).map(mapReviewItem);
  } catch (err) {
    console.error('리뷰 데이터를 불러오지 못했습니다:', err);
    representativeReviews.value = [];
  }
};

watch(
  () => userId.value,
  (nextUserId) => {
    if (!nextUserId) {
      clearFavorites();
      return;
    }
    fetchFavorites(nextUserId);
  },
  { immediate: true }
);

onMounted(async () => {
  if (typeof window !== 'undefined') {
    window.scrollTo({ top: 0, behavior: 'auto' });
  }

  await Promise.all([loadRepresentativeReviews(), fetchRestaurantDetail()]);

  const scrollContainers = document.querySelectorAll('.review-image-scroll');
  scrollContainers.forEach((container) => {
    setupDragScroll(container);
  });
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
      <!-- Loading Spinner -->
      <div v-if="isLoading" class="flex justify-center items-center h-64">
        <div
          class="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-[#ff6b4a]"
        ></div>
      </div>

      <!-- Error Message -->
      <div v-else-if="error" class="text-center py-10 px-4">
        <p class="text-red-500 font-semibold">
          {{ error }}
        </p>
        <Button @click="fetchRestaurantDetail" class="mt-4">재시도</Button>
      </div>

      <!-- Restaurant Content -->
      <template v-if="!isLoading && restaurantInfo">
        <!-- Image Gallery -->
        <div
          class="relative w-full h-64 bg-gradient-to-br from-orange-400 to-pink-400 overflow-hidden"
        >
          <img
            :src="restaurantImages[currentImageIndex]?.url || '/placeholder.svg'"
            :alt="restaurantImages[currentImageIndex]?.alt || '식당 이미지'"
            class="w-full h-full object-cover"
          />

          <button
            @click="handlePrevImage"
            class="absolute left-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/80 hover:bg-white flex items-center justify-center shadow-lg transition-all"
            aria-label="이전 이미지"
          >
            <ChevronLeft class="w-5 h-5 text-[#1e3a5f]" />
          </button>

          <button
            @click="handleNextImage"
            class="absolute right-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/80 hover:bg-white flex items-center justify-center shadow-lg transition-all"
            aria-label="다음 이미지"
          >
            <ChevronRight class="w-5 h-5 text-[#1e3a5f]" />
          </button>

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
                <span class="text-base font-semibold text-[#1e3a5f]">{{
                  ratingDisplay
                }}</span>
                <span class="text-sm text-gray-700"
                  >({{ reviewCountDisplay }}개 리뷰)</span
                >
              </div>
              <p class="text-sm text-gray-700 mb-3 leading-relaxed">
                {{ taglineDisplay }}
              </p>
              <div v-if="highlightTags" class="mb-3">
                <div class="flex flex-wrap gap-1.5">
                  <span
                    v-for="(tag, idx) in highlightTags.split(',')"
                    :key="idx"
                    class="inline-flex items-center px-2.5 py-1 text-xs rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ff8e72] text-white font-semibold shadow-sm"
                  >
                    {{ formatReviewTag(tag) }}
                  </span>
                </div>
              </div>
            </div>
            <FavoriteStarButton
              :restaurant-id="restaurantInfo?.id || Number(restaurantId)"
              button-class="p-2 rounded-full bg-[#f8f9fa] text-gray-700 hover:text-[#ff6b4a] transition-colors"
            />
          </div>

          <div class="space-y-2.5">
            <div class="flex items-start gap-2 text-sm">
              <MapPin class="w-4 h-4 text-gray-700 mt-0.5 flex-shrink-0" />
              <span class="text-gray-700 leading-relaxed">{{
                addressDisplay
              }}</span>
            </div>
            <div class="flex items-start gap-2 text-sm">
              <Clock class="w-4 h-4 text-gray-700 mt-0.5 flex-shrink-0" />
              <span class="text-gray-700 leading-relaxed">{{ hoursDisplay }}</span>
            </div>
            <div class="flex items-start gap-2 text-sm">
              <Phone class="w-4 h-4 text-gray-700 mt-0.5 flex-shrink-0" />
              <span class="text-gray-700 leading-relaxed">{{ phoneDisplay }}</span>
            </div>
            <div class="flex items-start gap-2 text-sm">
              <Users class="w-4 h-4 text-gray-700 mt-0.5 flex-shrink-0" />
              <span class="text-gray-700 leading-relaxed">{{
                capacityDisplay
              }}</span>
            </div>
          </div>
        </div>

        <!-- Map Section -->
        <div class="px-4 py-5 bg-white border-b border-[#e9ecef]">
          <div class="flex items-center gap-3 mb-3">
            <h3 class="text-lg font-semibold text-[#1e3a5f]">위치 안내</h3>
            <div class="flex items-center gap-2">
              <button
                type="button"
                class="px-3 py-1.5 rounded-full text-xs font-semibold border transition-colors"
                :class="routeInfo
                  ? 'bg-[#ff6b4a] border-[#ff6b4a] text-white hover:bg-[#ff7d61]'
                  : 'bg-white border-[#e9ecef] text-[#1e3a5f] hover:bg-[#f8f9fa]'"
                :disabled="isRouteLoading || routeInfo"
                @click="handleCheckRoute"
              >
                {{ isRouteLoading ? '경로 확인 중' : '경로 및 거리 확인' }}
              </button>
              <span
                v-if="routeInfo"
                class="text-xs font-semibold text-[#1e3a5f] whitespace-nowrap"
              >
                거리 {{ formatRouteDistance(routeInfo.distanceMeters) }} · 예상
                {{ formatRouteDurationMinutes(routeInfo.durationSeconds) }}
              </span>
              <span v-else-if="routeError" class="text-xs text-[#e03131]">
                {{ routeError }}
              </span>
            </div>
          </div>
          <div
            class="relative w-full h-56 rounded-xl border border-[#e9ecef] overflow-hidden"
          >
            <div ref="detailMapContainer" class="absolute inset-0" />
            <div
              class="absolute top-3 right-3 z-10 flex flex-col items-center gap-2 pointer-events-auto"
            >
              <button
                @click="changeDetailMapDistance(-1)"
                class="w-7 h-7 rounded-sm bg-white shadow-card flex items-center justify-center text-[#1e3a5f] hover:bg-[#f8f9fa]"
              >
                <Plus class="w-3.5 h-3.5" />
              </button>
              <div
                class="h-16 w-[5px] bg-white/80 rounded relative shadow-card overflow-hidden"
              >
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
          <p class="text-xs text-gray-700 mt-2">{{ addressDisplay }}</p>
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
                  :src="item.imageUrl || '/placeholder.svg'"
                  :alt="item.name"
                  class="w-20 h-20 rounded-lg object-cover flex-shrink-0"
                />
                <div class="flex-1 min-w-0">
                  <p class="font-semibold text-[#1e3a5f] mb-1">{{ item.name }}</p>
                  <p
                    v-if="item.description"
                    class="text-xs text-gray-700 leading-relaxed mb-2"
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

          <div v-if="representativeReviews.length > 0" class="space-y-3">
            <div v-for="review in representativeReviews" :key="review.id">
              <Card
                :class="`p-4 border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-md transition-shadow ${
                  review.isBlinded ? 'opacity-60' : ''
                }`"
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
                        <div
                          v-if="!review.isBlinded"
                          class="flex items-center gap-1"
                        >
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
                        v-if="!review.isBlinded && review.visitCount"
                        class="text-xs text-gray-700 mt-0.5"
                      >
                        {{ review.visitCount }}번째 방문
                      </span>
                    </div>
                  </div>
                  <span class="text-xs text-gray-700">{{ review.date }}</span>
                </div>

                <!-- Review Images Gallery -->
                <div
                  v-if="
                    !review.isBlinded && review.images && review.images.length > 0
                  "
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
                        idx === 0
                          ? 'w-[calc(100%-3rem)]'
                          : 'w-[calc(100%-6rem)]'
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
                        <div
                          class="absolute top-2 right-2 bg-black/60 text-white text-xs px-2 py-1 rounded-full"
                        >
                          {{ idx + 1 }} / {{ review.images.length }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Review Content -->
                <div v-if="!review.isBlinded">
                  <p class="text-sm text-gray-700 leading-relaxed mb-2">
                    {{ truncateText(review.content, review.isExpanded) }}
                  </p>

                  <button
                    v-if="shouldShowExpandButton(review.content)"
                    @click.prevent="toggleReviewExpand(review)"
                    class="text-xs text-gray-700 hover:text-[#ff6b4a] font-medium mb-3 transition-colors"
                  >
                    {{ review.isExpanded ? '접기' : '더보기' }}
                  </button>

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
                        class="inline-flex items-center px-2.5 py-1 text-xs rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ff8e72] text-white font-semibold shadow-sm"
                      >
                        {{ formatReviewTag(tag) }}
                      </span>
                    </div>
                  </RouterLink>
                </div>
                <div v-else>
                  <p class="text-sm text-gray-700 leading-relaxed mb-2">
                    {{ review.content }}
                  </p>
                  <span
                    class="inline-block px-2.5 py-1 text-xs rounded-full bg-[#6c757d] text-white"
                  >
                    사유: {{ review.blindReason }}
                  </span>
                </div>
              </Card>
            </div>
          </div>
          <div v-else class="text-center py-8 text-sm text-gray-700">
            <p>작성된 리뷰가 없습니다.</p>
          </div>
        </div>

        <!-- Additional Info -->
        <div class="bg-white px-4 py-5 border-t border-[#e9ecef]">
          <h3 class="text-base font-semibold text-[#1e3a5f] mb-3">
            예약 안내
          </h3>
          <div class="space-y-2 text-sm text-gray-700 leading-relaxed">
            <p>• 예약은 최소 1일 전까지 가능합니다.</p>
            <p>
              • 예약 취소는 1일 전까지 무료이며, 당일 취소 시 위약금이 발생할 수
              있습니다.
            </p>
            <p>• 인원 변경은 예약일 기준 2일 전까지 가능합니다.</p>
            <p>• 노쇼 시 향후 예약이 제한될 수 있습니다.</p>
          </div>
        </div>
      </template>
    </main>

    <!-- Fixed Bottom Buttons -->
    <div v-if="!isLoading" class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg">
      <div class="relative max-w-[500px] mx-auto px-4 py-3">
        <RouterLink
          to="/"
          class="absolute -top-14 right-4 w-12 h-12 rounded-full bg-white border border-[#ffe0d6] flex items-center justify-center text-[#ff6b4a] shadow-card hover:bg-[#fff7f4] transition-colors"
          aria-label="홈으로 이동"
        >
          <HomeIcon class="w-5 h-5" />
        </RouterLink>
        <div class="flex gap-3">
          <template v-if="isLoggedIn">
            <RouterLink
              v-if="restaurantInfo?.preorderAvailable"
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
          </template>
          <RouterLink v-else :to="loginRedirectPath" class="flex-1">
            <Button
              class="w-full h-12 gradient-primary text-white font-semibold text-base rounded-xl shadow-button-hover hover:shadow-button-pressed"
            >
              로그인 하기
            </Button>
          </RouterLink>
        </div>
      </div>
    </div>

    <!-- Image Modal -->
    <div
      v-if="isImageModalOpen"
      class="fixed inset-0 z-[100] bg-black/95 flex items-center justify-center"
      @click="closeImageModal"
    >
      <div class="relative w-full h-full flex items-center justify-center">
        <button
          @click.stop="closeImageModal"
          class="absolute top-4 right-4 w-10 h-10 rounded-full bg-white/20 hover:bg-white/30 flex items-center justify-center transition-colors z-10"
          aria-label="닫기"
        >
          <X class="w-6 h-6 text-white" />
        </button>

        <div class="relative max-w-[90vw] max-h-[90vh]" @click.stop>
          <img
            :src="modalImageUrl || '/placeholder.svg'"
            :alt="`확대 이미지 ${modalImageIndex + 1}`"
            class="max-w-full max-h-[90vh] object-contain rounded-lg"
          />

          <template v-if="modalImages.length > 1">
            <button
              @click.stop="handleModalPrevImage"
              class="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/30 hover:bg-white/50 flex items-center justify-center transition-colors"
              aria-label="이전 이미지"
            >
              <ChevronLeft class="w-6 h-6 text-white" />
            </button>

            <button
              @click.stop="handleModalNextImage"
              class="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/30 hover:bg-white/50 flex items-center justify-center transition-colors"
              aria-label="다음 이미지"
            >
              <ChevronRight class="w-6 h-6 text-white" />
            </button>

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
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

.snap-x {
  scroll-behavior: smooth;
}

.review-image-scroll {
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.review-image-scroll img {
  pointer-events: none;
  -webkit-user-drag: none;
  user-select: none;
}
</style>
