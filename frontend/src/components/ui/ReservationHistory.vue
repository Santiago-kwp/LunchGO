<script setup>
import { RouterLink, useRouter } from 'vue-router';
import { Calendar, Clock, Users, MapPin } from 'lucide-vue-next';
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';

// 부모로부터 예약 데이터 받기
defineProps({
  reservations: {
    type: Array,
    required: true,
    default: () => [],
  },
});

// 상태에 따른 뱃지 스타일 (컴포넌트 내부 로직)
const getStatusInfo = (reservationStatus) => {
  const statusMap = {
    pending_payment: {
      text: '결제대기',
      bgColor: 'bg-rose-50',
      textColor: 'text-rose-600',
      borderColor: 'border-rose-200',
    },
    confirmed: {
      text: '예약확정',
      bgColor: 'bg-blue-50',
      textColor: 'text-blue-600',
      borderColor: 'border-blue-200',
    },
  };
  return statusMap[reservationStatus] || statusMap.confirmed;
};

const router = useRouter();

const goCancel = (id) => {
  router.push({ name: "reservation-cancel", params: { id: String(id) } });
};
</script>

<template>
  <div class="space-y-3">
    <div v-if="reservations.length === 0" class="text-center py-12">
      <p class="text-[#6c757d] text-sm">예정된 예약이 없습니다.</p>
    </div>

    <Card
      v-else
      v-for="reservation in reservations"
      :key="reservation.id"
      class="overflow-hidden border-[#e9ecef] rounded-xl bg-white shadow-sm"
    >
      <div class="p-4">
        <div class="flex items-start justify-between mb-3">
          <div>
            <h3 class="font-semibold text-[#1e3a5f] text-base mb-1">
              {{ reservation.restaurant.name }}
            </h3>
            <p class="text-xs text-[#6c757d]">
              예약번호: {{ reservation.confirmationNumber }}
            </p>
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

        <div class="space-y-2 mb-4">
          <div class="flex items-center gap-2 text-sm">
            <Calendar class="w-4 h-4 text-[#6c757d]" />
            <span class="text-[#495057]">{{ reservation.booking.date }}</span>
          </div>
          <div class="flex items-center gap-2 text-sm">
            <Clock class="w-4 h-4 text-[#6c757d]" />
            <span class="text-[#495057]">{{ reservation.booking.time }}</span>
          </div>
          <div class="flex items-center gap-2 text-sm">
            <Users class="w-4 h-4 text-[#6c757d]" />
            <span class="text-[#495057]"
              >{{ reservation.booking.partySize }}명</span
            >
          </div>
          <div class="flex items-start gap-2 text-sm">
            <MapPin class="w-4 h-4 text-[#6c757d] mt-0.5 flex-shrink-0" />
            <span class="text-[#495057] leading-relaxed">{{
              reservation.restaurant.address
            }}</span>
          </div>
        </div>

        <div class="flex gap-2">
          <RouterLink
            :to="`/restaurant/${reservation.restaurant.id}/confirmation`"
            class="flex-1"
          >
            <Button
              variant="outline"
              class="w-full h-10 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg text-sm font-medium"
            >
              예약 상세
            </Button>
          </RouterLink>
          <Button
            variant="outline"
            class="flex-1 h-10 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg text-sm font-medium"
            @click="goCancel(reservation.id)"
          >
            예약 취소
          </Button>
        </div>
      </div>
    </Card>
  </div>
</template>
