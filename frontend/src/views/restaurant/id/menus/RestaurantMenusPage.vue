<script setup>
import { computed } from 'vue';
import { ArrowLeft } from 'lucide-vue-next';
import { RouterLink, useRoute } from 'vue-router';
import Card from '@/components/ui/Card.vue';
import { getMenuCategoriesByRestaurant } from '@/data/restaurantMenus';

const route = useRoute();
const restaurantId = computed(() => route.params.id || '1');

const menuCategories = computed(() =>
  getMenuCategoriesByRestaurant(restaurantId.value),
);
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink :to="`/restaurant/${restaurantId}`" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">전체 메뉴</h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto px-4 py-5 pb-8">
      <div v-if="menuCategories.length" class="space-y-6">
        <div v-for="(category, idx) in menuCategories" :key="category.id || idx">
          <h3 class="text-base font-semibold text-[#1e3a5f] mb-3 px-1">{{ category.name }}</h3>
          <div class="space-y-2">
            <Card
              v-for="(item, itemIdx) in category.items"
              :key="item.id || itemIdx"
              class="p-4 border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-md transition-shadow"
            >
              <div class="flex items-center justify-between">
                <div class="flex-1">
                  <p class="font-semibold text-[#1e3a5f] mb-1">{{ item.name }}</p>
                  <p v-if="item.description" class="text-xs text-[#6c757d] leading-relaxed">
                    {{ item.description }}
                  </p>
                </div>
                <p class="font-semibold text-[#1e3a5f] ml-3">{{ item.price }}</p>
              </div>
            </Card>
          </div>
        </div>
      </div>
      <div v-else class="py-20 text-center text-sm text-[#6c757d]">
        메뉴 정보가 곧 업데이트될 예정입니다.
      </div>
    </main>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
