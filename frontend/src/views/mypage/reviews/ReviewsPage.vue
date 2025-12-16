<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import {
  ArrowLeft,
  Star,
  MoreVertical,
  Edit,
  Trash2,
  MapPin,
} from 'lucide-vue-next';
import Card from '@/components/ui/Card.vue';

const route = useRoute();
const router = useRouter();

// 내가 작성한 리뷰 목록 (Mock data - 실제로는 API에서 가져옴)
const myReviews = ref([
  {
    id: 'review-1',
    reservationId: 3,
    restaurant: {
      id: 1,
      name: '식당명',
      address: '서울시 강남구 테헤란로 132',
    },
    visitCount: 2,
    rating: 5,
    date: '2024.11.16',
    visitDate: '2024.11.15',
    content:
      '회식하기 정말 좋았어요. 음식도 맛있고 분위기도 최고였습니다! 특히 룸이 프라이빗해서 회사 동료들과 편하게 대화할 수 있었고, 음식 양도 정말 푸짐해서 배불리 먹었습니다. 다음에 또 방문하고 싶어요.',
    tags: [
      '인테리어가 세련돼요',
      '재료가 신선해요',
      '직원들이 적극적으로 도와줘요',
    ],
    images: [
      '/korean-appetizer-main-dessert.jpg',
      '/premium-course-meal-with-wine.jpg',
    ],
  },
  {
    id: 'review-2',
    reservationId: 5,
    restaurant: {
      id: 1,
      name: '식당명',
      address: '서울시 강남구 테헤란로 132',
    },
    visitCount: 3,
    rating: 4,
    date: '2024.11.11',
    visitDate: '2024.11.10',
    content: '가격 대비 훌륭한 퀄리티입니다. 다음에 또 방문할게요.',
    tags: ['가격 대비 만족스러워요', '청결 관리가 잘 돼요'],
    images: ['/italian-pasta-dish.png'],
  },
  {
    id: 'review-3',
    reservationId: 6,
    restaurant: {
      id: 2,
      name: '맛있는집',
      address: '서울시 강남구 역삼동 456',
    },
    visitCount: 1,
    rating: 5,
    date: '2024.10.20',
    visitDate: '2024.10.19',
    content:
      '직원분들이 친절하시고 코스 구성이 알차서 만족스러웠습니다. 예약 시간에 맞춰 테이블이 완벽하게 세팅되어 있었고, 서비스도 훌륭했습니다.',
    tags: ['메뉴 설명을 잘 해줘요', '시그니처 메뉴가 있어요'],
    images: [],
  },
]);

// 리뷰 메뉴 드롭다운 상태
const activeReviewMenu = ref(null);

// 리뷰 메뉴 토글
const toggleReviewMenu = (reviewId) => {
  if (activeReviewMenu.value === reviewId) {
    activeReviewMenu.value = null;
  } else {
    activeReviewMenu.value = reviewId;
  }
};

// 리뷰 수정
const handleEditReview = (review) => {
  console.log('리뷰 수정:', review.id);
  activeReviewMenu.value = null;
  // 리뷰 수정 페이지로 이동
  router.push(`/restaurant/${review.restaurant.id}/reviews/${review.id}/edit`);
};

// 리뷰 삭제
const handleDeleteReview = (review) => {
  if (confirm('리뷰를 삭제하시겠습니까?')) {
    console.log('리뷰 삭제:', review.id);
    // TODO: API 호출하여 리뷰 삭제
    myReviews.value = myReviews.value.filter((r) => r.id !== review.id);
  }
  activeReviewMenu.value = null;
};

// 리뷰 텍스트 더보기/접기 상태
const expandedReviews = ref(new Set());

// 리뷰 확장/축소 토글
const toggleReviewExpand = (reviewId) => {
  if (expandedReviews.value.has(reviewId)) {
    expandedReviews.value.delete(reviewId);
  } else {
    expandedReviews.value.add(reviewId);
  }
};

// 리뷰 텍스트 길이 체크 (70자 이상이면 더보기 버튼 표시)
const shouldShowExpandButton = (content) => {
  return content.length > 70;
};

// 리뷰 텍스트 자르기
const truncateText = (content, reviewId) => {
  if (expandedReviews.value.has(reviewId) || content.length <= 70) {
    return content;
  }
  return content.substring(0, 70) + '...';
};

// URL에서 highlight 쿼리 파라미터 가져오기
const highlightedReviewId = computed(() => route.query.highlight);

// 하이라이트된 리뷰로 스크롤
const scrollToHighlightedReview = () => {
  if (highlightedReviewId.value) {
    nextTick(() => {
      const element = document.getElementById(
        `review-${highlightedReviewId.value}`
      );
      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center' });
        // 하이라이트 효과를 위해 잠시 후 클래스 추가
        setTimeout(() => {
          element.classList.add('highlight-pulse');
          setTimeout(() => {
            element.classList.remove('highlight-pulse');
          }, 2000);
        }, 500);
      }
    });
  }
};

onMounted(() => {
  scrollToHighlightedReview();
});
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink to="/mypage" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">내가 쓴 리뷰</h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-8">
      <!-- 리뷰 개수 표시 -->
      <div class="px-4 py-4 bg-white border-b border-[#e9ecef]">
        <p class="text-sm text-[#6c757d]">
          총
          <span class="font-semibold text-[#1e3a5f]">{{
            myReviews.length
          }}</span
          >개의 리뷰
        </p>
      </div>

      <!-- 리뷰가 없는 경우 -->
      <div
        v-if="myReviews.length === 0"
        class="flex flex-col items-center justify-center py-16 px-4"
      >
        <div
          class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mb-4"
        >
          <Star class="w-10 h-10 text-gray-300" />
        </div>
        <p class="text-[#6c757d] text-sm mb-6">아직 작성한 리뷰가 없습니다.</p>
        <RouterLink to="/my-reservations?tab=past">
          <button
            class="px-6 py-3 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
          >
            지난 예약 보기
          </button>
        </RouterLink>
      </div>

      <!-- 리뷰 목록 -->
      <div v-else class="px-4 pt-4 space-y-3">
        <Card
          v-for="review in myReviews"
          :key="review.id"
          :id="`review-${review.id}`"
          :class="`p-4 border-[#e9ecef] rounded-2xl bg-white shadow-card hover:shadow-md transition-all ${
            highlightedReviewId === review.id ? 'ring-2 ring-orange-400' : ''
          }`"
        >
          <!-- 식당 정보 -->
          <div class="flex items-start justify-between mb-3">
            <div class="flex-1">
              <RouterLink
                :to="`/restaurant/${review.restaurant.id}`"
                class="block hover:text-blue-600 transition-colors"
              >
                <h3 class="font-semibold text-[#1e3a5f] text-base mb-1">
                  {{ review.restaurant.name }}
                </h3>
              </RouterLink>
              <div class="flex items-start gap-1 text-xs text-[#6c757d]">
                <MapPin class="w-3 h-3 mt-0.5 flex-shrink-0" />
                <span>{{ review.restaurant.address }}</span>
              </div>
            </div>

            <!-- 햄버거 메뉴 버튼 -->
            <div class="relative ml-2">
              <button
                @click="toggleReviewMenu(review.id)"
                class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors"
              >
                <MoreVertical class="w-4 h-4 text-gray-600" />
              </button>

              <!-- 드롭다운 메뉴 -->
              <div
                v-if="activeReviewMenu === review.id"
                class="absolute top-10 right-0 bg-white border border-gray-200 rounded-lg shadow-lg py-1 min-w-[120px] z-10"
              >
                <button
                  @click="handleEditReview(review)"
                  class="w-full flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                >
                  <Edit class="w-4 h-4" />
                  수정
                </button>
                <button
                  @click="handleDeleteReview(review)"
                  class="w-full flex items-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
                >
                  <Trash2 class="w-4 h-4" />
                  삭제
                </button>
              </div>
            </div>
          </div>

          <!-- 별점 & 방문 정보 -->
          <div class="flex items-center justify-between mb-3">
            <div class="flex items-center gap-2">
              <div class="flex items-center gap-1">
                <Star
                  v-for="(_, i) in Array.from({ length: review.rating })"
                  :key="i"
                  class="w-4 h-4 fill-[#ffc107] text-[#ffc107]"
                />
              </div>
              <span class="text-xs text-[#6c757d]">
                {{ review.visitCount }}번째 방문
              </span>
            </div>
            <div class="text-xs text-[#6c757d]">
              <span>{{ review.date }}</span>
            </div>
          </div>

          <!-- 리뷰 이미지 (있는 경우) -->
          <div
            v-if="review.images && review.images.length > 0"
            class="mb-3 -mx-4"
          >
            <div class="flex gap-2 overflow-x-auto px-4 scrollbar-hide">
              <RouterLink
                v-for="(image, idx) in review.images"
                :key="idx"
                :to="`/restaurant/${review.restaurant.id}/reviews/${review.id}`"
                class="flex-shrink-0 w-32 h-32"
              >
                <div
                  class="relative rounded-lg overflow-hidden bg-[#f8f9fa] h-full hover:opacity-95 transition-opacity"
                >
                  <img
                    :src="image || '/placeholder.svg'"
                    :alt="`리뷰 이미지 ${idx + 1}`"
                    class="w-full h-full object-cover"
                  />
                </div>
              </RouterLink>
            </div>
          </div>

          <!-- 리뷰 내용 -->
          <div class="mb-3">
            <p class="text-sm text-[#495057] leading-relaxed">
              {{ truncateText(review.content, review.id) }}
            </p>
            <!-- 더보기/접기 버튼 -->
            <button
              v-if="shouldShowExpandButton(review.content)"
              @click="toggleReviewExpand(review.id)"
              class="text-xs text-[#6c757d] hover:text-[#ff6b4a] font-medium mt-1 transition-colors"
            >
              {{ expandedReviews.has(review.id) ? '접기' : '더보기' }}
            </button>
          </div>

          <!-- 태그 -->
          <RouterLink
            :to="`/restaurant/${review.restaurant.id}/reviews/${review.id}`"
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

          <!-- 리뷰 상세 보기 버튼 -->
          <div class="mt-3 pt-3 border-t border-[#e9ecef]">
            <RouterLink
              :to="`/restaurant/${review.restaurant.id}/reviews/${review.id}`"
              class="text-sm text-blue-600 hover:text-blue-700 font-medium transition-colors"
            >
              리뷰 상세 보기 →
            </RouterLink>
          </div>
        </Card>
      </div>
    </main>
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

/* 하이라이트 애니메이션 */
@keyframes highlight-pulse {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(251, 146, 60, 0);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(251, 146, 60, 0.3);
  }
}

.highlight-pulse {
  animation: highlight-pulse 2s ease-in-out;
}
</style>
