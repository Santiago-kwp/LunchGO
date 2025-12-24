<script setup>
import { ref, computed } from "vue";
import AdminSidebar from "@/components/ui/AdminSideBar.vue";
import AdminHeader from "@/components/ui/AdminHeader.vue";
import Pagination from "@/components/ui/Pagination.vue";
import AdminSearchFilter from "@/components/ui/AdminSearchFilter.vue";
import Button from "@/components/ui/Button.vue";
import {
  MessageSquare,
  Star,
  Calendar,
  User,
  AlertTriangle,
  Clock,
  ThumbsUp,
  ArrowUpRight,
  ArrowDownRight,
} from "lucide-vue-next";

// 검색 및 필터
const searchQuery = ref("");
const selectedStatus = ref("all");
const startDate = ref("");
const endDate = ref("");

// 모달 상태
const isDetailModalOpen = ref(false);
const isReportModalOpen = ref(false);
const selectedReview = ref(null);
const reportTagId = ref("");
const reportReason = ref("");

const adminReportTags = [
  { id: 21, name: "욕설/비속어 포함" },
  { id: 22, name: "인신공격/명예훼손" },
  { id: 23, name: "허위 사실 유포" },
  { id: 24, name: "도배/스팸/광고" },
  { id: 25, name: "경쟁 업체 비방" },
];

// 페이지네이션
const currentPage = ref(1);
const itemsPerPage = 10;

// 리뷰 상태 옵션
const statusOptions = [
  { value: "all", label: "전체" },
  { value: "pending", label: "답변 대기" },
  { value: "answered", label: "답변 완료" },
  { value: "reported", label: "신고 접수" },
  { value: "hidden", label: "블라인드" },
];

// 리뷰 상태별 배지 색상
const getStatusBadgeColor = (status) => {
  const colors = {
    pending: "bg-yellow-100 text-yellow-800",
    answered: "bg-green-100 text-green-800",
    reported: "bg-red-100 text-red-800",
    hidden: "bg-gray-100 text-gray-800",
  };
  return colors[status] || "bg-gray-100 text-gray-800";
};

// 리뷰 상태 라벨
const getStatusLabel = (status) => {
  const labels = {
    pending: "답변 대기",
    answered: "답변 완료",
    reported: "신고 접수",
    hidden: "블라인드",
  };
  return labels[status] || status;
};

// 작성자명 마스킹 함수
const maskName = (name) => {
  if (!name || name.length === 0) return "";
  if (name.length === 1) return name;
  if (name.length === 2) return name[0] + "*";
  return name[0] + "*".repeat(name.length - 2) + name[name.length - 1];
};

// 리뷰 내용 미리보기 (30자 제한)
const getContentPreview = (content) => {
  if (!content) return "";
  return content.length > 30 ? content.substring(0, 30) + "..." : content;
};

// 날짜 포맷팅
const formatDate = (dateString) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}.${month}.${day} ${hours}:${minutes}`;
};

// Mock 데이터 (총 65개)
const allReviews = ref([
  {
    id: "RV001",
    reviewerName: "김민수",
    restaurantName: "한신포차 강남점",
    rating: 4.5,
    content:
      "음식이 맛있고 분위기도 좋았습니다. 직원분들도 친절하셔서 다시 방문하고 싶네요.",
    totalAmount: 85000,
    createdAt: "2024-12-15",
    status: "answered",
    visitCount: 3,
    visitInfo: {
      date: "2024-12-15",
      partySize: 8,
      totalAmount: 85000,
      menuItems: [
        { name: "숙성 삼겹살", quantity: 2, price: 18000 },
        { name: "김치찌개", quantity: 1, price: 8000 },
      ],
    },
    images: [
      "https://images.unsplash.com/photo-1529692236671-f1dc1f1b0c35?w=800&h=600&fit=crop",
    ],
    tags: ["재료가 신선해요", "직원들이 적극적으로 도와줘요"],
    comments: [
      {
        id: "CM001",
        authorType: "owner",
        authorName: "한신포차 강남점",
        content: "리뷰 감사합니다. 다음 방문도 잘 모시겠습니다!",
        createdAt: "2024-12-16T10:10:00",
      },
    ],
    isTempHidden: false,
  },
  {
    id: "RV002",
    reviewerName: "이영희",
    restaurantName: "본죽&비빔밥 서초점",
    rating: 5.0,
    content: "건강한 한 끼 식사로 딱 좋았어요. 깔끔하고 맛있습니다.",
    totalAmount: 12000,
    createdAt: "2024-12-14",
    status: "answered",
    tags: ["가격 대비 만족스러워요"],
    images: [],
    comments: [],
    isTempHidden: false,
  },
  {
    id: "RV003",
    reviewerName: "박철수",
    restaurantName: "스시로 판교점",
    rating: 1.0,
    content: "음식에서 이물질이 나왔어요. 너무 실망입니다.",
    totalAmount: 45000,
    createdAt: "2024-12-13",
    status: "reported",
    tags: [],
    images: [],
    comments: [],
    isTempHidden: false,
  },
  {
    id: "RV004",
    reviewerName: "정다은",
    restaurantName: "아웃백 스테이크하우스 홍대점",
    rating: 4.0,
    content: "스테이크가 적당히 익어서 좋았고, 샐러드바도 신선했습니다.",
    totalAmount: 68000,
    createdAt: "2024-12-12",
    status: "pending",
    tags: ["인테리어가 세련돼요"],
    images: [],
    comments: [],
    isTempHidden: false,
  },
  {
    id: "RV005",
    reviewerName: "최지훈",
    restaurantName: "청년다방 신논현점",
    rating: 3.5,
    content: "평범한 맛이었어요. 가격 대비 괜찮은 편입니다.",
    totalAmount: 18000,
    createdAt: "2024-12-11",
    status: "answered",
    tags: ["가격 대비 만족스러워요"],
    images: [],
    comments: [],
    isTempHidden: false,
  },
  {
    id: "RV006",
    reviewerName: "강서연",
    restaurantName: "곱창이야기 강남점",
    rating: 5.0,
    content: "곱창이 정말 신선하고 맛있었어요! 소스도 환상적이었습니다.",
    totalAmount: 52000,
    createdAt: "2024-12-10",
    status: "answered",
    tags: ["재료가 신선해요"],
    images: [],
    comments: [],
    isTempHidden: false,
  },
  {
    id: "RV007",
    reviewerName: "윤준호",
    restaurantName: "교촌치킨 역삼점",
    rating: 4.0,
    content: "치킨이 바삭하고 맛있었습니다. 배달도 빨랐어요.",
    totalAmount: 24000,
    createdAt: "2024-12-09",
    status: "pending",
    tags: [],
    images: [],
    comments: [],
    isTempHidden: false,
  },
  {
    id: "RV008",
    reviewerName: "임수진",
    restaurantName: "도쿄스테이크 압구정점",
    rating: 2.0,
    content: "주문한 음식이 너무 늦게 나왔고, 온도도 미지근했습니다.",
    totalAmount: 75000,
    createdAt: "2024-12-08",
    status: "reported",
    tags: [],
    images: [],
    comments: [],
    isTempHidden: false,
  },
  {
    id: "RV009",
    reviewerName: "송민재",
    restaurantName: "한신포차 신촌점",
    rating: 4.5,
    content: "분위기 좋고 안주도 푸짐해요. 회식 장소로 추천합니다.",
    totalAmount: 120000,
    createdAt: "2024-12-07",
    status: "answered",
    tags: ["분위기가 좋아요"],
    images: [],
    comments: [],
    isTempHidden: false,
  },
  {
    id: "RV010",
    reviewerName: "한지우",
    restaurantName: "본죽&비빔밥 잠실점",
    rating: 3.0,
    content: "무난했어요. 특별히 나쁘지도 좋지도 않았습니다.",
    totalAmount: 14000,
    createdAt: "2024-12-06",
    status: "pending",
    tags: [],
    images: [],
    comments: [],
    isTempHidden: false,
  },
  // 추가 데이터 생성 (RV011 ~ RV065)
  ...Array.from({ length: 55 }, (_, i) => {
    const idx = i + 11;
    const ratings = [1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0];
    const rating = ratings[i % ratings.length];
    const statusList = [
      "pending",
      "answered",
      "answered",
      "answered",
      "reported",
      "hidden",
    ];
    const status = statusList[i % statusList.length];

    const monthOffset = Math.floor(i / 11);
    const now = new Date();
    const targetMonth = now.getMonth() - monthOffset;
    const targetYear = now.getFullYear() + Math.floor(targetMonth / 12);
    const finalMonth = (((targetMonth % 12) + 12) % 12) + 1;
    const day = 1 + (i % 28);

    return {
      id: `RV${String(idx).padStart(3, "0")}`,
      reviewerName:
        ["김", "이", "박", "정", "최", "강", "윤", "임", "송"][i % 9] +
        ["민수", "영희", "철수", "다은", "지훈"][i % 5],
      restaurantName:
        [
          "한신포차",
          "본죽&비빔밥",
          "스시로",
          "아웃백 스테이크하우스",
          "청년다방",
          "곱창이야기",
        ][i % 6] +
        " " +
        ["강남점", "서초점", "판교점", "홍대점"][i % 4],
      rating: rating,
      content:
        rating >= 4
          ? "음식이 맛있고 서비스도 좋았습니다. 재방문 의사 있습니다."
          : rating >= 2.5
          ? "보통 수준이었어요. 가격 대비 괜찮은 편입니다."
          : "기대에 못 미쳤습니다. 개선이 필요해 보입니다.",
      totalAmount: 10000 + i * 3000,
      createdAt: `${targetYear}-${String(finalMonth).padStart(2, "0")}-${String(
        day
      ).padStart(2, "0")}`,
      status: status,
      tags: [],
      images: [],
      comments: [],
      isTempHidden: false,
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
  if (selectedStatus.value !== "all") {
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
    (r) => r.status === "reported"
  ).length;

  // 답변 대기 리뷰
  const pendingReviews = allReviewsList.filter(
    (r) => r.status === "pending"
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
  if (model === "selectedStatus") {
    selectedStatus.value = value;
  }
  currentPage.value = 1;
};

// 필터 초기화
const resetFilters = () => {
  searchQuery.value = "";
  selectedStatus.value = "all";
  startDate.value = "";
  endDate.value = "";
  currentPage.value = 1;
};

// 필터 설정
const filters = computed(() => [
  {
    model: "selectedStatus",
    label: "상태",
    value: selectedStatus.value,
    options: statusOptions,
  },
]);

const selectedReviewDetail = computed(() => {
  if (!selectedReview.value) return null;
  return {
    id: selectedReview.value.id,
    restaurantName: selectedReview.value.restaurantName,
    author: {
      name: selectedReview.value.reviewerName,
      company: "",
      isBlind: selectedReview.value.status === "hidden",
    },
    rating: selectedReview.value.rating,
    visitCount: selectedReview.value.visitCount,
    visitInfo: selectedReview.value.visitInfo || null,
    images: selectedReview.value.images || [],
    tags: selectedReview.value.tags || [],
    content:
      selectedReview.value.status === "hidden" ? "" : selectedReview.value.content,
    blindReason:
      selectedReview.value.status === "hidden"
        ? "관리자에 의해 숨김 처리된 리뷰입니다."
        : "",
    createdAt: selectedReview.value.createdAt,
    comments: selectedReview.value.comments || [],
  };
});

// 관리 버튼 핸들러
const handleViewDetail = (review) => {
  selectedReview.value = review;
  isDetailModalOpen.value = true;
};

const handleProcessReport = (review) => {
  selectedReview.value = review;
  reportTagId.value = "";
  reportReason.value = "";
  isReportModalOpen.value = true;
};

const handleHideReview = (review) => {
  if (review.isTempHidden) {
    const confirmShow = confirm(
      `숨김 해제하시겠습니까?\n리뷰: ${review.id} (${review.restaurantName})`
    );
    if (confirmShow) {
      review.isTempHidden = false;
    } else {
      alert("숨김 해제를 취소했습니다.");
    }
    return;
  }

  const confirmHide = confirm(
    `숨김 처리하시겠습니까?\n리뷰: ${review.id} (${review.restaurantName})`
  );
  if (confirmHide) {
    review.isTempHidden = true;
  } else {
    alert("숨김 처리를 취소했습니다.");
  }
};

const closeDetailModal = () => {
  isDetailModalOpen.value = false;
  selectedReview.value = null;
};

const closeReportModal = () => {
  isReportModalOpen.value = false;
  selectedReview.value = null;
  reportTagId.value = "";
  reportReason.value = "";
};

const submitReportProcess = () => {
  if (!reportTagId.value) {
    alert("신고 태그를 선택해주세요.");
    return;
  }
  if (!reportReason.value.trim()) {
    alert("신고 사유를 입력해주세요.");
    return;
  }

  if (selectedReview.value) {
    selectedReview.value.status = "hidden";
    selectedReview.value.reportTagId = reportTagId.value;
    selectedReview.value.reportReason = reportReason.value.trim();
  }

  alert("신고 처리가 완료되었습니다.");
  closeReportModal();
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
              <p class="text-2xl font-bold mb-2">
                {{ stats.total.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.totalDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1"
                />
                <span class="">
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
              <p class="text-2xl font-bold">
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
              <p class="text-2xl font-bold mb-2">
                {{ stats.reported.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.reportedDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1"
                />
                <span class="">
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
              <p class="text-2xl font-bold mb-2">
                {{ stats.pending.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.pendingDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1"
                />
                <span class="">
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
              <p class="text-2xl font-bold mb-2">
                {{ stats.highRating.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="
                    stats.highRatingDiff >= 0 ? ArrowUpRight : ArrowDownRight
                  "
                  class="w-4 h-4 mr-1"
                />
                <span class="">
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
                        >
                          {{ review.isTempHidden ? "숨김 해제" : "숨김 처리" }}
                        </Button>
                      </div>
                      <div
                        v-if="review.isTempHidden"
                        class="mt-2 text-xs text-[#dc3545]"
                      >
                        임시 숨김 처리됨
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

  <!-- Detail Modal -->
  <Teleport to="body">
    <div
      v-if="isDetailModalOpen && selectedReviewDetail"
      class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
      @click="closeDetailModal"
    >
      <div
        class="bg-white rounded-2xl max-w-5xl w-full p-6 shadow-xl max-h-[90vh] overflow-y-auto"
        @click.stop
      >
        <div class="flex items-center justify-between mb-4">
          <div>
            <h3 class="text-xl font-bold text-[#1e3a5f]">리뷰 상세</h3>
            <p class="text-sm text-[#6c757d]">
              {{ selectedReviewDetail.restaurantName }}
            </p>
          </div>
          <button
            @click="closeDetailModal"
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

        <div class="bg-white rounded-2xl border border-[#e9ecef] p-6">
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
                      selectedReviewDetail.author.isBlind
                        ? 'text-[#6c757d]'
                        : 'text-[#1e3a5f]',
                    ]"
                  >
                    {{ selectedReviewDetail.author.name }}
                  </span>
                  <span class="text-sm text-[#6c757d]">
                    {{ selectedReviewDetail.author.company }}
                  </span>
                  <span
                    v-if="selectedReviewDetail.visitCount"
                    class="text-xs px-2 py-0.5 rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ffc4b8] text-white"
                  >
                    {{ selectedReviewDetail.visitCount }}번째 방문
                  </span>
                </div>
                <div class="flex items-center gap-3">
                  <div class="flex items-center gap-1">
                    <Star
                      v-for="i in 5"
                      :key="i"
                      :class="[
                        'w-4 h-4',
                        i <= selectedReviewDetail.rating
                          ? 'fill-[#ff6b4a] text-[#ff6b4a]'
                          : 'fill-[#dee2e6] text-[#dee2e6]',
                      ]"
                    />
                  </div>
                  <span class="text-sm text-[#6c757d]">
                    {{ formatDate(selectedReviewDetail.createdAt) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div
            v-if="selectedReviewDetail.author.isBlind"
            class="bg-[#f8f9fa] rounded-lg p-4 text-center opacity-60"
          >
            <p class="text-sm text-[#6c757d]">
              {{ selectedReviewDetail.blindReason }}
            </p>
          </div>

          <template v-else>
            <div v-if="selectedReviewDetail.visitInfo" class="mb-4">
              <div class="bg-[#f8f9fa] border border-[#e9ecef] rounded-xl p-4">
                <div class="flex items-center gap-6 text-sm mb-3">
                  <div class="flex items-center gap-2">
                    <Calendar class="w-4 h-4 text-[#6c757d]" />
                    <span class="text-[#1e3a5f]">{{
                      selectedReviewDetail.visitInfo.date
                    }}</span>
                  </div>
                  <div class="text-[#6c757d]">
                    {{ selectedReviewDetail.visitInfo.partySize }}명
                  </div>
                  <div class="font-semibold text-[#ff6b4a]">
                    {{
                      selectedReviewDetail.visitInfo.totalAmount.toLocaleString()
                    }}원
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
                        v-for="(item, idx) in selectedReviewDetail.visitInfo
                          .menuItems"
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

            <div
              v-if="selectedReviewDetail.images.length > 0"
              class="mb-4 flex gap-2 overflow-x-auto pb-2"
            >
              <div
                v-for="(image, idx) in selectedReviewDetail.images"
                :key="idx"
                class="relative flex-shrink-0 w-32 h-32 rounded-lg overflow-hidden"
              >
                <img
                  :src="image"
                  :alt="`리뷰 이미지 ${idx + 1}`"
                  class="w-full h-full object-cover"
                />
              </div>
            </div>

            <div
              v-if="selectedReviewDetail.tags.length > 0"
              class="mb-4 flex flex-wrap gap-2"
            >
              <span
                v-for="tag in selectedReviewDetail.tags"
                :key="tag"
                class="px-3 py-1 rounded-full text-sm bg-gradient-to-r from-[#ff6b4a]/10 to-[#ffc4b8]/10 text-[#ff6b4a] border border-[#ff6b4a]/20"
              >
                {{ tag }}
              </span>
            </div>

            <p class="text-[#1e3a5f] leading-relaxed mb-4">
              {{ selectedReviewDetail.content }}
            </p>

            <div
              v-if="selectedReviewDetail.comments.length > 0"
              class="border-t border-[#e9ecef] pt-4 space-y-3"
            >
              <div
                v-for="comment in selectedReviewDetail.comments"
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
                        comment.authorType === "owner" ? "사장님" : "관리자"
                      }}
                    </span>
                    <span class="font-semibold text-[#1e3a5f]">
                      {{ comment.authorName }}
                    </span>
                    <span class="text-xs text-[#6c757d]">
                      {{ formatDate(comment.createdAt) }}
                    </span>
                  </div>
                </div>
                <p class="text-sm text-[#1e3a5f]">{{ comment.content }}</p>
              </div>
            </div>
            <div
              v-else
              class="border-t border-[#e9ecef] pt-4 text-sm text-[#6c757d] flex items-center gap-2"
            >
              <MessageSquare class="w-4 h-4" />
              아직 등록된 댓글이 없습니다.
            </div>
          </template>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- Report Process Modal -->
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
          <h3 class="text-xl font-bold text-[#1e3a5f] flex items-center gap-2">
            <AlertTriangle class="w-6 h-6 text-[#dc3545]" />
            신고 처리
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
            신고된 리뷰에 적용할 태그와 사유를 입력해주세요.
          </p>
        </div>

        <div class="mb-4">
          <label class="block text-sm font-semibold text-[#1e3a5f] mb-2">
            신고 태그 <span class="text-[#dc3545]">*</span>
          </label>
          <select
            v-model="reportTagId"
            class="w-full px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent"
          >
            <option value="">태그를 선택해주세요</option>
            <option
              v-for="tag in adminReportTags"
              :key="tag.id"
              :value="tag.id"
            >
              {{ tag.name }}
            </option>
          </select>
        </div>

        <div class="mb-6">
          <label class="block text-sm font-semibold text-[#1e3a5f] mb-2">
            신고 사유 <span class="text-[#dc3545]">*</span>
          </label>
          <textarea
            v-model="reportReason"
            placeholder="신고 처리 사유를 입력해주세요."
            rows="4"
            class="w-full px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent resize-none"
          ></textarea>
        </div>

        <div class="flex gap-3">
          <button
            @click="closeReportModal"
            class="flex-1 px-4 py-2 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
          >
            취소
          </button>
          <button
            @click="submitReportProcess"
            class="flex-1 px-4 py-2 bg-gradient-to-r from-[#dc3545] to-[#c82333] text-white rounded-lg hover:opacity-90 transition-opacity"
          >
            처리 완료
          </button>
        </div>
      </div>
    </div>
  </Teleport>
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
