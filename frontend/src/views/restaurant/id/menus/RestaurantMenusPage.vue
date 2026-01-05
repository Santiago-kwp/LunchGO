<script setup>
import { ref, computed, onMounted } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { ArrowLeft } from 'lucide-vue-next';
import axios from 'axios';
import Card from '@/components/ui/Card.vue';
import Button from '@/components/ui/Button.vue';

const route = useRoute();
const restaurantId = route.params.id || '1';

const menus = ref([]);
const isLoading = ref(true);
const error = ref(null);

const fetchMenus = async () => {
  isLoading.value = true;
  error.value = null;
  try {
    const response = await axios.get(`/api/restaurants/${restaurantId}/menus`);
    menus.value = response.data;
  } catch (err) {
    console.error("메뉴 정보를 불러오는 데 실패했습니다:", err);
    error.value = "메뉴 정보를 불러오는 데 실패했습니다. 다시 시도해 주세요.";
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchMenus);

const groupedMenus = computed(() => {
  if (!menus.value || menus.value.length === 0) {
    return [];
  }
  
  const groups = menus.value.reduce((acc, menu) => {
    const categoryName = menu.category.value;
    if (!acc[categoryName]) {
      acc[categoryName] = [];
    }
    acc[categoryName].push(menu);
    return acc;
  }, {});

  return Object.entries(groups).map(([name, items]) => ({
    name,
    items,
  }));
});

const menuItemCount = computed(() => menus.value.length);
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink
          :to="`/restaurant/${restaurantId}`"
          class="mr-3"
        >
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">메뉴 전체보기</h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-8">
      <!-- Loading Spinner -->
      <div v-if="isLoading" class="flex justify-center items-center h-64">
        <div class="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-[#ff6b4a]"></div>
      </div>

      <!-- Error Message -->
      <div v-else-if="error" class="text-center py-10 px-4">
        <p class="text-red-500 font-semibold mb-4">{{ error }}</p>
        <Button @click="fetchMenus">재시도</Button>
      </div>
      
      <!-- Menu Content -->
      <template v-else>
        <div class="bg-white px-4 py-3 border-b border-[#e9ecef]">
          <p class="text-sm text-[#495057] leading-relaxed">
            총 {{ menuItemCount.toLocaleString() }}개의 메뉴가 있어요.
          </p>
        </div>

        <div v-if="menuItemCount > 0">
          <div v-for="category in groupedMenus" :key="category.name" class="px-4 py-5">
            <h3 class="text-base font-semibold text-[#1e3a5f] mb-4">{{ category.name }}</h3>
            <div class="space-y-3">
              <Card
                  v-for="item in category.items"
                  :key="item.id"
                  class="p-4 border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-md transition-shadow"
              >
                <div class="flex items-start justify-between gap-3">
                  <div class="flex-1">
                    <h4 class="font-semibold text-[#1e3a5f] mb-1">{{ item.name }}</h4>
                    <p v-if="item.description" class="text-xs text-[#6c757d] mb-2 leading-relaxed">
                      {{ item.description }}
                    </p>
                    <p class="text-base font-semibold text-[#1e3a5f]">{{ item.price.toLocaleString() }}원</p>
                  </div>
                  <div v-if="item.imageUrl" class="flex-shrink-0">
                    <img :src="item.imageUrl" :alt="item.name" class="w-20 h-20 rounded-lg object-cover" />
                  </div>
                </div>
              </Card>
            </div>
          </div>
        </div>
        <div v-else class="text-center py-10 px-4 text-gray-500">
          <p>등록된 메뉴 정보가 없습니다.</p>
        </div>
      </template>
    </main>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
