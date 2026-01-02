<script setup>
import { computed } from 'vue';
import { useRouter, RouterLink } from 'vue-router';
import { Bell, LogOut } from 'lucide-vue-next';
import httpRequest from '@/router/httpRequest';
import { useAccountStore } from '@/stores/account';

const router = useRouter();
const accountStore = useAccountStore();

const getStoredMember = () => {
  if (typeof window === 'undefined') return null;
  const raw = localStorage.getItem('member');
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
};

const member = computed(() => accountStore.member || getStoredMember());
const userName = computed(() => {
  if(member.value?.role === 'ROLE_OWNER' || member.value?.role === 'ROLE_STAFF'){
    return member.value.name;
  }
  return '사용자';
});
const greetingSuffix = computed(() => {
  if(member.value?.role === 'ROLE_OWNER'){
    return '님, 안녕하세요'
  }else if(member.value?.role === 'ROLE_STAFF'){
    return '님, 안녕하세요(직원용)';
  }
});

const handleLogout = async () => {
  const confirmLogout = confirm('정말 로그아웃 하시겠습니까?');
  if (!confirmLogout) return;

  try {
    await httpRequest.post('/api/logout', {});

    alert("로그아웃 되었습니다.");
  } catch (error) {
    console.warn('로그아웃 요청 실패:', error);
  } finally {
    accountStore.clearAccount();
    router.push('/');
  }
};
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
        <span class="text-sm text-gray-600">
          <span class="text-[#1e3a5f] font-bold">{{ userName }}</span>
          <span>{{ greetingSuffix }}</span>
        </span>

        <button
          @click="handleLogout"
          class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg hover:bg-red-50 hover:text-red-500 hover:border-red-200 transition-colors"
        >
          <span class="text-sm">로그아웃</span>
          <LogOut class="w-4 h-4" />
        </button>
      </div>
    </div>
  </header>
</template>
