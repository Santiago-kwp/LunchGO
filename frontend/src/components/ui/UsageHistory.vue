<script setup>
import { ref } from "vue";
import { RouterLink, useRouter } from "vue-router";
import {
  Calendar,
  Users,
  MapPin,
  Star,
  MoreVertical,
  Edit,
  Trash2,
} from "lucide-vue-next";
import httpRequest from "@/router/httpRequest";
import Button from "@/components/ui/Button.vue";
import Card from "@/components/ui/Card.vue";
import FavoriteHeart from "@/components/ui/FavoriteHeart.vue";

const router = useRouter();

// Props 정의
const props = defineProps({
  reservations: {
    type: Array,
    required: true,
    default: () => [],
  },
  favorites: {
    type: Array,
    default: () => [],
  },
  userId: {
    type: Number,
    default: null,
  },
});

// 리뷰 메뉴 드롭다운 상태 (로컬 상태로 이동)
const activeReviewMenu = ref(null);

const isFavorite = (restaurantId) =>
  props.favorites?.includes(restaurantId);

// 리뷰 메뉴 토글
const toggleReviewMenu = (reservationId) => {
  activeReviewMenu.value =
    activeReviewMenu.value === reservationId ? null : reservationId;
};

// 지난 예약 상태 정보
const getStatusInfo = (reservationStatus) => {
  const statusMap = {
    completed: {
      text: "이용완료",
      bgColor: "bg-emerald-50",
      textColor: "text-emerald-600",
      borderColor: "border-emerald-200",
    },
    refund_pending: {
      text: "환불대기",
      bgColor: "bg-amber-50",
      textColor: "text-amber-600",
      borderColor: "border-amber-200",
    },
    refunded: {
      text: "환불완료",
      bgColor: "bg-gray-50",
      textColor: "text-gray-600",
      borderColor: "border-gray-200",
    },
    cancelled: {
      text: "취소",
      bgColor: "bg-gray-50",
      textColor: "text-gray-600",
      borderColor: "border-gray-200",
    },
    restaurant_cancelled: {
      text: "식당 취소",
      bgColor: "bg-gray-50",
      textColor: "text-gray-600",
      borderColor: "border-gray-200",
    },
  };
  return statusMap[reservationStatus] || statusMap.completed;
};

// 방문 정보 텍스트
const getVisitInfoText = (visitCount, daysSinceLastVisit) => {
  if (visitCount === 1) {
    return "1번째 방문";
  } else if (daysSinceLastVisit) {
    return `${visitCount}번째, ${daysSinceLastVisit}일만의 방문`;
  } else {
    return `${visitCount}번째 방문`;
  }
};

// 리뷰 텍스트 축약
const truncateReviewText = (text, maxLength = 50) => {
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + "...";
};

// 리뷰 수정 핸들러
const handleEditReview = (reservation) => {
  activeReviewMenu.value = null;
  router.push(
    `/restaurant/${reservation.restaurant.id}/reviews/${reservation.review.id}/edit`
  );
};

// 리뷰 삭제 핸들러
const handleDeleteReview = (reservation) => {
  if (!confirm("리뷰를 삭제하시겠습니까?")) return;
  httpRequest
    .delete(
      `/api/restaurants/${reservation.restaurant.id}/reviews/${reservation.review.id}`
    )
    .then(() => {
      reservation.review = null;
    })
    .catch((error) => {
      console.error("리뷰 삭제 실패:", error);
      alert("리뷰 삭제에 실패했습니다.");
    });
  activeReviewMenu.value = null;
};
</script>

<template>
  <div class="space-y-3">
    <div v-if="props.reservations.length === 0" class="text-center py-12">
      <p class="text-[#6c757d] text-sm">지난 예약 내역이 없습니다.</p>
    </div>

    <Card
      v-else
      v-for="reservation in props.reservations"
      :key="reservation.reservationId || reservation.id"
      class="overflow-hidden border-[#e9ecef] rounded-2xl bg-white shadow-sm"
    >
      <div class="p-4">
        <!-- 상단: 식당 이름, 즐겨찾기, 상태 배지 -->
        <div class="flex items-start justify-between mb-3">
          <div class="flex items-center gap-2">
            <h3 class="font-semibold text-[#1e3a5f] text-base">
              {{ reservation.restaurant.name }}
            </h3>
            <FavoriteHeart
              :restaurant-id="reservation.restaurant.id"
              :user-id="userId"
              :initial-favorite="isFavorite(reservation.restaurant.id)"
            />
          </div>
          <span
            :class="[
              'px-2.5 py-1 rounded-full border text-xs font-medium whitespace-nowrap',
              getStatusInfo(reservation.reservationStatus).bgColor,
              getStatusInfo(reservation.reservationStatus).textColor,
              getStatusInfo(reservation.reservationStatus).borderColor,
            ]"
          >
            {{ getStatusInfo(reservation.reservationStatus).text }}
          </span>
        </div>

        <!-- 예약 상세 정보 -->
        <div class="space-y-2 mb-3">
          <div class="flex items-start gap-2 text-sm">
            <MapPin class="w-4 h-4 text-[#6c757d] mt-0.5 flex-shrink-0" />
            <span class="text-[#495057] leading-relaxed">{{
              reservation.restaurant.address
            }}</span>
          </div>
          <div class="flex items-center justify-between text-sm">
            <div class="flex items-center gap-2">
              <Calendar class="w-4 h-4 text-[#6c757d]" />
              <span class="text-[#495057]">{{ reservation.booking.date }}</span>
            </div>
            <span class="text-[#495057] text-xs">
              {{
                getVisitInfoText(
                  reservation.visitCount,
                  reservation.daysSinceLastVisit
                )
              }}
            </span>
          </div>
          <div class="flex items-center justify-between text-sm">
            <div
              v-if="reservation.booking.partySize"
              class="flex items-center gap-2"
            >
              <Users class="w-4 h-4 text-[#6c757d]" />
              <span class="text-[#495057]"
                >{{ reservation.booking.partySize }}명</span
              >
            </div>
            <div v-if="reservation.payment" class="text-[#495057]">
              {{ reservation.payment.amount.toLocaleString() }} 원
            </div>
          </div>
        </div>

        <div
          v-if="
            reservation.cancelledReason &&
            (reservation.reservationStatus === 'cancelled' ||
              reservation.reservationStatus === 'restaurant_cancelled')
          "
          class="text-sm text-[#6c757d] bg-[#f8f9fa] border border-[#e9ecef] rounded-lg px-3 py-2 mb-3"
        >
          취소 사유: {{ reservation.cancelledReason }}
        </div>

        <!-- 다시 예약 / 예약 내역 버튼 -->
        <div class="flex gap-2">
          <RouterLink
            :to="`/restaurant/${reservation.restaurant.id}`"
            class="flex-1"
          >
            <Button
              variant="outline"
              class="w-full h-10 border-blue-200 text-blue-600 bg-white hover:bg-blue-50 rounded-lg text-sm font-medium"
            >
              다시 예약
            </Button>
          </RouterLink>
          <RouterLink
            :to="{
              path: `/restaurant/${reservation.restaurant.id}/confirmation`,
              query: { reservationId: reservation.id },
            }"
            class="flex-1"
          >
            <Button
              variant="outline"
              class="w-full h-10 border-gray-300 text-gray-700 bg-white hover:bg-gray-50 rounded-lg text-sm font-medium"
            >
              예약 내역
            </Button>
          </RouterLink>
        </div>

        <!-- 리뷰 영역: 이용 완료(completed) 상태일 때만 노출 -->
        <div class="mt-2" v-if="reservation.reservationStatus === 'completed'">
          <!-- 리뷰가 없을 때: 리뷰 쓰기 버튼 -->
          <RouterLink
            v-if="!reservation.review"
            :to="{
              path: `/restaurant/${reservation.restaurant.id}/reviews/write`,
              query: { reservationId: reservation.id },
            }"
            class="block"
          >
            <Button
              variant="outline"
              class="w-full h-10 border-orange-200 text-orange-600 bg-white hover:bg-orange-50 rounded-lg text-sm font-medium"
            >
              리뷰 쓰기
            </Button>
          </RouterLink>

          <!-- 리뷰가 있을 때: 리뷰 미리보기 및 수정/삭제 -->
          <div v-else class="relative">
            <RouterLink
              :to="`/mypage/reviews?highlight=${reservation.review.id}`"
              class="block p-3 bg-gradient-to-r from-orange-50 to-pink-50 border border-orange-200 rounded-lg hover:shadow-md transition-all"
            >
              <div class="flex items-center gap-1 mb-2">
                <Star
                  v-for="(_, i) in Array.from({
                    length: reservation.review.rating,
                  })"
                  :key="i"
                  class="w-4 h-4 fill-orange-400 text-orange-400"
                />
                <span class="text-xs text-gray-500 ml-1">
                  {{ reservation.review.createdAt }}
                </span>
              </div>

              <p class="text-sm text-gray-700 mb-2 line-clamp-2">
                {{ truncateReviewText(reservation.review.content) }}
              </p>

              <div class="flex flex-wrap gap-1">
                <span
                  v-for="(tag, idx) in reservation.review.tags.slice(0, 2)"
                  :key="idx"
                  class="inline-flex items-center px-2 py-0.5 text-xs rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ff8e72] text-white font-semibold shadow-sm"
                >
                  {{ tag }}
                </span>
                <span
                  v-if="reservation.review.tags.length > 2"
                  class="inline-block px-2 py-0.5 text-xs rounded-full bg-gray-200 text-gray-600"
                >
                  +{{ reservation.review.tags.length - 2 }}
                </span>
              </div>
            </RouterLink>

            <div class="absolute top-2 right-2">
              <button
                @click.prevent="toggleReviewMenu(reservation.id)"
                class="w-8 h-8 flex items-center justify-center rounded-full bg-white/80 hover:bg-white shadow-sm transition-colors"
              >
                <MoreVertical class="w-4 h-4 text-gray-600" />
              </button>

              <div
                v-if="activeReviewMenu === reservation.id"
                class="absolute top-10 right-0 bg-white border border-gray-200 rounded-lg shadow-lg py-1 min-w-[120px] z-10"
              >
                <button
                  @click="handleEditReview(reservation)"
                  class="w-full flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                >
                  <Edit class="w-4 h-4" />
                  수정
                </button>
                <button
                  @click="handleDeleteReview(reservation)"
                  class="w-full flex items-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
                >
                  <Trash2 class="w-4 h-4" />
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Card>
  </div>
</template>
