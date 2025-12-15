<script setup>
import { ref, computed } from 'vue';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';

const selectedDate = ref(new Date(2024, 9, 24)); // October 24, 2024
const currentMonth = ref(new Date(2024, 9, 1));

const reservations = ref([
  {
    id: 1,
    name: '홍길동',
    phone: '010-1234-5678',
    datetime: '2025-12-25 12:00',
    guests: 4,
    amount: 111000,
    status: '확정',
  },
  {
    id: 2,
    name: '김영희',
    phone: '010-2345-6789',
    datetime: '2025-12-25 13:00',
    guests: 6,
    amount: 150000,
    status: '대기',
  },
  {
    id: 3,
    name: '이철수',
    phone: '010-3456-7890',
    datetime: '2025-12-25 11:00',
    guests: 8,
    amount: 200000,
    status: '확정',
  },
  {
    id: 4,
    name: '박민수',
    phone: '010-4567-8901',
    datetime: '2025-12-25 14:00',
    guests: 5,
    amount: 125000,
    status: '취소',
  },
]);

const stats = ref({
  total: 7,
  confirmed: 4,
  waiting: 2,
  cancelled: 1,
});

// Calendar helper functions
const getDaysInMonth = (date) => {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
};

const getFirstDayOfMonth = (date) => {
  // getDay() returns 0 for Sunday, 1 for Monday, etc.
  // We want Monday to be the first day, so adjust if Sunday is first.
  const day = new Date(date.getFullYear(), date.getMonth(), 1).getDay();
  return day === 0 ? 6 : day - 1; // 0 (Sun) -> 6, 1 (Mon) -> 0, etc.
};

const generateCalendarDays = computed(() => {
  const daysInMonth = getDaysInMonth(currentMonth.value);
  const firstDay = getFirstDayOfMonth(currentMonth.value);
  const days = [];

  // Previous month days (placeholders)
  for (let i = 0; i < firstDay; i++) {
    days.push(null);
  }

  // Current month days
  for (let i = 1; i <= daysInMonth; i++) {
    days.push(i);
  }

  return days;
});

const isSelectedDate = (day) => {
  if (!day) return false;
  return (
    day === selectedDate.value.getDate() &&
    currentMonth.value.getMonth() === selectedDate.value.getMonth() &&
    currentMonth.value.getFullYear() === selectedDate.value.getFullYear()
  );
};

const previousMonth = () => {
  currentMonth.value = new Date(
    currentMonth.value.getFullYear(),
    currentMonth.value.getMonth() - 1,
    1
  );
};

const nextMonth = () => {
  currentMonth.value = new Date(
    currentMonth.value.getFullYear(),
    currentMonth.value.getMonth() + 1,
    1
  );
};

const formattedMonth = computed(() => {
  return currentMonth.value.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
  });
});

const selectDay = (day) => {
  if (day) {
    selectedDate.value = new Date(
      currentMonth.value.getFullYear(),
      currentMonth.value.getMonth(),
      day
    );
  }
};
</script>

<template>
  <div class="flex h-screen bg-gray-50">
    <BusinessSidebar activeMenu="reservations" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col">
      <BusinessHeader />

      <!-- Content -->
      <main class="flex-1 overflow-auto p-8">
        <div class="max-w-7xl mx-auto">
          <h2 class="text-3xl font-bold text-[#1E3A5F] mb-8">전체 예약 관리</h2>

          <!-- Calendar -->
          <div
            class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-8"
          >
            <div class="flex items-center justify-between mb-4">
              <button
                @click="previousMonth"
                class="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg"
              >
                &lt;
              </button>
              <h3 class="text-lg font-semibold text-gray-900">
                {{ formattedMonth }}
              </h3>
              <button
                @click="nextMonth"
                class="px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-lg"
              >
                &gt;
              </button>
            </div>

            <div class="grid grid-cols-7 gap-2">
              <div
                v-for="dayName in ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su']"
                :key="dayName"
                class="text-center text-sm font-medium text-gray-500 py-2"
              >
                {{ dayName }}
              </div>
              <button
                v-for="(day, index) in generateCalendarDays"
                :key="index"
                @click="selectDay(day)"
                :disabled="!day"
                :class="[
                  'aspect-square flex items-center justify-center rounded-lg text-sm',
                  { invisible: !day },
                  {
                    'bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] text-white font-semibold':
                      isSelectedDate(day),
                  },
                  { 'hover:bg-gray-100 text-gray-700': !isSelectedDate(day) },
                ]"
              >
                {{ day }}
              </button>
            </div>
          </div>

          <!-- Stats Cards -->
          <div class="grid grid-cols-4 gap-6 mb-8">
            <div
              class="bg-white rounded-xl shadow-sm border border-gray-200 p-6"
            >
              <h3 class="text-lg font-semibold text-gray-600 mb-2">
                전체 예약
              </h3>
              <p class="text-4xl font-bold text-[#1E3A5F]">
                {{ stats.total }}건
              </p>
            </div>
            <div
              class="bg-white rounded-xl shadow-sm border border-gray-200 p-6"
            >
              <h3 class="text-lg font-semibold text-gray-600 mb-2">확정</h3>
              <p class="text-4xl font-bold text-[#1E3A5F]">
                {{ stats.confirmed }}건
              </p>
            </div>
            <div
              class="bg-white rounded-xl shadow-sm border border-gray-200 p-6"
            >
              <h3 class="text-lg font-semibold text-gray-600 mb-2">대기</h3>
              <p class="text-4xl font-bold text-[#1E3A5F]">
                {{ stats.waiting }}건
              </p>
            </div>
            <div
              class="bg-white rounded-xl shadow-sm border border-gray-200 p-6"
            >
              <h3 class="text-lg font-semibold text-gray-600 mb-2">취소</h3>
              <p class="text-4xl font-bold text-[#1E3A5F]">
                {{ stats.cancelled }}건
              </p>
            </div>
          </div>

          <!-- Reservations Table -->
          <div
            class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden"
          >
            <table class="w-full">
              <thead class="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th
                    class="px-6 py-4 text-left text-sm font-semibold text-gray-700"
                  >
                    예약자
                  </th>
                  <th
                    class="px-6 py-4 text-left text-sm font-semibold text-gray-700"
                  >
                    연락처
                  </th>
                  <th
                    class="px-6 py-4 text-left text-sm font-semibold text-gray-700"
                  >
                    예약시간
                  </th>
                  <th
                    class="px-6 py-4 text-left text-sm font-semibold text-gray-700"
                  >
                    인원
                  </th>
                  <th
                    class="px-6 py-4 text-left text-sm font-semibold text-gray-700"
                  >
                    결제금액
                  </th>
                  <th
                    class="px-6 py-4 text-left text-sm font-semibold text-gray-700"
                  >
                    상태
                  </th>
                  <th
                    class="px-6 py-4 text-left text-sm font-semibold text-gray-700"
                  >
                    작업
                  </th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200">
                <tr
                  v-for="reservation in reservations"
                  :key="reservation.id"
                  class="hover:bg-gray-50"
                >
                  <td class="px-6 py-4 text-sm text-gray-900">
                    {{ reservation.name }}
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-900">
                    {{ reservation.phone }}
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-900">
                    {{ reservation.datetime }}
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-900">
                    {{ reservation.guests }}명
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-900">
                    {{ reservation.amount.toLocaleString() }}원
                  </td>
                  <td class="px-6 py-4">
                    <span
                      :class="`inline-flex px-3 py-1 rounded-full text-xs font-medium ${
                        reservation.status === '확정'
                          ? 'bg-green-100 text-green-800'
                          : reservation.status === '대기'
                          ? 'bg-yellow-100 text-yellow-800'
                          : 'bg-red-100 text-red-800'
                      }`"
                    >
                      {{ reservation.status }}
                    </span>
                  </td>
                  <td class="px-6 py-4">
                    <div class="flex items-center gap-2">
                      <button
                        class="px-3 py-1 text-xs font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg"
                      >
                        상세보기
                      </button>
                      <template v-if="reservation.status === '대기'">
                        <button
                          class="px-3 py-1 text-xs font-medium text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] hover:opacity-90 rounded-lg"
                        >
                          확정
                        </button>
                        <button
                          class="px-3 py-1 text-xs font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg"
                        >
                          취소
                        </button>
                      </template>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
