<script setup>
import { ref, computed } from 'vue';
import AdminSidebar from '@/components/ui/AdminSideBar.vue';
import AdminHeader from '@/components/ui/AdminHeader.vue';
import Pagination from '@/components/ui/Pagination.vue';
import AdminSearchFilter from '@/components/ui/AdminSearchFilter.vue';
import Button from '@/components/ui/Button.vue';
import {
  MessageSquare,
  Star,
  AlertTriangle,
  Clock,
  ThumbsUp,
  ArrowUpRight,
  ArrowDownRight,
} from 'lucide-vue-next';

// 검색 및 필터
const searchQuery = ref('');
const selectedStatus = ref('all');
const startDate = ref('');
const endDate = ref('');

// 페이지네이션
const currentPage = ref(1);
const itemsPerPage = 10;

// 리뷰 상태 옵션
const statusOptions = [
  { value: 'all', label: '전체' },
  { value: 'pending', label: '답변 대기' },
  { value: 'answered', label: '답변 완료' },
  { value: 'reported', label: '신고 접수' },
  { value: 'hidden', label: '블라인드' },
];

// 리뷰 상태별 배지 색상
const getStatusBadgeColor = (status) => {
  const colors = {
    pending: 'bg-yellow-100 text-yellow-800',
    answered: 'bg-green-100 text-green-800',
    reported: 'bg-red-100 text-red-800',
    hidden: 'bg-gray-100 text-gray-800',
  };
  return colors[status] || 'bg-gray-100 text-gray-800';
};

// 리뷰 상태 라벨
const getStatusLabel = (status) => {
  const labels = {
    pending: '답변 대기',
    answered: '답변 완료',
    reported: '신고 접수',
    hidden: '블라인드',
  };
  return labels[status] || status;
};

// 작성자명 마스킹 함수
const maskName = (name) => {
  if (!name || name.length === 0) return '';
  if (name.length === 1) return name;
  if (name.length === 2) return name[0] + '*';
  return name[0] + '*'.repeat(name.length - 2) + name[name.length - 1];
};

// 리뷰 내용 미리보기 (30자 제한)
const getContentPreview = (content) => {
  if (!content) return '';
  return content.length > 30 ? content.substring(0, 30) + '...' : content;
};

// Mock 데이터 (총 65개)
const allReviews = ref([
  {
    id: 'RV001',
    reviewerName: '김민수',
    restaurantName: '한신포차 강남점',
    rating: 4.5,
    content:
      '음식이 맛있고 분위기도 좋았습니다. 직원분들도 친절하셔서 다시 방문하고 싶네요.',
    totalAmount: 85000,
    createdAt: '2024-12-15',
    status: 'answered',
  },
  {
    id: 'RV002',
    reviewerName: '이영희',
    restaurantName: '본죽&비빔밥 서초점',
    rating: 5.0,
    content: '건강한 한 끼 식사로 딱 좋았어요. 깔끔하고 맛있습니다.',
    totalAmount: 12000,
    createdAt: '2024-12-14',
    status: 'answered',
  },
  {
    id: 'RV003',
    reviewerName: '박철수',
    restaurantName: '스시로 판교점',
    rating: 1.0,
    content: '음식에서 이물질이 나왔어요. 너무 실망입니다.',
    totalAmount: 45000,
    createdAt: '2024-12-13',
    status: 'reported',
  },
  {
    id: 'RV004',
    reviewerName: '정다은',
    restaurantName: '아웃백 스테이크하우스 홍대점',
    rating: 4.0,
    content: '스테이크가 적당히 익어서 좋았고, 샐러드바도 신선했습니다.',
    totalAmount: 68000,
    createdAt: '2024-12-12',
    status: 'pending',
  },
  {
    id: 'RV005',
    reviewerName: '최지훈',
    restaurantName: '청년다방 신논현점',
    rating: 3.5,
    content: '평범한 맛이었어요. 가격 대비 괜찮은 편입니다.',
    totalAmount: 18000,
    createdAt: '2024-12-11',
    status: 'answered',
  },
  {
    id: 'RV006',
    reviewerName: '강서연',
    restaurantName: '곱창이야기 강남점',
    rating: 5.0,
    content: '곱창이 정말 신선하고 맛있었어요! 소스도 환상적이었습니다.',
    totalAmount: 52000,
    createdAt: '2024-12-10',
    status: 'answered',
  },
  {
    id: 'RV007',
    reviewerName: '윤준호',
    restaurantName: '교촌치킨 역삼점',
    rating: 4.0,
    content: '치킨이 바삭하고 맛있었습니다. 배달도 빨랐어요.',
    totalAmount: 24000,
    createdAt: '2024-12-09',
    status: 'pending',
  },
  {
    id: 'RV008',
    reviewerName: '임수진',
    restaurantName: '도쿄스테이크 압구정점',
    rating: 2.0,
    content: '주문한 음식이 너무 늦게 나왔고, 온도도 미지근했습니다.',
    totalAmount: 75000,
    createdAt: '2024-12-08',
    status: 'reported',
  },
  {
    id: 'RV009',
    reviewerName: '송민재',
    restaurantName: '한신포차 신촌점',
    rating: 4.5,
    content: '분위기 좋고 안주도 푸짐해요. 회식 장소로 추천합니다.',
    totalAmount: 120000,
    createdAt: '2024-12-07',
    status: 'answered',
  },
  {
    id: 'RV010',
    reviewerName: '한지우',
    restaurantName: '본죽&비빔밥 잠실점',
    rating: 3.0,
    content: '무난했어요. 특별히 나쁘지도 좋지도 않았습니다.',
    totalAmount: 14000,
    createdAt: '2024-12-06',
    status: 'pending',
  },
  // 추가 데이터 생성 (RV011 ~ RV065)
  ...Array.from({ length: 55 }, (_, i) => {
    const idx = i + 11;
    const ratings = [1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0];
    const rating = ratings[i % ratings.length];
    const statusList = [
      'pending',
      'answered',
      'answered',
      'answered',
      'reported',
      'hidden',
    ];
    const status = statusList[i % statusList.length];

    const monthOffset = Math.floor(i / 11);
    const now = new Date();
    const targetMonth = now.getMonth() - monthOffset;
    const targetYear = now.getFullYear() + Math.floor(targetMonth / 12);
    const finalMonth = (((targetMonth % 12) + 12) % 12) + 1;
    const day = 1 + (i % 28);

    return {
      id: `RV${String(idx).padStart(3, '0')}`,
      reviewerName:
        ['김', '이', '박', '정', '최', '강', '윤', '임', '송'][i % 9] +
        ['민수', '영희', '철수', '다은', '지훈'][i % 5],
      restaurantName:
        [
          '한신포차',
          '본죽&비빔밥',
          '스시로',
          '아웃백 스테이크하우스',
          '청년다방',
          '곱창이야기',
        ][i % 6] +
        ' ' +
        ['강남점', '서초점', '판교점', '홍대점'][i % 4],
      rating: rating,
      content:
        rating >= 4
          ? '음식이 맛있고 서비스도 좋았습니다. 재방문 의사 있습니다.'
          : rating >= 2.5
          ? '보통 수준이었어요. 가격 대비 괜찮은 편입니다.'
          : '기대에 못 미쳤습니다. 개선이 필요해 보입니다.',
      totalAmount: 10000 + i * 3000,
      createdAt: `${targetYear}-${String(finalMonth).padStart(2, '0')}-${String(
        day
      ).padStart(2, '0')}`,
      status: status,
    };
  }),
]);

// 필터링된 리뷰 목록
const filteredReviews = computed(() => {
  let filtered = allReviews.value;

  // 검색어 필터
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(
      (r) =>
        r.reviewerName.toLowerCase().includes(query) ||
        r.restaurantName.toLowerCase().includes(query) ||
        r.content.toLowerCase().includes(query)
    );
  }

  // 상태 필터
  if (selectedStatus.value !== 'all') {
    filtered = filtered.filter((r) => r.status === selectedStatus.value);
  }

  // 날짜 범위 필터
  if (startDate.value || endDate.value) {
    filtered = filtered.filter((r) => {
      const reviewDate = new Date(r.createdAt);

      if (startDate.value && endDate.value) {
        const start = new Date(startDate.value);
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999);
        return reviewDate >= start && reviewDate <= end;
      } else if (startDate.value) {
        const start = new Date(startDate.value);
        return reviewDate >= start;
      } else if (endDate.value) {
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999);
        return reviewDate <= end;
      }

      return true;
    });
  }

  return filtered;
});

// 통계 데이터
const stats = computed(() => {
  const now = new Date();
  const currentMonth = now.getMonth() + 1;
  const currentYear = now.getFullYear();
  const lastMonth = currentMonth === 1 ? 12 : currentMonth - 1;
  const lastMonthYear = currentMonth === 1 ? currentYear - 1 : currentYear;

  const allReviewsList = allReviews.value;

  // 평균 평점 계산
  const totalRating = allReviewsList.reduce((sum, r) => sum + r.rating, 0);
  const averageRating =
    allReviewsList.length > 0
      ? (totalRating / allReviewsList.length).toFixed(1)
      : 0;

  // 신고 리뷰
  const reportedReviews = allReviewsList.filter(
    (r) => r.status === 'reported'
  ).length;

  // 답변 대기 리뷰
  const pendingReviews = allReviewsList.filter(
    (r) => r.status === 'pending'
  ).length;

  // 높은 평점 리뷰 (4점 이상)
  const highRatingReviews = allReviewsList.filter(
    (r) => r.rating >= 4.0
  ).length;

  // 이번 달 리뷰
  const thisMonthReviews = allReviewsList.filter((r) => {
    const reviewDate = new Date(r.createdAt);
    return (
      reviewDate.getMonth() + 1 === currentMonth &&
      reviewDate.getFullYear() === currentYear
    );
  }).length;

  // 지난 달 리뷰
  const lastMonthReviews = allReviewsList.filter((r) => {
    const reviewDate = new Date(r.createdAt);
    return (
      reviewDate.getMonth() + 1 === lastMonth &&
      reviewDate.getFullYear() === lastMonthYear
    );
  }).length;

  // 전월 대비 증감
  const totalDiff = thisMonthReviews - lastMonthReviews;
  const reportedDiff = Math.floor(reportedReviews * 0.15); // Mock data
  const pendingDiff = Math.floor(pendingReviews * -0.1); // Mock data
  const highRatingDiff = Math.floor(highRatingReviews * 0.08); // Mock data

  return {
    total: allReviewsList.length,
    totalDiff,
    averageRating,
    reported: reportedReviews,
    reportedDiff,
    pending: pendingReviews,
    pendingDiff,
    highRating: highRatingReviews,
    highRatingDiff,
  };
});

// 페이지네이션된 리뷰 목록
const paginatedReviews = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredReviews.value.slice(start, end);
});

// 총 페이지 수
const totalPages = computed(() => {
  return Math.ceil(filteredReviews.value.length / itemsPerPage);
});

// 페이지 변경 핸들러
const handlePageChange = (page) => {
  currentPage.value = page;
};

// 필터 값 업데이트 핸들러
const handleFilterUpdate = ({ model, value }) => {
  if (model === 'selectedStatus') {
    selectedStatus.value = value;
  }
  currentPage.value = 1;
};

// 필터 초기화
const resetFilters = () => {
  searchQuery.value = '';
  selectedStatus.value = 'all';
  startDate.value = '';
  endDate.value = '';
  currentPage.value = 1;
};

// 필터 설정
const filters = computed(() => [
  {
    model: 'selectedStatus',
    label: '상태',
    value: selectedStatus.value,
    options: statusOptions,
  },
]);

// 관리 버튼 핸들러
const handleViewDetail = (review) => {
  alert(
    `상세 보기: ${review.id}\n작성자: ${review.reviewerName}\n식당: ${review.restaurantName}`
  );
};

const handleProcessReport = (review) => {
  alert(`신고 처리: ${review.id}\n식당: ${review.restaurantName}`);
};

const handleHideReview = (review) => {
  alert(`숨김 처리: ${review.id}\n식당: ${review.restaurantName}`);
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <AdminSidebar activeMenu="reviews" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <AdminHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-6">
          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">리뷰 관리</h2>
            <p class="text-sm text-[#6c757d]">
              총 {{ filteredReviews.length.toLocaleString() }}건
            </p>
          </div>

          <!-- 통계 카드 -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
            <!-- 전체 리뷰 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-purple-50 rounded-lg">
                  <MessageSquare class="w-6 h-6 text-purple-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">전체 리뷰</p>
              <p class="text-2xl font-bold text-purple-600 mb-2">
                {{ stats.total.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.totalDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1 text-purple-600"
                />
                <span class="text-purple-600">
                  {{ Math.abs(stats.totalDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 평균 평점 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-yellow-50 rounded-lg">
                  <Star class="w-6 h-6 text-yellow-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">평균 평점</p>
              <p class="text-2xl font-bold text-yellow-600">
                {{ stats.averageRating }}
              </p>
              <p class="text-xs text-[#6c757d] mt-2">5.0 만점</p>
            </div>

            <!-- 신고 리뷰 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-red-50 rounded-lg">
                  <AlertTriangle class="w-6 h-6 text-red-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">신고 리뷰</p>
              <p class="text-2xl font-bold text-red-600 mb-2">
                {{ stats.reported.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.reportedDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1 text-red-600"
                />
                <span class="text-red-600">
                  {{ Math.abs(stats.reportedDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 답변 대기 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-orange-50 rounded-lg">
                  <Clock class="w-6 h-6 text-orange-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">답변 대기</p>
              <p class="text-2xl font-bold text-orange-600 mb-2">
                {{ stats.pending.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.pendingDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1 text-orange-600"
                />
                <span class="text-orange-600">
                  {{ Math.abs(stats.pendingDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 높은 평점 리뷰 (4점 이상) -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-green-50 rounded-lg">
                  <ThumbsUp class="w-6 h-6 text-green-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">높은 평점 (4점↑)</p>
              <p class="text-2xl font-bold text-green-600 mb-2">
                {{ stats.highRating.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="
                    stats.highRatingDiff >= 0 ? ArrowUpRight : ArrowDownRight
                  "
                  class="w-4 h-4 mr-1 text-green-600"
                />
                <span class="text-green-600">
                  {{ Math.abs(stats.highRatingDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>
          </div>

          <!-- 필터 및 검색 -->
          <AdminSearchFilter
            v-model:search-query="searchQuery"
            v-model:start-date="startDate"
            v-model:end-date="endDate"
            :filters="filters"
            :show-date-filter="true"
            search-label="검색"
            search-placeholder="작성자, 식당명, 리뷰 내용으로 검색"
            start-date-label="작성일 (시작)"
            end-date-label="작성일 (종료)"
            @update:filter-value="handleFilterUpdate"
            @reset-filters="resetFilters"
          />

          <!-- 리뷰 목록 테이블 -->
          <div
            class="bg-white rounded-xl border border-[#e9ecef] overflow-hidden"
          >
            <div class="overflow-x-auto">
              <table class="w-full">
                <thead class="bg-[#f8f9fa] border-b border-[#e9ecef]">
                  <tr>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      작성자
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      식당명
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      평점
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      내용
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      총 금액
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      작성일
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      상태
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      관리
                    </th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-[#e9ecef]">
                  <tr
                    v-for="review in paginatedReviews"
                    :key="review.id"
                    class="hover:bg-[#f8f9fa] transition-colors"
                  >
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm font-medium text-[#1e3a5f]"
                    >
                      {{ maskName(review.reviewerName) }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ review.restaurantName }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm">
                      <div class="flex items-center">
                        <Star
                          class="w-4 h-4 text-yellow-500 fill-yellow-500 mr-1"
                        />
                        <span class="font-semibold text-[#1e3a5f]">{{
                          review.rating
                        }}</span>
                        <span class="text-[#6c757d] ml-1">/ 5.0</span>
                      </div>
                    </td>
                    <td class="px-6 py-4 text-sm text-[#495057] max-w-xs">
                      <div class="truncate">
                        {{ getContentPreview(review.content) }}
                      </div>
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ review.totalAmount.toLocaleString() }}원
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ review.createdAt }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span
                        :class="[
                          'px-3 py-1 rounded-full text-xs font-medium',
                          getStatusBadgeColor(review.status),
                        ]"
                      >
                        {{ getStatusLabel(review.status) }}
                      </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm">
                      <div class="flex gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          @click="handleViewDetail(review)"
                        >
                          상세 보기
                        </Button>
                        <Button
                          variant="destructive"
                          size="sm"
                          @click="handleProcessReport(review)"
                          v-if="review.status === 'reported'"
                        >
                          신고 처리
                        </Button>
                        <Button
                          variant="secondary"
                          size="sm"
                          @click="handleHideReview(review)"
                          v-if="review.status !== 'hidden'"
                        >
                          숨김 처리
                        </Button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>

              <!-- 데이터 없을 때 -->
              <div
                v-if="paginatedReviews.length === 0"
                class="text-center py-12 text-[#6c757d]"
              >
                <p class="text-lg">검색 결과가 없습니다.</p>
                <p class="text-sm mt-2">다른 조건으로 검색해보세요.</p>
              </div>
            </div>

            <!-- 페이지네이션 -->
            <div
              v-if="totalPages > 1"
              class="px-6 py-4 border-t border-[#e9ecef] flex justify-center"
            >
              <Pagination
                :current-page="currentPage"
                :total-pages="totalPages"
                @change-page="handlePageChange"
              />
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* 스크롤바 스타일링 */
.overflow-y-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #ff6b4a;
  border-radius: 10px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #e55a39;
}
</style>
