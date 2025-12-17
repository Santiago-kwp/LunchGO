<script setup>
import { RouterLink } from 'vue-router';
import { Calendar, Home, User } from 'lucide-vue-next';
</script>

<template>
  <nav class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg">
    <div class="max-w-[500px] mx-auto flex items-center justify-around h-16 px-4">
      <RouterLink
        to="/my-reservations"
        class="flex items-center justify-center text-[#ff6b4a] hover:text-[#FF8A6D] transition-colors min-w-[60px]"
      >
        <Calendar class="w-6 h-6" />
      </RouterLink>

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

      <RouterLink
        to="/mypage"
        class="flex items-center justify-center text-[#ff6b4a] hover:text-[#FF8A6D] transition-colors min-w-[60px]"
      >
        <User class="w-6 h-6" />
      </RouterLink>
    </div>
  </nav>
</template>

<style scoped></style>
