<script setup>
import { computed } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { ArrowLeft } from 'lucide-vue-next';
import { getMenuCategoriesByRestaurant } from '@/data/restaurantMenus';
import RestaurantMenuList from '@/components/ui/RestaurantMenuList.vue';

const route = useRoute();
const restaurantId = computed(() => route.params.id || '1');

const menuCategories = computed(() =>
  getMenuCategoriesByRestaurant(restaurantId.value),
);
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink :to="`/restaurant/${restaurantId}`" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">전체 메뉴</h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto px-4 py-5 pb-8">
      <RestaurantMenuList :menu-categories="menuCategories" />
    </main>
  </div>
</template>


