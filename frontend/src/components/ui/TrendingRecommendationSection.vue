<script setup>
import Button from "@/components/ui/Button.vue";
import RestaurantCardList from "@/components/ui/RestaurantCardList.vue";
import RestaurantCardSkeletonList from "@/components/ui/RestaurantCardSkeletonList.vue";

defineProps({
  isActive: {
    type: Boolean,
    default: false,
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: "",
  },
  cards: {
    type: Array,
    default: () => [],
  },
  favoriteRestaurantIds: {
    type: Array,
    required: true,
  },
  onToggleFavorite: {
    type: Function,
    required: true,
  },
  onClear: {
    type: Function,
    required: true,
  },
});
</script>

<template>
  <section v-if="isActive" class="space-y-3">
    <div class="flex items-center justify-between">
      <h4 class="text-base font-semibold text-[#1e3a5f]">
        트렌딩 인기순 추천
      </h4>
      <div class="flex items-center gap-2">
        <span v-if="isLoading" class="text-xs text-[#6c757d]">
          불러오는 중...
        </span>
        <Button
          variant="outline"
          size="sm"
          class="h-8 px-3 text-xs border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] hover:text-[#1e3a5f] rounded-lg"
          @click="onClear"
        >
          추천 해제
        </Button>
      </div>
    </div>
    <p v-if="error" class="text-xs text-[#e03131]">
      {{ error }}
    </p>
    <RestaurantCardSkeletonList v-if="isLoading" :count="3" />
    <RestaurantCardList
      v-if="!isLoading && cards.length"
      :restaurants="cards"
      :favoriteRestaurantIds="favoriteRestaurantIds"
      :onToggleFavorite="onToggleFavorite"
    />
  </section>
</template>
