<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";
import { ArrowLeft } from "lucide-vue-next";
import { useBookmarkShare } from "@/composables/useBookmarkShare";
import httpRequest from "@/router/httpRequest";
import { useAccountStore } from "@/stores/account";

// 분리한 자식 컴포넌트 임포트
import ReservationHistory from "@/components/ui/ReservationHistory.vue"; // 예정된 예약 목록
import UsageHistory from "@/components/ui/UsageHistory.vue"; // 지난 예약(이용완료/환불) 목록

const route = useRoute();
const { getMyBookmarks } = useBookmarkShare();
const accountStore = useAccountStore();

// 탭 상태 관리 ('upcoming' | 'past')
const activeTab = ref("upcoming");
const favorites = ref([]);
const upcomingReservations = ref([]);
const pastReservations = ref([]);

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

// URL 쿼리에 따라 초기 탭 설정 (예: ?tab=past)
onMounted(() => {
  if (route.query.tab === "past") {
    activeTab.value = "past";
  }
  loadFavorites();
  loadReservations("upcoming");
  loadReservations("past");
});

const loadFavorites = async () => {
  if (!memberId.value) return;
  try {
    const response = await getMyBookmarks(memberId.value);
    const data = Array.isArray(response.data) ? response.data : [];
    favorites.value = data.map((item) => item.restaurantId);
  } catch (error) {
    console.error("즐겨찾기 목록 조회 실패:", error);
    favorites.value = [];
  }
};

const statusMap = {
  TEMPORARY: "pending_payment",
  CONFIRMED: "confirmed",
  PREPAID_CONFIRMED: "confirmed",
  COMPLETED: "completed",
  REFUND_PENDING: "refund_pending",
  REFUNDED: "refunded",
  CANCELLED: "refunded",
  NOSHOW: "refunded",
  NO_SHOW: "refunded",
};

const formatDate = (value) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}.${month}.${day}`;
};

const formatTime = (value) => {
  if (!value) return "";
  return String(value).slice(0, 5);
};

const mapReservation = (item) => {
  const reservationStatus = statusMap[item.reservationStatus] || "confirmed";
  const fallbackVisitCount = reservationStatus === "completed" ? 1 : 0;

  return {
    id: item.id,
    confirmationNumber: item.confirmationNumber,
    restaurant: {
      id: item.restaurant?.id,
      name: item.restaurant?.name,
      address: item.restaurant?.address,
    },
    booking: {
      date: formatDate(item.booking?.date),
      time: formatTime(item.booking?.time),
      partySize: item.booking?.partySize,
    },
    visitCount: item.visitCount ?? fallbackVisitCount,
    daysSinceLastVisit: item.daysSinceLastVisit ?? null,
    payment: item.payment?.amount ? { amount: item.payment.amount } : null,
    reservationStatus,
    review: item.review
      ? {
          id: item.review.id,
          rating: item.review.rating,
          content: item.review.content || "",
          tags: Array.isArray(item.review.tags) ? item.review.tags : [],
          createdAt: formatDate(item.review.createdAt),
        }
      : null,
  };
};

const loadReservations = async (type) => {
  if (!memberId.value) return;
  try {
    const response = await httpRequest.get("/api/reservations/history", {
      userId: memberId.value,
      type,
    });
    const data = Array.isArray(response.data) ? response.data : [];
    const mapped = data.map(mapReservation);
    if (type === "past") {
      pastReservations.value = mapped;
    } else {
      upcomingReservations.value = mapped;
    }
  } catch (error) {
    console.error("예약 내역 조회 실패:", error);
    if (type === "past") {
      pastReservations.value = [];
    } else {
      upcomingReservations.value = [];
    }
  }
};

watch(
  () => memberId.value,
  (next) => {
    if (!next) return;
    loadFavorites();
    loadReservations("upcoming");
    loadReservations("past");
  }
);
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink to="/" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">예약 내역</h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-5">
      <div class="bg-white border-b border-[#e9ecef] sticky top-14 z-40">
        <div class="flex">
          <button
            @click="activeTab = 'upcoming'"
            :class="[
              'flex-1 py-3 text-sm font-medium transition-colors relative',
              activeTab === 'upcoming'
                ? 'text-[#1e3a5f] font-semibold'
                : 'text-[#6c757d]',
            ]"
          >
            예약 내역
            <div
              v-if="activeTab === 'upcoming'"
              class="absolute bottom-0 left-0 right-0 h-0.5 bg-[#1e3a5f]"
            ></div>
          </button>
          <button
            @click="activeTab = 'past'"
            :class="[
              'flex-1 py-3 text-sm font-medium transition-colors relative',
              activeTab === 'past'
                ? 'text-[#1e3a5f] font-semibold'
                : 'text-[#6c757d]',
            ]"
          >
            지난 예약
            <div
              v-if="activeTab === 'past'"
              class="absolute bottom-0 left-0 right-0 h-0.5 bg-[#1e3a5f]"
            ></div>
          </button>
        </div>
      </div>

      <div class="px-4 pt-5">
        <ReservationHistory
          v-show="activeTab === 'upcoming'"
          :reservations="upcomingReservations"
        />

        <UsageHistory
          v-show="activeTab === 'past'"
          :reservations="pastReservations"
          :user-id="memberId"
          :favorites="favorites"
        />
      </div>
    </main>
  </div>
</template>
