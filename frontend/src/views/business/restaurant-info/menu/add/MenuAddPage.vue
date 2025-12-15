<script setup>
import { ref, reactive } from 'vue';
import { Upload, X } from 'lucide-vue-next'; // User, Bell은 Header로 이동
import { useRouter } from 'vue-router';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';

const router = useRouter(); // Vue Router's useRouter

const menuData = reactive({
  name: '',
  type: '',
  category: '',
  price: '',
});

const selectedAllergens = ref([]);

const toggleAllergen = (allergen) => {
  const index = selectedAllergens.value.indexOf(allergen);
  if (index > -1) {
    selectedAllergens.value.splice(index, 1);
  } else {
    selectedAllergens.value.push(allergen);
  }
};

const allergens = ref([
  '견과류',
  '우유',
  '계란',
  '밀',
  '대두',
  '갑각류',
  '조개류',
  '생선',
  '메밀',
]);
const specialAttributes = ref(['알레르기 - 알레르기 유발 or 호불호 강한']);

const saveMenu = () => {
  alert('메뉴가 추가되었습니다.');
  router.back();
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="restaurant-info" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-2xl mx-auto">
          <!-- Page Title with Close Button -->
          <div class="flex items-center justify-between mb-8">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">
              식당 메뉴 정보 추가
            </h2>
            <button
              @click="router.back()"
              class="p-2 hover:bg-[#f8f9fa] rounded-lg transition-colors"
            >
              <X class="w-6 h-6 text-[#6c757d]" />
            </button>
          </div>

          <!-- Menu Form Card -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <div class="space-y-6">
              <!-- Menu Image Upload -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >메뉴사진</label
                >
                <div
                  class="border-2 border-dashed border-[#dee2e6] rounded-xl p-12 text-center bg-[#f8f9fa] hover:bg-[#e9ecef] transition-colors cursor-pointer"
                >
                  <Upload class="w-12 h-12 text-[#6c757d] mx-auto mb-3" />
                  <p class="text-sm text-[#6c757d]">이미지</p>
                </div>
                <button
                  class="mt-3 px-6 py-2 border border-[#dee2e6] rounded-lg text-[#1e3a5f] hover:bg-[#f8f9fa] transition-colors"
                >
                  파일 업로드
                </button>
              </div>

              <!-- Menu Name -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >메뉴이름</label
                >
                <input
                  type="text"
                  placeholder="메뉴이름 입력창(텍스트)"
                  v-model="menuData.name"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
              </div>

              <!-- Menu Type -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >메뉴타입</label
                >
                <input
                  type="text"
                  placeholder="콤보세트(메뉴들, 서브메뉴들, 기타)"
                  v-model="menuData.type"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
              </div>

              <!-- Menu Category -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >가격</label
                >
                <input
                  type="text"
                  placeholder="가격 입력창"
                  v-model="menuData.price"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
              </div>

              <!-- Allergen Information -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-3"
                  >주재료</label
                >
                <div class="flex flex-wrap gap-3 mb-6">
                  <button
                    v-for="allergen in allergens"
                    :key="allergen"
                    @click="toggleAllergen(allergen)"
                    :class="`px-6 py-3 rounded-lg border transition-colors ${
                      selectedAllergens.includes(allergen)
                        ? 'gradient-primary text-white border-transparent'
                        : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                    }`"
                  >
                    {{ allergen }}
                  </button>
                  <button
                    class="px-6 py-3 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
                  >
                    ...
                  </button>
                </div>

                <label class="block text-sm font-semibold text-[#1e3a5f] mb-3">
                  특이사항(기타재료 - 알레르기 유발 or 호불호 강함)
                </label>
                <div class="flex flex-wrap gap-3">
                  <button
                    v-for="attr in specialAttributes"
                    :key="attr"
                    @click="toggleAllergen(attr)"
                    :class="`px-6 py-3 rounded-lg border transition-colors ${
                      selectedAllergens.includes(attr)
                        ? 'gradient-primary text-white border-transparent'
                        : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                    }`"
                  >
                    {{ attr }}
                  </button>
                  <button
                    class="px-6 py-3 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
                  >
                    ...
                  </button>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex justify-end gap-3 mt-8">
              <button
                @click="router.back()"
                class="px-8 py-3 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
              >
                취소
              </button>
              <button
                @click="saveMenu"
                class="px-8 py-3 gradient-primary text-white rounded-lg font-semibold hover:opacity-90 transition-opacity"
              >
                저장
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
