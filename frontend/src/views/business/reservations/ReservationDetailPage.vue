<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';

const route = useRoute();
const router = useRouter();

const reservationId = computed(() => Number(route.params.id));

//임시 mock (나중에 이 부분만 API 호출로 교체)
const reservations = [
  {
    id: 1,
    name: '홍길동',
    phone: '010-1234-5678',
    date: '2025-12-25',
    time: '12:00',
    guests: 4,
    amount: 72000,
    status: 'confirmed',
    requestNote: '창가 자리 부탁드립니다. 아이 의자 필요해요.',
    paymentType: 'prepaid',
    preorderItems: [
      { name: '런치 스테이크', qty: 2, price: 18000 },
      { name: '파스타', qty: 1, price: 16000 },
    ],
  },
  {
    id: 2,
    name: '김철수',
    phone: '010-2345-6789',
    date: '2025-12-25',
    time: '13:00',
    guests: 6,
    amount: 108000,
    status: 'confirmed',
    requestNote: '',
    paymentType: 'onsite',
    preorderItems: [],
  },
  {
    id: 3,
    name: '이영희',
    phone: '010-3456-7890',
    date: '2025-12-25',
    time: '12:30',
    guests: 8,
    amount: 144000,
    status: 'pending',
    requestNote: '조용한 자리 부탁드립니다.',
    paymentType: 'onsite',
    preorderItems: [],
  },
  {
    id: 4,
    name: '박민수',
    phone: '010-4567-8901',
    date: '2025-12-25',
    time: '14:00',
    guests: 5,
    amount: 90000,
    status: 'confirmed',
    requestNote: '',
    paymentType: 'onsite',
    preorderItems: [],
  },
  {
    id: 5,
    name: '정수진',
    phone: '010-5678-9012',
    date: '2025-12-25',
    time: '11:30',
    guests: 4,
    amount: 72000,
    status: 'pending',
    requestNote: '유아 동반입니다.',
    paymentType: 'onsite',
    preorderItems: [],
  },
  {
    id: 6,
    name: '최동훈',
    phone: '010-6789-0123',
    date: '2025-12-25',
    time: '13:30',
    guests: 7,
    amount: 126000,
    status: 'confirmed',
    requestNote: '',
    paymentType: 'onsite',
    preorderItems: [],
  },
  {
    id: 7,
    name: '윤서연',
    phone: '010-7890-1234',
    date: '2025-12-25',
    time: '12:00',
    guests: 4,
    amount: 72000,
    status: 'cancelled',
    requestNote: '',
    paymentType: 'onsite',
    preorderItems: [],
  },
];

const reservation = computed(() =>
  reservations.find((r) => r.id === reservationId.value)
);
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="dashboard" />
    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-5xl mx-auto space-y-6">
          <div class="flex items-center gap-3">
            <button @click="router.back()" class="px-4 py-2 border rounded-lg">뒤로</button>
            <h2 class="text-3xl font-bold text-[#1e3a5f]">예약 상세</h2>
          </div>

          <div v-if="!reservation" class="bg-white rounded-xl border p-10 text-center text-[#6c757d]">
            해당 예약을 찾을 수 없습니다.
          </div>

          <div v-else class="bg-white rounded-xl border p-6">
            <p class="text-sm text-[#6c757d] mb-2">예약자</p>
            <p class="text-2xl font-bold text-[#1e3a5f]">{{ reservation.name }}</p>
            <p class="text-sm text-[#6c757d]">{{ reservation.phone }}</p>

            <div class="mt-6 grid grid-cols-2 gap-6">
              <div>
                <p class="text-sm text-[#6c757d]">예약시간</p>
                <p class="font-semibold">{{ reservation.date }} {{ reservation.time }}</p>
              </div>
              <div>
                <p class="text-sm text-[#6c757d]">결제금액</p>
                <p class="font-semibold">{{ reservation.amount.toLocaleString() }}원</p>
              </div>
            </div>
            <!-- 상태 -->
            <div class="mt-6">
            <p class="text-sm text-[#6c757d] mb-2">예약 상태</p>
            <span
                class="inline-flex px-3 py-1 rounded-full text-xs font-semibold"
                :class="
                reservation.status === 'confirmed'
                    ? 'bg-[#d4edda] text-[#155724]'
                    : reservation.status === 'pending'
                    ? 'bg-[#fff3cd] text-[#856404]'
                    : reservation.status === 'cancelled'
                    ? 'bg-[#f8d7da] text-[#721c24]'
                    : 'bg-[#d1ecf1] text-[#0c5460]'
                "
            >
                {{
                reservation.status === 'confirmed'
                    ? '확정'
                    : reservation.status === 'pending'
                    ? '대기'
                    : reservation.status === 'cancelled'
                    ? '취소'
                    : '환불'
                }}
            </span>
            </div>

            <!-- 요청사항 -->
            <div class="mt-6">
            <p class="text-sm text-[#6c757d] mb-2">요청사항</p>
            <div class="bg-[#f8f9fa] border border-[#e9ecef] rounded-lg p-4 text-sm text-[#1e3a5f]">
                {{ reservation.requestNote || '요청사항 없음' }}
            </div>
            </div>

            <!-- 선주문/선결제 메뉴 -->
            <div class="mt-6">
            <p class="text-sm text-[#6c757d] mb-2">선주문 메뉴</p>

            <div
                v-if="reservation.preorderItems && reservation.preorderItems.length"
                class="border border-[#e9ecef] rounded-lg overflow-hidden"
            >
                <div class="bg-[#f8f9fa] px-4 py-3 text-sm font-semibold text-[#1e3a5f]">
                {{ reservation.paymentType === 'prepaid' ? '선결제' : '현장결제' }}
                </div>

                <div class="divide-y divide-[#e9ecef]">
                <div
                    v-for="(item, idx) in reservation.preorderItems"
                    :key="idx"
                    class="px-4 py-3 flex items-center justify-between text-sm"
                >
                    <div class="text-[#1e3a5f]">
                    {{ item.name }} <span class="text-[#6c757d]">x{{ item.qty }}</span>
                    </div>
                    <div class="text-[#1e3a5f] font-semibold">
                    {{ (item.price * item.qty).toLocaleString() }}원
                    </div>
                </div>
                </div>

                <div class="bg-white px-4 py-3 flex items-center justify-between text-sm">
                <span class="text-[#6c757d]">합계</span>
                <span class="font-bold text-[#1e3a5f]">
                    {{
                    reservation.preorderItems
                        .reduce((sum, i) => sum + i.price * i.qty, 0)
                        .toLocaleString()
                    }}원
                </span>
                </div>
            </div>

            <div v-else class="bg-[#f8f9fa] border border-[#e9ecef] rounded-lg p-4 text-sm text-[#6c757d]">
                선주문 내역 없음
            </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>