<script setup>
import { ref, computed, onMounted, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, X, Upload, Plus, Star } from "lucide-vue-next";
import Button from "@/components/ui/Button.vue";
import Card from "@/components/ui/Card.vue";
import httpRequest from "@/router/httpRequest";
import { useAccountStore } from "@/stores/account";

const route = useRoute();
const router = useRouter();
const accountStore = useAccountStore();
const restaurantId = route.params.id || "1";
const reviewId = route.params.reviewId; // 수정 모드일 때 리뷰 ID
const MAX_UPLOAD_SIZE = 10 * 1024 * 1024;
const ALLOWED_IMAGE_TYPES = ["image/jpeg", "image/png", "image/webp"];
const reservationId = computed(() => {
  const value = route.query.reservationId;
  if (!value) return null;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
});
const editReservationId = ref(null);

// 작성 모드 vs 수정 모드 판단
const isEditMode = computed(() => !!reviewId);

// 단계 관리 (1: 태그 선택, 2: 리뷰 작성)
const currentStep = ref(1);

// 영수증 업로드 모달
const isReceiptModalOpen = ref(false);
const isOcrProcessing = ref(false);

// 사진 업로드 모달
const isPhotoModalOpen = ref(false);

// 리뷰 등록 완료 모달
const isReviewCompleteModalOpen = ref(false);
const submittedReviewId = ref(null); // 등록된 리뷰 ID (API 응답에서 받아옴)
const getStoredMember = () => {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem("member");
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
};

const member = computed(() => accountStore.member || getStoredMember());
const memberId = computed(() => {
  const rawId = member.value?.id ?? member.value?.userId ?? member.value?.memberId;
  if (rawId === null || rawId === undefined) return null;
  const parsed = Number(rawId);
  return Number.isNaN(parsed) ? null : parsed;
});
const rating = ref(0);
const receiptId = ref(null);
const selectedTagIds = ref([]);
const tagIdByName = ref({});

// 방문 정보
const visitInfo = ref({
  restaurantName: "식당명",
  visitNumber: 1, // n번째 방문
});

// 영수증 정보
const receipt = ref({
  date: "",
  partySize: 0,
  totalAmount: 0,
  uploaded: false, // 영수증 업로드 여부
  items: [],
});
const receiptDraftItems = ref(
  receipt.value.items.map((item) => ({ ...item }))
);

// 선택된 태그
const selectedTags = ref([]);

// 태그 카테고리
const tagCategories = ref([
  {
    id: "speed",
    icon: "🏃",
    name: "속도/효율성",
    tags: [
      "주문 즉시 조리 시작해요",
      "계산이 빨라요",
      "웨이팅 관리가 잘 돼요",
      "음식이 동시에 나와요",
    ],
  },
  {
    id: "space",
    icon: "🪑",
    name: "공간/분위기",
    tags: [
      "인테리어가 세련돼요",
      "조명이 아늑해요",
      "아이 동반하기 좋아요",
      "야외 테라스가 있어요",
      "음악이 적당해요",
    ],
  },
  {
    id: "taste",
    icon: "🍲",
    name: "맛/가성비",
    tags: [
      "재료가 신선해요",
      "가격 대비 만족스러워요",
      "시그니처 메뉴가 있어요",
      "디저트가 맛있어요",
      "술과 안주 궁합이 좋아요",
    ],
  },
  {
    id: "service",
    icon: "🤝",
    name: "서비스/기타",
    tags: [
      "직원들이 적극적으로 도와줘요",
      "메뉴 설명을 잘 해줘요",
      "결제 방식이 다양해요 (QR, 간편결제 등)",
      "반려동물 동반 가능해요",
      "청결 관리가 잘 돼요",
    ],
  },
]);

// 리뷰 작성 데이터
const reviewPhotos = ref([]);
const reviewText = ref("");

// Mock 기존 리뷰 데이터 (API에서 가져올 데이터)
const existingReviews = {
  1: {
    id: 1,
    restaurantId: 1,
    restaurantName: "식당명",
    visitNumber: 2,
    receipt: {
      date: "2024년 11월 15일 (금)",
      partySize: 8,
      totalAmount: 111000,
      uploaded: true,
      items: [
        { name: "메뉴명1", quantity: 1, price: 18000 },
        { name: "메뉴명2", quantity: 2, price: 9000 },
        { name: "메뉴명3", quantity: 3, price: 11000 },
      ],
    },
    selectedTags: [
      "인테리어가 세련돼요", // space 카테고리
      "재료가 신선해요", // taste 카테고리
      "직원들이 적극적으로 도와줘요", // service 카테고리
    ],
    photos: [
      {
        id: 1,
        url: "/korean-appetizer-main-dessert.jpg",
        file: null,
      },
      {
        id: 2,
        url: "/premium-course-meal-with-wine.jpg",
        file: null,
      },
    ],
    text: "회식하기 정말 좋았어요. 음식도 맛있고 분위기도 최고였습니다! 특히 룸이 프라이빗해서 회사 동료들과 편하게 대화할 수 있었고, 음식 양도 정말 푸짐해서 배불리 먹었습니다.",
  },
  2: {
    id: 2,
    restaurantId: 1,
    restaurantName: "식당명",
    visitNumber: 3,
    receipt: {
      date: "2024년 11월 10일 (금)",
      partySize: 2,
      totalAmount: 42000,
      uploaded: false,
      items: [
        { name: "까르보나라", quantity: 1, price: 18000 },
        { name: "알리오올리오", quantity: 1, price: 16000 },
        { name: "타파스", quantity: 2, price: 4000 },
      ],
    },
    selectedTags: ["가격 대비 만족스러워요", "청결 관리가 잘 돼요"],
    photos: [
      {
        id: 3,
        url: "/italian-pasta-dish.png",
        file: null,
      },
    ],
    text: "가격 대비 훌륭한 퀄리티입니다. 다음에 또 방문할게요.",
  },
  3: {
    id: 3,
    restaurantId: 2,
    restaurantName: "맛있는집",
    visitNumber: 1,
    receipt: {
      date: "2024년 11월 10일 (금)",
      partySize: 2,
      totalAmount: 42000,
      uploaded: true,
      items: [
        { name: "까르보나라", quantity: 1, price: 18000 },
        { name: "알리오올리오", quantity: 1, price: 16000 },
        { name: "타파스", quantity: 2, price: 4000 },
      ],
    },
    selectedTags: ["메뉴 설명을 잘 해줘요", "시그니처 메뉴가 있어요"],
    images: [],
    text: "",
  },
};

// 수정 모드일 때 기존 리뷰 데이터 로드
const applyReservationSummary = (data, shouldApplyReceipt) => {
  visitInfo.value.restaurantName = data.restaurant?.name || "";
  const count = Number(data.visitCount);
  visitInfo.value.visitNumber = Number.isFinite(count) && count > 0 ? count : 1;

  if (!shouldApplyReceipt) return;

  receipt.value.date = formatOcrDate(data.booking?.date);
  receipt.value.partySize = data.booking?.partySize || 0;
  receipt.value.totalAmount = data.totalAmount ?? data.payment?.amount ?? 0;
  const menuItems = Array.isArray(data.menuItems) ? data.menuItems : [];
  const mappedItems = menuItems.map((item) => ({
    name: item.name,
    quantity: Number(item.quantity) || 0,
    price: Number(item.unitPrice) || 0,
  }));
  receipt.value.items = mappedItems.map((item) => ({ ...item }));
  receiptDraftItems.value = mappedItems.map((item) => ({ ...item }));
  receipt.value.uploaded = false;
};

const fetchReservationSummary = async (summaryReservationId, shouldApplyReceipt) => {
  if (!summaryReservationId) return;
  try {
    const response = await httpRequest.get(
      `/api/reservations/${summaryReservationId}/summary`
    );
    const data = response.data || {};
    applyReservationSummary(data, shouldApplyReceipt);
  } catch (error) {
    console.error("예약 정보 로드 실패:", error);
  }
};

const loadExistingReview = async () => {
  if (isEditMode.value && reviewId) {
    try {
      const response = await httpRequest.get(
        `/api/restaurants/${restaurantId}/reviews/${reviewId}/edit`
      );
      const data = response.data?.data ?? response.data;

      receiptId.value = data.receiptId || null;
      rating.value = data.rating ?? 0;
      reviewText.value = data.content || "";

      const tags = data.tags || [];
      selectedTags.value = tags.map((tag) => tag.name);
      selectedTagIds.value = tags.map((tag) => tag.tagId);
      tagIdByName.value = {
        ...tagIdByName.value,
        ...tags.reduce((acc, tag) => {
          acc[tag.name] = tag.tagId;
          return acc;
        }, {}),
      };

      reviewPhotos.value = (data.images || []).map((url, index) => ({
        id: `${Date.now()}-${index}`,
        url,
        file: null,
      }));

      editReservationId.value = data.reservationId ?? null;
      const summaryReservationId = editReservationId.value ?? reservationId.value;
      if (summaryReservationId) {
        await fetchReservationSummary(summaryReservationId, !data.receiptId);
      }

      if (data.visitInfo) {
        receipt.value.date = formatOcrDate(data.visitInfo.date);
        receipt.value.partySize = data.visitInfo.partySize || 0;
        receipt.value.totalAmount = data.visitInfo.totalAmount || 0;
        const mappedItems = (data.visitInfo.menuItems || []).map((item) => ({
          name: item.name,
          quantity: Number(item.qty) || 0,
          price: Number(item.unitPrice) || 0,
        }));
        receipt.value.items = mappedItems.map((item) => ({ ...item }));
        receiptDraftItems.value = mappedItems.map((item) => ({ ...item }));
        receipt.value.uploaded = !!data.receiptId;
        recalculateReceiptTotal(receipt.value.items);
      }
    } catch (error) {
      console.error("리뷰 수정 데이터 로드 실패:", error);
      const existingReview = existingReviews[reviewId];
      if (existingReview) {
        // 방문 정보 로드
        visitInfo.value.restaurantName = existingReview.restaurantName;
        visitInfo.value.visitNumber = existingReview.visitNumber;

        // 영수증 정보 로드
        receipt.value = { ...existingReview.receipt };
        receiptDraftItems.value = (receipt.value.items || []).map((item) => ({
          ...item,
        }));

        // 선택된 태그 로드
        selectedTags.value = [...existingReview.selectedTags];

        // 사진 로드
        reviewPhotos.value = existingReview.photos.map((photo) => ({
          ...photo,
        }));

        // 리뷰 텍스트 로드
        reviewText.value = existingReview.text;
      }
    }
  }
};

const loadTagMap = async () => {
  try {
    const response = await httpRequest.get("/api/reviews/tags");
    const payload = response.data?.data ?? response.data;
    const tags = Array.isArray(payload) ? payload : [];
    const map = tags.reduce((acc, tag) => {
      if (tag?.name && tag?.tagId) {
        acc[tag.name] = tag.tagId;
      }
      return acc;
    }, {});
    tagIdByName.value = map;
    return map;
  } catch (error) {
    console.error("리뷰 태그 목록 조회 실패:", error);
    return {};
  }
};

const loadReservationSummary = async () => {
  if (isEditMode.value || !reservationId.value) return;
  await fetchReservationSummary(reservationId.value, true);
};

// 태그 선택/해제
const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag);
  if (index > -1) {
    selectedTags.value.splice(index, 1);
    const tagId = tagIdByName.value[tag];
    if (tagId) {
      selectedTagIds.value = selectedTagIds.value.filter((id) => id !== tagId);
    }
  } else {
    if (selectedTags.value.length < 7) {
      selectedTags.value.push(tag);
      const tagId = tagIdByName.value[tag];
      if (tagId) {
        selectedTagIds.value.push(tagId);
      }
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
    if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
      alert("JPG, PNG, WEBP 파일만 업로드할 수 있습니다.");
      return;
    }
    if (file.size > MAX_UPLOAD_SIZE) {
      alert("파일 용량은 10MB 이하만 업로드할 수 있습니다.");
      return;
    }
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

const uploadReviewPhotos = async () => {
  const existingUrls = [];
  const uploadedUrls = [];

  for (const photo of reviewPhotos.value) {
    if (photo.file) {
      const formData = new FormData();
      formData.append("file", photo.file);

      const response = await httpRequest.post(
        "/api/v1/images/upload/reviews",
        formData,
        {
          headers: { "Content-Type": "multipart/form-data" },
        }
      );
      const data = response.data?.data ?? response.data;
      const url = data?.fileUrl || data?.key;
      if (url) {
        uploadedUrls.push(url);
        photo.url = url;
        photo.file = null;
      }
    } else if (typeof photo.url === "string" && photo.url.startsWith("http")) {
      existingUrls.push(photo.url);
    }
  }

  return [...existingUrls, ...uploadedUrls];
};

const setRating = (value) => {
  rating.value = value;
};

// 리뷰 등록 또는 수정
const submitReview = async () => {
  let imageUrls = [];
  try {
    imageUrls = await uploadReviewPhotos();
  } catch (error) {
    console.error("이미지 업로드 실패:", error);
    alert("이미지 업로드에 실패했습니다.");
    return;
  }

  let tagIdsForSubmit = selectedTags.value
    .map((tag) => tagIdByName.value[tag])
    .filter((id) => id);
  if (selectedTags.value.length > 0 && tagIdsForSubmit.length === 0) {
    const refreshedMap = await loadTagMap();
    tagIdsForSubmit = selectedTags.value
      .map((tag) => refreshedMap[tag])
      .filter((id) => id);
  }
  if (selectedTags.value.length > 0 && tagIdsForSubmit.length === 0) {
    alert("리뷰 태그 정보를 불러오지 못했습니다. 다시 시도해주세요.");
    return;
  }

  if (isEditMode.value) {
    try {
      const response = await httpRequest.put(
        `/api/restaurants/${restaurantId}/reviews/${reviewId}`,
        {
          receiptId: receiptId.value,
          rating: rating.value,
          content: reviewText.value,
          tagIds: tagIdsForSubmit,
          imageUrls: imageUrls,
          receiptItems: receiptDraftItems.value.map((item) => ({
            name: item.name,
            quantity: Number(item.quantity) || 0,
            price: Number(item.price) || 0,
          })),
        }
      );
      const updatedId = response.data.reviewId || reviewId;
      router.replace(`/restaurant/${restaurantId}/reviews/${updatedId}`);
    } catch (error) {
      console.error("리뷰 수정 실패:", error);
      alert("리뷰 수정에 실패했습니다.");
    }
  } else {
    try {
      if (!memberId.value) {
        alert("로그인이 필요합니다.");
        return;
      }
      const response = await httpRequest.post(
        `/api/restaurants/${restaurantId}/reviews`,
        {
          userId: memberId.value,
          reservationId: reservationId.value,
          receiptId: receiptId.value,
          rating: rating.value,
          content: reviewText.value,
          tagIds: tagIdsForSubmit,
          imageUrls: imageUrls,
        }
      );
      submittedReviewId.value = response.data.reviewId;
      isReviewCompleteModalOpen.value = true;
    } catch (error) {
      console.error("리뷰 등록 실패:", error);
      alert("리뷰 등록에 실패했습니다.");
    }
  }
};

// 리뷰 등록 완료 모달 닫기
const closeReviewCompleteModal = () => {
  isReviewCompleteModalOpen.value = false;
};

// 내 리뷰 보러가기
const goToMyReview = () => {
  closeReviewCompleteModal();
  router.push({
    path: `/restaurant/${restaurantId}/reviews/${submittedReviewId.value}`,
    query: { from: "my-reservations" },
  });
};

// 지난 예약 페이지로 가기
const goToMyReservations = () => {
  closeReviewCompleteModal();
  router.push({ path: "/my-reservations", query: { tab: "past" } });
};

// 홈으로 가기
const goToHome = () => {
  closeReviewCompleteModal();
  router.push("/");
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
const handleReceiptUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
    alert("JPG, PNG, WEBP 파일만 업로드할 수 있습니다.");
    return;
  }
  if (file.size > MAX_UPLOAD_SIZE) {
    alert("파일 용량은 10MB 이하만 업로드할 수 있습니다.");
    return;
  }

  isOcrProcessing.value = true;

  const formData = new FormData();
  formData.append("file", file);
  const targetReservationId = reservationId.value ?? editReservationId.value;
  if (!targetReservationId) {
    alert("예약 정보를 찾을 수 없습니다.");
    isOcrProcessing.value = false;
    return;
  }

  try {
    const response = await httpRequest.post("/api/ocr/receipt", formData, {
      params: { reservationId: targetReservationId },
      headers: { "Content-Type": "multipart/form-data" },
    });

    const data = response.data;

    // 2. 영수증 데이터 UI에 매핑
    receiptId.value = data.receiptId || null;
    receipt.value.date = formatOcrDate(data.date); // 날짜 포맷팅 함수 필요시 적용
    receipt.value.totalAmount = data.totalAmount ?? receipt.value.totalAmount;
    const mappedItems = (data.items || []).map((item) => ({
      name: item.name || "",
      quantity: Number(item.quantity) || 0,
      price: Number(item.price) || 0,
    }));
    receipt.value.items = mappedItems.map((item) => ({ ...item }));
    receiptDraftItems.value = mappedItems.map((item) => ({ ...item }));
    receipt.value.uploaded = true;

    closeReceiptModal();
    alert("영수증 인식이 완료되었습니다.");
  } catch (error) {
    console.error("OCR 실패:", error);
    alert("영수증 인식에 실패했습니다. 직접 입력해주세요.");
  } finally {
    isOcrProcessing.value = false;
  }
};

const recalculateReceiptTotal = (items) => {
  if (!items || items.length === 0) return;
  receipt.value.totalAmount = items.reduce(
    (sum, item) =>
      sum + (Number(item.price) || 0) * (Number(item.quantity) || 0),
    0
  );
};

const addReceiptItem = () => {
  receiptDraftItems.value.push({ name: "", quantity: 1, price: 0 });
};

const removeReceiptItem = (index) => {
  receiptDraftItems.value.splice(index, 1);
};

const confirmReceiptEdits = () => {
  const confirmed = receiptDraftItems.value.map((item) => ({
    name: item.name?.trim() || "",
    quantity: Number(item.quantity) || 0,
    price: Number(item.price) || 0,
  }));
  receipt.value.items = confirmed;
  recalculateReceiptTotal(confirmed);
};

const hasReceiptEdits = computed(() => {
  return (
    JSON.stringify(receiptDraftItems.value) !==
    JSON.stringify(receipt.value.items)
  );
});
// 날짜 형식 예쁘게 바꾸는 헬퍼 함수 (선택)
const formatOcrDate = (dateStr) => {
  if (!dateStr) return "";
  // '2023-07-11' -> '2023년 07월 11일'
  const date = new Date(dateStr);
  const days = ["일", "월", "화", "수", "목", "금", "토"];
  return `${date.getFullYear()}년 ${
    date.getMonth() + 1
  }월 ${date.getDate()}일 (${days[date.getDay()]})`;
};

// 마우스 드래그 스크롤 설정
const setupDragScroll = () => {
  nextTick(() => {
    const scrollContainers = document.querySelectorAll(".tag-category-scroll");

    scrollContainers.forEach((container) => {
      let isDown = false;
      let startX;
      let scrollLeft;

      container.addEventListener("mousedown", (e) => {
        isDown = true;
        container.classList.add("cursor-grabbing");
        startX = e.pageX - container.offsetLeft;
        scrollLeft = container.scrollLeft;
      });

      container.addEventListener("mouseleave", () => {
        isDown = false;
        container.classList.remove("cursor-grabbing");
      });

      container.addEventListener("mouseup", () => {
        isDown = false;
        container.classList.remove("cursor-grabbing");
      });

      container.addEventListener("mousemove", (e) => {
        if (!isDown) return;
        e.preventDefault();
        const x = e.pageX - container.offsetLeft;
        const walk = (x - startX) * 2;
        container.scrollLeft = scrollLeft - walk;
      });
    });
  });
};

onMounted(async () => {
  setupDragScroll();
  await loadTagMap();
  await loadExistingReview(); // 수정 모드일 때 기존 리뷰 데이터 로드
  loadReservationSummary();
});
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- 헤더 -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div
        class="max-w-[500px] mx-auto px-4 h-14 flex items-center justify-between"
      >
        <h1 class="font-semibold text-[#1e3a5f] text-base">
          {{ isEditMode ? "리뷰 수정" : "리뷰 작성" }}
        </h1>
        <button @click="isEditMode ? router.back() : goToMyReservations()">
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
              <div class="col-span-3 text-right">단가</div>
              <div class="col-span-1 text-right">삭제</div>
            </div>
            <div
              v-for="(item, index) in receiptDraftItems"
              :key="index"
              class="grid grid-cols-12 gap-2 text-xs text-[#495057] items-center"
            >
              <div class="col-span-6">
                <input
                  v-model="item.name"
                  type="text"
                  placeholder="메뉴명"
                  class="w-full px-2 py-1 border border-[#dee2e6] rounded-md text-xs focus:outline-none focus:border-purple-300"
                />
              </div>
              <div class="col-span-2 text-center">
                <input
                  v-model.number="item.quantity"
                  type="number"
                  min="0"
                  class="w-full px-2 py-1 border border-[#dee2e6] rounded-md text-xs text-center focus:outline-none focus:border-purple-300"
                />
              </div>
              <div class="col-span-3 text-right">
                <input
                  v-model.number="item.price"
                  type="number"
                  min="0"
                  class="w-full px-2 py-1 border border-[#dee2e6] rounded-md text-xs text-right focus:outline-none focus:border-purple-300"
                />
              </div>
              <div class="col-span-1 text-right">
                <button
                  @click="removeReceiptItem(index)"
                  class="text-xs text-[#dc3545] hover:underline"
                >
                  삭제
                </button>
              </div>
            </div>
          </div>

          <button
            @click="addReceiptItem"
            class="w-full mb-3 py-2 border border-dashed border-[#dee2e6] rounded-lg text-xs text-[#6c757d] hover:border-purple-300 hover:bg-purple-50 transition-colors"
          >
            메뉴 추가
          </button>

          <div class="flex items-center justify-between mb-3">
            <p class="text-xs text-[#6c757d]">
              수정한 내용을 확인 후 확정해주세요.
            </p>
            <button
              @click="confirmReceiptEdits"
              :disabled="!hasReceiptEdits"
              :class="[
                'px-3 py-1.5 rounded-lg text-xs font-medium transition-colors',
                hasReceiptEdits
                  ? 'bg-purple-600 text-white hover:bg-purple-700'
                  : 'bg-gray-200 text-gray-400 cursor-not-allowed',
              ]"
            >
              수정 확정
            </button>
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
            이 곳에 어울리는 키워드를 골라주세요. (1개 ~ 7개)
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

        <!-- 별점 선택 -->
        <Card class="mb-3 p-4 rounded-2xl border-[#e9ecef]">
          <h3 class="text-sm font-semibold text-[#1e3a5f] mb-2">
            별점을 선택해주세요
          </h3>
          <div class="flex items-center gap-1">
            <button
              v-for="value in [1, 2, 3, 4, 5]"
              :key="value"
              type="button"
              @click="setRating(value)"
              class="transition-transform hover:scale-105"
              :aria-label="`별점 ${value}점`"
            >
              <Star
                :class="[
                  'w-7 h-7',
                  value <= rating ? 'fill-orange-400 text-orange-400' : 'text-gray-300',
                ]"
              />
            </button>
            <span class="ml-2 text-sm text-[#495057]">{{ rating }}점</span>
          </div>
        </Card>

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
            :class="[
              'flex flex-col items-center justify-center w-full h-48 border-2 border-dashed rounded-xl cursor-pointer transition-colors',
              isOcrProcessing
                ? 'border-[#dee2e6] bg-gray-50 cursor-not-allowed'
                : 'border-[#dee2e6] hover:border-purple-300 hover:bg-purple-50',
            ]"
          >
            <Upload class="w-12 h-12 text-[#6c757d] mb-2" />
            <span class="text-sm text-[#495057]">
              {{ isOcrProcessing ? "OCR 처리 중..." : "클릭하여 파일 선택" }}
            </span>
            <span class="text-xs text-[#6c757d] mt-1"
              >JPG, PNG, WEBP (최대 10MB)</span
            >
            <input
              type="file"
              class="hidden"
              accept="image/jpeg,image/png,image/webp"
              @change="handleReceiptUpload"
              :disabled="isOcrProcessing"
            />
          </label>
        </div>

        <div class="flex gap-2">
          <button
            @click="closeReceiptModal"
            :disabled="isOcrProcessing"
            class="flex-1 py-3 border border-[#dee2e6] rounded-lg text-sm font-medium text-[#495057] hover:bg-gray-50"
          >
            취소
          </button>
          <button
            disabled
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
              >JPG, PNG, WEBP (최대 5장, 각 10MB)</span
            >
            <input
              type="file"
              class="hidden"
              accept="image/jpeg,image/png,image/webp"
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

    <!-- 리뷰 등록 완료 모달 -->
    <div
      v-if="isReviewCompleteModalOpen"
      class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
      @click="closeReviewCompleteModal"
    >
      <div class="bg-white rounded-2xl p-6 max-w-md w-full" @click.stop>
        <div class="text-center mb-6">
          <div
            class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4"
          >
            <svg
              class="w-8 h-8 text-green-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M5 13l4 4L19 7"
              ></path>
            </svg>
          </div>
          <h3 class="text-xl font-bold text-[#1e3a5f] mb-2">리뷰 등록 완료!</h3>
          <p class="text-sm text-[#6c757d]">
            소중한 리뷰 감사합니다.<br />
            다른 이용자들에게 큰 도움이 될 거예요.
          </p>
        </div>

        <div class="space-y-2">
          <button
            @click="goToMyReview"
            class="w-full py-3 bg-blue-600 text-white rounded-lg text-sm font-semibold hover:bg-blue-700 transition-colors"
          >
            내 리뷰 보러가기
          </button>
          <button
            @click="goToMyReservations"
            class="w-full py-3 bg-white border-2 border-[#dee2e6] text-[#495057] rounded-lg text-sm font-medium hover:bg-gray-50 transition-colors"
          >
            지난 예약 페이지로 가기
          </button>
          <button
            @click="goToHome"
            class="w-full py-3 bg-white border border-[#dee2e6] text-[#6c757d] rounded-lg text-sm font-medium hover:bg-gray-50 transition-colors"
          >
            홈으로 가기
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
          :disabled="rating < 1"
          :class="[
            'flex-1 h-12 rounded-lg font-medium transition-colors',
            rating >= 1
              ? 'bg-blue-600 text-white hover:bg-blue-700'
              : 'bg-gray-200 text-gray-400 cursor-not-allowed',
          ]"
        >
          {{ isEditMode ? "수정 완료" : "등록" }}
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
