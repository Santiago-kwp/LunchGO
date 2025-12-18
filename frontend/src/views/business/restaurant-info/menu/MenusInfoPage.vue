<script setup>
import { ref, computed } from 'vue';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import { ArrowLeft } from 'lucide-vue-next';
import { useRouter, RouterLink } from 'vue-router';
import { getMenuCategoriesByRestaurant } from '@/data/restaurantMenus';
import RestaurantMenuList from '@/components/ui/RestaurantMenuList.vue';

// Define props to receive the restaurant ID from the route
const props = defineProps({
  id: {
    type: String,
    required: true,
  },
});

const router = useRouter();

const goBack = () => {
  router.back();
};

// Data fetching for menus
const menuCategories = computed(() =>
  getMenuCategoriesByRestaurant(props.id),
);
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="restaurant-info" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-4xl mx-auto space-y-8">
          <!-- Page Title and Back Button -->
          <div class="flex items-center">
            <button @click="goBack" class="mr-4 p-2 rounded-full hover:bg-gray-200">
              <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
            </button>
            <h2 class="text-3xl font-bold text-[#1e3a5f]">전체 메뉴 보기</h2>
          </div>

          <!-- Menu List Section -->
          <section>
            <RestaurantMenuList :menu-categories="menuCategories" />
          </section>
        </div>
      </main>
    </div>
  </div>
</template>