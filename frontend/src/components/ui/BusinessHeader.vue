<script setup>
import { User, Bell, LogOut } from 'lucide-vue-next';
import { RouterLink } from 'vue-router';
</script>

<template>
  <header
    class="bg-white border-b border-[#e9ecef] px-8 py-5 flex items-center justify-between"
  >
    <RouterLink to="/business/dashboard">
      <h1
        class="text-2xl font-bold text-[#1E3A5F] hover:text-[#FF6B4A] transition-colors cursor-pointer"
      >
        LunchGo
      </h1>
    </RouterLink>
    <div class="flex items-center gap-4">
      <button class="text-[#1e3a5f] hover:text-[#FF6B4A] transition-colors">
        <Bell class="w-6 h-6" />
      </button>
      <div class="flex items-center gap-4">
        <span class="text-sm text-gray-600">OOO님 안녕하세요!</span>
        <button
          class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
        >
          <User class="w-4 h-4" />
        </button>
        <div class="relative">
          <button
            class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
          >
            <span class="text-sm">로그아웃</span>
            <LogOut class="w-4 h-4" />
          </button>
          <!-- This dropdown logic needs to be implemented in Vue if it's functional -->
          <div
            class="absolute top-full right-0 mt-2 bg-white border border-gray-200 rounded-lg shadow-lg p-2 min-w-[120px] hidden group-hover:block"
          >
            <button
              class="block w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 rounded"
            >
              마이페이지
            </button>
            <button
              class="block w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 rounded"
            >
              식당 정보
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>
