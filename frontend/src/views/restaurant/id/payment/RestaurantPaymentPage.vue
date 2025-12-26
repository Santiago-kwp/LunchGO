<script setup>
import { ref, computed, onMounted } from "vue";
import { RouterLink, useRouter, useRoute } from "vue-router";
import {
  ArrowLeft,
  CreditCard,
  Smartphone,
  Building2,
  Check,
} from "lucide-vue-next";
import axios from "axios";
import Button from "@/components/ui/Button.vue";
import Card from "@/components/ui/Card.vue";

const router = useRouter();
const route = useRoute();
const restaurantId = route.params.id || "7"; // Default to '1' if id is not available

//예약금 / 선주문선결제 분기
const paymentType = computed(() => String(route.query.type || "deposit"));
const isDepositOnly = computed(() => paymentType.value === "deposit");

const selectedPayment = ref(null);
const agreedToTerms = ref(false);
const agreedToRefund = ref(false);
const isProcessing = ref(false);
const isPortOneReady = ref(false);
const errorMessage = ref("");
const isTermsModalOpen = ref(false);
const modalTitle = ref("");
const modalContent = ref("");

//인원수 : query에서 partySize로 받는다고 가정 (없으면 1명)
const headcount = computed(() => {
  const q = Number(route.query.partySize);
  return Number.isFinite(q) && q > 0 ? q : 1;
});

//예약금만 결제 시 예약금 1~6인 5,000원 / 7인 이상 : 10,000원
const depositPerPerson = computed(() => (headcount.value >= 7 ? 10000 : 5000));

//총 예약금
// query로 넘어온 선주문 합계(메뉴 페이지에서 전달)
const preorderTotal = computed(() => {
  const v = Number(route.query.totalAmount);
  return Number.isFinite(v) && v >= 0 ? v : 0;
});

const totalAmount = computed(() => {
  // 예약하기 플로우: 예약금 = 인원수 * 인당금액
  if (isDepositOnly.value) return headcount.value * depositPerPerson.value;

  // 선주문/선결제 플로우: 메뉴 합계
  return preorderTotal.value;
});

const bookingId = computed(() => route.query.bookingId || null);
const reservationId = computed(
  () => route.query.reservationId || bookingId.value || null
);
const bookingSummary = ref({
  requestNote: "", // 요청사항(read-only 표시용)
});
const isBookingLoading = ref(false);
const fetchBookingSummary = async () => {
  isBookingLoading.value = true;
  try {
    // TODO: 백엔드 연결 시 여기만 실제 API로 교체
    // ex) const res = await api.get(`/bookings/${bookingId.value}`);
    // bookingSummary.value.requestNote = res.data.requestNote;

    // 임시 더미(모양 확인용)
    bookingSummary.value.requestNote = "유아용 의자 부탁드려요";
  } finally {
    isBookingLoading.value = false;
  }
};

onMounted(() => {
  fetchBookingSummary();
  loadPortOneSdk();
});

// 모달에 보여줄 텍스트 (원하는 약관 원문 그대로 붙여넣기)
const SERVICE_TERMS_TEXT = `서비스 이용약관 원문을 여기에 붙여 넣으세요.`;

// (필수) 취소 및 환불 정책 모달용 텍스트
const REFUND_POLICY_TEXT = `## **제 1 조 (목적)**

본 예약 서비스 이용 약관(이하 “본 약관”)은 회사가 제공하는 “예약 서비스”의 이용과 관련하여 회사와 회원 간의 권리, 의무 및 책임사항과 예약의 **변경 제한**, **취소**, **환불**, **자동 취소(타임아웃)** 등에 관한 사항을 규정함을 목적으로 합니다.


## **제 2 조 (정의)**

① 본 약관에서 사용하는 용어의 정의는 다음과 같습니다.

1. “회사”란 예약 서비스 및 결제·환불 처리 기능을 제공하는 주체를 말합니다.
2. “플랫폼”이란 회원이 예약 서비스를 이용할 수 있도록 회사가 제공하는 웹/앱 등 일체의 서비스를 말합니다.
3. “회원”이란 플랫폼에 가입하여 로그인한 이용자를 말합니다.
4. “점주(사업자)”란 본인이 소유 또는 관리하는 매장의 예약 관련 기능을 이용하는 사업자를 말합니다.
5. “매장”이란 점주가 플랫폼에 등록하여 예약 서비스를 제공하는 사업장을 말합니다.
6. “예약”이란 회원이 매장의 특정 **예약일시(날짜+시간)** 및 **예약인원**에 대해 이용 의사를 표시하고 회사가 정한 절차에 따라 생성되는 예약 건을 말합니다.
7. “임시 예약”이란 결제 완료 이전 단계에서 생성되어 결제 대기 중인 예약 상태를 말합니다.
8. “예약 확정”이란 예약금 또는 선결제가 완료되어 예약이 확정된 상태를 말합니다.
9. “예약금 결제 예약”이란 회원이 [예약하기]를 통해 예약을 생성하고 예약금을 결제하여 예약이 확정된 예약을 말합니다.
10. “선주문/선결제 예약”이란 회원이 [선주문/선결제]를 통해 메뉴를 선택하고 음식값 전액을 선결제하여 예약이 확정된 예약을 말합니다(이 경우 예약금은 별도로 결제하지 않습니다).
11. “예약 취소”란 회원이 예약된 이용일 또는 이용 예정시간 이전에 플랫폼 또는 기타 방법을 통해 예약을 해제하는 의사표시를 하는 행위를 말합니다.
12. “노쇼(No-show)”란 회원이 사전 통지 없이 예약 시간까지 매장에 도착하지 않거나, 예약을 취소하지 않고 예약을 이행하지 않는 행위를 말합니다.
13. “카드 유형(card_type)”이란 결제 카드의 구분값으로서 사업자 / 개인 / 구분없음 을말하며, 결제대행사(PG)로부터 전달받은 값에 따라 저장됩니다.

② 본 약관에서 정하지 아니한 사항은 관련 법령 및 일반 상관례에 따릅니다.


## **제 3 조 (예약 서비스의 이용 및 결제 방식)**

① 예약 서비스는 **로그인한 회원**에 한하여 이용할 수 있으며, 비회원은 예약 서비스 및 선주문/선결제 기능을 이용할 수 없습니다.

② 회원은 예약 시 다음 각 호 중 하나의 방식으로 예약을 진행합니다.

1. **[예약하기]**: 예약 생성과 동시에 예약금 결제가 이루어지며, 음식값은 방문 시 현장 결제로 처리됩니다.
2. **[선주문/선결제]**: 매장이 해당 옵션을 제공하는 경우에 한하여 이용할 수 있으며, 메뉴 선택 후 음식값 전액을 선결제하여 예약이 확정됩니다.

③ 회사는 예약의 오남용 방지 및 오버부킹 방지를 위해 예약 처리 과정에서 잠금(락), 결제 기한, 상태 검증 등의 운영정책을 적용할 수 있습니다.


## **제 4 조 (예약 변경 제한)**

① 회사는 시스템 상 **예약 시간 및 예약 인원 변경 기능을 제공하지 않습니다.**

② 회원이 예약 내용(예약일시, 예약인원)의 변경을 원하는 경우, 회원은 **기존 예약을 취소한 후** 원하는 조건으로 **새 예약을 생성**하여야 합니다.

③ 제2항에 따라 기존 예약을 취소하는 경우, 본 약관의 **취소 및 환불 규정이 동일하게 적용**됩니다.

④ 요청사항은 매장이 참고하는 메모이며, 요청사항 기재만으로 예약 조건이 변경되지 않습니다.


## **제 5 조 (예약 취소)**

① 예약 취소는 **회원(예약자) 또는 점주 및 임직원**이 수행할 수 있습니다.

② 회사는 예약 상태 및 취소 시점을 기준으로 환불 가능 여부와 환불 비율을 산정합니다.

③ 예약이 취소된 경우 회사는 해당 예약 인원 수만큼 해당 시간대의 **잔여 수용 인원**을 복구합니다.


## **제 6 조 (취소 시점별 환불 비율)**

① 회원의 예약 취소 시점에 따른 환불 비율은 다음과 같습니다.

1. **예약 전일 취소**: 100% 환불합니다.
2. **예약 당일, 예약시간 2시간 전까지 취소**: 50% 환불합니다.
3. **예약시간 2시간 이내 취소**: 20% 환불합니다.
4. 예약시간 2시간 이내에도 불구하고 **예약 인원 8인 이상**인 경우: 10% 환불합니다.
5. **노쇼(No-show)**: 환불하지 않습니다(0%).

② 전일/당일 및 “2시간 전/2시간 이내”의 산정 기준은 예약 확정 시 시스템에 기록된 예약일시를 기준으로 합니다.

---

## **제 7 조 (결제 유형별 환불 산정)**

### **① 예약금 결제 예약([예약하기])**

1. 환불 기준 금액은 회원이 실제 결제한 **예약금 전액**으로 합니다.
2. 최종 환불 금액은 **예약금 × 제6조 환불 비율**로 산정합니다.
3. 단, 제9조(예외 규정)에 해당하는 경우에는 예외 규정이 우선 적용됩니다.

### **② 선주문/선결제 예약([선주문/선결제])**

1. 선주문/선결제 예약은 예약금이 별도로 결제되지 않으나, 취소/환불 산정 시 회사는 “예약금 가정액”을 적용합니다.
2. 예약금 가정액은 다음 각 호에 따릅니다.
    1. 1~6인: **인당 5,000원**
    2. 7인 이상: **인당 10,000원**
    3. 예약금 가정액 = (인당 기준금액 × 예약 인원수)
    4. 단, 예약금 가정액은 **음식값 총액을 초과할 수 없으며**, 초과하는 경우 음식값 총액을 한도로 합니다.
3. 회사는 선결제 금액을 다음과 같이 분리하여 환불합니다.
    1. **메뉴금액 잔액(음식값 총액 − 예약금 가정액)**: 취소 시점과 무관하게 **100% 환불**합니다.
    2. **예약금 가정액**: 제6조의 환불 비율을 적용하여 환불합니다.
4. 최종 환불 금액은 다음과 같습니다.
    - **최종 환불액 = (메뉴금액 잔액 × 100%) + (예약금 가정액 × 제6조 환불 비율)**
5. 선주문/선결제 예약의 취소에 따른 수수료 및 정산 처리 방식은 회사의 정산 정책에 따라 최종 환불액을 기준으로 취소·정산될 수 있습니다.


## **제 8 조 (환불 절차 및 상태 변경)**

① 회사는 본 약관에 따라 산정된 환불 금액을 기준으로 결제대행사(PG)의 환불 절차를 통해 환불을 처리합니다.

② 환불 완료 시 결제 상태는 환불 완료로 변경될 수 있으며, 예약 상태는 취소/환불로 변경됩니다.

③ 회사는 환불 처리 결과(환불 금액, 환불 비율, 결제 유형 등)를 플랫폼 화면 또는 알림 등을 통해 안내할 수 있습니다.


## **제 9 조 (예외 규정: 법인카드 및 점주 강제 취소)**

① **회원 취소**이면서 결제 카드가 **법인카드**인 경우, 위약금 성격의 금액은 면제하며 다음과 같이 환불합니다.

1. 예약금 결제 예약: 취소 시점과 무관하게 **예약금 전액을 환불**합니다.
2. 선주문/선결제 예약:
    - 메뉴금액 잔액은 100% 환불하고,
    - 예약금 가정액은 취소 시점과 무관하게 100% 환불하며,
    - 결과적으로 **선결제 금액 전액을 환불**합니다.

② **점주가 예약을 강제 취소**하는 경우(취소 사유 입력 필수)에는 카드 유형과 무관하게 결제 금액 전액을 100% 환불하며, 다음과 같이 처리합니다.

1. 예약금 결제 예약: 예약금 전액을 환불합니다.
2. 선주문/선결제 예약: 선결제 금액 전액을 환불합니다.


## **제 10 조 (결제 전 자동 취소 및 환불)**

① 회사는 임시 예약 단계에서 다음 각 호의 사유가 발생하는 경우 해당 임시 예약을 자동 취소할 수 있습니다.

1. 임시 예약 생성 시점부터 **7분** 이내에 회원이 이탈, 취소하거나 슬롯 잠금이 만료된 경우
2. 결제 요청 시점부터 **결제 가능 시간 10분** 이내에 결제가 완료되지 않은 경우(타임아웃)

② 제1항의 자동 취소는 결제 완료 이전 단계에서 발생하므로, 결제가 이루어지지 않은 경우 **환불 절차가 발생하지 않습니다.**

③ 자동 취소 시 회사는 임시로 차감되었던 인원 수만큼 해당 시간대의 잔여 수용 인원을 복구합니다.


## 부칙

본 약관은 2025년 12월 19일에 공지되어, 2025년 12월 19일부터 시행됩니다.`;

// 이용약관 모달
const openModal = (type) => {
  isTermsModalOpen.value = true;

  if (type === "terms") {
    modalTitle.value = "서비스 이용약관";
    modalContent.value = SERVICE_TERMS_TEXT;
    return;
  }

  // type === 'refund'
  modalTitle.value = "취소 및 환불 정책";
  modalContent.value = REFUND_POLICY_TEXT;
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
    id: "card",
    name: "신용/체크카드",
    iconName: "CreditCard", // Use string name to reference iconComponents
    description: "국내 모든 카드 사용 가능",
  },
];

const canProceed = computed(
  () =>
    selectedPayment.value &&
    agreedToTerms.value &&
    agreedToRefund.value &&
    !isProcessing.value
);

const PAYMENT_TIMEOUT_MS = 7 * 60 * 1000;
const PORTONE_STORE_ID = import.meta.env.VITE_PORTONE_STORE_ID || "";
const PORTONE_CHANNEL_KEY = import.meta.env.VITE_PORTONE_CHANNEL_KEY || "";
const PORTONE_OPEN_TYPE = import.meta.env.VITE_PORTONE_OPEN_TYPE || "popup";

const createIdempotencyKey = () => {
  if (typeof crypto !== "undefined" && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return `idempo_${Date.now()}_${Math.random().toString(16).slice(2, 10)}`;
};

const loadPortOneSdk = () => {
  if (window.PortOne) {
    isPortOneReady.value = true;
    return Promise.resolve();
  }

  const existing = document.querySelector("script[data-portone-sdk]");
  if (existing) {
    return new Promise((resolve, reject) => {
      existing.addEventListener("load", () => {
        isPortOneReady.value = true;
        resolve();
      });
      existing.addEventListener("error", () => {
        reject(new Error("PortOne SDK 로드에 실패했습니다."));
      });
    });
  }

  const script = document.createElement("script");
  script.src =
    import.meta.env.VITE_PORTONE_SDK_URL ||
    "https://cdn.portone.io/v2/browser-sdk.js";
  script.async = true;
  script.defer = true;
  script.setAttribute("data-portone-sdk", "true");
  document.head.appendChild(script);

  return new Promise((resolve, reject) => {
    script.onload = () => {
      isPortOneReady.value = true;
      resolve();
    };
    script.onerror = () => {
      reject(new Error("PortOne SDK 로드에 실패했습니다."));
    };
  });
};

const requestPayment = async ({ merchantUid, amount, reservationId }) => {
  // TODO: PortOne SDK 연동 지점
  // window.PortOne.requestPayment(...) 결과를 반환하도록 구현
  await loadPortOneSdk();
  return new Promise((resolve, reject) => {
    if (!window.PortOne) {
      reject(new Error("PortOne SDK가 로드되지 않았습니다."));
      return;
    }
    if (!PORTONE_STORE_ID || !PORTONE_CHANNEL_KEY) {
      reject(
        new Error("PortOne 설정값이 누락되었습니다. 관리자에게 문의해 주세요.")
      );
      return;
    }
    const openTypeValue =
      PORTONE_OPEN_TYPE === "popup" || PORTONE_OPEN_TYPE === "iframe"
        ? PORTONE_OPEN_TYPE
        : "popup";
    const redirectParams = new URLSearchParams({
      reservationId: String(reservationId || ""),
      totalAmount: String(amount),
      type: paymentType.value,
      paymentId: merchantUid,
    });
    const redirectUrl = `${
      window.location.origin
    }/restaurant/${restaurantId}/confirmation?${redirectParams.toString()}`;
    window.PortOne.requestPayment(
      {
        storeId: PORTONE_STORE_ID,
        channelKey: PORTONE_CHANNEL_KEY,
        paymentId: merchantUid,
        orderName: isDepositOnly.value ? "예약금 결제" : "선결제",
        totalAmount: amount,
        currency: "KRW",
        payMethod: "CARD",
        openType: openTypeValue,
        redirectUrl,
      },
      (response) => {
        if (response?.error) {
          reject(new Error(response.error.message || "결제에 실패했습니다."));
          return;
        }
        resolve(response);
      }
    );
  });
};

//선주문 메뉴 합계 전달
const handlePayment = async () => {
  if (!canProceed.value) return;
  errorMessage.value = "";
  isProcessing.value = true;

  try {
    const currentReservationId =
      reservationId.value || (import.meta.env.DEV ? "7" : null); // 테스트용으로 7을 넣고 하자
    if (!currentReservationId) {
      throw new Error("예약 정보가 없습니다. 다시 예약 과정을 진행해 주세요.");
    }

    // TODO: 임시 예약 생성이 완료되었다고 가정하고 이 블록은 비워둡니다.
    // (임시 예약 생성/슬롯 락은 다른 담당에서 구현)

    const paymentTypeValue = isDepositOnly.value ? "DEPOSIT" : "PREPAID_FOOD";
    const paymentRes = await axios.post(
      `/api/reservations/${currentReservationId}/payments`,
      {
        paymentType: paymentTypeValue,
        amount: totalAmount.value,
        method: selectedPayment.value === "card" ? "CARD" : "UNKNOWN",
        idempotencyKey: createIdempotencyKey(),
      }
    );

    const { merchantUid, amount } = paymentRes.data;

    try {
      await axios.post("/api/payments/portone/requested", {
        merchantUid,
      });
    } catch (requestError) {
      console.warn("결제 요청 시작 기록 실패", requestError);
    }

    const paymentPromise = requestPayment({
      merchantUid,
      amount,
      reservationId: currentReservationId,
    });
    const timeoutPromise = new Promise((_, reject) => {
      setTimeout(
        () =>
          reject(new Error("결제 시간이 초과되었습니다. 다시 시도해 주세요.")),
        PAYMENT_TIMEOUT_MS
      );
    });

    const portoneResult = await Promise.race([paymentPromise, timeoutPromise]);

    await axios.post("/api/payments/portone/complete", {
      merchantUid,
      impUid:
        portoneResult.imp_uid ||
        portoneResult.impUid ||
        portoneResult.txId ||
        portoneResult.transactionId,
      paidAmount: amount,
    });

    router.push({
      path: `/restaurant/${restaurantId}/confirmation`,
      query: {
        type: paymentType.value,
        totalAmount: String(totalAmount.value),
        partySize: String(route.query.partySize ?? ""),
        requestNote: String(route.query.requestNote ?? ""),
        dateIndex: String(route.query.dateIndex ?? ""),
        time: String(route.query.time ?? ""),
        reservationId: String(currentReservationId),
      },
    });
  } catch (error) {
    const message = error?.message || "결제 처리 중 오류가 발생했습니다.";
    errorMessage.value = message;

    try {
      if (message.includes("초과")) {
        await axios.post(
          `/api/reservations/${reservationId.value}/payments/expire`
        );
      } else {
        await axios.post("/api/payments/portone/fail", {
          reservationId: reservationId.value,
          reason: message,
        });
      }
    } catch (callbackError) {
      console.error("결제 실패/만료 콜백 처리 실패", callbackError);
    }
  } finally {
    isProcessing.value = false;
  }
};

const backTarget = computed(() => {
  // 예약금 결제(booking -> payment)
  if (isDepositOnly.value) {
    return {
      path: `/restaurant/${restaurantId}/booking`,
      query: {
        type: "reservation",
        partySize: route.query.partySize,
        requestNote: route.query.requestNote,
        dateIndex: route.query.dateIndex,
        time: route.query.time,
      },
    };
  }

  // 선주문 결제(menu -> payment)
  return {
    path: `/restaurant/${restaurantId}/menu`,
    query: {
      type: "preorder",
      partySize: route.query.partySize,
      requestNote: route.query.requestNote,
      dateIndex: route.query.dateIndex,
      time: route.query.time,
    },
  };
});
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <button type="button" class="mr-3" @click="router.back()">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </button>

        <h1 class="font-semibold text-[#1e3a5f] text-base">
          {{ isDepositOnly ? "예약금 결제" : "결제하기" }}
        </h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-32">
      <!-- Payment Amount -->
      <div class="bg-white px-4 py-6 border-b border-[#e9ecef]">
        <div class="text-center">
          <p class="text-sm text-[#6c757d] mb-2">
            {{ isDepositOnly ? "예약금" : "최종 결제 금액" }}
          </p>
          <p class="text-3xl font-bold text-[#1e3a5f]">
            {{ totalAmount.toLocaleString() }}원
          </p>
          <p v-if="isDepositOnly" class="text-xs text-[#6c757d] mt-2">
            * 예약금은 이용 완료 시 돌려드리며, 노쇼 시 환불되지 않습니다
          </p>
        </div>
      </div>

      <!-- Request Note (Read-only) -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
        <div class="flex items-center justify-between mb-2">
          <h2 class="text-base font-semibold text-[#1e3a5f]">요청사항</h2>
          <span v-if="isBookingLoading" class="text-xs text-[#6c757d]"
            >불러오는 중...</span
          >
        </div>

        <div class="rounded-xl border border-[#e9ecef] bg-[#f8f9fa] px-3 py-3">
          <p class="text-sm text-[#495057] whitespace-pre-line">
            {{
              bookingSummary.requestNote?.trim()
                ? bookingSummary.requestNote
                : "-"
            }}
          </p>
        </div>

        <p class="mt-2 text-xs text-[#6c757d] leading-relaxed">
          요청사항은 매장 참고용이며, 예약 변경은 매장으로 직접 문의해 주세요.
        </p>
      </div>

      <!-- Payment Method Selection -->
      <div class="px-4 py-5">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">
          결제 수단 선택
        </h2>
        <div class="space-y-3">
          <button
            v-for="method in paymentMethods"
            :key="method.id"
            @click="selectedPayment = method.id"
            :class="`w-full text-left transition-all ${
              selectedPayment === method.id ? 'ring-2 ring-[#ff6b4a]' : ''
            }`"
          >
            <Card
              class="p-4 border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-md"
            >
              <div class="flex items-center gap-3">
                <div
                  :class="`w-12 h-12 rounded-lg flex items-center justify-center flex-shrink-0 ${
                    selectedPayment === method.id
                      ? 'gradient-primary'
                      : 'bg-[#f8f9fa]'
                  }`"
                >
                  <component
                    :is="iconComponents[method.iconName]"
                    :class="`w-6 h-6 ${
                      selectedPayment === method.id
                        ? 'text-white'
                        : 'text-[#6c757d]'
                    }`"
                  />
                </div>
                <div class="flex-1">
                  <p
                    :class="`font-semibold mb-0.5 ${
                      selectedPayment === method.id
                        ? 'text-[#ff6b4a]'
                        : 'text-[#1e3a5f]'
                    }`"
                  >
                    {{ method.name }}
                  </p>
                  <p class="text-xs text-[#6c757d]">{{ method.description }}</p>
                </div>
                <div
                  v-if="selectedPayment === method.id"
                  class="w-6 h-6 rounded-full gradient-primary flex items-center justify-center flex-shrink-0"
                >
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
          <label
            :class="`flex items-start gap-3 cursor-pointer group rounded-xl p-2 -m-2 transition-colors ${
              agreedToTerms ? 'bg-[#fff5f3]' : ''
            }`"
          >
            <div class="relative flex-shrink-0 mt-0.5">
              <input type="checkbox" v-model="agreedToTerms" class="sr-only" />
              <div
                :class="`w-5 h-5 rounded border-2 flex items-center justify-center transition-all ${
                  agreedToTerms
                    ? 'bg-[#ff6b4a] border-[#ff6b4a]'
                    : 'bg-white border-[#dee2e6]'
                } group-hover:border-[#ff6b4a]`"
              >
                <Check v-if="agreedToTerms" class="w-3.5 h-3.5 text-white" />
              </div>
            </div>
            <div class="flex-1">
              <p class="text-sm text-[#1e3a5f] font-medium mb-0.5">
                (필수) 서비스 이용약관 동의
              </p>
              <button
                type="button"
                @click.stop="openModal('terms')"
                class="text-xs text-[#6c757d] underline hover:text-[#ff6b4a]"
              >
                자세히 보기
              </button>
              <!--이용약관 모달 버튼-->
            </div>
          </label>

          <label
            :class="`flex items-start gap-3 cursor-pointer group rounded-xl p-2 -m-2 transition-colors ${
              agreedToRefund ? 'bg-[#fff5f3]' : ''
            }`"
          >
            <div class="relative flex-shrink-0 mt-0.5">
              <input type="checkbox" v-model="agreedToRefund" class="sr-only" />
              <div
                :class="`w-5 h-5 rounded border-2 flex items-center justify-center transition-all ${
                  agreedToRefund
                    ? 'bg-[#ff6b4a] border-[#ff6b4a]'
                    : 'bg-white border-[#dee2e6]'
                } group-hover:border-[#ff6b4a]`"
              >
                <Check v-if="agreedToRefund" class="w-3.5 h-3.5 text-white" />
              </div>
            </div>
            <div class="flex-1">
              <p class="text-sm text-[#1e3a5f] font-medium mb-0.5">
                (필수) 취소 및 환불 정책 동의
              </p>
              <!--이용약관 모달 버튼-->
              <button
                type="button"
                @click.stop="openModal('refund')"
                class="text-xs text-[#6c757d] underline hover:text-[#ff6b4a]"
              >
                자세히 보기
              </button>
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
              <li>
                • 취소는 방문 1일 전까지 가능하며, 당일 취소 및 노쇼 시 환불
                정책에 따라 환불 됩니다.
              </li>
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
      <div
        class="absolute left-1/2 top-1/2 w-[calc(100%-32px)] max-w-[500px] -translate-x-1/2 -translate-y-1/2"
      >
        <Card class="p-5 rounded-2xl bg-white border-[#e9ecef] shadow-lg">
          <div class="flex items-start justify-between gap-3">
            <h3 class="text-base font-semibold text-[#1e3a5f]">
              {{ modalTitle }}
            </h3>
            <button
              type="button"
              class="text-sm text-[#6c757d] hover:text-[#ff6b4a]"
              @click="closeModal"
            >
              닫기
            </button>
          </div>

          <div
            class="mt-3 max-h-[60vh] overflow-auto text-sm text-[#495057] leading-relaxed whitespace-pre-line"
          >
            {{ modalContent }}
          </div>

          <div class="mt-4">
            <Button
              type="button"
              class="w-full h-11 gradient-primary text-white font-semibold rounded-xl"
              @click="closeModal"
              >확인</Button
            >
          </div>
        </Card>
      </div>
    </div>

    <!-- Fixed Bottom Button -->
    <div
      class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg"
    >
      <div class="max-w-[500px] mx-auto px-4 py-3">
        <p v-if="errorMessage" class="mb-2 text-xs text-red-500">
          {{ errorMessage }}
        </p>
        <Button
          @click="handlePayment"
          :disabled="!canProceed"
          class="w-full h-12 gradient-primary text-white font-semibold text-base rounded-xl shadow-button-hover hover:shadow-button-pressed disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{
            isProcessing
              ? "결제 진행 중..."
              : `${totalAmount.toLocaleString()}원 결제하기`
          }}
        </Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
