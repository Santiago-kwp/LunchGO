<script setup>
import { ref, computed } from 'vue';
import { RouterLink } from 'vue-router';
import { marked } from 'marked';
import PRIVACY_POLICY_TEXT from '@/content/privacyPolicy.md?raw';
import LOCATION_POLICY_TEXT from '@/content/locationServicePolicy.md?raw';

const isPrivacyOpen = ref(false);
const isLocationPolicyOpen = ref(false);
const privacyHtml = computed(() => marked.parse(PRIVACY_POLICY_TEXT, { breaks: true }));
const locationPolicyHtml = computed(() =>
  marked.parse(LOCATION_POLICY_TEXT, { breaks: true })
);

const openPrivacy = () => {
  isPrivacyOpen.value = true;
};

const closePrivacy = () => {
  isPrivacyOpen.value = false;
};

const openLocationPolicy = () => {
  isLocationPolicyOpen.value = true;
};

const closeLocationPolicy = () => {
  isLocationPolicyOpen.value = false;
};
</script>

<template>
  <footer class="bg-white border-t border-[#e9ecef] px-4 py-6 mt-8">
    <div class="text-xs text-[#6c757d] space-y-1 leading-relaxed">
      <p class="font-semibold text-[#1e3a5f]">(주) 런치고</p>
      <p>주소 : 경기 성남시 분당구 판교동 621 1501호</p>
      <p>
        <RouterLink to="/intro" class="hover:underline">서비스 소개</RouterLink> |
        서비스 이용약관 |
        <button type="button" class="hover:underline" @click="openLocationPolicy">
          위치정보 이용약관
        </button>
        |
        <RouterLink to="/partner" class="text-[#FF6B4A] hover:underline">
          파트너 문의
        </RouterLink>
      </p>
      <p>
        <button type="button" class="hover:underline" @click="openPrivacy">
          개인정보 처리방침
        </button>
        |
        <RouterLink to="/chatbot" class="hover:underline">고객센터</RouterLink>
      </p>
    </div>
  </footer>

  <Transition
    enter-active-class="transition duration-200 ease-out"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition duration-150 ease-in"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div v-if="isPrivacyOpen" class="fixed inset-0 z-[999]">
      <div
        class="absolute inset-0 bg-black/40 backdrop-blur-sm"
        @click="closePrivacy"
      ></div>

      <div
        class="absolute left-1/2 top-1/2 w-[calc(100%-32px)] max-w-[600px] -translate-x-1/2 -translate-y-1/2"
      >
        <div
          class="flex flex-col max-h-[70vh] rounded-2xl bg-white shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-200"
        >
          <div
            class="px-5 py-4 border-b border-[#f1f3f5] flex items-center justify-between bg-white"
          >
            <h3 class="text-base font-bold text-[#1e3a5f]">
              개인정보 처리방침
            </h3>
            <button
              type="button"
              class="text-[#adb5bd] hover:text-[#495057] transition-colors p-1"
              @click="closePrivacy"
            >
              닫기
            </button>
          </div>

          <div
            class="p-5 overflow-y-auto text-sm text-[#495057] leading-relaxed whitespace-pre-line bg-[#f8f9fa]"
          >
            <div v-html="privacyHtml"></div>
          </div>

          <div class="p-4 bg-white border-t border-[#f1f3f5]">
            <button
              type="button"
              class="w-full h-11 bg-[#1e3a5f] text-white font-semibold rounded-xl hover:bg-[#162c4b] transition-colors"
              @click="closePrivacy"
            >
              확인
            </button>
          </div>
        </div>
      </div>
    </div>
  </Transition>

  <Transition
    enter-active-class="transition duration-200 ease-out"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition duration-150 ease-in"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div v-if="isLocationPolicyOpen" class="fixed inset-0 z-[999]">
      <div
        class="absolute inset-0 bg-black/40 backdrop-blur-sm"
        @click="closeLocationPolicy"
      ></div>

      <div
        class="absolute left-1/2 top-1/2 w-[calc(100%-32px)] max-w-[600px] -translate-x-1/2 -translate-y-1/2"
      >
        <div
          class="flex flex-col max-h-[70vh] rounded-2xl bg-white shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-200"
        >
          <div
            class="px-5 py-4 border-b border-[#f1f3f5] flex items-center justify-between bg-white"
          >
            <h3 class="text-base font-bold text-[#1e3a5f]">
              위치정보 이용약관
            </h3>
            <button
              type="button"
              class="text-[#adb5bd] hover:text-[#495057] transition-colors p-1"
              @click="closeLocationPolicy"
            >
              닫기
            </button>
          </div>

          <div
            class="p-5 overflow-y-auto text-sm text-[#495057] leading-relaxed whitespace-pre-line bg-[#f8f9fa]"
          >
            <div v-html="locationPolicyHtml"></div>
          </div>

          <div class="p-4 bg-white border-t border-[#f1f3f5]">
            <button
              type="button"
              class="w-full h-11 bg-[#1e3a5f] text-white font-semibold rounded-xl hover:bg-[#162c4b] transition-colors"
              @click="closeLocationPolicy"
            >
              확인
            </button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped></style>
