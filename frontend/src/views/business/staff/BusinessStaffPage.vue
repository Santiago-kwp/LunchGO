<script setup>
import { ref, computed, watch } from 'vue';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';

const emailInput = ref('');
const emailFormatMsg = ref('');
const showSuccessMessage = ref(false);
const currentPage = ref(1);
const itemsPerPage = 7;
const maxPageButtons = 5; // 최대 보여질 페이지 번호 개수

// 데이터 20명으로 늘림 (페이지네이션 테스트 용)
const staffList = ref([
  { id: 1, name: '김민준', email: 'minjun.kim@example.com' },
  { id: 2, name: '이서연', email: 'seoyeon.lee@example.com' },
  { id: 3, name: '박지호', email: 'jiho.park@example.com' },
  { id: 4, name: '최수빈', email: 'subin.choi@example.com' },
  { id: 5, name: '정예은', email: 'yeeun.jung@example.com' },
  { id: 6, name: '강현우', email: 'hyunwoo.kang@example.com' },
  { id: 7, name: '윤지아', email: 'jia.yoon@example.com' },
  { id: 8, name: '임도현', email: 'dohyun.lim@example.com' },
  { id: 9, name: '한소희', email: 'sohee.han@example.com' },
  { id: 10, name: '송태우', email: 'taewoo.song@example.com' },
  { id: 11, name: '전정국', email: 'jk.jeon@example.com' },
  { id: 12, name: '박지민', email: 'jimin.park@example.com' },
  { id: 13, name: '김태형', email: 'v.kim@example.com' },
  { id: 14, name: '민윤기', email: 'suga.min@example.com' },
  { id: 15, name: '정호석', email: 'jhope.jung@example.com' },
  { id: 16, name: '김남준', email: 'rm.kim@example.com' },
  { id: 17, name: '김석진', email: 'jin.kim@example.com' },
  { id: 18, name: '아이유', email: 'iu.lee@example.com' },
  { id: 19, name: '박효신', email: 'hyoshin.park@example.com' },
  { id: 20, name: '성시경', email: 'sikyung.sung@example.com' },
]);

const totalPages = computed(() => {
  return Math.ceil(staffList.value.length / itemsPerPage);
});

// 화면에 보여줄 페이지 번호 배열 계산 (최대 5개)
const visiblePages = computed(() => {
  const current = currentPage.value;
  const total = totalPages.value;

  if (total <= maxPageButtons) {
    // 전체 페이지가 5개 이하면 전체 표시
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  //보여줄 페이지 번호 계산
  let start = current - Math.floor(maxPageButtons / 2);
  let end = start + maxPageButtons - 1;

  //시작점이 1보다 작을 경우
  if (start < 1) {
    start = 1;
    end = maxPageButtons;
  }

  //끝점이 전체 페이지를 넘을 경우
  if (end > total) {
    end = total;
    start = end - maxPageButtons + 1;
  }

  // start부터 end까지의 숫자 배열 생성
  const pages = [];
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }
  return pages;
});

const paginatedStaffList = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return staffList.value.slice(start, end);
});

watch(totalPages, (newTotalPages) => {
  if (currentPage.value > newTotalPages && newTotalPages > 0) {
    currentPage.value = newTotalPages;
  }
});

const handleInputChange = () => {
  if (emailFormatMsg.value) {
    emailFormatMsg.value = '';
  }
};

const handleAddStaff = async () => {
  if (!emailInput.value) {
    emailFormatMsg.value = '이메일을 입력해주세요.';
    return;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(emailInput.value)) {
    emailFormatMsg.value = '올바른 이메일 형식이 아닙니다.';
    return;
  }

  //백엔드에서 정보 불러오고 유효한지 확인 (이메일이 존재하지 않으면?)

  const newStaff = {
    id: staffList.value.length + 1,
    name: '신규 직원',
    email: emailInput.value,
  };
  staffList.value.push(newStaff);

  currentPage.value = Math.ceil(staffList.value.length / itemsPerPage);

  emailInput.value = '';
  emailFormatMsg.value = '';
  showSuccessMessage.value = true;
  setTimeout(() => (showSuccessMessage.value = false), 3000);
};

const handleDeleteStaff = (id) => {
  staffList.value = staffList.value.filter((staff) => staff.id !== id);
};

// [추가됨] 이전, 다음 페이지 이동 함수
const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--;
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="staff" />

    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <main class="flex-1 overflow-auto p-8">
        <div class="max-w-6xl mx-auto">
          <h2 class="text-3xl font-bold text-[#1E3A5F] mb-8">임직원 현황</h2>

          <div class="bg-white rounded-xl border border-gray-200 p-6 pb-9 mb-6">
            <div class="flex items-start gap-4">
              <div class="flex-1 relative">
                <input
                  type="email"
                  v-model="emailInput"
                  @input="handleInputChange"
                  placeholder="임직원 이메일을 입력하세요."
                  class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 transition-colors"
                  :class="[
                    emailFormatMsg
                      ? 'border-red-500 focus:border-red-500 focus:ring-red-200'
                      : 'border-gray-300 focus:ring-[#FF6B4A] focus:border-transparent',
                  ]"
                />

                <p
                  v-if="emailFormatMsg"
                  class="absolute left-1 top-full mt-2 text-xs text-red-500 font-medium"
                >
                  {{ emailFormatMsg }}
                </p>
              </div>

              <button
                @click="handleAddStaff"
                class="px-6 py-3 bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] text-white font-semibold rounded-lg hover:opacity-90 transition-opacity whitespace-nowrap"
              >
                임직원 추가
              </button>
            </div>
          </div>

          <div
            class="bg-white rounded-xl border border-gray-200 overflow-hidden"
          >
            <table class="w-full">
              <thead class="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th
                    class="px-6 py-4 text-left text-sm font-semibold text-gray-700 w-24"
                  >
                    번호
                  </th>
                  <th
                    class="px-6 py-4 text-left text-sm font-semibold text-gray-700"
                  >
                    이름
                  </th>
                  <th
                    class="px-6 py-4 text-left text-sm font-semibold text-gray-700"
                  >
                    이메일
                  </th>
                  <th
                    class="px-6 py-4 text-center text-sm font-semibold text-gray-700 w-32"
                  ></th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200">
                <tr
                  v-for="(staff, index) in paginatedStaffList"
                  :key="staff.id"
                  class="hover:bg-gray-50"
                >
                  <td class="px-6 py-4 text-sm text-gray-900">
                    {{ (currentPage - 1) * itemsPerPage + index + 1 }}
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-900">
                    {{ staff.name }}
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-900">
                    {{ staff.email }}
                  </td>
                  <td class="px-6 py-4 text-center">
                    <button
                      @click="handleDeleteStaff(staff.id)"
                      class="px-4 py-2 bg-[#ffcdd2] text-[#c62828] rounded-lg text-sm font-medium hover:bg-[#ef9a9a] transition-colors"
                    >
                      삭제
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>

            <div
              v-if="totalPages > 0"
              class="border-t border-gray-200 px-6 py-4 flex items-center justify-center gap-2"
            >
              <button
                @click="prevPage"
                :disabled="currentPage === 1"
                class="px-3 py-2 rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-30 disabled:hover:bg-transparent transition-colors"
              >
                <svg
                  class="w-5 h-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 19l-7-7 7-7"
                  />
                </svg>
              </button>

              <button
                v-for="page in visiblePages"
                :key="page"
                @click="currentPage = page"
                :class="`w-8 h-8 flex items-center justify-center rounded-lg text-sm font-medium transition-colors ${
                  currentPage === page
                    ? 'bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] text-white shadow-sm'
                    : 'bg-white text-gray-700 hover:bg-gray-100'
                }`"
              >
                {{ page }}
              </button>

              <button
                @click="nextPage"
                :disabled="currentPage === totalPages"
                class="px-3 py-2 rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-30 disabled:hover:bg-transparent transition-colors"
              >
                <svg
                  class="w-5 h-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9 5l7 7-7 7"
                  />
                </svg>
              </button>
            </div>
          </div>

          <div
            v-if="showSuccessMessage"
            class="fixed bottom-8 right-8 bg-blue-50 border border-blue-200 rounded-lg px-6 py-4 shadow-lg flex items-center gap-3 animate-fade-in"
          >
            <div
              class="w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center"
            >
              <svg
                class="w-4 h-4 text-white"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>
            <span class="text-sm font-medium text-blue-900"
              >임직원이 등록되었습니다.</span
            >
          </div>
        </div>
      </main>
    </div>
  </div>
</template>
