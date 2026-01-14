<script setup>
import { ref, computed } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { Bot, Calendar, Home, User } from "lucide-vue-next";
import { useAccountStore } from "@/stores/account";
import LoginRequiredModal from "@/components/ui/LoginRequiredModal.vue";

const router = useRouter();
const accountStore = useAccountStore();
const isLoggedIn = computed(() =>
  Boolean(accountStore.accessToken || localStorage.getItem("accessToken"))
);
const isLoginModalOpen = ref(false);
const modalAnchor = ref(null);
const reservationsButtonRef = ref(null);
const myPageButtonRef = ref(null);
const modalMessage = ref("");
const redirectPath = ref("");

const handleMyReservationsClick = () => {
  if (isLoggedIn.value) {
    router.push("/my-reservations");
    return;
  }
  modalMessage.value = "나의 예약을 보기 위해 로그인이 필요합니다";
  redirectPath.value = "/my-reservations";
  modalAnchor.value = reservationsButtonRef.value?.getBoundingClientRect() ?? null;
  isLoginModalOpen.value = true;
};

const handleMyPageClick = () => {
  if (isLoggedIn.value) {
    router.push("/mypage");
    return;
  }
  modalMessage.value = "마이 페이지는 로그인이 필요합니다";
  redirectPath.value = "/mypage";
  modalAnchor.value = myPageButtonRef.value?.getBoundingClientRect() ?? null;
  isLoginModalOpen.value = true;
};
</script>

<template>
  <nav class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg">
    <div class="max-w-[500px] mx-auto flex items-center justify-around h-16 px-4">
      <button
        ref="reservationsButtonRef"
        type="button"
        class="flex items-center justify-center -mt-4 text-[#ff6b4a] hover:text-[#FF8A6D] transition-colors min-w-[60px]"
        @click="handleMyReservationsClick"
        aria-label="예약 내역"
      >
        <Calendar class="w-6 h-6" />
      </button>

      <button
        type="button"
        class="flex items-center justify-center -mt-4"
        @click="$emit('home')"
        aria-label="홈으로 이동"
      >
        <div
          class="w-12 h-12 rounded-full gradient-primary flex items-center justify-center shadow-button-hover border-4 border-white"
        >
          <Home class="w-5 h-5 text-white" />
          <span class="sr-only">홈으로 이동</span>
        </div>
      </button>

      <button
        ref="myPageButtonRef"
        type="button"
        class="flex items-center justify-center -mt-4 text-[#ff6b4a] hover:text-[#FF8A6D] transition-colors min-w-[60px]"
        @click="handleMyPageClick"
        aria-label="마이페이지"
      >
        <User class="w-6 h-6" />
      </button>
    </div>
    <button
      v-if="isLoggedIn"
      type="button"
      class="group fixed bottom-20 right-4 w-12 h-12 rounded-full border border-[#d7dbe0] bg-[#f7f8fa] text-[#4b4f56] shadow-lg flex items-center justify-center transition hover:border-[#c3c8ce] hover:bg-[#eef1f3] active:border-[#b9bec0] active:bg-[#e6eaee]"
      @click="$emit('chatbot')"
      aria-label="CS 챗봇 열기"
    >
      <Bot class="w-6 h-6" />
      <span
        class="pointer-events-none absolute -top-7 right-1/2 translate-x-1/2 whitespace-nowrap rounded-full bg-[#6b7077] px-2.5 py-1 text-[10px] font-semibold text-white opacity-0 transition group-hover:opacity-100 group-active:opacity-100"
      >
        고객센터
      </span>
    </button>
    <LoginRequiredModal
      :is-open="isLoginModalOpen"
      :redirect-path="redirectPath"
      :message="modalMessage"
      :anchor-rect="modalAnchor"
      @close="
        isLoginModalOpen = false;
        modalAnchor = null;
        modalMessage = '';
        redirectPath = '';
      "
    />
  </nav>
</template>

<style scoped></style>
