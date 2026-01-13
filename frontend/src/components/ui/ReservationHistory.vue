<script setup lang="ts">
import { RouterLink, useRouter } from 'vue-router';
import { Calendar, Clock, Users, MapPin, CheckCircle2 } from 'lucide-vue-next';
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';

// 부모로부터 예약 데이터 받기
const props = defineProps<{ reservations: any[] }>();

// 상태에 따른 뱃지 스타일 (컴포넌트 내부 로직)
const getStatusInfo = (reservationStatus) => {
  const statusMap = {
    pending_payment: {
      text: '결제대기',
      bgColor: 'bg-rose-50',
      textColor: 'text-rose-600',
      borderColor: 'border-rose-200',
      icon: Clock,
    },
    confirmed: {
      text: '예약확정',
      bgColor: 'bg-blue-50',
      textColor: 'text-blue-600',
      borderColor: 'border-blue-200',
      icon: CheckCircle2,
    },
    cancelled: {
      text: '취소',
      bgColor: 'bg-gray-50',
      textColor: 'text-gray-600',
      borderColor: 'border-gray-200',
    },
    restaurant_cancelled: {
      text: '식당 취소',
      bgColor: 'bg-gray-50',
      textColor: 'text-gray-600',
      borderColor: 'border-gray-200',
    },
  };
  return statusMap[reservationStatus] || statusMap.confirmed;
};

const router = useRouter();

const getReservationId = (reservation) =>
  reservation?.id ?? reservation?.reservationId ?? null;

const goCancel = (id) => {
  router.push({ name: "reservation-cancel", params: { id: String(id) } });
};

const shortConfirmationNumber = (value: unknown) => {
  const raw = String(value ?? '').trim();
  if (!raw) return '';
  const last = raw.split('-').pop() || raw;
  return String(last).slice(-4);
};
</script>

<template>
  <div class="space-y-3">
    <div v-if="props.reservations.length === 0" class="text-center py-12">
      <p class="text-[#6c757d] text-sm">예정된 예약이 없습니다.</p>
    </div>

    <Card
      v-else
      v-for="reservation in props.reservations"
      :key="reservation.reservationId || reservation.id"
      class="overflow-hidden border-[#e9ecef] rounded-xl bg-white shadow-sm"
    >
      <div class="p-4">
        <div class="flex items-start justify-between mb-3">
          <div>
            <h3 class="font-semibold text-[#1e3a5f] text-base mb-1">
              {{ reservation.restaurant.name }}
            </h3>
            <p class="text-xs text-[#6c757d]">
              예약번호: {{ shortConfirmationNumber(reservation.confirmationNumber) }}
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
            <component
              v-if="getStatusInfo(reservation.reservationStatus).icon"
              :is="getStatusInfo(reservation.reservationStatus).icon"
              class="w-3.5 h-3.5 inline-block mr-1 -mt-0.5"
            />
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
            :to="{
              path: `/restaurant/${reservation.restaurant.id}/confirmation`,
              query: { reservationId: getReservationId(reservation) },
            }"
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
