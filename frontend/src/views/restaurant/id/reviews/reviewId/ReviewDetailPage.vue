<script setup>
import { ref, computed, onMounted } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import {
  ArrowLeft,
  Star,
  ChevronLeft,
  ChevronRight,
  X,
  MessageSquare,
} from "lucide-vue-next";
import Card from "@/components/ui/Card.vue";

const route = useRoute();
const router = useRouter();
const restaurantId = route.params.id || "1";
const reviewId = route.params.reviewId;

// Mock data - 실제 구현 시 API에서 reviewId로 조회
const reviews = [
  {
    id: "1",
    author: "김**",
    company: "네이버",
    visitCount: 3,
    rating: 5,
    date: "2024.01.15",
    content: "회식하기 정말 좋았어요. 음식도 맛있고 분위기도 최고였습니다!",
    tags: ["룸이 있어 프라이빗해요", "대화하기 좋아요", "양이 푸짐해요"],
    images: [
      "/korean-appetizer-main-dessert.jpg",
      "/premium-course-meal-with-wine.jpg",
      "/korean-fine-dining.jpg",
    ],
    detailedContent: `회사 팀 회식으로 방문했는데 정말 만족스러웠습니다.

먼저 룸으로 안내받아서 다른 손님들 눈치 보지 않고 편하게 대화하며 식사할 수 있었어요. 음식은 하나하나 정성스럽게 나왔고, 특히 메인 요리가 정말 맛있었습니다.

직원분들도 친절하시고 서비스가 세심했어요. 가격대는 있지만 그만큼 충분히 가치가 있는 곳입니다. 

다음 회식 때도 꼭 다시 방문하고 싶습니다!`,
    // 예약/방문 정보
    visitInfo: {
      date: "2025년 11월 15일 (금)",
      partySize: 8,
      totalAmount: 111000,
      menuItems: [
        { name: "메뉴명1", quantity: 1, price: 18000 },
        { name: "메뉴명2", quantity: 2, price: 9000 },
        { name: "메뉴명3", quantity: 3, price: 11000 },
      ],
    },
    // 댓글 (사장님/관리자가 작성)
    comments: [
      {
        id: "comment-1",
        authorType: "owner", // 'owner' (사장님) 또는 'admin' (관리자)
        authorName: "식당명",
        content:
          "소중한 리뷰 감사합니다! 저희 식당을 방문해주셔서 감사드리며, 더욱 노력하는 모습 보여드리겠습니다. 다음에도 좋은 추억 만들어드릴 수 있도록 최선을 다하겠습니다.",
        createdAt: "2024.01.16 14:30",
      },
      {
        id: "comment-2",
        authorType: "owner",
        authorName: "식당명",
        content:
          "추가로, 다음 방문 시 웰컴 드링크를 서비스로 제공해드리겠습니다. 예약 시 리뷰 고객님이라고 말씀해주세요!",
        createdAt: "2024.01.16 15:45",
      },
    ],
  },
  {
    id: "2",
    author: "이**",
    company: null,
    visitCount: 2,
    rating: 5,
    date: "2024.01.10",
    content: "직원분들이 친절하시고 코스 구성이 알차서 만족스러웠습니다!",
    tags: ["사장님이 친절해요", "예약 시간 맞춰 세팅돼요"],
    images: [],
    detailedContent: `B코스를 주문했는데 구성이 정말 알차더라고요. 전채부터 메인, 디저트까지 하나하나 맛있었습니다.

특히 직원분들이 정말 친절하셔서 기분 좋게 식사할 수 있었어요. 음식 설명도 자세히 해주시고, 필요한 게 있으면 바로바로 챙겨주셨습니다.

회식 장소로 강력 추천합니다!`,
    visitInfo: {
      date: "2025년 10월 20일 (목)",
      partySize: 4,
      totalAmount: 65000,
      menuItems: [{ name: "B코스", quantity: 4, price: 16250 }],
    },
    comments: [
      {
        id: "comment-3",
        authorType: "owner",
        authorName: "식당명",
        content: "리뷰 감사합니다! 친절한 서비스로 보답하겠습니다.",
        createdAt: "2024.01.11 10:20",
      },
    ],
  },
  {
    id: "3",
    author: "박**",
    company: "카카오",
    visitCount: 1,
    rating: 4,
    date: "2024.01.05",
    content: "가격 대비 훌륭한 퀄리티입니다. 다음에 또 방문할게요.",
    tags: ["법카 쓰기 좋은 가격대에요", "주차가 편해요"],
    images: ["/italian-pasta-dish.png"],
    detailedContent: `주차장이 있어서 편하게 방문할 수 있었고, 음식 퀄리티도 가격 대비 정말 훌륭했습니다.

파스타를 주문했는데 면의 식감도 좋고 소스도 진하고 맛있었어요. 양도 적당해서 다른 메뉴도 함께 주문해서 나눠 먹기 좋았습니다.

다음에는 코스 메뉴도 도전해보고 싶네요!`,
    visitInfo: {
      date: "2025년 09월 10일 (화)",
      partySize: 2,
      totalAmount: 42000,
      menuItems: [
        { name: "까르보나라", quantity: 1, price: 18000 },
        { name: "알리오올리오", quantity: 1, price: 16000 },
        { name: "타파스", quantity: 2, price: 4000 },
      ],
    },
    comments: [
      {
        id: "comment-4",
        authorType: "admin",
        authorName: "런치고 관리자",
        content:
          "안녕하세요, 런치고 관리자입니다. 좋은 리뷰 감사드립니다. 더욱 나은 서비스를 제공할 수 있도록 노력하겠습니다.",
        createdAt: "2024.01.06 09:15",
      },
      {
        id: "comment-5",
        authorType: "owner",
        authorName: "식당명",
        content:
          "파스타 맛있게 드셨다니 기쁩니다. 다음에는 코스 메뉴도 꼭 맛보시길 추천드립니다!",
        createdAt: "2024.01.07 16:00",
      },
    ],
  },
  {
    id: "9",
    author: "[블라인드]",
    company: null,
    visitCount: null,
    rating: 0,
    date: "2023.12.08",
    content: "관리자에 의해 블라인드 처리된 리뷰입니다.",
    tags: [],
    images: [],
    isBlinded: true,
    blindReason: "욕설/비속어 포함",
    detailedContent:
      "이 리뷰는 약관 위반으로 블라인드 처리되었습니다. 런치고는 건전한 리뷰 문화를 위해 노력하고 있습니다.",
    visitInfo: null,
    comments: [], // 블라인드 리뷰는 댓글 없음
  },
];

// 댓글 개수 계산
const commentCount = computed(() => {
  return review.value?.comments?.length || 0;
});

const review = computed(() => {
  return reviews.find((r) => r.id === reviewId) || reviews[0];
});

// 이미지 확대 모달 상태
const isImageModalOpen = ref(false);
const modalImageUrl = ref("");
const modalImageIndex = ref(0);
const modalImages = ref([]);

// 이미지 클릭 시 모달 열기
const openImageModal = (images, index) => {
  modalImages.value = images;
  modalImageIndex.value = index;
  modalImageUrl.value = images[index];
  isImageModalOpen.value = true;
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

// 마우스 드래그 스크롤 기능
const setupDragScroll = (element) => {
  if (!element) return;

  let isDown = false;
  let startX;
  let scrollLeft;

  element.addEventListener("mousedown", (e) => {
    isDown = true;
    element.style.cursor = "grabbing";
    startX = e.pageX - element.offsetLeft;
    scrollLeft = element.scrollLeft;
  });

  element.addEventListener("mouseleave", () => {
    isDown = false;
    element.style.cursor = "grab";
  });

  element.addEventListener("mouseup", () => {
    isDown = false;
    element.style.cursor = "grab";
  });

  element.addEventListener("mousemove", (e) => {
    if (!isDown) return;
    e.preventDefault();
    const x = e.pageX - element.offsetLeft;
    const walk = (x - startX) * 2;
    element.scrollLeft = scrollLeft - walk;
  });
};

// 이전 단계로 이동
const goToPreviousStep = () => {
  router.back();
};

// 컴포넌트 마운트 후 드래그 스크롤 설정
onMounted(() => {
  const scrollContainer = document.querySelector(".review-image-scroll");
  if (scrollContainer) {
    setupDragScroll(scrollContainer);
  }
});
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <Button @click="goToPreviousStep()" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </Button>
        <h1 class="font-semibold text-[#1e3a5f] text-base">리뷰 상세</h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-8">
      <div class="p-4">
        <Card
          :class="`p-5 border-[#e9ecef] rounded-2xl bg-white shadow-card ${
            review.isBlinded ? 'opacity-60' : ''
          }`"
        >
          <!-- User Info & Rating -->
          <div class="flex items-start justify-between mb-4">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="font-bold text-[#1e3a5f] text-base">{{
                  review.author
                }}</span>
                <template v-if="review.company">
                  <span class="text-[#adb5bd]">•</span>
                  <span class="text-sm text-[#6c757d]">{{
                    review.company
                  }}</span>
                </template>
              </div>
              <div class="flex items-center gap-2">
                <div v-if="!review.isBlinded" class="flex items-center gap-1">
                  <Star
                    v-for="(_, i) in Array.from({ length: review.rating })"
                    :key="i"
                    class="w-4 h-4 fill-[#ffc107] text-[#ffc107]"
                  />
                </div>
                <span
                  v-if="!review.isBlinded && review.visitCount"
                  class="text-xs text-[#6c757d]"
                >
                  • {{ review.visitCount }}번째 방문
                </span>
              </div>
            </div>
            <span class="text-sm text-[#6c757d]">{{ review.date }}</span>
          </div>

          <!-- Visit Info (방문 정보) -->
          <div
            v-if="!review.isBlinded && review.visitInfo"
            class="mb-4 p-4 bg-[#f8f9fa] rounded-xl border border-[#e9ecef]"
          >
            <h3 class="text-sm font-semibold text-[#1e3a5f] mb-2">방문 정보</h3>
            <div class="space-y-1 text-sm text-[#495057] mb-3">
              <p>{{ review.visitInfo.date }}</p>
              <p>
                · {{ review.visitInfo.partySize }}명 ·
                {{ review.visitInfo.totalAmount.toLocaleString() }} 원
              </p>
            </div>

            <!-- Menu Items -->
            <div v-if="review.visitInfo.menuItems" class="space-y-2">
              <div
                class="grid grid-cols-12 gap-2 text-xs font-semibold text-[#6c757d] pb-2 border-b border-[#dee2e6]"
              >
                <div class="col-span-6">메뉴이름</div>
                <div class="col-span-2 text-center">수량</div>
                <div class="col-span-4 text-right">단가</div>
              </div>
              <div
                v-for="(item, index) in review.visitInfo.menuItems"
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

            <!-- Total Amount -->
            <div
              class="flex justify-between items-center pt-3 mt-3 border-t-2 border-[#1e3a5f]"
            >
              <span class="text-sm font-semibold text-[#1e3a5f]">총 금액</span>
              <span class="text-base font-bold text-[#1e3a5f]">
                {{ review.visitInfo.totalAmount.toLocaleString() }}원
              </span>
            </div>
          </div>

          <!-- Review Images (좌우 스크롤) -->
          <div
            v-if="
              !review.isBlinded && review.images && review.images.length > 0
            "
            class="mb-4 -mx-5"
          >
            <div
              class="flex gap-2 overflow-x-auto px-5 snap-x snap-mandatory scrollbar-hide review-image-scroll cursor-grab active:cursor-grabbing"
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
                  class="relative rounded-lg overflow-hidden bg-[#f8f9fa] h-64 cursor-pointer hover:opacity-95 transition-opacity"
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

          <!-- Review Tags or Blind Reason -->
          <div v-if="review.isBlinded" class="mb-4">
            <span
              class="inline-block px-3 py-1.5 text-sm rounded-full bg-[#6c757d] text-white font-medium"
            >
              사유: {{ review.blindReason }}
            </span>
          </div>
          <div
            v-else-if="review.tags && review.tags.length > 0"
            class="flex flex-wrap gap-2 mb-4"
          >
            <span
              v-for="(tag, idx) in review.tags"
              :key="idx"
              class="inline-block px-3 py-1.5 text-sm rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ffc4b8] text-white font-medium"
            >
              {{ tag }}
            </span>
          </div>

          <!-- Review Content -->
          <div
            class="text-sm text-[#495057] leading-relaxed whitespace-pre-line mb-4"
          >
            {{ review.detailedContent || review.content }}
          </div>

          <!-- 댓글 섹션 (사장님/관리자만 작성 가능) -->
          <div
            v-if="
              !review.isBlinded && review.comments && review.comments.length > 0
            "
            class="mt-6 pt-6 border-t-2 border-[#e9ecef]"
          >
            <!-- 댓글 헤더 -->
            <div class="flex items-center gap-2 mb-4">
              <MessageSquare class="w-5 h-5 text-[#1e3a5f]" />
              <h3 class="text-base font-semibold text-[#1e3a5f]">
                사장님 댓글
              </h3>
              <span class="text-sm text-[#6c757d]">({{ commentCount }})</span>
            </div>

            <!-- 댓글 목록 -->
            <div class="space-y-4">
              <div
                v-for="comment in review.comments"
                :key="comment.id"
                class="bg-[#f8f9fa] rounded-lg p-4"
              >
                <!-- 댓글 작성자 정보 -->
                <div class="flex items-center justify-between mb-2">
                  <div class="flex items-center gap-2">
                    <!-- 사장님/관리자 뱃지 -->
                    <span
                      :class="[
                        'px-2.5 py-1 rounded-full text-xs font-semibold',
                        comment.authorType === 'owner'
                          ? 'bg-blue-100 text-blue-700'
                          : 'bg-purple-100 text-purple-700',
                      ]"
                    >
                      {{ comment.authorType === "owner" ? "사장님" : "관리자" }}
                    </span>
                    <span class="font-medium text-[#1e3a5f] text-sm">
                      {{ comment.authorName }}
                    </span>
                  </div>
                  <span class="text-xs text-[#6c757d]">
                    {{ comment.createdAt }}
                  </span>
                </div>

                <!-- 댓글 내용 -->
                <p class="text-sm text-[#495057] leading-relaxed">
                  {{ comment.content }}
                </p>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </main>

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
