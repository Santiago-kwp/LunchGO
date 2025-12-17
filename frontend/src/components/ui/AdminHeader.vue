<script setup>
import { User, Bell, LogOut } from 'lucide-vue-next';
import { RouterLink } from 'vue-router';
</script>

<template>
  <header
    class="bg-white border-b border-[#e9ecef] px-8 py-5 flex items-center justify-between"
  >
    <RouterLink to="/admin/dashboard">
      <h1
        class="text-2xl font-bold text-[#1E3A5F] hover:text-[#FF6B4A] transition-colors cursor-pointer"
      >
        LunchGo 총관리자
      </h1>
    </RouterLink>
    <div class="flex items-center gap-4">
      <button class="text-[#1e3a5f] hover:text-[#FF6B4A] transition-colors">
        <Bell class="w-6 h-6" />
      </button>
      <div class="flex items-center gap-4">
        <span class="text-sm text-gray-600">관리자님 안녕하세요!</span>
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
        </div>
      </div>
    </div>
  </header>
</template>
