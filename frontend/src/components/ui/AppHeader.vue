<script setup>
import { ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';

const router = useRouter();

// --- 상태 관리 --- (pinia 추후 연결)
const isLoggedIn = ref(true); //일단 로그인 화면
const userName = ref('김런치');
const userProfileImage = ref(
  'https://api.dicebear.com/9.x/avataaars/svg?seed=Felix'
);
const isMenuOpen = ref(false);

// --- 핸들러 ---
const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value;
};

const handleLogout = async () => {
  alert('로그아웃 되었습니다.');

  //로그아웃 로직 구현
  isLoggedIn.value = false;
  isMenuOpen.value = false;
  router.push('/');
};
</script>

<template>
  <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
    <div
      class="max-w-[500px] mx-auto px-4 h-14 flex items-center justify-between"
    >
      <div class="flex items-center gap-3">
        <img
          src="/images/lunch-go-whitebg.png"
          alt="런치고"
          width="56"
          height="56"
          class="w-14 h-14 object-contain -ml-2"
        />
        <RouterLink
          to="/intro"
          class="font-semibold text-[#1e3a5f] text-base hover:text-[#ff6b4a] transition-colors"
        >
          서비스 소개
        </RouterLink>
      </div>

      <div class="flex items-center gap-4">
        <template v-if="isLoggedIn">
          <span class="text-sm font-medium text-[#495057]">
            <span class="text-[#1e3a5f] font-bold">{{ userName }}</span
            >님 안녕하세요!
          </span>

          <div class="relative">
            <button
              @click="toggleMenu"
              class="flex items-center justify-center w-9 h-9 rounded-full overflow-hidden border border-gray-200 hover:ring-2 hover:ring-[#ff6b4a] transition-all focus:outline-none"
            >
              <img
                :src="userProfileImage"
                alt="프로필"
                class="w-full h-full object-cover"
              />
            </button>

            <Transition name="dropdown">
              <div
                v-if="isMenuOpen"
                class="absolute right-0 top-full mt-2 w-max bg-white rounded-lg shadow-xl border border-gray-100 py-2 z-50"
              >
                <RouterLink
                  to="/mypage"
                  class="block px-4 py-2 text-sm text-center text-gray-700 hover:bg-gray-50 hover:text-[#ff6b4a] transition-colors"
                  @click="isMenuOpen = false"
                >
                  마이페이지
                </RouterLink>

                <button
                  @click="handleLogout"
                  class="w-full text-center block px-4 py-2 text-sm text-red-500 hover:bg-red-50 transition-colors"
                >
                  로그아웃
                </button>
              </div>
            </Transition>
          </div>
        </template>

        <template v-else>
          <RouterLink
            to="/login"
            class="text-sm text-[#495057] font-medium hover:text-[#ff6b4a] transition-colors"
          >
            로그인
          </RouterLink>
          <RouterLink
            to="/signup"
            class="text-sm text-[#495057] font-medium hover:text-[#ff6b4a] transition-colors"
          >
            회원가입
          </RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
/* Vue Transition Classes 
  name="dropdown"으로 설정했으므로 접두사가 dropdown- 입니다.
*/

/* 애니메이션 동작 시간 및 가속도 설정 (나타날 때 & 사라질 때) */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease-out;
}

/* 시작 상태 (Enter From) & 끝 상태 (Leave To) 
  투명하고 살짝 위로 올라가 있음 
*/
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 완료 상태 (Enter To) & 시작 상태 (Leave From)
  완전 불투명하고 제자리에 있음 
*/
.dropdown-enter-to,
.dropdown-leave-from {
  opacity: 1;
  transform: translateY(0);
}
</style>
