<script setup>
import { CheckCircle2, MapPin, Calendar, Clock, Users, Download, Share2 } from 'lucide-vue-next'; // Import Lucide icons for Vue
import { RouterLink } from 'vue-router'; // Import Vue RouterLink
import Button from '@/components/ui/Button.vue'; // Import custom Button
import Card from '@/components/ui/Card.vue';

const reservation = {
  confirmationNumber: 'LG2024121500123',
  restaurant: {
    name: '식당명',
    address: '서울시 강남구 테헤란로 123',
    phone: '02-1234-5678',
  },
  booking: {
    date: '2024년 12월 15일 (금)',
    time: '18:00',
    partySize: 4,
    requestNote: '', //요청사항 (백엔드 연동 시 값 채우기)
  },
  payment: {
    amount: 176000,
    method: '신용카드',
    paidAt: '2024. 12. 10. 14:35',
  },
};
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <main class="max-w-[500px] mx-auto pb-24">
      <!-- Success Header -->
      <div class="bg-white px-4 pt-12 pb-8 text-center border-b border-[#e9ecef]">
        <div class="w-16 h-16 rounded-full gradient-primary flex items-center justify-center mx-auto mb-4 shadow-button-hover">
          <CheckCircle2 class="w-10 h-10 text-white" />
        </div>
        <h1 class="text-2xl font-bold text-[#1e3a5f] mb-2">예약이 완료되었습니다</h1>
        <p class="text-sm text-[#6c757d] leading-relaxed">
          예약 정보를 확인하시고
          <br />
          방문 시 QR 코드를 제시해 주세요
        </p>
      </div>

      <!-- QR Code Section -->
      <div class="bg-white px-4 py-6 border-b border-[#e9ecef]">
        <div class="text-center">
          <div class="w-48 h-48 mx-auto bg-white border-4 border-[#e9ecef] rounded-2xl flex items-center justify-center shadow-card mb-4">
            <div class="w-40 h-40 bg-gradient-to-br from-gray-100 to-gray-200 rounded-lg flex items-center justify-center">
              <div class="grid grid-cols-8 gap-1 p-2">
                <div
                  v-for="(_, i) in Array.from({ length: 64 })"
                  :key="i"
                  :class="`w-2 h-2 ${Math.random() > 0.5 ? 'bg-[#1e3a5f]' : 'bg-white'} rounded-sm`"
                />
              </div>
            </div>
          </div>
          <p class="text-xs text-[#6c757d] mb-4 leading-relaxed">방문 시 이 QR 코드를 제시해 주세요</p>

          <div class="flex gap-2 justify-center">
            <Button
              variant="outline"
              size="sm"
              class="h-9 px-4 text-sm border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg"
            >
              <Download class="w-4 h-4 mr-1.5" />
              저장
            </Button>
            <Button
              variant="outline"
              size="sm"
              class="h-9 px-4 text-sm border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg"
            >
              <Share2 class="w-4 h-4 mr-1.5" />
              공유
            </Button>
          </div>
        </div>
      </div>

      <!-- Confirmation Number -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
        <div class="text-center">
          <p class="text-xs text-[#6c757d] mb-1">예약 번호</p>
          <p class="text-lg font-bold text-[#1e3a5f] tracking-wide">{{ reservation.confirmationNumber }}</p>
        </div>
      </div>

      <!-- Restaurant Info -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef] mt-2">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">식당 정보</h2>
        <div class="space-y-3">
          <div>
            <p class="font-semibold text-[#1e3a5f] text-lg mb-1">{{ reservation.restaurant.name }}</p>
          </div>
          <div class="flex items-start gap-2 text-sm">
            <MapPin class="w-4 h-4 text-[#6c757d] mt-0.5 flex-shrink-0" />
            <span class="text-[#495057] leading-relaxed">{{ reservation.restaurant.address }}</span>
          </div>
        </div>
      </div>

      <!-- Booking Details -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">예약 정보</h2>
        <div class="space-y-3">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <Calendar class="w-4 h-4 text-[#6c757d]" />
              <span class="text-sm text-[#6c757d]">예약 날짜</span>
            </div>
            <span class="text-sm font-semibold text-[#1e3a5f]">{{ reservation.booking.date }}</span>
          </div>

          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <Clock class="w-4 h-4 text-[#6c757d]" />
              <span class="text-sm text-[#6c757d]">예약 시간</span>
            </div>
            <span class="text-sm font-semibold text-[#1e3a5f]">{{ reservation.booking.time }}</span>
          </div>

          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <Users class="w-4 h-4 text-[#6c757d]" />
              <span class="text-sm text-[#6c757d]">예약 인원</span>
            </div>
            <span class="text-sm font-semibold text-[#1e3a5f]">{{ reservation.booking.partySize }}명</span>
          </div>
        </div>

        <!-- Request Note (Read-only) -->
        <div class="mt-4 pt-4 border-t border-[#e9ecef]">
          <h3 class="text-sm font-semibold text-[#1e3a5f] mb-2">요청사항</h3>

          <div class="rounded-xl border border-[#e9ecef] bg-[#f8f9fa] px-3 py-3">
            <p class="text-sm text-[#495057] whitespace-pre-line">
              {{ reservation.booking.requestNote?.trim() ? reservation.booking.requestNote : '-' }}
            </p>
          </div>

          <p class="mt-2 text-xs text-[#6c757d] leading-relaxed">
            요청사항은 매장 참고용이며, 예약 변경은 매장으로 직접 문의해 주세요.
          </p>
        </div>
      </div>

      <!-- Payment Info -->
      <div class="bg-white px-4 py-5 mt-2">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">결제 정보</h2>
        <div class="space-y-2.5">
          <div class="flex items-center justify-between text-sm">
            <span class="text-[#6c757d]">결제 금액</span>
            <span class="font-semibold text-[#1e3a5f]">{{ reservation.payment.amount.toLocaleString() }}원</span>
          </div>
          <div class="flex items-center justify-between text-sm">
            <span class="text-[#6c757d]">결제 수단</span>
            <span class="font-medium text-[#495057]">{{ reservation.payment.method }}</span>
          </div>
          <div class="flex items-center justify-between text-sm">
            <span class="text-[#6c757d]">결제 일시</span>
            <span class="font-medium text-[#495057]">{{ reservation.payment.paidAt }}</span>
          </div>
        </div>
      </div>

      <!-- Notice -->
      <div class="mx-4 mt-4">
        <Card class="p-4 border-[#e9ecef] rounded-xl bg-[#f8f9fa]">
          <h3 class="text-sm font-semibold text-[#1e3a5f] mb-2">예약 안내</h3>
          <ul class="space-y-1 text-xs text-[#6c757d] leading-relaxed">
            <li>• 예약 시간 10분 전까지 도착해 주세요</li>
            <li>• 예약 시간이 지나면 자동으로 취소될 수 있습니다</li>
            <li>• 예약 취소는 마이페이지에서 가능합니다</li>
            <li>• 문의사항은 식당으로 직접 연락 주시기 바랍니다</li>
          </ul>
        </Card>
      </div>
    </main>

    <!-- Fixed Bottom Buttons -->
    <div class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg">
      <div class="max-w-[500px] mx-auto px-4 py-3 flex gap-2">
        <RouterLink to="/" class="flex-1">
          <Button
            variant="outline"
            class="w-full h-12 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-xl font-semibold"
          >
            홈으로
          </Button>
        </RouterLink>
        <RouterLink to="/my-reservations" class="flex-1">
          <Button class="w-full h-12 gradient-primary text-white font-semibold rounded-xl shadow-button-hover hover:shadow-button-pressed">
            예약 내역
          </Button>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
