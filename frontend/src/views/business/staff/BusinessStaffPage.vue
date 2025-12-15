<script setup>
import { ref } from 'vue';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';

const emailInput = ref('');
const showSuccessMessage = ref(false);
const currentPage = ref(1);

const staffList = ref([
  { id: 1, name: '김민준', email: 'minjun.kim@example.com' },
  { id: 2, name: '이서연', email: 'seoyeon.lee@example.com' },
  { id: 3, name: '박지호', email: 'jiho.park@example.com' },
  { id: 4, name: '최수빈', email: 'subin.choi@example.com' },
  { id: 5, name: '정예은', email: 'yeeun.jung@example.com' },
]);

const handleAddStaff = () => {
  if (emailInput.value.trim()) {
    const newStaff = {
      id: staffList.value.length + 1,
      name: '신규 직원',
      email: emailInput.value,
    };
    staffList.value.push(newStaff); // Directly modify reactive array
    emailInput.value = '';
    showSuccessMessage.value = true;
    setTimeout(() => (showSuccessMessage.value = false), 3000);
  }
};

const handleDeleteStaff = (id) => {
  staffList.value = staffList.value.filter((staff) => staff.id !== id);
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="staff" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <!-- Content -->
      <main class="flex-1 overflow-auto p-8">
        <div class="max-w-6xl mx-auto">
          <h2 class="text-3xl font-bold text-[#1E3A5F] mb-8">임직원 현황</h2>

          <!-- Search and Add Section -->
          <div class="bg-white rounded-xl border border-gray-200 p-6 mb-6">
            <div class="flex items-center gap-4">
              <div class="flex-1 relative">
                <input
                  type="email"
                  v-model="emailInput"
                  placeholder="이메일을 입력하세요."
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A] focus:border-transparent"
                />
              </div>
              <button
                @click="handleAddStaff"
                class="px-6 py-3 bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] text-white font-semibold rounded-lg hover:opacity-90 transition-opacity whitespace-nowrap"
              >
                임직원 추가
              </button>
            </div>
          </div>

          <!-- Staff Table -->
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
                  v-for="(staff, index) in staffList"
                  :key="staff.id"
                  class="hover:bg-gray-50"
                >
                  <td class="px-6 py-4 text-sm text-gray-900">
                    {{ index + 1 }}
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

            <!-- Pagination -->
            <div
              class="border-t border-gray-200 px-6 py-4 flex items-center justify-center gap-2"
            >
              <button
                @click="currentPage = 1"
                :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  currentPage === 1
                    ? 'bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`"
              >
                1
              </button>
              <button
                @click="currentPage = 2"
                :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  currentPage === 2
                    ? 'bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`"
              >
                2
              </button>
              <button
                @click="currentPage = 3"
                :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  currentPage === 3
                    ? 'bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-[#e9ecef]'
                }`"
              >
                3
              </button>
            </div>
          </div>

          <!-- Success Message -->
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

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
