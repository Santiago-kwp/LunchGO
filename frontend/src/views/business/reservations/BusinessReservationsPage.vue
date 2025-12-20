<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { Filter, ChevronLeft, ChevronRight } from 'lucide-vue-next';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import StaffSideBar from '@/components/ui/StaffSideBar.vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 권한 확인 로직 (실제 앱에서는 Pinia Store나 localStorage에서 가져옵니다)
// 예: const authStore = useAuthStore(); const userRole = computed(() => authStore.userRole);
const userRole = ref('owner'); // 현재는 직접 입력으로 바꿔야함(owner/staff)


const goDetail = (id) => {
  router.push({ name: 'reservation-detail', params: { id: String(id) } });
};

const selectedDate = ref(new Date(2024, 9, 24));
const currentMonth = ref(new Date(2024, 9, 1));

// 선택 날짜를 "YYYY-MM-DD"로 관리 백엔드 연동 시 수정
const selectedDateStr = computed(() => {
  const d = selectedDate.value;
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
});

// 예약 datetime
const getReservationDateStr = (datetime) => {
  return String(datetime).split(' ')[0];
};

// 날짜 필터 on/off 
const onlySelectedDate = ref(true);

// 1차: 날짜로 걸러진 리스트
const dateFilteredReservations = computed(() => {
  if (!onlySelectedDate.value) return reservations.value;
  return reservations.value.filter(
    (r) => getReservationDateStr(r.datetime) === selectedDateStr.value
  );
});

const reservations = ref([
  { id: 1, name: '홍길동', phone: '010-1234-5678', datetime: '2025-12-25 12:00', guests: 4, amount: 111000, status: '확정' },
  { id: 2, name: '김영희', phone: '010-2345-6789', datetime: '2025-12-25 13:00', guests: 6, amount: 150000, status: '대기' },
  { id: 3, name: '이철수', phone: '010-3456-7890', datetime: '2025-12-25 11:00', guests: 8, amount: 200000, status: '확정' },
  { id: 4, name: '박민수', phone: '010-4567-8901', datetime: '2025-12-25 14:00', guests: 5, amount: 125000, status: '취소' },
  // { id: 5, ... status: '환불' } 이런 데이터가 들어와도 필터/뱃지 동작함
]);

reservations.value.push({
  id: 99,
  name: '테스트',
  phone: '010-0000-0000',
  datetime: `${selectedDateStr.value} 12:00`,
  guests: 2,
  amount: 50000,
  status: '확정',
});

for (let i = 0; i < 25; i++) {
  reservations.value.push({
    id: 200 + i,
    name: `테스트${i + 1}`,
    phone: '010-0000-0000',
    datetime: `${selectedDateStr.value} ${String(10 + (i % 10)).padStart(2, '0')}:00`,
    guests: 2 + (i % 4),
    amount: 50000 + i * 1000,
    status: i % 3 === 0 ? '확정' : i % 3 === 1 ? '대기' : '취소',
  });
}



// 통계
const stats = computed(() => ({
  total: reservations.value.length,
  confirmed: reservations.value.filter(r => r.status === '확정').length,
  waiting: reservations.value.filter(r => r.status === '대기').length,
  cancelled: reservations.value.filter(r => r.status === '취소').length,
}));

// 상태 필터
const filterOpen = ref(false);
const statusFilter = ref('전체'); // 전체 | 확정 | 대기 | 취소 | 환불

// 2차: 상태 필터까지 적용

// 날짜별 예약 건수 (YYYY-MM-DD -> count)
const reservationCountByDate = computed(() => {
  const map = new Map();
  for (const r of reservations.value) {
    const dateKey = getReservationDateStr(r.datetime); // 'YYYY-MM-DD'
    map.set(dateKey, (map.get(dateKey) || 0) + 1);
  }
  return map;
});

// 현재 캘린더(월)에서 특정 day의 YYYY-MM-DD 키 만들기
const getDateKeyForDay = (day) => {
  if (!day) return null;
  const y = currentMonth.value.getFullYear();
  const m = String(currentMonth.value.getMonth() + 1).padStart(2, '0');
  const d = String(day).padStart(2, '0');
  return `${y}-${m}-${d}`;
};

// 해당 day의 예약 건수
const getReservationCountForDay = (day) => {
  const key = getDateKeyForDay(day);
  if (!key) return 0;
  return reservationCountByDate.value.get(key) || 0;
};

const filteredReservations = computed(() => {
  const base = dateFilteredReservations.value;
  if (statusFilter.value === '전체') return base;
  return base.filter((r) => r.status === statusFilter.value);
});

// --- Pagination ---
const PAGE_SIZE = 10;
const currentPage = ref(1);

// 필터/날짜 바뀌면 1페이지로 리셋
watch([selectedDateStr, onlySelectedDate, statusFilter], () => {
  currentPage.value = 1;
});

const totalPages = computed(() => {
  const total = filteredReservations.value.length;
  return Math.max(1, Math.ceil(total / PAGE_SIZE));
});

const pagedReservations = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE;
  return filteredReservations.value.slice(start, start + PAGE_SIZE);
});

const pages = computed(() => {
  return Array.from({ length: totalPages.value }, (_, i) => i + 1);
});

const goPage = (p) => {
  if (p < 1 || p > totalPages.value) return;
  currentPage.value = p;
};

const prevPage = () => goPage(currentPage.value - 1);
const nextPage = () => goPage(currentPage.value + 1);

// 필터 드롭다운 바깥 클릭 시 닫기
const filterWrap = ref(null);
const handleOutsideClick = (e) => {
  if (!filterOpen.value) return;
  if (filterWrap.value && !filterWrap.value.contains(e.target)) {
    filterOpen.value = false;
  }
};
onMounted(() => document.addEventListener('click', handleOutsideClick));
onBeforeUnmount(() => document.removeEventListener('click', handleOutsideClick));

// --- 취소 모달 상태 ---
const cancelModalOpen = ref(false);
const cancelTargetId = ref(null);
const cancelReason = ref('');
const cancelError = ref('');
const MAX_CANCEL_REASON = 50;

const openCancelModal = (id) => {
  const target = reservations.value.find((r) => r.id === id);
  if (!target) return;
  if (target.status === '취소' || target.status === '환불') return; //환불도 취소 막기

  cancelTargetId.value = id;
  cancelReason.value = '';
  cancelError.value = '';
  cancelModalOpen.value = true;
};

const closeCancelModal = () => {
  cancelModalOpen.value = false;
  cancelTargetId.value = null;
  cancelReason.value = '';
  cancelError.value = '';
};

const submitCancel = () => {
  const reason = cancelReason.value.trim();

  if (!reason) {
    cancelError.value = '취소 사유는 필수 입니다.';
    return;
  }

  if (reason.length > MAX_CANCEL_REASON) {
    cancelError.value = `취소 사유는 ${MAX_CANCEL_REASON}자 이내로 입력해주세요.`;
    return;
  }

  const target = reservations.value.find((r) => r.id === cancelTargetId.value);
  if (!target) return;

  target.status = '취소';
  target.cancelReason = reason;
  target.cancelledAt = new Date().toISOString();

  closeCancelModal();
  window.alert('취소가 완료되었습니다.');
};

// Calendar helpers 
const getDaysInMonth = (date) => new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
const getFirstDayOfMonth = (date) => {
  const day = new Date(date.getFullYear(), date.getMonth(), 1).getDay();
  return day === 0 ? 6 : day - 1;
};

const generateCalendarDays = computed(() => {
  const daysInMonth = getDaysInMonth(currentMonth.value);
  const firstDay = getFirstDayOfMonth(currentMonth.value);
  const days = [];
  for (let i = 0; i < firstDay; i++) days.push(null);
  for (let i = 1; i <= daysInMonth; i++) days.push(i);
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
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() - 1, 1);
};
const nextMonth = () => {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 1);
};

const formattedMonth = computed(() =>
  currentMonth.value.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long' })
);

const selectDay = (day) => {
  if (!day) return;
  selectedDate.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth(), day);
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar 
      v-if="userRole === 'owner'" 
      activeMenu="reservations" 
    />
    <StaffSideBar 
      v-else-if="userRole === 'staff'" 
      activeMenu="reservations" 
    />

    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-8">
          <h2 class="text-3xl font-bold text-[#1e3a5f]">전체 예약 관리</h2>

          <!-- Calendar -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
            <div class="flex items-center justify-between mb-4">
              <button @click="previousMonth" class="px-4 py-2 text-sm font-medium text-[#1e3a5f] hover:bg-[#f8f9fa] rounded-lg">&lt;</button>
              <h3 class="text-lg font-semibold text-[#1e3a5f]">{{ formattedMonth }}</h3>
              <button @click="nextMonth" class="px-4 py-2 text-sm font-medium text-[#1e3a5f] hover:bg-[#f8f9fa] rounded-lg">&gt;</button>
            </div>

            <div class="grid grid-cols-7 gap-x-2 gap-y-1">
              <div
                v-for="dayName in ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su']"
                :key="dayName"
                class="text-center text-sm font-medium text-[#6c757d] py-1"
              >
                {{ dayName }}
              </div>
              <button
                v-for="(day, index) in generateCalendarDays"
                :key="index"
                @click="selectDay(day)"
                :disabled="!day"
                :class="[
                  'relative h-24 rounded-xl p-3 flex flex-col justify-between items-stretch border transition-all',
                  
                  { invisible: !day },
                  isSelectedDate(day)
                    ? 'bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] text-white border-transparent shadow-md'
                    : 'bg-white text-[#1e3a5f] border-[#e9ecef] hover:bg-[#f8f9fa]'
                ]"
              >
                <div class="flex items-start justify-between">
                  <span class="text-lg font-bold">{{ day }}</span>
                  <span v-if="isSelectedDate(day)" class="w-2.5 h-2.5 rounded-full bg-white/80 mt-1" />
                </div>

                <!-- 예약 있는 날만 배너 -->
                <div
                  v-if="day && getReservationCountForDay(day) > 0"
                  :class="[
                    'mt-1 rounded-lg px-2 py-1 text-center font-semibold text-xs',
                    isSelectedDate(day)
                      ? 'bg-white/20 text-white'
                      : 'bg-[#1e3a5f] text-white'
                  ]"
                >
                  예약 <span class="text-base font-extrabold">{{ getReservationCountForDay(day) }}</span>건
                </div>
              </button>
            </div>
          </div>

          <!-- Stats Cards  -->
          <div class="grid grid-cols-4 gap-6">
            <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
              <p class="text-sm text-[#6c757d] mb-2">전체 예약</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">{{ stats.total }}건</p>
            </div>
            <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
              <p class="text-sm text-[#6c757d] mb-2">확정</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">{{ stats.confirmed }}건</p>
            </div>
            <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
              <p class="text-sm text-[#6c757d] mb-2">대기</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">{{ stats.waiting }}건</p>
            </div>
            <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
              <p class="text-sm text-[#6c757d] mb-2">취소</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">{{ stats.cancelled }}건</p>
            </div>
          </div>

          <!-- Reservations Table -->
          <div class="bg-white rounded-xl border border-[#e9ecef] overflow-visible">
            <div class="border-b border-[#e9ecef] p-6 flex items-center justify-between bg-[#f8f9fa]">
              <h3 class="text-lg font-bold text-[#1e3a5f]">예약 목록</h3>

              <div class="flex items-center gap-3">
                <label class="flex items-center gap-2 text-sm text-[#6c757d] select-none">
                  <input type="checkbox" v-model="onlySelectedDate" />
                  선택 날짜만 보기
                </label>

                <!-- 필터 버튼 + 드롭다운-->
                <div class="relative" ref="filterWrap">
                  <button
                    @click.stop="filterOpen = !filterOpen"
                    class="flex items-center gap-2 px-4 py-2 border border-[#dee2e6] rounded-lg text-[#1e3a5f] hover:bg-white transition-colors"
                  >
                    <Filter class="w-4 h-4" />
                    <span class="text-sm">필터</span>
                  </button>

                  <div
                    v-if="filterOpen"
                    class="absolute right-0 mt-2 w-40 bg-white border border-[#e9ecef] rounded-lg shadow-md z-20 overflow-hidden"
                  >
                    <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='확정'; filterOpen=false">확정</button>
                    <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='대기'; filterOpen=false">대기</button>
                    <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='취소'; filterOpen=false">취소</button>
                    <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='환불'; filterOpen=false">환불</button>
                    <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='전체'; filterOpen=false">전체</button>
                  </div>
                </div>
              </div>
            </div>

            <div class="overflow-x-auto">
              <table class="w-full">
                <thead class="bg-[#f8f9fa] border-b border-[#e9ecef]">
                  <tr>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">예약자</th>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">연락처</th>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">예약시간</th>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">인원</th>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">결제금액</th>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">상태</th>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">작업</th>
                  </tr>
                </thead>

                <tbody class="divide-y divide-[#e9ecef]">
                  <tr
                    v-for="reservation in pagedReservations"
                    :key="reservation.id"
                    class="hover:bg-[#f8f9fa] transition-colors"
                  >
                    <td class="px-6 py-4 text-sm text-[#1e3a5f]">{{ reservation.name }}</td>
                    <td class="px-6 py-4 text-sm text-[#6c757d]">{{ reservation.phone }}</td>
                    <td class="px-6 py-4 text-sm text-[#1e3a5f]">{{ reservation.datetime }}</td>
                    <td class="px-6 py-4 text-sm text-[#1e3a5f]">{{ reservation.guests }}명</td>
                    <td class="px-6 py-4 text-sm text-[#1e3a5f]">{{ reservation.amount.toLocaleString() }}원</td>

                    <td class="px-6 py-4">
                      <span
                        :class="`inline-flex px-3 py-1 rounded-full text-xs font-semibold ${
                          reservation.status === '확정'
                            ? 'bg-[#d4edda] text-[#155724]'
                            : reservation.status === '대기'
                            ? 'bg-[#fff3cd] text-[#856404]'
                            : reservation.status === '취소'
                            ? 'bg-[#f8d7da] text-[#721c24]'
                            : 'bg-[#d1ecf1] text-[#0c5460]'
                        }`"
                      >
                        {{ reservation.status }}
                      </span>
                    </td>

                    <td class="px-6 py-4">
                      <!-- 상세보기 1개만 -->
                      <div class="flex items-center gap-2">
                        <button
                          @click="goDetail(reservation.id)"
                          class="bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] text-white px-4 py-2 rounded-full text-xs hover:opacity-90 transition-opacity"
                        >
                          상세보기
                        </button>

                        <!-- 확정/대기만 취소 가능 -->
                        <button
                          v-if="reservation.status !== '취소' && reservation.status !== '환불'"
                          @click="openCancelModal(reservation.id)"
                          class="bg-[#dc3545] text-white px-4 py-2 rounded-full text-xs hover:opacity-90 transition-opacity"
                        >
                          취소
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div
                v-if="filteredReservations.length > 0"
                class="flex items-center justify-center gap-3 py-5"
              >
                <button
                  type="button"
                  @click="prevPage"
                  :disabled="currentPage === 1"
                  class="p-2 rounded-full hover:bg-[#f8f9fa] disabled:opacity-30 disabled:cursor-not-allowed"
                >
                  <ChevronLeft class="w-5 h-5 text-[#6c757d]" />
                </button>

                <button
                  v-for="p in pages"
                  :key="p"
                  type="button"
                  @click="goPage(p)"
                  :class="[
                    'w-8 h-8 rounded-full text-sm font-semibold transition-all',
                    p === currentPage
                      ? 'bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] text-white shadow-sm'
                      : 'text-[#1e3a5f] hover:bg-[#f8f9fa]'
                  ]"
                >
                  {{ p }}
                </button>

                <button
                  type="button"
                  @click="nextPage"
                  :disabled="currentPage === totalPages"
                  class="p-2 rounded-full hover:bg-[#f8f9fa] disabled:opacity-30 disabled:cursor-not-allowed"
                >
                  <ChevronRight class="w-5 h-5 text-[#6c757d]" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- 취소 사유 모달  -->
      <div v-if="cancelModalOpen" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/30" @click="closeCancelModal"></div>

        <div class="relative w-full max-w-lg bg-white rounded-xl border border-[#e9ecef] shadow-lg p-6">
          <h3 class="text-lg font-bold text-[#1e3a5f]">예약 취소</h3>
          <p class="text-sm text-[#6c757d] mt-1">취소 사유를 50자 이내로 입력해주세요.</p>

          <div class="mt-4">
            <textarea
              v-model="cancelReason"
              rows="4"
              maxlength="50"
              class="w-full border border-[#dee2e6] rounded-lg p-3 text-sm outline-none focus:ring-2 focus:ring-[#ff6b4a]/30"
              placeholder="예: 고객 요청 / 매장 사정 / 노쇼 등"
            />
            <p class="mt-2 text-xs text-[#6c757d] text-right">
              {{ cancelReason.trim().length }}/50
            </p>
            <p v-if="cancelError" class="mt-2 text-sm text-[#dc3545]">{{ cancelError }}</p>
          </div>

          <div class="mt-5 flex justify-end gap-2">
            <button
              @click="closeCancelModal"
              class="px-4 py-2 border border-[#dee2e6] rounded-lg text-sm text-[#1e3a5f] hover:bg-[#f8f9fa]"
            >
              닫기
            </button>
            <button
              @click="submitCancel"
              class="px-4 py-2 rounded-lg text-sm text-white bg-[#dc3545] hover:opacity-90"
            >
              취소 확정
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>