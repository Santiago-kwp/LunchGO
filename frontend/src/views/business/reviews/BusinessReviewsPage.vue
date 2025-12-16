<script setup>
import { ref, computed, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import {
  Star,
  MessageSquare,
  Calendar,
  User,
  Image as ImageIcon,
  ChevronLeft,
  ChevronRight,
  Search,
  Filter,
  AlertTriangle,
  CheckCircle,
  Clock,
  XCircle,
} from 'lucide-vue-next';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';

// 필터 상태
const selectedRating = ref('all'); // 'all', '5', '4', '3', '2', '1'
const searchQuery = ref('');
const selectedSort = ref('latest'); // 'latest', 'most-commented'
const selectedResponseStatus = ref('all'); // 'all', 'need-response', 'responded'
const selectedReportStatus = ref('all'); // 'all', 'none', 'pending', 'approved', 'rejected'

// 댓글 입력 상태
const commentInputs = ref({});
const showCommentInput = ref({});

// 모달 상태
const isImageModalOpen = ref(false);
const modalImages = ref([]);
const modalImageIndex = ref(0);

// 블라인드 요청 모달
const isReportModalOpen = ref(false);
const reportReviewId = ref(null);
const reportReason = ref('');

// Mock 데이터 - 실제로는 API에서 가져옴
const reviews = ref([
  {
    id: 'review-1',
    author: {
      name: '김철수',
      company: 'ABC 회사',
      isBlind: false,
    },
    rating: 5,
    visitCount: 3,
    visitInfo: {
      date: '2025년 11월 15일 (금)',
      partySize: 8,
      totalAmount: 111000,
      menuItems: [
        { name: '김치찌개', quantity: 4, price: 12000 },
        { name: '된장찌개', quantity: 2, price: 11000 },
        { name: '제육볶음', quantity: 2, price: 15000 },
      ],
    },
    images: [
      'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=800&h=600&fit=crop',
    ],
    tags: ['양이 푸짐해요', '가성비가 좋아요', '재방문 의사 있어요'],
    content:
      '회식하기 정말 좋은 곳이에요! 음식 양도 푸짐하고 맛도 좋아서 직원들 모두 만족했습니다. 특히 김치찌개가 일품이었어요.',
    createdAt: '2025-11-15T19:30:00',
    reportStatus: 'none', // 'none', 'pending', 'approved', 'rejected'
    reportReason: '',
    reportedAt: null,
    comments: [
      {
        id: 'comment-1',
        authorType: 'owner',
        authorName: '런치고 한식당',
        content: '소중한 리뷰 감사합니다! 다음에도 맛있게 해드릴게요 😊',
        createdAt: '2025-11-16T10:00:00',
      },
    ],
  },
  {
    id: 'review-2',
    author: {
      name: '이영희',
      company: 'XYZ 기업',
      isBlind: false,
    },
    rating: 4,
    visitCount: 1,
    visitInfo: {
      date: '2025년 11월 10일 (일)',
      partySize: 4,
      totalAmount: 68000,
      menuItems: [
        { name: '비빔밥', quantity: 3, price: 13000 },
        { name: '불고기', quantity: 1, price: 18000 },
        { name: '공기밥', quantity: 1, price: 2000 },
      ],
    },
    images: [
      'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800&h=600&fit=crop',
    ],
    tags: ['분위기가 좋아요', '주차가 편해요'],
    content:
      '음식이 깔끔하고 맛있어요. 다만 대기 시간이 좀 길었던 점은 아쉬웠습니다. 그래도 전반적으로 만족스러웠어요.',
    createdAt: '2025-11-10T14:20:00',
    reportStatus: 'none',
    reportReason: '',
    reportedAt: null,
    comments: [],
  },
  {
    id: 'review-3',
    author: {
      name: '블라인드',
      company: '블라인드',
      isBlind: true,
    },
    rating: 3,
    visitCount: 2,
    visitInfo: null,
    images: [],
    tags: [],
    content: '',
    blindReason: '부적절한 내용이 포함되어 블라인드 처리되었습니다.',
    createdAt: '2025-11-08T18:45:00',
    reportStatus: 'approved',
    reportReason: '허위 사실 유포',
    reportedAt: '2025-11-08T10:00:00',
    comments: [],
  },
  {
    id: 'review-4',
    author: {
      name: '박민수',
      company: 'DEF 그룹',
      isBlind: false,
    },
    rating: 5,
    visitCount: 5,
    visitInfo: {
      date: '2025년 11월 05일 (화)',
      partySize: 10,
      totalAmount: 185000,
      menuItems: [
        { name: '삼겹살', quantity: 5, price: 16000 },
        { name: '목살', quantity: 3, price: 15000 },
        { name: '공기밥', quantity: 10, price: 2000 },
        { name: '된장찌개', quantity: 5, price: 5000 },
      ],
    },
    images: [
      'https://images.unsplash.com/photo-1529042410759-befb1204b468?w=800&h=600&fit=crop',
      'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800&h=600&fit=crop',
    ],
    tags: [
      '양이 푸짐해요',
      '직원이 친절해요',
      '재방문 의사 있어요',
      '법카 쓰기 좋은 가격대에요',
    ],
    content:
      '단골 맛집입니다! 벌써 다섯 번째 방문인데 항상 만족스러워요. 고기 질도 좋고 직원분들도 친절하세요. 회식 장소로 강추합니다!',
    createdAt: '2025-11-05T20:15:00',
    comments: [
      {
        id: 'comment-2',
        authorType: 'owner',
        authorName: '런치고 한식당',
        content: '항상 찾아주셔서 감사합니다! 앞으로도 최선을 다하겠습니다 🙏',
        createdAt: '2025-11-06T09:30:00',
      },
    ],
    reportStatus: 'pending',
    reportReason: '욕설 포함',
    reportedAt: '2025-11-06T15:00:00',
  },
]);

// 통계 계산
const stats = computed(() => {
  const validReviews = reviews.value.filter((r) => !r.author.isBlind);
  const totalReviews = validReviews.length;
  const avgRating =
    totalReviews > 0
      ? (
          validReviews.reduce((sum, r) => sum + r.rating, 0) / totalReviews
        ).toFixed(1)
      : 0;
  const totalComments = reviews.value.reduce(
    (sum, r) => sum + r.comments.length,
    0
  );
  const needsResponse = reviews.value.filter(
    (r) => !r.author.isBlind && r.comments.length === 0
  ).length;
  const reportedReviews = reviews.value.filter(
    (r) => r.reportStatus === 'pending'
  ).length;

  return {
    totalReviews,
    avgRating,
    totalComments,
    needsResponse,
    reportedReviews,
  };
});

// 필터링된 리뷰
const filteredReviews = computed(() => {
  let result = [...reviews.value];

  // 평점 필터
  if (selectedRating.value !== 'all') {
    const rating = parseInt(selectedRating.value);
    result = result.filter((r) => r.rating === rating);
  }

  // 답변 상태 필터
  if (selectedResponseStatus.value === 'need-response') {
    result = result.filter((r) => !r.author.isBlind && r.comments.length === 0);
  } else if (selectedResponseStatus.value === 'responded') {
    result = result.filter((r) => r.comments.length > 0);
  }

  // 신고 상태 필터
  if (selectedReportStatus.value === 'none') {
    result = result.filter((r) => r.reportStatus === 'none');
  } else if (selectedReportStatus.value === 'pending') {
    result = result.filter((r) => r.reportStatus === 'pending');
  } else if (selectedReportStatus.value === 'approved') {
    result = result.filter((r) => r.reportStatus === 'approved');
  } else if (selectedReportStatus.value === 'rejected') {
    result = result.filter((r) => r.reportStatus === 'rejected');
  }

  // 검색어 필터
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(
      (r) =>
        r.content.toLowerCase().includes(query) ||
        r.author.name.toLowerCase().includes(query) ||
        r.author.company.toLowerCase().includes(query)
    );
  }

  // 정렬
  if (selectedSort.value === 'latest') {
    result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } else if (selectedSort.value === 'most-commented') {
    result.sort((a, b) => b.comments.length - a.comments.length);
  }

  return result;
});

// 날짜 포맷팅
const formatDate = (dateString) => {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}.${month}.${day} ${hours}:${minutes}`;
};

// 이미지 모달 열기
const openImageModal = (images, index) => {
  modalImages.value = images;
  modalImageIndex.value = index;
  isImageModalOpen.value = true;
};

// 이미지 모달 닫기
const closeImageModal = () => {
  isImageModalOpen.value = false;
  modalImages.value = [];
  modalImageIndex.value = 0;
};

// 이미지 모달 네비게이션
const nextImage = () => {
  modalImageIndex.value =
    (modalImageIndex.value + 1) % modalImages.value.length;
};

const prevImage = () => {
  modalImageIndex.value =
    (modalImageIndex.value - 1 + modalImages.value.length) %
    modalImages.value.length;
};

// 댓글 입력 토글
const toggleCommentInput = (reviewId) => {
  showCommentInput.value[reviewId] = !showCommentInput.value[reviewId];
  if (!commentInputs.value[reviewId]) {
    commentInputs.value[reviewId] = '';
  }
};

// 댓글 추가
const addComment = (reviewId) => {
  const content = commentInputs.value[reviewId]?.trim();
  if (!content) return;

  const review = reviews.value.find((r) => r.id === reviewId);
  if (review) {
    review.comments.push({
      id: `comment-${Date.now()}`,
      authorType: 'owner',
      authorName: '런치고 한식당',
      content,
      createdAt: new Date().toISOString(),
    });

    // 입력 초기화
    commentInputs.value[reviewId] = '';
    showCommentInput.value[reviewId] = false;
  }
};

// 댓글 삭제
const deleteComment = (reviewId, commentId) => {
  const review = reviews.value.find((r) => r.id === reviewId);
  if (review) {
    review.comments = review.comments.filter((c) => c.id !== commentId);
  }
};

// 블라인드 요청 모달 열기
const openReportModal = (reviewId) => {
  reportReviewId.value = reviewId;
  reportReason.value = '';
  isReportModalOpen.value = true;
};

// 블라인드 요청 모달 닫기
const closeReportModal = () => {
  isReportModalOpen.value = false;
  reportReviewId.value = null;
  reportReason.value = '';
};

// 블라인드 요청 제출
const submitReport = () => {
  if (!reportReason.value.trim()) {
    alert('신고 사유를 입력해주세요.');
    return;
  }

  const review = reviews.value.find((r) => r.id === reportReviewId.value);
  if (review) {
    review.reportStatus = 'pending';
    review.reportReason = reportReason.value;
    review.reportedAt = new Date().toISOString();
  }

  closeReportModal();
  alert('블라인드 요청이 접수되었습니다. 관리자 검토 후 처리됩니다.');
};

// 신고 상태 텍스트 및 스타일
const getReportStatusInfo = (status) => {
  const statusMap = {
    none: { text: '', color: '', icon: null },
    pending: {
      text: '검토 중',
      color: 'text-yellow-600 bg-yellow-50',
      icon: Clock,
    },
    approved: {
      text: '승인됨',
      color: 'text-green-600 bg-green-50',
      icon: CheckCircle,
    },
    rejected: {
      text: '거부됨',
      color: 'text-red-600 bg-red-50',
      icon: XCircle,
    },
  };
  return statusMap[status] || statusMap.none;
};

// 키보드 이벤트 (모달)
onMounted(() => {
  const handleKeydown = (e) => {
    if (!isImageModalOpen.value) return;
    if (e.key === 'ArrowLeft') prevImage();
    if (e.key === 'ArrowRight') nextImage();
    if (e.key === 'Escape') closeImageModal();
  };
  window.addEventListener('keydown', handleKeydown);
  return () => window.removeEventListener('keydown', handleKeydown);
});
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="reviews" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-8">
          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">리뷰 관리</h2>
          </div>

          <!-- Stats Cards -->
          <div class="grid grid-cols-5 gap-6">
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">전체 리뷰</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">
                {{ stats.totalReviews }}개
              </p>
            </div>
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">평균 평점</p>
              <p class="text-4xl font-bold gradient-primary-text">
                {{ stats.avgRating }}
                <span class="text-base">/ 5.0</span>
              </p>
            </div>
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">답변 완료</p>
              <p class="text-4xl font-bold text-[#28a745]">
                {{ stats.totalComments }}개
              </p>
            </div>
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">답변 필요</p>
              <p class="text-4xl font-bold text-[#ff6b4a]">
                {{ stats.needsResponse }}건
              </p>
            </div>
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">신고 대기</p>
              <p class="text-4xl font-bold text-[#ffc107]">
                {{ stats.reportedReviews }}건
              </p>
            </div>
          </div>

          <!-- Filters -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
            <div class="flex flex-col md:flex-row gap-4">
              <!-- 검색 -->
              <div class="flex-1 relative">
                <Search
                  class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-[#6c757d]"
                />
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="리뷰 내용, 작성자 검색..."
                  class="w-full pl-10 pr-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent"
                />
              </div>

              <!-- 평점 필터 -->
              <div class="flex items-center gap-2">
                <Filter class="w-5 h-5 text-[#6c757d]" />
                <select
                  v-model="selectedRating"
                  class="px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent"
                >
                  <option value="all">전체 평점</option>
                  <option value="5">⭐ 5점</option>
                  <option value="4">⭐ 4점</option>
                  <option value="3">⭐ 3점</option>
                  <option value="2">⭐ 2점</option>
                  <option value="1">⭐ 1점</option>
                </select>
              </div>

              <!-- 답변 상태 필터 -->
              <select
                v-model="selectedResponseStatus"
                class="px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent"
              >
                <option value="all">전체 답변 상태</option>
                <option value="need-response">답변 필요</option>
                <option value="responded">답변 완료</option>
              </select>

              <!-- 신고 상태 필터 -->
              <select
                v-model="selectedReportStatus"
                class="px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent"
              >
                <option value="all">전체 신고 상태</option>
                <option value="none">신고 안됨</option>
                <option value="pending">검토 중</option>
                <option value="approved">승인됨</option>
                <option value="rejected">거부됨</option>
              </select>

              <!-- 정렬 -->
              <select
                v-model="selectedSort"
                class="px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent"
              >
                <option value="latest">최신순</option>
                <option value="most-commented">댓글 많은순</option>
              </select>
            </div>
          </div>

          <!-- Reviews List -->
          <div class="space-y-6">
            <div
              v-for="review in filteredReviews"
              :key="review.id"
              class="bg-white rounded-2xl border border-[#e9ecef] p-6 shadow-sm hover:shadow-md transition-shadow"
            >
              <!-- Review Header -->
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-start gap-4">
                  <div
                    class="w-12 h-12 rounded-full bg-gradient-to-br from-[#ff6b4a] to-[#ffc4b8] flex items-center justify-center text-white font-semibold"
                  >
                    <User class="w-6 h-6" />
                  </div>
                  <div>
                    <div class="flex items-center gap-2 mb-1">
                      <span
                        :class="[
                          'font-semibold',
                          review.author.isBlind
                            ? 'text-[#6c757d]'
                            : 'text-[#1e3a5f]',
                        ]"
                      >
                        {{ review.author.name }}
                      </span>
                      <span class="text-sm text-[#6c757d]">
                        {{ review.author.company }}
                      </span>
                      <span
                        v-if="!review.author.isBlind"
                        class="text-xs px-2 py-0.5 rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ffc4b8] text-white"
                      >
                        {{ review.visitCount }}번째 방문
                      </span>
                    </div>
                    <div class="flex items-center gap-3">
                      <div class="flex items-center gap-1">
                        <Star
                          v-for="i in 5"
                          :key="i"
                          :class="[
                            'w-4 h-4',
                            i <= review.rating
                              ? 'fill-[#ff6b4a] text-[#ff6b4a]'
                              : 'fill-[#dee2e6] text-[#dee2e6]',
                          ]"
                        />
                      </div>
                      <span class="text-sm text-[#6c757d]">
                        {{ formatDate(review.createdAt) }}
                      </span>
                      <!-- 신고 상태 뱃지 -->
                      <span
                        v-if="review.reportStatus !== 'none'"
                        :class="[
                          'flex items-center gap-1 text-xs px-2 py-1 rounded-full font-semibold',
                          getReportStatusInfo(review.reportStatus).color,
                        ]"
                      >
                        <component
                          :is="getReportStatusInfo(review.reportStatus).icon"
                          class="w-3 h-3"
                        />
                        {{ getReportStatusInfo(review.reportStatus).text }}
                      </span>
                    </div>
                  </div>
                </div>

                <!-- Actions -->
                <div class="flex items-center gap-2">
                  <RouterLink
                    :to="`/restaurant/${review.restaurantId || '1'}/reviews/${
                      review.id
                    }`"
                    class="text-sm text-[#ff6b4a] hover:underline"
                  >
                    상세보기
                  </RouterLink>
                  <!-- 블라인드 요청 버튼 (아직 신고하지 않은 경우만) -->
                  <button
                    v-if="
                      !review.author.isBlind && review.reportStatus === 'none'
                    "
                    @click="openReportModal(review.id)"
                    class="flex items-center gap-1 text-sm text-[#dc3545] hover:underline"
                  >
                    <AlertTriangle class="w-4 h-4" />
                    블라인드 요청
                  </button>
                </div>
              </div>

              <!-- Blind Review -->
              <div
                v-if="review.author.isBlind"
                class="bg-[#f8f9fa] rounded-lg p-4 text-center opacity-60"
              >
                <p class="text-sm text-[#6c757d]">{{ review.blindReason }}</p>
              </div>

              <!-- Normal Review Content -->
              <template v-else>
                <!-- Visit Info (if available) -->
                <div v-if="review.visitInfo" class="mb-4">
                  <div
                    class="bg-[#f8f9fa] border border-[#e9ecef] rounded-xl p-4"
                  >
                    <div class="flex items-center gap-6 text-sm mb-3">
                      <div class="flex items-center gap-2">
                        <Calendar class="w-4 h-4 text-[#6c757d]" />
                        <span class="text-[#1e3a5f]">{{
                          review.visitInfo.date
                        }}</span>
                      </div>
                      <div class="text-[#6c757d]">
                        {{ review.visitInfo.partySize }}명
                      </div>
                      <div class="font-semibold text-[#ff6b4a]">
                        {{ review.visitInfo.totalAmount.toLocaleString() }}원
                      </div>
                    </div>
                    <div class="border-t border-[#dee2e6] pt-3">
                      <table class="w-full text-sm">
                        <thead class="text-[#6c757d] text-xs">
                          <tr>
                            <th class="text-left pb-2">메뉴명</th>
                            <th class="text-center pb-2">수량</th>
                            <th class="text-right pb-2">단가</th>
                          </tr>
                        </thead>
                        <tbody class="text-[#1e3a5f]">
                          <tr
                            v-for="(item, idx) in review.visitInfo.menuItems"
                            :key="idx"
                            class="border-t border-[#e9ecef]"
                          >
                            <td class="py-1">{{ item.name }}</td>
                            <td class="text-center">{{ item.quantity }}개</td>
                            <td class="text-right">
                              {{ item.price.toLocaleString() }}원
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>

                <!-- Images -->
                <div
                  v-if="review.images.length > 0"
                  class="mb-4 flex gap-2 overflow-x-auto pb-2"
                >
                  <div
                    v-for="(image, idx) in review.images"
                    :key="idx"
                    class="relative flex-shrink-0 w-32 h-32 rounded-lg overflow-hidden cursor-pointer hover:opacity-90 transition-opacity"
                    @click="openImageModal(review.images, idx)"
                  >
                    <img
                      :src="image"
                      :alt="`리뷰 이미지 ${idx + 1}`"
                      class="w-full h-full object-cover"
                    />
                    <div
                      class="absolute top-2 right-2 bg-black/60 text-white text-xs px-2 py-1 rounded"
                    >
                      {{ idx + 1 }}/{{ review.images.length }}
                    </div>
                  </div>
                </div>

                <!-- Tags -->
                <div
                  v-if="review.tags.length > 0"
                  class="mb-4 flex flex-wrap gap-2"
                >
                  <span
                    v-for="tag in review.tags"
                    :key="tag"
                    class="px-3 py-1 rounded-full text-sm bg-gradient-to-r from-[#ff6b4a]/10 to-[#ffc4b8]/10 text-[#ff6b4a] border border-[#ff6b4a]/20"
                  >
                    {{ tag }}
                  </span>
                </div>

                <!-- Content -->
                <p class="text-[#1e3a5f] leading-relaxed mb-4">
                  {{ review.content }}
                </p>

                <!-- Comments Section -->
                <div
                  v-if="review.comments.length > 0"
                  class="border-t border-[#e9ecef] pt-4 space-y-3"
                >
                  <div
                    v-for="comment in review.comments"
                    :key="comment.id"
                    class="bg-[#f8f9fa] rounded-lg p-4"
                  >
                    <div class="flex items-start justify-between mb-2">
                      <div class="flex items-center gap-2">
                        <span
                          :class="[
                            'text-xs px-2 py-1 rounded font-semibold',
                            comment.authorType === 'owner'
                              ? 'bg-[#007bff] text-white'
                              : 'bg-[#6f42c1] text-white',
                          ]"
                        >
                          {{
                            comment.authorType === 'owner' ? '사장님' : '관리자'
                          }}
                        </span>
                        <span class="font-semibold text-[#1e3a5f]">
                          {{ comment.authorName }}
                        </span>
                        <span class="text-xs text-[#6c757d]">
                          {{ formatDate(comment.createdAt) }}
                        </span>
                      </div>
                      <button
                        @click="deleteComment(review.id, comment.id)"
                        class="text-xs text-[#dc3545] hover:underline"
                      >
                        삭제
                      </button>
                    </div>
                    <p class="text-sm text-[#1e3a5f]">{{ comment.content }}</p>
                  </div>
                </div>

                <!-- Comment Input Toggle -->
                <div class="border-t border-[#e9ecef] pt-4 mt-4">
                  <button
                    v-if="!showCommentInput[review.id]"
                    @click="toggleCommentInput(review.id)"
                    class="flex items-center gap-2 text-sm text-[#ff6b4a] hover:underline"
                  >
                    <MessageSquare class="w-4 h-4" />
                    댓글 작성하기
                  </button>

                  <!-- Comment Input Form -->
                  <div v-else class="space-y-2">
                    <textarea
                      v-model="commentInputs[review.id]"
                      placeholder="고객에게 답변을 남겨주세요..."
                      rows="3"
                      class="w-full px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent resize-none"
                    ></textarea>
                    <div class="flex gap-2 justify-end">
                      <button
                        @click="toggleCommentInput(review.id)"
                        class="px-4 py-2 border border-[#dee2e6] rounded-lg text-sm text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
                      >
                        취소
                      </button>
                      <button
                        @click="addComment(review.id)"
                        class="px-4 py-2 bg-gradient-to-r from-[#ff6b4a] to-[#ffc4b8] text-white rounded-lg text-sm hover:opacity-90 transition-opacity"
                      >
                        댓글 등록
                      </button>
                    </div>
                  </div>
                </div>
              </template>
            </div>

            <!-- Empty State -->
            <div
              v-if="filteredReviews.length === 0"
              class="bg-white rounded-2xl border border-[#e9ecef] p-12 text-center"
            >
              <MessageSquare class="w-16 h-16 text-[#dee2e6] mx-auto mb-4" />
              <p class="text-lg font-semibold text-[#1e3a5f] mb-2">
                조회된 리뷰가 없습니다
              </p>
              <p class="text-sm text-[#6c757d]">
                필터 조건을 변경하거나 검색어를 확인해주세요.
              </p>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- Image Modal -->
    <Teleport to="body">
      <div
        v-if="isImageModalOpen"
        class="fixed inset-0 bg-black/90 z-50 flex items-center justify-center p-4"
        @click="closeImageModal"
      >
        <button
          @click.stop="closeImageModal"
          class="absolute top-4 right-4 text-white hover:text-[#ff6b4a] transition-colors z-10"
        >
          <svg
            class="w-8 h-8"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>

        <button
          v-if="modalImages.length > 1"
          @click.stop="prevImage"
          class="absolute left-4 text-white hover:text-[#ff6b4a] transition-colors"
        >
          <ChevronLeft class="w-12 h-12" />
        </button>

        <div class="max-w-4xl max-h-[90vh] relative" @click.stop>
          <img
            :src="modalImages[modalImageIndex]"
            alt="리뷰 이미지"
            class="max-w-full max-h-[90vh] object-contain rounded-lg"
          />
          <div
            class="absolute bottom-4 left-1/2 -translate-x-1/2 bg-black/60 text-white px-4 py-2 rounded-full text-sm"
          >
            {{ modalImageIndex + 1 }} / {{ modalImages.length }}
          </div>
        </div>

        <button
          v-if="modalImages.length > 1"
          @click.stop="nextImage"
          class="absolute right-4 text-white hover:text-[#ff6b4a] transition-colors"
        >
          <ChevronRight class="w-12 h-12" />
        </button>
      </div>
    </Teleport>

    <!-- Report Modal (블라인드 요청 모달) -->
    <Teleport to="body">
      <div
        v-if="isReportModalOpen"
        class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
        @click="closeReportModal"
      >
        <div
          class="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl"
          @click.stop
        >
          <div class="flex items-center justify-between mb-4">
            <h3
              class="text-xl font-bold text-[#1e3a5f] flex items-center gap-2"
            >
              <AlertTriangle class="w-6 h-6 text-[#dc3545]" />
              블라인드 요청
            </h3>
            <button
              @click="closeReportModal"
              class="text-[#6c757d] hover:text-[#1e3a5f] transition-colors"
            >
              <svg
                class="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>

          <div class="mb-4">
            <p class="text-sm text-[#6c757d] mb-2">
              부적절한 리뷰를 관리자에게 신고하여 블라인드 처리를 요청할 수
              있습니다.
            </p>
            <p class="text-xs text-[#dc3545]">
              ※ 허위 신고는 제재 대상이 될 수 있습니다.
            </p>
          </div>

          <div class="mb-6">
            <label class="block text-sm font-semibold text-[#1e3a5f] mb-2">
              신고 사유 <span class="text-[#dc3545]">*</span>
            </label>
            <textarea
              v-model="reportReason"
              placeholder="신고 사유를 상세히 작성해주세요&#10;예) 허위 사실 유포, 욕설 포함, 명예훼손 등"
              rows="4"
              class="w-full px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent resize-none"
            ></textarea>
            <p class="text-xs text-[#6c757d] mt-1">
              최소 10자 이상 작성해주세요
            </p>
          </div>

          <div class="flex gap-3">
            <button
              @click="closeReportModal"
              class="flex-1 px-4 py-2 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
            >
              취소
            </button>
            <button
              @click="submitReport"
              class="flex-1 px-4 py-2 bg-gradient-to-r from-[#dc3545] to-[#c82333] text-white rounded-lg hover:opacity-90 transition-opacity"
            >
              신고하기
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.gradient-primary-text {
  background: linear-gradient(135deg, #ff6b4a 0%, #ffaa8d 50%, #ffc4b8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
}

/* 스크롤바 스타일링 */
.overflow-x-auto::-webkit-scrollbar {
  height: 6px;
}

.overflow-x-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.overflow-x-auto::-webkit-scrollbar-thumb {
  background: #ff6b4a;
  border-radius: 10px;
}

.overflow-x-auto::-webkit-scrollbar-thumb:hover {
  background: #e55a39;
}
</style>
