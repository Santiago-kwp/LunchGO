<script setup>
import { ref, computed } from 'vue';
import { RouterLink, useRouter, useRoute } from 'vue-router';
import { ArrowLeft, CreditCard, Smartphone, Building2, Check } from 'lucide-vue-next';
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';

const router = useRouter();
const route = useRoute();
const restaurantId = route.params.id || '1'; // Default to '1' if id is not available
const paymentType = route.query.type || 'full'; // 'deposit' or 'full'

const isDepositOnly = computed(() => paymentType === 'deposit');

const selectedPayment = ref(null);
const agreedToTerms = ref(false);
const agreedToRefund = ref(false);
const isTermsModalOpen = ref(false);
const modalTitle = ref('');
const modalContent = ref('');

const depositAmount = 10000; // 예약금 1만원
const totalAmount = computed(() => (isDepositOnly.value ? depositAmount : 176000));

// 이용약관 모달
const openModal = (type) => {
  isTermsModalOpen.value = true;
  if (type === 'terms') {
    modalTitle.value = '서비스 이용약관';
    modalContent.value = '약관 내용 준비 중입니다.';
  } else {
    modalTitle.value = '취소 및 환불 정책';
    modalContent.value = '정책 내용 준비 중입니다.';
  }
};

const closeModal = () => {
  isTermsModalOpen.value = false;
};

// Map Lucide icons to a component object for dynamic rendering
const iconComponents = {
  CreditCard: CreditCard,
  Smartphone: Smartphone,
  Building2: Building2,
  Check: Check, // Not strictly used dynamically in paymentMethods, but good to have
};

const paymentMethods = [
  {
    id: 'card',
    name: '신용/체크카드',
    iconName: 'CreditCard', // Use string name to reference iconComponents
    description: '국내 모든 카드 사용 가능',
  }
];

const canProceed = computed(() => selectedPayment.value && agreedToTerms.value && agreedToRefund.value);

const handlePayment = () => {
  if (canProceed.value) {
    router.push(`/restaurant/${restaurantId}/confirmation`);
  }
};
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink
          :to="isDepositOnly ? `/restaurant/${restaurantId}/booking?type=reservation` : `/restaurant/${restaurantId}/summary`"
          class="mr-3"
        >
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">
          {{ isDepositOnly ? '예약금 결제' : '결제하기' }}
        </h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-32">
      <!-- Payment Amount -->
      <div class="bg-white px-4 py-6 border-b border-[#e9ecef]">
        <div class="text-center">
          <p class="text-sm text-[#6c757d] mb-2">{{ isDepositOnly ? '예약금' : '최종 결제 금액' }}</p>
          <p class="text-3xl font-bold text-[#1e3a5f]">{{ totalAmount.toLocaleString() }}원</p>
          <p v-if="isDepositOnly" class="text-xs text-[#6c757d] mt-2">
            * 예약금은 이용 완료 시 돌려드리며, 노쇼 시 환불되지 않습니다
          </p>
        </div>
      </div>

      <!-- Payment Method Selection -->
      <div class="px-4 py-5">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">결제 수단 선택</h2>
        <div class="space-y-3">
          <button
            v-for="method in paymentMethods"
            :key="method.id"
            @click="selectedPayment = method.id"
            :class="`w-full text-left transition-all ${selectedPayment === method.id ? 'ring-2 ring-[#ff6b4a]' : ''}`"
          >
            <Card class="p-4 border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-md">
              <div class="flex items-center gap-3">
                <div
                  :class="`w-12 h-12 rounded-lg flex items-center justify-center flex-shrink-0 ${
                    selectedPayment === method.id ? 'gradient-primary' : 'bg-[#f8f9fa]'
                  }`"
                >
                  <component :is="iconComponents[method.iconName]" :class="`w-6 h-6 ${selectedPayment === method.id ? 'text-white' : 'text-[#6c757d]'}`" />
                </div>
                <div class="flex-1">
                  <p :class="`font-semibold mb-0.5 ${selectedPayment === method.id ? 'text-[#ff6b4a]' : 'text-[#1e3a5f]'}`">
                    {{ method.name }}
                  </p>
                  <p class="text-xs text-[#6c757d]">{{ method.description }}</p>
                </div>
                <div v-if="selectedPayment === method.id" class="w-6 h-6 rounded-full gradient-primary flex items-center justify-center flex-shrink-0">
                  <Check class="w-4 h-4 text-white" />
                </div>
              </div>
            </Card>
          </button>
        </div>
      </div>

      <!-- Terms Agreement -->
      <div class="px-4 py-5 bg-white border-t border-b border-[#e9ecef]">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">약관 동의</h2>
        <div class="space-y-3">
          <label :class="`flex items-start gap-3 cursor-pointer group rounded-xl p-2 -m-2 transition-colors ${agreedToTerms ? 'bg-[#fff5f3]' : ''}`">
            <div class="relative flex-shrink-0 mt-0.5">
              <input type="checkbox" v-model="agreedToTerms" class="sr-only" />
              <div :class="`w-5 h-5 rounded border-2 flex items-center justify-center transition-all ${agreedToTerms ? 'bg-[#ff6b4a] border-[#ff6b4a]' : 'bg-white border-[#dee2e6]'} group-hover:border-[#ff6b4a]`">
                <Check v-if="agreedToTerms" class="w-3.5 h-3.5 text-white" />
              </div>
            </div>
            <div class="flex-1">
              <p class="text-sm text-[#1e3a5f] font-medium mb-0.5">(필수) 서비스 이용약관 동의</p>
              <button type="button" @click.stop="openModal('terms')" class="text-xs text-[#6c757d] underline hover:text-[#ff6b4a]">자세히 보기</button> <!--이용약관 모달 버튼-->
            </div>
          </label>

          <label :class="`flex items-start gap-3 cursor-pointer group rounded-xl p-2 -m-2 transition-colors ${agreedToRefund ? 'bg-[#fff5f3]' : ''}`">
            <div class="relative flex-shrink-0 mt-0.5">
              <input type="checkbox" v-model="agreedToRefund" class="sr-only" />
              <div :class="`w-5 h-5 rounded border-2 flex items-center justify-center transition-all ${agreedToRefund ? 'bg-[#ff6b4a] border-[#ff6b4a]' : 'bg-white border-[#dee2e6]'} group-hover:border-[#ff6b4a]`">
                <Check v-if="agreedToRefund" class="w-3.5 h-3.5 text-white" />
              </div>
            </div>
            <div class="flex-1">
              <p class="text-sm text-[#1e3a5f] font-medium mb-0.5">(필수) 취소 및 환불 정책 동의</p>
              <!--이용약관 모달 버튼-->
              <button type="button" @click.stop="openModal('refund')" class="text-xs text-[#6c757d] underline hover:text-[#ff6b4a]">자세히 보기</button>
            </div>
          </label>
        </div>
      </div>

      <!-- Payment Notice -->
      <div class="mx-4 mt-4">
        <Card class="p-4 border-[#e9ecef] rounded-xl bg-[#f8f9fa]">
          <h3 class="text-sm font-semibold text-[#1e3a5f] mb-2">결제 안내</h3>
          <ul class="space-y-1 text-xs text-[#6c757d] leading-relaxed">
            <template v-if="isDepositOnly">
              <li>• 예약금 결제 후 즉시 예약이 확정됩니다.</li>
              <li>• 예약금은 이용 완료 확인 시 돌려드립니다.</li>
              <li>• 취소는 방문 1일 전까지 가능하며, 당일 취소 및 노쇼 시 환불 정책에 따라 환불 됩니다.</li>
              <li>• 예약 변경 시 취소 후 재예약 해주시길 바랍니다.</li>
              <li>• 취소 후 재예약시 환불 정책에 따라 환불 됩니다.</li>
            </template>
            <template v-else>
              <li>• 결제 후 즉시 예약이 확정됩니다.</li>
              <li>• 영업일 기준 3-5일 이내 환불 처리됩니다.</li>
              <li>• 문의사항은 고객센터로 연락 주시기 바랍니다.</li>
            </template>
          </ul>
        </Card>
      </div>
    </main>

    <!-- Terms Modal -->
  <div v-if="isTermsModalOpen" class="fixed inset-0 z-[999]">
  <!-- Backdrop -->
  <div class="absolute inset-0 bg-black/40" @click="closeModal"></div>

  <!-- Modal -->
    <div class="absolute left-1/2 top-1/2 w-[calc(100%-32px)] max-w-[500px] -translate-x-1/2 -translate-y-1/2">
      <Card class="p-5 rounded-2xl bg-white border-[#e9ecef] shadow-lg">
        <div class="flex items-start justify-between gap-3">
          <h3 class="text-base font-semibold text-[#1e3a5f]">{{ modalTitle }}</h3>
          <button type="button" class="text-sm text-[#6c757d] hover:text-[#ff6b4a]" @click="closeModal">닫기</button>
        </div>

        <div class="mt-3 max-h-[60vh] overflow-auto text-sm text-[#495057] leading-relaxed whitespace-pre-line">
          {{ modalContent }}
        </div>

        <div class="mt-4">
          <Button type="button" class="w-full h-11 gradient-primary text-white font-semibold rounded-xl" @click="closeModal">확인</Button>
        </div>
      </Card>
    </div>
  </div>

    <!-- Fixed Bottom Button -->
    <div class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg">
      <div class="max-w-[500px] mx-auto px-4 py-3">
        <Button
          @click="handlePayment"
          :disabled="!canProceed"
          class="w-full h-12 gradient-primary text-white font-semibold text-base rounded-xl shadow-button-hover hover:shadow-button-pressed disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ totalAmount.toLocaleString() }}원 결제하기
        </Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
