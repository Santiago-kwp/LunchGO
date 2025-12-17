<script setup>
import { ref, computed } from 'vue';
import AdminSidebar from '@/components/ui/AdminSideBar.vue';
import AdminHeader from '@/components/ui/AdminHeader.vue';
import Pagination from '@/components/ui/Pagination.vue';
import AdminSearchFilter from '@/components/ui/AdminSearchFilter.vue';
import {
  Calendar,
  CheckCircle,
  Clock,
  CreditCard,
  XCircle,
  ArrowUpRight,
  ArrowDownRight,
} from 'lucide-vue-next';

// 검색 및 필터
const searchQuery = ref('');
const selectedStatus = ref('all');
const selectedType = ref('all');
const startDate = ref('');
const endDate = ref('');

// 페이지네이션
const currentPage = ref(1);
const itemsPerPage = 10;

// 예약 상태 옵션
const statusOptions = [
  { value: 'all', label: '전체' },
  { value: 'temp', label: '임시 예약' },
  { value: 'confirmed', label: '예약 확정' },
  { value: 'completed', label: '이용 완료' },
  { value: 'refund_pending', label: '환불 대기' },
  { value: 'refunded', label: '환불 완료' },
];

// 예약 타입 옵션
const typeOptions = [
  { value: 'all', label: '전체' },
  { value: 'prepaid', label: '선결제' },
  { value: 'deposit', label: '예약금' },
];

// 예약 상태별 배지 색상
const getStatusBadgeColor = (status) => {
  const colors = {
    temp: 'bg-yellow-100 text-yellow-800',
    confirmed: 'bg-blue-100 text-blue-800',
    completed: 'bg-green-100 text-green-800',
    refund_pending: 'bg-orange-100 text-orange-800',
    refunded: 'bg-gray-100 text-gray-800',
  };
  return colors[status] || 'bg-gray-100 text-gray-800';
};

// 예약 상태 라벨
const getStatusLabel = (status) => {
  const labels = {
    temp: '임시 예약',
    confirmed: '예약 확정',
    completed: '이용 완료',
    refund_pending: '환불 대기',
    refunded: '환불 완료',
  };
  return labels[status] || status;
};

// 예약 타입 라벨
const getTypeLabel = (type) => {
  return type === 'prepaid' ? '선결제' : '예약금';
};

// 이름 블라인드 처리 함수
const maskName = (name) => {
  if (name.length <= 1) return name;
  return name[0] + '*'.repeat(name.length - 1);
};

// Mock 데이터 (총 45개 - 페이지네이션 테스트용)
const allReservations = ref([
  {
    id: 'R2024121701',
    restaurantName: '한신포차 강남점',
    customerName: '김민수',
    reservationDateTime: '2024-12-20 18:30',
    partySize: 4,
    type: 'prepaid',
    status: 'confirmed',
  },
  {
    id: 'R2024121702',
    restaurantName: '본죽&비빔밥 서초점',
    customerName: '이영희',
    reservationDateTime: '2024-12-21 12:00',
    partySize: 2,
    type: 'deposit',
    status: 'temp',
  },
  {
    id: 'R2024121703',
    restaurantName: '스시로 판교점',
    customerName: '박철수',
    reservationDateTime: '2024-12-19 19:00',
    partySize: 3,
    type: 'prepaid',
    status: 'completed',
  },
  {
    id: 'R2024121704',
    restaurantName: '아웃백 스테이크하우스 홍대점',
    customerName: '정다은',
    reservationDateTime: '2024-12-22 18:00',
    partySize: 6,
    type: 'deposit',
    status: 'confirmed',
  },
  {
    id: 'R2024121705',
    restaurantName: '청년다방 신논현점',
    customerName: '최지훈',
    reservationDateTime: '2024-12-18 17:30',
    partySize: 2,
    type: 'prepaid',
    status: 'refunded',
  },
  {
    id: 'R2024121706',
    restaurantName: '곱창이야기 강남점',
    customerName: '강서연',
    reservationDateTime: '2024-12-23 19:30',
    partySize: 5,
    type: 'prepaid',
    status: 'confirmed',
  },
  {
    id: 'R2024121707',
    restaurantName: '교촌치킨 역삼점',
    customerName: '윤준호',
    reservationDateTime: '2024-12-21 18:00',
    partySize: 3,
    type: 'deposit',
    status: 'temp',
  },
  {
    id: 'R2024121708',
    restaurantName: '도쿄스테이크 압구정점',
    customerName: '임수진',
    reservationDateTime: '2024-12-20 19:00',
    partySize: 4,
    type: 'prepaid',
    status: 'confirmed',
  },
  {
    id: 'R2024121709',
    restaurantName: '한신포차 신촌점',
    customerName: '송민재',
    reservationDateTime: '2024-12-19 18:30',
    partySize: 2,
    type: 'deposit',
    status: 'completed',
  },
  {
    id: 'R2024121710',
    restaurantName: '본죽&비빔밥 잠실점',
    customerName: '한지우',
    reservationDateTime: '2024-12-22 12:30',
    partySize: 3,
    type: 'prepaid',
    status: 'refund_pending',
  },
  {
    id: 'R2024121711',
    restaurantName: '스시로 강남점',
    customerName: '배수현',
    reservationDateTime: '2024-12-21 19:30',
    partySize: 4,
    type: 'prepaid',
    status: 'confirmed',
  },
  {
    id: 'R2024121712',
    restaurantName: '청년다방 건대점',
    customerName: '오태양',
    reservationDateTime: '2024-12-23 17:00',
    partySize: 2,
    type: 'deposit',
    status: 'temp',
  },
  {
    id: 'R2024121713',
    restaurantName: '아웃백 스테이크하우스 여의도점',
    customerName: '서은영',
    reservationDateTime: '2024-12-20 18:30',
    partySize: 5,
    type: 'prepaid',
    status: 'confirmed',
  },
  {
    id: 'R2024121714',
    restaurantName: '곱창이야기 홍대점',
    customerName: '권민호',
    reservationDateTime: '2024-12-19 19:00',
    partySize: 4,
    type: 'deposit',
    status: 'completed',
  },
  {
    id: 'R2024121715',
    restaurantName: '교촌치킨 강남점',
    customerName: '조혜린',
    reservationDateTime: '2024-12-22 18:30',
    partySize: 3,
    type: 'prepaid',
    status: 'confirmed',
  },
  {
    id: 'R2024121716',
    restaurantName: '도쿄스테이크 신사점',
    customerName: '남궁현',
    reservationDateTime: '2024-12-21 19:00',
    partySize: 2,
    type: 'deposit',
    status: 'temp',
  },
  {
    id: 'R2024121717',
    restaurantName: '한신포차 판교점',
    customerName: '유채원',
    reservationDateTime: '2024-12-20 18:00',
    partySize: 6,
    type: 'prepaid',
    status: 'confirmed',
  },
  {
    id: 'R2024121718',
    restaurantName: '본죽&비빔밥 강남점',
    customerName: '홍석진',
    reservationDateTime: '2024-12-23 12:00',
    partySize: 2,
    type: 'deposit',
    status: 'refund_pending',
  },
  {
    id: 'R2024121719',
    restaurantName: '스시로 서초점',
    customerName: '안소희',
    reservationDateTime: '2024-12-19 19:30',
    partySize: 3,
    type: 'prepaid',
    status: 'completed',
  },
  {
    id: 'R2024121720',
    restaurantName: '청년다방 판교점',
    customerName: '장우진',
    reservationDateTime: '2024-12-22 17:30',
    partySize: 4,
    type: 'deposit',
    status: 'confirmed',
  },
  // 추가 데이터 (21-45)
  ...Array.from({ length: 25 }, (_, i) => ({
    id: `R20241217${21 + i}`,
    restaurantName:
      [
        '한신포차',
        '본죽&비빔밥',
        '스시로',
        '아웃백 스테이크하우스',
        '청년다방',
      ][i % 5] +
      ' ' +
      ['강남점', '서초점', '판교점', '홍대점', '잠실점'][Math.floor(i / 5)],
    customerName:
      ['김', '이', '박', '정', '최', '강', '윤', '임', '송'][i % 9] +
      ['민수', '영희', '철수', '지훈', '서연'][Math.floor(i / 5) % 5],
    reservationDateTime: `2024-12-${18 + (i % 5)} ${12 + (i % 8)}:${
      (i % 2) * 30
    }0`,
    partySize: 2 + (i % 5),
    type: i % 2 === 0 ? 'prepaid' : 'deposit',
    status: ['temp', 'confirmed', 'completed', 'refund_pending', 'refunded'][
      i % 5
    ],
  })),
]);

// 필터링된 예약 목록
const filteredReservations = computed(() => {
  let filtered = allReservations.value;

  // 검색어 필터
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(
      (r) =>
        r.id.toLowerCase().includes(query) ||
        r.restaurantName.toLowerCase().includes(query) ||
        r.customerName.toLowerCase().includes(query)
    );
  }

  // 상태 필터
  if (selectedStatus.value !== 'all') {
    filtered = filtered.filter((r) => r.status === selectedStatus.value);
  }

  // 타입 필터
  if (selectedType.value !== 'all') {
    filtered = filtered.filter((r) => r.type === selectedType.value);
  }

  // 날짜 범위 필터
  if (startDate.value || endDate.value) {
    filtered = filtered.filter((r) => {
      const reservationDate = new Date(r.reservationDateTime);

      if (startDate.value && endDate.value) {
        const start = new Date(startDate.value);
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999); // 종료일의 마지막 시간까지 포함
        return reservationDate >= start && reservationDate <= end;
      } else if (startDate.value) {
        const start = new Date(startDate.value);
        return reservationDate >= start;
      } else if (endDate.value) {
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999);
        return reservationDate <= end;
      }

      return true;
    });
  }

  return filtered;
});

// 통계 데이터 (필터링된 예약 기준)
const stats = computed(() => {
  const now = new Date();
  const currentMonth = now.getMonth() + 1;
  const currentYear = now.getFullYear();
  const lastMonth = currentMonth === 1 ? 12 : currentMonth - 1;
  const lastMonthYear = currentMonth === 1 ? currentYear - 1 : currentYear;

  // 이번 달 예약
  const thisMonthReservations = allReservations.value.filter((r) => {
    const resDate = new Date(r.reservationDateTime);
    return (
      resDate.getMonth() + 1 === currentMonth &&
      resDate.getFullYear() === currentYear
    );
  });

  // 지난 달 예약
  const lastMonthReservations = allReservations.value.filter((r) => {
    const resDate = new Date(r.reservationDateTime);
    return (
      resDate.getMonth() + 1 === lastMonth &&
      resDate.getFullYear() === lastMonthYear
    );
  });

  // 전월 대비 증감
  const totalDiff = thisMonthReservations.length - lastMonthReservations.length;
  const confirmedDiff =
    thisMonthReservations.filter((r) => r.status === 'confirmed').length -
    lastMonthReservations.filter((r) => r.status === 'confirmed').length;
  const tempDiff =
    thisMonthReservations.filter((r) => r.status === 'temp').length -
    lastMonthReservations.filter((r) => r.status === 'temp').length;
  const refundPendingDiff =
    thisMonthReservations.filter((r) => r.status === 'refund_pending').length -
    lastMonthReservations.filter((r) => r.status === 'refund_pending').length;
  const refundedDiff =
    thisMonthReservations.filter((r) => r.status === 'refunded').length -
    lastMonthReservations.filter((r) => r.status === 'refunded').length;

  return {
    total: filteredReservations.value.length,
    totalDiff,
    confirmed: filteredReservations.value.filter(
      (r) => r.status === 'confirmed'
    ).length,
    confirmedDiff,
    temp: filteredReservations.value.filter((r) => r.status === 'temp').length,
    tempDiff,
    refundPending: filteredReservations.value.filter(
      (r) => r.status === 'refund_pending'
    ).length,
    refundPendingDiff,
    refunded: filteredReservations.value.filter((r) => r.status === 'refunded')
      .length,
    refundedDiff,
  };
});

// 페이지네이션된 예약 목록
const paginatedReservations = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredReservations.value.slice(start, end);
});

// 총 페이지 수
const totalPages = computed(() => {
  return Math.ceil(filteredReservations.value.length / itemsPerPage);
});

// 페이지 변경 핸들러
const handlePageChange = (page) => {
  currentPage.value = page;
};

// 필터 값 업데이트 핸들러
const handleFilterUpdate = ({ model, value }) => {
  if (model === 'selectedStatus') {
    selectedStatus.value = value;
  } else if (model === 'selectedType') {
    selectedType.value = value;
  }
  currentPage.value = 1;
};

// 필터 초기화
const resetFilters = () => {
  searchQuery.value = '';
  selectedStatus.value = 'all';
  selectedType.value = 'all';
  startDate.value = '';
  endDate.value = '';
  currentPage.value = 1;
};

// 필터 설정
const filters = computed(() => [
  {
    model: 'selectedStatus',
    label: '예약 상태',
    value: selectedStatus.value,
    options: statusOptions,
  },
  {
    model: 'selectedType',
    label: '예약 타입',
    value: selectedType.value,
    options: typeOptions,
  },
]);
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <AdminSidebar activeMenu="reservations" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <AdminHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-6">
          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">예약 등록 현황</h2>
            <p class="text-sm text-[#6c757d]">
              총 {{ filteredReservations.length }}건
            </p>
          </div>

          <!-- 요약 통계 카드 -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
            <!-- 총 예약 수 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-purple-50 rounded-lg">
                  <Calendar class="w-6 h-6 text-purple-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">총 예약 수</p>
              <p class="text-2xl font-bold text-purple-600 mb-2">
                {{ stats.total.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.totalDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1 text-purple-600"
                />
                <span class="text-purple-600">
                  {{ Math.abs(stats.totalDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 예약 확정 수 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-blue-50 rounded-lg">
                  <CheckCircle class="w-6 h-6 text-blue-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">예약 확정</p>
              <p class="text-2xl font-bold text-blue-600 mb-2">
                {{ stats.confirmed.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.confirmedDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1 text-blue-600"
                />
                <span class="text-blue-600">
                  {{ Math.abs(stats.confirmedDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 임시 예약 수 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-yellow-50 rounded-lg">
                  <Clock class="w-6 h-6 text-yellow-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">임시 예약</p>
              <p class="text-2xl font-bold text-yellow-600 mb-2">
                {{ stats.temp.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.tempDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1 text-yellow-600"
                />
                <span class="text-yellow-600">
                  {{ Math.abs(stats.tempDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 환불 대기 수 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-orange-50 rounded-lg">
                  <CreditCard class="w-6 h-6 text-orange-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">환불 대기</p>
              <p class="text-2xl font-bold text-orange-600 mb-2">
                {{ stats.refundPending.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="
                    stats.refundPendingDiff >= 0 ? ArrowUpRight : ArrowDownRight
                  "
                  class="w-4 h-4 mr-1 text-orange-600"
                />
                <span class="text-orange-600">
                  {{ Math.abs(stats.refundPendingDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 환불 완료 수 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-gray-50 rounded-lg">
                  <XCircle class="w-6 h-6 text-gray-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">환불 완료</p>
              <p class="text-2xl font-bold text-gray-600 mb-2">
                {{ stats.refunded.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.refundedDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1 text-gray-600"
                />
                <span class="text-gray-600">
                  {{ Math.abs(stats.refundedDiff) }}
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
            search-placeholder="예약번호, 식당명, 예약자명으로 검색"
            start-date-label="시작 날짜"
            end-date-label="종료 날짜"
            @update:filter-value="handleFilterUpdate"
            @reset-filters="resetFilters"
          />

          <!-- 예약 목록 테이블 -->
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
                      예약 번호
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      식당명
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      예약자
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      예약 일시
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      예약 인원
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      예약 타입
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      예약 상태
                    </th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-[#e9ecef]">
                  <tr
                    v-for="reservation in paginatedReservations"
                    :key="reservation.id"
                    class="hover:bg-[#f8f9fa] transition-colors"
                  >
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm font-medium text-[#1e3a5f]"
                    >
                      {{ reservation.id }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ reservation.restaurantName }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ maskName(reservation.customerName) }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ reservation.reservationDateTime }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ reservation.partySize }}명
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ getTypeLabel(reservation.type) }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span
                        :class="[
                          'px-3 py-1 rounded-full text-xs font-medium',
                          getStatusBadgeColor(reservation.status),
                        ]"
                      >
                        {{ getStatusLabel(reservation.status) }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>

              <!-- 데이터 없을 때 -->
              <div
                v-if="paginatedReservations.length === 0"
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
