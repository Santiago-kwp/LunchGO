<script setup>
import { ref, computed } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { ArrowLeft, CalendarIcon, Users } from 'lucide-vue-next';
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';

const route = useRoute();
const bookingType = route.query.type || 'reservation'; // 'reservation' or 'preorder'
const isPreorder = computed(() => bookingType === 'preorder');
const restaurantId = route.params.id; // Access the dynamic ID

const selectedDateIndex = ref(null); // Changed from selectedDate to selectedDateIndex to match React's use of index
const selectedTime = ref(null);
const partySize = ref(4);
const requestNote = ref('');

// Generate dates for the next 30 days
const dates = computed(() => {
  return Array.from({ length: 30 }, (_, i) => {
    const date = new Date();
    date.setDate(date.getDate() + i);
    return {
      day: date.getDate(),
      month: date.getMonth() + 1,
      weekday: ['일', '월', '화', '수', '목', '금', '토'][date.getDay()],
      isToday: i === 0,
    };
  });
});

const timeSlots = ref(['11:00', '12:00', '13:00', '14:00']);

const canProceed = computed(() => selectedDateIndex.value !== null && selectedTime.value !== null);

//예약금 * 인원수 계산식 + 선주문/선결제 플로우 포함
const nextPage = computed(() => {
  if (isPreorder.value) {
  return {
    path: `/restaurant/${restaurantId}/menu`,
    query: {
      type: 'preorder',
      partySize: partySize.value,
      requestNote: requestNote.value,
      dateIndex: selectedDateIndex.value,
      time: selectedTime.value,
    },
  };
}

  // 예약금 결제 페이지로 partySize 전달
  return {
    path: `/restaurant/${restaurantId}/payment`,
    query: {
      type: 'deposit',
      partySize: partySize.value,
      // 원하면 요청사항도 같이 넘김 (백엔드 붙기 전 임시로라도 UI 반영 가능)
      requestNote: requestNote.value,
      // 날짜/시간도 넘기고 싶으면 같이:
      // dateIndex: selectedDateIndex.value,
      // time: selectedTime.value,
    },
  };
});

const selectDate = (idx) => {
  selectedDateIndex.value = idx;
};
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink :to="`/restaurant/${restaurantId}`" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">
          {{ isPreorder ? '선주문/선결제' : '예약 정보 입력' }}
        </h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-24">
      <!-- Date Selection -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
        <div class="flex items-center gap-2 mb-4">
          <CalendarIcon class="w-5 h-5 text-[#ff6b4a]" />
          <h2 class="text-base font-semibold text-[#1e3a5f]">날짜 선택</h2>
        </div>

        <div class="overflow-x-auto -mx-4 px-4">
          <div class="flex gap-2 pb-2">
            <button
              v-for="(date, idx) in dates"
              :key="idx"
              @click="selectDate(idx)"
              :class="`flex-shrink-0 w-16 h-20 rounded-lg border-2 flex flex-col items-center justify-center gap-1 transition-all ${
                selectedDateIndex === idx
                  ? 'border-[#ff6b4a] bg-[#fff5f3]'
                  : 'border-[#dee2e6] bg-white hover:border-[#ffc4b8]'
              }`"
            >
              <span :class="`text-xs ${selectedDateIndex === idx ? 'text-[#ff6b4a] font-semibold' : 'text-[#6c757d]'}`">
                {{ date.weekday }}
              </span>
              <span :class="`text-lg font-semibold ${selectedDateIndex === idx ? 'text-[#ff6b4a]' : 'text-[#1e3a5f]'}`">
                {{ date.day }}
              </span>
              <span :class="`text-xs ${selectedDateIndex === idx ? 'text-[#ff6b4a]' : 'text-[#6c757d]'}`">
                {{ date.month }}월
              </span>
            </button>
          </div>
        </div>
      </div>

      <!-- Time Selection -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">시간 선택</h2>

        <div class="grid grid-cols-4 gap-2">
          <button
            v-for="time in timeSlots"
            :key="time"
            @click="selectedTime = time"
            :class="`h-11 rounded-lg border-2 text-sm font-medium transition-all ${
              selectedTime === time
                ? 'border-[#ff6b4a] bg-[#fff5f3] text-[#ff6b4a]'
                : 'border-[#dee2e6] bg-white text-[#495057] hover:border-[#ffc4b8]'
            }`"
          >
            {{ time }}
          </button>
        </div>
      </div>

      <!-- Party Size Selection -->
      <div class="bg-white px-4 py-5">
        <div class="flex items-center gap-2 mb-4">
          <Users class="w-5 h-5 text-[#ff6b4a]" />
          <h2 class="text-base font-semibold text-[#1e3a5f]">인원 선택</h2>
        </div>

        <div class="flex items-center justify-between max-w-xs mx-auto">
          <button
            @click="partySize = Math.max(4, partySize - 1)"
            :disabled="partySize <= 4"
            class="w-12 h-12 rounded-full border-2 border-[#dee2e6] bg-white text-[#1e3a5f] font-bold text-xl disabled:opacity-30 disabled:cursor-not-allowed hover:border-[#ff6b4a] hover:text-[#ff6b4a] transition-colors"
          >
            -
          </button>

          <div class="text-center">
            <p class="text-3xl font-bold text-[#1e3a5f]">{{ partySize }}</p>
            <p class="text-sm text-[#6c757d] mt-1">명</p>
          </div>

          <button
            @click="partySize = Math.min(12, partySize + 1)"
            :disabled="partySize >= 12"
            class="w-12 h-12 rounded-full border-2 border-[#dee2e6] bg-white text-[#1e3a5f] font-bold text-xl disabled:opacity-30 disabled:cursor-not-allowed hover:border-[#ff6b4a] hover:text-[#ff6b4a] transition-colors"
          >
            +
          </button>
        </div>

        <p class="text-xs text-[#6c757d] text-center mt-4 leading-relaxed">
          최소 4인 ~ 최대 12인까지 예약 가능합니다
        </p>
      </div>

      <!-- Request Note -->
      <div v-if="canProceed" class="bg-white px-4 py-5 border-t border-[#e9ecef]">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-3">요청사항</h2>
        <textarea
          v-model="requestNote"
          rows="4"
          placeholder="예: 유아용 의자 부탁드려요 등"
          class="w-full rounded-xl border-2 border-[#dee2e6] px-3 py-3 text-sm text-[#1e3a5f] placeholder:text-[#adb5bd] focus:outline-none focus:border-[#ff6b4a]"
        />
        <p class="mt-2 text-xs text-[#6c757d] leading-relaxed">
        요청사항은 매장의 참고 용이며, 예약내역 변경 사항은 취소 후 재예약 혹은 매장으로 직접 문의 하시길 바랍니다.
        </p>
      </div>

      <!-- Booking Summary -->
      <div v-if="canProceed" class="mx-4 mt-4">
        <Card class="p-4 border-[#e9ecef] rounded-xl bg-white shadow-card">
          <h3 class="font-semibold text-[#1e3a5f] mb-3">{{ isPreorder ? '선주문 정보' : '예약 정보' }}</h3>
          <div class="space-y-2 text-sm">
            <div class="flex justify-between">
              <span class="text-[#6c757d]">식당명</span>
              <span class="text-[#1e3a5f] font-medium">식당명</span>
            </div>
            <div class="flex justify-between">
            <span class="text-[#6c757d]">요청사항</span>
            <span class="text-[#1e3a5f] font-medium">{{ requestNote?.trim() || '-' }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-[#6c757d]">예약 날짜</span>
              <span class="text-[#1e3a5f] font-medium">
                {{ dates[selectedDateIndex]?.month }}월 {{ dates[selectedDateIndex]?.day }}일 ({{
                  dates[selectedDateIndex]?.weekday
                }})
              </span>
            </div>
            <div class="flex justify-between">
              <span class="text-[#6c757d]">예약 시간</span>
              <span class="text-[#1e3a5f] font-medium">{{ selectedTime }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-[#6c757d]">인원</span>
              <span class="text-[#1e3a5f] font-medium">{{ partySize }}명</span>
            </div>
          </div>
        </Card>
      </div>
    </main>

    <!-- Fixed Bottom Button -->
    <div class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg">
      <div class="max-w-[500px] mx-auto px-4 py-3">
        <RouterLink :to="canProceed ? nextPage : '#'">
          <Button
            :disabled="!canProceed"
            class="w-full h-12 gradient-primary text-white font-semibold text-base rounded-xl shadow-button-hover hover:shadow-button-pressed disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ isPreorder ? '메뉴 선택하기' : '예약금 결제하기' }}
          </Button>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
